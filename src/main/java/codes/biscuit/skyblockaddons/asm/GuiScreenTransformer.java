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
                    // Insert:   ReturnValue returnValue = new ReturnValue();
                    //           GuiScreenHook.renderBackpack(stack, x, y, returnValue);
                    //           if (returnValue.isCancelled()) {
                    //               return;
                    //           }

                    methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertRenderBackpack());
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

    private InsnList insertRenderBackpack() {
        InsnList list = new InsnList();

        list.add(new TypeInsnNode(Opcodes.NEW, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue"));
        list.add(new InsnNode(Opcodes.DUP)); // ReturnValue returnValue = new ReturnValue();
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "<init>", "()V", false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 6));

        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // stack
        list.add(new VarInsnNode(Opcodes.ILOAD, 2)); // x
        list.add(new VarInsnNode(Opcodes.ILOAD, 3)); // y
        list.add(new VarInsnNode(Opcodes.ALOAD, 6)); // GuiScreenHook.renderBackpack(stack, x, y, returnValue);
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiScreenHook", "renderBackpack",
                "("+TransformerClass.ItemStack.getName()+"IILcodes/biscuit/skyblockaddons/asm/utils/ReturnValue;)V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 6));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "isCancelled",
                "()Z", false));
        LabelNode notCancelled = new LabelNode(); // if (returnValue.isCancelled())
        list.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled));

        list.add(new InsnNode(Opcodes.RETURN)); // return;
        list.add(notCancelled);

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
