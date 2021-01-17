package codes.biscuit.skyblockaddons.tweaker;

import codes.biscuit.skyblockaddons.asm.*;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.FMLRelaunchLog;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.Level;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;

public class SkyblockAddonsTransformer implements IClassTransformer {

    @Getter private static boolean deobfuscated = (boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
    @Getter private static boolean usingNotchMappings = !deobfuscated;

    private final Multimap<String, ITransformer> transformerMap = ArrayListMultimap.create();

    public SkyblockAddonsTransformer() {
        registerTransformer(new TileEntityEnderChestRendererTransformer());
        registerTransformer(new MouseHelperTransformer());
        registerTransformer(new EntityPlayerSPTransformer());
        registerTransformer(new EntityRendererTransformer());
        registerTransformer(new SoundManagerTransformer());
        registerTransformer(new RenderManagerTransformer());
        registerTransformer(new PlayerControllerMPTransformer());
        registerTransformer(new MinecraftTransformer());
        registerTransformer(new ItemTransformer());
        registerTransformer(new GuiScreenTransformer());

        registerTransformer(new GuiContainerTransformer());
        registerTransformer(new GuiChestTransformer());
        registerTransformer(new GuiNewChatTransformer());
        registerTransformer(new RendererLivingEntityTransformer());
        registerTransformer(new GuiDisconnectedTransformer());

        registerTransformer(new GuiIngameMenuTransformer());

        registerTransformer(new FontRendererTransformer());
        registerTransformer(new RenderItemTransformer());
        registerTransformer(new EntityLivingBaseTransformer());
        registerTransformer(new InventoryPlayerTransformer());
        registerTransformer(new GuiIngameCustomTransformer());
        registerTransformer(new RenderEndermanTransformer());
        registerTransformer(new ModelEndermanTransformer());
        registerTransformer(new RenderGlobalTransformer());
        registerTransformer(new EffectRendererTransformer());
        registerTransformer(new WorldClientTransformer());
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

        log(Level.INFO, String.format("Found %s transformers for %s", transformers.size(), transformedName));

        ClassReader reader = new ClassReader(bytes);
        ClassNode node = new ClassNode();
        reader.accept(node, ClassReader.EXPAND_FRAMES);

        MutableInt classWriterFlags = new MutableInt(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        transformers.forEach(transformer -> {
            log(Level.INFO, String.format("Applying transformer %s on %s...", transformer.getClass().getName(), transformedName));
            transformer.transform(node, transformedName);

            if (transformer instanceof FontRendererTransformer) {
                classWriterFlags.setValue(0);
            }
        });

        ClassWriter writer = new ClassWriter(classWriterFlags.getValue());

        try {
            node.accept(writer);
        } catch (Throwable ex) {
            log(Level.ERROR, "An exception occurred while transforming " + transformedName);
            ex.printStackTrace();
            outputBytecode(transformedName, writer);
            return bytes;
        }

        outputBytecode(transformedName, writer);

        return writer.toByteArray();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void outputBytecode(String transformedName, ClassWriter writer) {
        try {
            File bytecodeDirectory = new File("bytecode");
            if (!bytecodeDirectory.exists()) return;

            File bytecodeOutput = new File(bytecodeDirectory, transformedName + ".class");
            if (!bytecodeOutput.exists()) bytecodeOutput.createNewFile();

            FileOutputStream os = new FileOutputStream(bytecodeOutput);
            os.write(writer.toByteArray());
            os.close();
        } catch (Exception ex) {
            log(Level.ERROR, "An error occurred writing bytecode of transformed class \"" + transformedName + "\" to file");
            ex.printStackTrace();
        }
    }

    public void log(Level level, String message) {
        String name = "SkyblockAddons/" + this.getClass().getSimpleName();
        FMLRelaunchLog.log(name, level, (SkyblockAddonsTransformer.isDeobfuscated() ? "" : "[" + name + "] ") + message);
    }
}