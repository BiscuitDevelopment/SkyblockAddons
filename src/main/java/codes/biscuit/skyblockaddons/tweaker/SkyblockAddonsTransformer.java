package codes.biscuit.skyblockaddons.tweaker;

import codes.biscuit.skyblockaddons.asm.*;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
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

    public final static boolean DEOBFUSCATED = (boolean)Launch.blackboard.get("fml.deobfuscatedEnvironment");

    private final static boolean OUTPUT_BYTECODE = true;
    private Logger logger = LogManager.getLogger("SkyblockAddons Transformer");
    private final Multimap<String, ITransformer> transformerMap = ArrayListMultimap.create();

    public SkyblockAddonsTransformer() {
        registerTransformer(new TileEntityEnderChestRendererTransformer()); //
        registerTransformer(new MouseHelperTransformer()); //
        registerTransformer(new EntityPlayerSPTransformer()); //
        registerTransformer(new EntityRendererTransformer()); //
        registerTransformer(new SoundManagerTransformer()); //
        registerTransformer(new RenderManagerTransformer()); //
        registerTransformer(new PlayerControllerMPTransformer()); //
        registerTransformer(new NetHandlerPlayClientTransformer()); //
        registerTransformer(new MinecraftTransformer()); //
//        registerTransformer(new ItemTransformer());
//        registerTransformer(new GuiScreenTransformer());
//        registerTransformer(new GuiInventoryTransformer());
//        registerTransformer(new GuiContainerTransformer());
//        registerTransformer(new GuiChestTransformer());
//        registerTransformer(new GuiNewChatTransformer());
//        registerTransformer(new RendererLivingEntityTransformer());
    }

    private void registerTransformer(ITransformer transformer) {
        for (String cls : transformer.getClassName()) {
            transformerMap.put(cls, transformer);
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] bytes) {
        if (bytes == null) return null;

        Collection<ITransformer> transformers = transformerMap.get(transformedName);
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
            outputBytecode(transformedName, writer);
            return bytes;
        }

        outputBytecode(transformedName, writer);

        return writer.toByteArray();
    }

    private void outputBytecode(String transformedName, ClassWriter writer) {
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
    }
}