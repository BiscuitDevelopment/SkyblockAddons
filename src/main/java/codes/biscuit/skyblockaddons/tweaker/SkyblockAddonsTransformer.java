package codes.biscuit.skyblockaddons.tweaker;

import codes.biscuit.skyblockaddons.asm.*;
import codes.biscuit.skyblockaddons.tweaker.transformer.Transformer;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

public class SkyblockAddonsTransformer implements IClassTransformer {

    private final static boolean OUTPUT_BYTECODE = true;
    private Logger logger = LogManager.getLogger("SkyblockAddons - Transformer");
    private final Multimap<String, Transformer> transformerMap = ArrayListMultimap.create();

    public SkyblockAddonsTransformer() {
        registerTransformer(new TileEntityEnderChestRendererTransformer());
        registerTransformer(new MouseHelperTransformer());
        registerTransformer(new EntityPlayerSPTransformer());
        registerTransformer(new EntityRendererTransformer());
        registerTransformer(new SoundManagerTransformer());
    }

    private void registerTransformer(Transformer transformer) {
        for (String cls : transformer.getClassName()) {
            transformerMap.put(cls, transformer);
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null) return null;

        Collection<Transformer> transformers = transformerMap.get(transformedName);
        if (transformers.isEmpty()) return bytes;

        logger.info("Found {} transformers for {}", transformers.size(), transformedName);

        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, ClassReader.EXPAND_FRAMES);

        transformers.forEach(transformer -> {
            logger.info("Applying transformer {} on {}...", transformer.getClass().getName(), transformedName);
            transformer.transform(node, transformedName);
        });

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        try {
            node.accept(writer);
        } catch (Throwable t) {
            logger.error("Exception when transforming " + transformedName + " : " + t.getClass().getSimpleName());
            t.printStackTrace();
        }

        if (OUTPUT_BYTECODE) {
            try {
                File file = new File("C:\\Users\\jlroc\\Desktop\\bytecode", transformedName + ".class");
                file.createNewFile();
                FileOutputStream os = new FileOutputStream(file);
                os.write(writer.toByteArray());
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return writer.toByteArray();
    }
}