package codes.biscuit.skyblockaddons.asm.api;

import codes.biscuit.skyblockaddons.asm.api.helper.TransformClassHelper;
import codes.biscuit.skyblockaddons.asm.api.helper.normal.NormalTransformClassHelper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.objectweb.asm.tree.ClassNode;

/**
 * Manage the transformers and transform any class node.
 *
 * @author iHDeveloper
 */
public abstract class TransformerEngine {

    // TODO Implement a logger for the engine
    private Multimap<String, Transformer> transformers = ArrayListMultimap.create();

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

        for (Transformer transformer : transformers.get(name)) {
            transformer.transform(classHelper, node);
        }

        // TODO Implement a way to change the write flags of the class writer
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
