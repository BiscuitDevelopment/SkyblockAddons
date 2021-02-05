package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerField;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class MinecraftTransformer implements ITransformer {

    /**
     * {@link net.minecraft.client.Minecraft}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.Minecraft.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {
        for (MethodNode methodNode : classNode.methods) {
            if (TransformerMethod.refreshResources.matches(methodNode)) {

                // Objective:
                // Find: Method return.
                // Insert: MinecraftHook.refreshResources(this.mcResourceManager);

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof InsnNode && abstractNode.getOpcode() == Opcodes.RETURN) {
                        methodNode.instructions.insertBefore(abstractNode, insertOnRefreshResources());
                        break;
                    }
                }
            }
            if (TransformerMethod.rightClickMouse.matches(methodNode)) {

                // Objective:
                // Find: Before "this.rightClickDelayTimer = 4;"
                // Insert:   ReturnValue returnValue = new ReturnValue();
                //           MinecraftHook.rightClickMouse(returnValue);
                //           if (returnValue.isCancelled()) {
                //               return;
                //           }

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof InsnNode && abstractNode.getOpcode() == Opcodes.ICONST_4) {
                        methodNode.instructions.insertBefore(abstractNode.getPrevious(), insertRightClickMouse());
                        break;
                    }
                }
            }
            if (TransformerMethod.runTick.matches(methodNode)) {

                // Objective:
                // Insert Before:
                //    this.thePlayer.inventory.currentItem = l;
                //
                // Put:   MinecraftHook.updatedCurrentItem();

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof FieldInsnNode && abstractNode.getOpcode() == Opcodes.PUTFIELD
                            && TransformerField.currentItem.matches((FieldInsnNode)abstractNode)) {
                        methodNode.instructions.insertBefore(abstractNode.getPrevious().getPrevious().getPrevious().getPrevious(),
                                new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/MinecraftHook",
                                "updatedCurrentItem", "()V", false)); // MinecraftHook.updatedCurrentItem();
                        break;
                    }
                }
            }
            if (TransformerMethod.clickMouse.matches(methodNode)) {
                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertOnClickMouse());
            }
            if (TransformerMethod.sendClickBlockToController.matches(methodNode)) {
                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertOnSendClickBlockToController());
            }
        }
    }

    private InsnList insertOnRefreshResources() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.

        list.add(TransformerField.mcResourceManager.getField(TransformerClass.Minecraft));

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/MinecraftHook", "onRefreshResources",
                "("+TransformerClass.IReloadableResourceManager.getName()+")V", false)); // MinecraftHook.refreshResources(this.mcResourceManager);

        return list;
    }

    private InsnList insertRightClickMouse() {
        InsnList list = new InsnList();

        list.add(new TypeInsnNode(Opcodes.NEW, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue"));
        list.add(new InsnNode(Opcodes.DUP)); // ReturnValue returnValue = new ReturnValue();
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "<init>", "()V", false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 6));

        list.add(new VarInsnNode(Opcodes.ALOAD, 6)); // MinecraftHook.rightClickMouse(returnValue);
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/MinecraftHook", "rightClickMouse",
                "(Lcodes/biscuit/skyblockaddons/asm/utils/ReturnValue;)V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 6));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "isCancelled",
                "()Z", false));
        LabelNode notCancelled = new LabelNode(); // if (returnValue.isCancelled())
        list.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled));

        list.add(new InsnNode(Opcodes.RETURN)); // return;
        list.add(notCancelled);

        return list;
    }

    private InsnList insertOnClickMouse() {
        InsnList list = new InsnList();

        list.add(new TypeInsnNode(Opcodes.NEW, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue"));
        list.add(new InsnNode(Opcodes.DUP)); // ReturnValue returnValue = new ReturnValue();
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "<init>", "()V", false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 3));

        list.add(new VarInsnNode(Opcodes.ALOAD, 3)); // MinecraftHook.onClickMouse(ReturnValue);
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/MinecraftHook", "onClickMouse",
                "(Lcodes/biscuit/skyblockaddons/asm/utils/ReturnValue;)V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 3));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "isCancelled", "()Z", false));
        LabelNode notCancelled = new LabelNode(); // if (returnValue.isCancelled())
        list.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled));

        list.add(new InsnNode(Opcodes.RETURN)); // return;
        list.add(notCancelled);

        return list;
    }



    private InsnList insertOnSendClickBlockToController() {
        InsnList list = new InsnList();

        list.add(new TypeInsnNode(Opcodes.NEW, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue"));
        list.add(new InsnNode(Opcodes.DUP)); // ReturnValue returnValue = new ReturnValue();
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "<init>", "()V", false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 3));

        list.add(new VarInsnNode(Opcodes.ILOAD, 1));
        list.add(new VarInsnNode(Opcodes.ALOAD, 3)); // MinecraftHook.onSendClickBlockToController(leftClick, ReturnValue);
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/MinecraftHook", "onSendClickBlockToController",
                "(ZLcodes/biscuit/skyblockaddons/asm/utils/ReturnValue;)V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 3));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "isCancelled", "()Z", false));
        LabelNode notCancelled = new LabelNode(); // if (returnValue.isCancelled())
        list.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled));

        list.add(new InsnNode(Opcodes.RETURN)); // return;
        list.add(notCancelled);

        return list;
    }
}
