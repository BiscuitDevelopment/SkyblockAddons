package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerField;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class GuiContainerTransformer implements ITransformer {

    /**
     * {@link net.minecraft.client.gui.inventory.GuiContainer}
     */
    @Override
    public String[] getClassName() {
        return new String[]{TransformerClass.GuiContainer.getTransformerName()};
    }

    @Override
    public void transform(ClassNode classNode, String name) {

//        classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "oldMouseX", "I", null, null));
//        classNode.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "oldMouseY", "I", null, null));

        for (MethodNode methodNode : classNode.methods) {
            if (TransformerMethod.drawSlot.matches(methodNode)) {

                // Objective:
                // Find: After this.itemRender.renderItemAndEffectIntoGUI(itemstack, i, j);
                // Add: GuiContainerHook.showEnchantments(slotIn, i, j, item);

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)abstractNode;
                        if (methodInsnNode.owner.equals(TransformerClass.RenderItem.getNameRaw()) &&
                                TransformerMethod.renderItemAndEffectIntoGUI.matches(methodInsnNode)) {

                            methodNode.instructions.insert(abstractNode, insertShowEnchantments());
                            break;
                        }
                    }
                }
            } else if (TransformerMethod.drawScreen.matches(methodNode)) {

                // Objective 1:
                // Find: int l = 240;
                // Add: GuiContainerHook.setLastSlot();

                // Objective 2:
                // Find: this.drawGradientRect(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
                // Add: GuiContainerHook.drawGradientRect(this, j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433, this.theSlot);

                // Objective 3:
                // Find: this.drawSlot(slot);
                // Add: GuiContainerHook.drawSlot(this, slot);

                // Objective 4:
                // Find: Return statement.
                // Add: GuiContainerHook.drawBackpacks(this, mouseX, mouseY, this.fontRendererObj);

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                     if (abstractNode instanceof VarInsnNode && abstractNode.getOpcode() == Opcodes.ISTORE) {
                        VarInsnNode varInsnNode = (VarInsnNode)abstractNode;
                        if (varInsnNode.var == 7) {
                            methodNode.instructions.insert(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiContainerHook",
                                    "setLastSlot", "()V", false)); // GuiContainerHook.setLastSlot();
                        }
                    } else if (abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)abstractNode;
                        if (methodInsnNode.owner.equals(TransformerClass.GuiContainer.getNameRaw()) &&
                                TransformerMethod.drawGradientRect.matches(methodInsnNode)) {
                            methodNode.instructions.insertBefore(abstractNode, new VarInsnNode(Opcodes.ALOAD, 0));
                            methodNode.instructions.insertBefore(abstractNode, TransformerField.theSlot.getField(TransformerClass.GuiContainer)); // this.theSlot

                            methodNode.instructions.insertBefore(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiContainerHook",
                                    "drawGradientRect", "("+TransformerClass.GuiContainer.getName()+"IIIIII"+TransformerClass.Slot.getName()+")V", false));
                            // GuiContainerHook.drawGradientRect(this, j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433, this.theSlot);

                            iterator.remove(); // Remove previous call.
                        }
                    }  else if (abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKESPECIAL ) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)abstractNode;
                        if (methodInsnNode.owner.equals(TransformerClass.GuiContainer.getNameRaw()) && TransformerMethod.drawSlot.matches(methodInsnNode)) {
                            methodNode.instructions.insert(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiContainerHook",
                                    "drawSlot","("+TransformerClass.GuiContainer.getName()+TransformerClass.Slot.getName()+")V", false));
                            // GuiContainerHook.drawSlot(this, slot);

                            methodNode.instructions.insert(abstractNode, new VarInsnNode(Opcodes.ALOAD, 9)); // slot

                            methodNode.instructions.insert(abstractNode, new VarInsnNode(Opcodes.ALOAD, 0)); // this
                        }
                    } else if (abstractNode instanceof InsnNode && abstractNode.getOpcode() == Opcodes.RETURN) {
                        methodNode.instructions.insertBefore(abstractNode, insertDrawBackpacks());
//                        methodNode.instructions.insertBefore(abstractNode, insertSetOldMousePosition());
                    }
                }
            } else if (TransformerMethod.keyTyped.matches(methodNode)) {

                // Objective 1:
                // Find: 2 lines before "this.checkHotbarKeys(keyCode);"
                // Add: ReturnValue returnValue = new ReturnValue();
                //      GuiContainerHook.keyTyped(this, keyCode, this.theSlot, returnValue);
                //      if (returnValue.isCancelled) {
                //          return;
                //      }

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)abstractNode;
                        if (methodInsnNode.owner.equals(TransformerClass.GuiContainer.getNameRaw())  &&
                                TransformerMethod.checkHotbarKeys.matches(methodInsnNode)) {
                            methodNode.instructions.insertBefore(abstractNode.getPrevious().getPrevious(), insertKeyTyped());
                        }
                    }
                }

                // Objective 2:
                // Find: Method head.
                // Add: GuiContainerHook.keyTyped(keyCode);

                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), insertKeyTypedTwo());

            } else if (TransformerMethod.handleMouseClick.matches(methodNode)) {

                // Objective 1:
                // Find: Method head.
                // Add:
                //     ReturnValue returnValue = new ReturnValue();
                //     GuiInventoryHook.handleMouseClick(this.guiLeft, this.guiTop, this.oldMouseX, this.oldMouseY, this.xSize, this.ySize, returnValue);
                //     if (returnValue.isCancelled()) {
                //         return;
                //     }
                //     super.handleMouseClick(slotIn, slotId, clickedButton, clickType);
                // }

                methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), handleMouseClickBody());
            }
        }
    }

