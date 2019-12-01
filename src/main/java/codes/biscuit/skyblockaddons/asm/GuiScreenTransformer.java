package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class GuiScreenTransformer implements ITransformer {

    /**
     * {@link net.minecraft.client.gui.GuiScreen}
     */
    @Override
    public String[] getClassName() {
        return new String[]{"net.minecraft.client.gui.GuiScreen"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        try {
            for (MethodNode methodNode : classNode.methods) { // Loop through all methods inside of the class.

                String methodName = mapMethodName(classNode, methodNode);
                if (nameMatches(methodName, "renderToolTip", "func_146285_a")) {

                    // Objective:
                    // Find: Method head.
                    // Insert:   ReturnValue returnValue = new ReturnValue();
                    //           GuiScreenHook.renderBackpack(stack, x, y, returnValue);
                    //           if (returnValue.isCancelled()) {
                    //               return;
                    //           }

                    methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertRenderBackpack());
                }
                if (nameMatches(methodName, "handleComponentClick", "func_175276_a")) {

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

        list.add(new TypeInsnNode(Opcodes.NEW, "codes/biscuit/skyblockaddons/asm/hooks/ReturnValue"));
        list.add(new InsnNode(Opcodes.DUP)); // ReturnValue returnValue = new ReturnValue();
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "codes/biscuit/skyblockaddons/asm/hooks/ReturnValue", "<init>", "()V", false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 6));

        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // stack
        list.add(new VarInsnNode(Opcodes.ILOAD, 2)); // x
        list.add(new VarInsnNode(Opcodes.ILOAD, 3)); // y
        list.add(new VarInsnNode(Opcodes.ALOAD, 6)); // GuiScreenHook.renderBackpack(stack, x, y, returnValue);
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiScreenHook", "renderBackpack",
                "(Lnet/minecraft/item/ItemStack;IILcodes/biscuit/skyblockaddons/asm/hooks/ReturnValue;)V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 6));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "codes/biscuit/skyblockaddons/asm/hooks/ReturnValue", "isCancelled",
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
                "(Lnet/minecraft/util/IChatComponent;)V", false)); // GuiScreenHook.handleComponentClick(component);

        return list;
    }
}
