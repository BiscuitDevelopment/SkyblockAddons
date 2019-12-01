package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsTransformer;
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
        return new String[]{"net.minecraft.client.gui.inventory.GuiContainer"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {

        for (MethodNode methodNode : classNode.methods) { // Loop through all methods inside of the class.

            String methodName = mapMethodName(classNode, methodNode); // Map all of the method names.
            if (nameMatches(methodName,"drawSlot", "func_146977_a")) {

                // Objective:
                // Find: After this.itemRender.renderItemAndEffectIntoGUI(itemstack, i, j);
                // Add: GuiContainerHook.showEnchantments(slotIn, i, j, item);

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)abstractNode;
                        if (nameMatches(methodInsnNode.owner,"net/minecraft/client/renderer/entity/RenderItem", "bjh") &&
                                nameMatches(methodInsnNode.name,"renderItemAndEffectIntoGUI", "func_180450_b", "b") &&
                                (!methodInsnNode.name.equals("b") || methodInsnNode.desc.equals("(Lzx;II)V"))) {

                            methodNode.instructions.insert(abstractNode, insertShowEnchantments());
                            break;
                        }
                    }
                }
            } else if (nameMatches(methodName,"drawScreen", "func_73863_a")) {

                // Objective 1:
                // Find: Return statement.
                // Add: GuiContainerHook.drawBackpacks(this, this.fontRendererObj);

                // Objective 2:
                // Find: int l = 240;
                // Add: GuiContainerHook.setLastSlot();

                // Objective 3:
                // Find: this.drawGradientRect(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
                // Add: GuiContainerHook.drawGradientRect(this, j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433, this.theSlot);

                // Objective 4:
                // Find: this.drawSlot(slot);
                // Add: GuiContainerHook.drawSlot(this, slot);

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof InsnNode && abstractNode.getOpcode() == Opcodes.RETURN) {
                        methodNode.instructions.insertBefore(abstractNode, insertDrawBackpacks());
                    } else if (abstractNode instanceof VarInsnNode && abstractNode.getOpcode() == Opcodes.ISTORE) {
                        VarInsnNode varInsnNode = (VarInsnNode)abstractNode;
                        if (varInsnNode.var == 7) {
                            methodNode.instructions.insert(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiContainerHook",
                                    "setLastSlot", "()V", false)); // GuiContainerHook.setLastSlot();
                        }
                    } else if (abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)abstractNode;
                        if (nameMatches(methodInsnNode.owner,"net/minecraft/client/gui/inventory/GuiContainer", "ayl") &&
                                nameMatches(methodInsnNode.name, "drawGradientRect", "func_73733_a", "a")
                                && (!methodInsnNode.name.equals("a") || methodInsnNode.desc.equals("(IIIIII)V"))) {
                            methodNode.instructions.insertBefore(abstractNode, new VarInsnNode(Opcodes.ALOAD, 0));
                            methodNode.instructions.insertBefore(abstractNode, new FieldInsnNode(Opcodes.GETFIELD, SkyblockAddonsTransformer.DEOBFUSCATED ? "net/minecraft/client/gui/inventory/GuiContainer" : "ayl",
                                    SkyblockAddonsTransformer.DEOBFUSCATED ? "theSlot" : "u", SkyblockAddonsTransformer.DEOBFUSCATED ? "Lnet/minecraft/inventory/Slot;" : "Lyg;")); // this.theSlot

                            methodNode.instructions.insertBefore(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiContainerHook",
                                    "drawGradientRect", SkyblockAddonsTransformer.DEOBFUSCATED ? "(Lnet/minecraft/client/gui/inventory/GuiContainer;IIIIIILnet/minecraft/inventory/Slot;)V" :
                                    "(Layl;IIIIIILyg;)V", false));
                            // GuiContainerHook.drawGradientRect(this, j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433, this.theSlot);

                            iterator.remove(); // Remove previous call.
                        }
                    }  else if (abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKESPECIAL ) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode)abstractNode;
                        if (nameMatches(methodInsnNode.owner,"net/minecraft/client/gui/inventory/GuiContainer", "ayl") && nameMatches(methodInsnNode.name, "drawSlot", "func_146977_a", "a")
                        && (!methodInsnNode.name.equals("a") || methodInsnNode.desc.equals("(Lyg;)V"))) {
                            methodNode.instructions.insert(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiContainerHook",
                                    "drawSlot", SkyblockAddonsTransformer.DEOBFUSCATED ? "(Lnet/minecraft/client/gui/inventory/GuiContainer;Lnet/minecraft/inventory/Slot;)V"
                                    : "(Layl;Lyg;)V", false));
                            // GuiContainerHook.drawSlot(this, slot);

                            methodNode.instructions.insert(abstractNode, new VarInsnNode(Opcodes.ALOAD, 9)); // slot

                            methodNode.instructions.insert(abstractNode, new VarInsnNode(Opcodes.ALOAD, 0)); // this
                        }
                    }
                }
            } else if (nameMatches(methodName,"keyTyped", "func_73869_a")) {

                // Objective:
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
                        if (nameMatches(methodInsnNode.owner,"net/minecraft/client/gui/inventory/GuiContainer", "ayl")  &&
                                nameMatches(methodInsnNode.name, "checkHotbarKeys", "func_146983_a", "b") && (!methodInsnNode.name.equals("b") || methodInsnNode.desc.equals("(I)Z"))) {
                            methodNode.instructions.insertBefore(abstractNode.getPrevious().getPrevious(), insertKeyTyped());
                        }
                    }
                }
            }
        }
    }

    private InsnList insertShowEnchantments() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // slotIn
        list.add(new VarInsnNode(Opcodes.ILOAD, 2)); // i
        list.add(new VarInsnNode(Opcodes.ILOAD, 3)); // j
        list.add(new VarInsnNode(Opcodes.ALOAD, 4)); // itemstack
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiContainerHook",
                "showEnchantments", DEOBFUSCATED ? "(Lnet/minecraft/client/gui/inventory/GuiContainer;IILnet/minecraft/item/ItemStack;)V" :
                "(Lyg;IILzx;)V", false)); // GuiContainerHook.showEnchantments(slotIn, i, j, item);

        return list;
    }

    private InsnList insertDrawBackpacks() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.
        list.add(new FieldInsnNode(Opcodes.GETFIELD, DEOBFUSCATED ? "net/minecraft/client/GuiScreen" : "ayl", SkyblockAddonsTransformer.DEOBFUSCATED ?
                "fontRendererObj" : "q", DEOBFUSCATED ? "Lnet/minecraft/client/gui/FontRenderer;" : "Lavn;")); // fontRendererObj
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiContainerHook", // GuiContainerHook.drawBackpacks(this, this.fontRendererObj);
                "drawBackpacks", DEOBFUSCATED ? "(Lnet/minecraft/client/gui/inventory/GuiContainer;Lnet/minecraft/client/gui/FontRenderer;)V" :
                "(Layl;Lavn;)V", false));

        return list;
    }

    private InsnList insertKeyTyped() {
        InsnList list = new InsnList();

        list.add(new TypeInsnNode(Opcodes.NEW, "codes/biscuit/skyblockaddons/asm/hooks/ReturnValue"));
        list.add(new InsnNode(Opcodes.DUP)); // ReturnValue returnValue = new ReturnValue();
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "codes/biscuit/skyblockaddons/asm/hooks/ReturnValue", "<init>", "()V", false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 3));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
        list.add(new VarInsnNode(Opcodes.ILOAD, 2)); // keyCode

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.theSlot
        list.add(new FieldInsnNode(Opcodes.GETFIELD, DEOBFUSCATED ? "net/minecraft/client/gui/inventory/GuiContainer" : "ayl", SkyblockAddonsTransformer.DEOBFUSCATED ?
                "theSlot" : "u",  //field_147006_u
                DEOBFUSCATED ? "Lnet/minecraft/inventory/Slot;" :"Lyg;"));

        list.add(new VarInsnNode(Opcodes.ALOAD, 3)); // GuiContainerHook.keyTyped(this, keyCode, this.theSlot, returnValue);
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiContainerHook", "keyTyped",
                DEOBFUSCATED ? "(Lnet/minecraft/client/gui/inventory/GuiContainer;ILnet/minecraft/inventory/Slot;Lcodes/biscuit/skyblockaddons/asm/hooks/ReturnValue;)V" :
                        "(Layl;ILyg;Lcodes/biscuit/skyblockaddons/asm/hooks/ReturnValue;)V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 3));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "codes/biscuit/skyblockaddons/asm/hooks/ReturnValue", "isCancelled",
                "()Z", false));
        LabelNode notCancelled = new LabelNode(); // if (returnValue.isCancelled())
        list.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled));

        list.add(new InsnNode(Opcodes.RETURN)); // return;
        list.add(notCancelled);

        return list;
    }
}
