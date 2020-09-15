package codes.biscuit.skyblockaddons.asm.api;

import codes.biscuit.skyblockaddons.asm.api.helper.TransformClassHelper;
import codes.biscuit.skyblockaddons.asm.api.helper.normal.NormalTransformClassHelper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

/**
 * Manage the transformers and transform any class node.
 *
 * @author iHDeveloper
 */
public abstract class TransformerEngine {

    private Logger logger = LogManager.getLogger("SBA: ASM Transformers");
    private Multimap<String, Transformer> transformers = ArrayListMultimap.create();
    private MutableInt writeFlags = new MutableInt();

    /**
     * Create an engine with transformers built-in
     *
     * @param transformers The transformers to register into the engine
     */
    public TransformerEngine(Transformer[] transformers) {
        for (Transformer transformer : transformers) {
            register(transformer);
        }
    }

    /**
     * Check if the class name is a target for transformation
     *
     * @param name The class name
     */
    public boolean exists(String name) {
        return transformers.containsKey(name);
    }

    /**
     * Transform the class node through the transformers
     *
     * @param node The class to transform
     */
    public void transform(String name, ClassNode node) {
        TransformClassHelper classHelper = new NormalTransformClassHelper(name);

        writeFlags.setValue(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        for (Transformer transformer : transformers.get(name)) {
            transformer.transform(this, classHelper, node);
        }
    }

    /**
     * Get the engine logger
     *
     * @return The logger of the engine
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Set the write flags of the current class for the writer
     *
     * @param flags The flags for the class writer see {@link ClassWriter}
     */
    public void setWriteFlags(int flags) {
        writeFlags.setValue(flags);
    }

    /**
     * Get the current write flags
     *
     * @return The write flags as integer
     */
    public int getWriteFlags() {
        return writeFlags.getValue();
    }

    /**
     * Register a transformer with its targets
     *
     * @param transformer The transformer to register
     */
    protected void register(Transformer transformer) {
        for (TransformClassHelper targetClassHelper : transformer.targets()) {
            transformers.put(targetClassHelper.getTransformerName(), transformer);
        }
    }

}
