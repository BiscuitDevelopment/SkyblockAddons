package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class FontRendererTransformer implements ITransformer {

    /**
     * {@link net.minecraft.client.gui.FontRenderer}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.FontRenderer.getTransformerName(), "club.sk1er.patcher.hooks.FontRendererHook"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {

            // Objective:
            // Find Method Head: Add:
            //   FontRendererHook.changeTextColor(); <- insert the call right before the return

            // TODO Test with patcher...
            if (TransformerMethod.renderChar.matches(methodNode) || methodNode.name.equals("renderChar")) {
                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(),
                        new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/FontRendererHook", "changeTextColor", "()V", false));
            }
        }
    }
}