//    private InsnList insertSetOldMousePosition() {
//        InsnList list = new InsnList();
//
//        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.oldMouseX =
//        list.add(new VarInsnNode(Opcodes.ILOAD, 1)); // mouseX
//        list.add(new FieldInsnNode(Opcodes.PUTFIELD, TransformerClass.GuiContainer.getNameRaw(), "oldMouseX", "I"));
//
//        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.oldMouseY =
//        list.add(new VarInsnNode(Opcodes.ILOAD, 2)); // mouseX
//        list.add(new FieldInsnNode(Opcodes.PUTFIELD, TransformerClass.GuiContainer.getNameRaw(), "oldMouseY", "I"));
//
//        return list;
//    }

    private InsnList insertKeyTypedTwo() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ILOAD, 2)); // keyCode
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiContainerHook",
                "keyTyped", "(I)V", false)); // GuiContainerHook.keyTyped(keyCode);

        return list;
    }

    private InsnList insertShowEnchantments() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // slotIn
        list.add(new VarInsnNode(Opcodes.ILOAD, 2)); // i
        list.add(new VarInsnNode(Opcodes.ILOAD, 3)); // j
        list.add(new VarInsnNode(Opcodes.ALOAD, 4)); // itemstack
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiContainerHook",
                "showEnchantments", "("+TransformerClass.Slot.getName()+"II"+TransformerClass.ItemStack.getName()+")V", false)); // GuiContainerHook.showEnchantments(slotIn, i, j, item);

        return list;
    }

    private InsnList insertDrawBackpacks() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this

        list.add(new VarInsnNode(Opcodes.ILOAD, 1)); // mouseX
        list.add(new VarInsnNode(Opcodes.ILOAD, 2)); // mouseY

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.
        list.add(TransformerField.fontRendererObj.getField(TransformerClass.GuiContainer)); // fontRendererObj
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiContainerHook", // GuiContainerHook.drawBackpacks(this, this.fontRendererObj);
                "drawBackpacks", "("+TransformerClass.GuiContainer.getName()+"II"+TransformerClass.FontRenderer.getName()+")V", false));

        return list;
    }

    private InsnList insertKeyTyped() {
        InsnList list = new InsnList();

        list.add(new TypeInsnNode(Opcodes.NEW, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue"));
        list.add(new InsnNode(Opcodes.DUP)); // ReturnValue returnValue = new ReturnValue();
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "<init>", "()V", false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 3));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
        list.add(new VarInsnNode(Opcodes.ILOAD, 2)); // keyCode

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.theSlot
        list.add(TransformerField.theSlot.getField(TransformerClass.GuiContainer));

        list.add(new VarInsnNode(Opcodes.ALOAD, 3)); // GuiContainerHook.keyTyped(this, keyCode, this.theSlot, returnValue);
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiContainerHook", "keyTyped",
                "("+TransformerClass.GuiContainer.getName()+"I"+TransformerClass.Slot.getName()+"Lcodes/biscuit/skyblockaddons/asm/utils/ReturnValue;)V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 3));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "isCancelled",
                "()Z", false));
        LabelNode notCancelled = new LabelNode(); // if (returnValue.isCancelled())
        list.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled));

        list.add(new InsnNode(Opcodes.RETURN)); // return;
        list.add(notCancelled);

        return list;
    }

    private InsnList handleMouseClickBody() {
        InsnList list = new InsnList();

        list.add(new TypeInsnNode(Opcodes.NEW, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue"));
        list.add(new InsnNode(Opcodes.DUP)); // ReturnValue returnValue = new ReturnValue();
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "<init>", "()V", false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 5));

        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // slotIn
        list.add(new VarInsnNode(Opcodes.ILOAD, 2)); // slotId
        list.add(new VarInsnNode(Opcodes.ILOAD, 3)); // clickedButton
        list.add(new VarInsnNode(Opcodes.ILOAD, 4)); // clickType
        list.add(new VarInsnNode(Opcodes.ALOAD, 5)); // returnValue

        // GuiContainerHook.handleMouseClick(slotIn, slotId, clickedButton, clickType, returnValue);
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiContainerHook", "handleMouseClick",
                "("+TransformerClass.Slot.getName()+"IIILcodes/biscuit/skyblockaddons/asm/utils/ReturnValue;)V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 5));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "isCancelled",
                "()Z", false));
        LabelNode notCancelled = new LabelNode(); // if (returnValue.isCancelled())
        list.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled));

        list.add(new InsnNode(Opcodes.RETURN)); // return;
        list.add(notCancelled);
        return list;
    }
}
