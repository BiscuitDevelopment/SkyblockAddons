package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class GuiScreenTransformer implements ITransformer {

    /**
     * {@link net.minecraft.client.gui.GuiScreen}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.GuiScreen.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        try {
            for (MethodNode methodNode : classNode.methods) {
                if (TransformerMethod.renderToolTip.matches(methodNode)) {

                    // Objective:
                    // Find: Method head.
                    // Insert:   if (GuiScreenHook.onRenderTooltip(stack, x, y)) {
                    //               return;
                    //           }

                    methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), onRenderTooltip());
                }
                if (TransformerMethod.handleComponentClick.matches(methodNode)) {

                    // Objective:
                    // Find: Method head.
                    // Insert: GuiScreenHook.handleComponentClick(component);

                    methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertComponentClick());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private InsnList onRenderTooltip() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // stack
        list.add(new VarInsnNode(Opcodes.ILOAD, 2)); // x
        list.add(new VarInsnNode(Opcodes.ILOAD, 3)); // y
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiScreenHook", "onRenderTooltip",
                "("+TransformerClass.ItemStack.getName()+"II)Z", false));
        LabelNode notCancelled = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled)); // if (GuiScreenHook.onRenderTooltip(stack, x, y)) {
        list.add(new InsnNode(Opcodes.RETURN)); // return;
        list.add(notCancelled); // }

        return list;
    }

    private InsnList insertComponentClick() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // component
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiScreenHook", "handleComponentClick",
                "("+TransformerClass.IChatComponent.getName()+")V", false)); // GuiScreenHook.handleComponentClick(component);

        return list;
    }
}
