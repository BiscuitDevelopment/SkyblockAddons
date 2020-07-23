package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

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
            // Find:
            //   return (float value)
            // Change to:
            //   float f4 = (float value)
            //   FontRendererHook.changeTextColor(); <- insert the call right before the return
            //   return f4;

            if (TransformerMethod.renderChar.matches(methodNode) || methodNode.name.equals("renderChar")) {
                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();

                    if (abstractNode.getOpcode() == Opcodes.FRETURN) {
                        methodNode.instructions.insertBefore(abstractNode, insertChangeTextColor());
                    }
                }
            }
        }
    }

    private InsnList insertChangeTextColor() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.FSTORE, 4)); // Store it in a variable for now.
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/FontRendererHook", "changeTextColor", "()V", false));
        list.add(new VarInsnNode(Opcodes.FLOAD, 4)); // Call back the stored variable to return it.

        return list;
    }
}
