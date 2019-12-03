package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.asm.utils.TransformerClass;
import codes.biscuit.skyblockaddons.asm.utils.TransformerField;
import codes.biscuit.skyblockaddons.asm.utils.TransformerMethod;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class GuiChestTransformer implements ITransformer {

    /**
     * {@link net.minecraft.client.gui.inventory.GuiChest}
     */
    @Override
    public String[] getClassName() {
        return new String[]{"net.minecraft.client.gui.inventory.GuiChest"};
    }

    @Override
    public void transform(ClassNode classNode, String name) {

        // Objective: Add:
        //
        // @Override
        // public updateScreen() {
        //     GuiChestHook.updateScreen();
        // }

        MethodNode updateScreen = TransformerMethod.updateScreen.createMethodNode();
        updateScreen.instructions.add(updateScreen());
        classNode.methods.add(updateScreen);

        // Objective: Add:
        //
        // @Override
        // public onGuiClosed() {
        //     GuiChestHook.onGuiClosed();
        // }

        MethodNode onGuiClosed = TransformerMethod.onGuiClosed.createMethodNode();
        onGuiClosed.instructions.add(onGuiClosed());
        classNode.methods.add(onGuiClosed);

        // Objective: Add:
        //
        // @Override
        // public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        //     super.drawScreen(mouseX, mouseY, partialTicks);
        //     GuiChestHook.drawScreen(this.guiLeft, this.guiTop);
        // }

        MethodNode drawScreen = TransformerMethod.drawScreen.createMethodNode();
        drawScreen.instructions.add(drawScreen());
        classNode.methods.add(drawScreen);

        // Objective: Add:
        //
        // @Override
        // public initGui() {
        //     super.initGui();
        //     GuiChestHook.initGui(this.lowerChestInventory, this.guiLeft, this.guiTop, this.fontRendererObj);
        // }

        MethodNode initGui = TransformerMethod.initGui.createMethodNode();
        initGui.instructions.add(initGui());
        classNode.methods.add(initGui);

        // Objective: Add:
        //
        // @Override
        // public keyTyped(char typedChar, int keyCode) throws IOException {
        //     if (GuiChestHook.keyTyped(typedChar, keyCode)) {
        //         super.keyTyped(typedChar, keyCode);
        //     }
        // }

        MethodNode keyTyped = TransformerMethod.keyTyped.createMethodNode();
        keyTyped.instructions.add(keyTyped());
        classNode.methods.add(keyTyped);

        // Objective: Add:
        //
        // @Override
        // public void handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType) {
        //     ReturnValue returnValue = new ReturnValue();
        //     GuiChestHook.initGui(this.lowerChestInventory, this.guiLeft, this.guiTop, this.fontRendererObj, returnValue);
        //     if (returnValue.isCancelled) {
        //         return;
        //     }
        //     super.handleMouseClick(slotIn, slotId, clickedButton, clickType);
        // }

        MethodNode handleMouseClick = TransformerMethod.handleMouseClick.createMethodNode();
        handleMouseClick.instructions.add(handleMouseClick());
        classNode.methods.add(handleMouseClick);

        // Objective: Add:
        //
        // @Override
        // public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        //     GuiChestHook.mouseClicked(mouseX, mouseY, mouseButton);
        //     super.mouseClicked(mouseX, mouseY, mouseButton);
        // }

        MethodNode mouseClicked = TransformerMethod.mouseClicked.createMethodNode();
        mouseClicked.instructions.add(mouseClicked());
        classNode.methods.add(mouseClicked);

        for (MethodNode methodNode : classNode.methods) { // Loop through all methods inside of the class.

            String methodName = mapMethodName(classNode, methodNode);
            if (nameMatches(methodName, "drawGuiContainerBackgroundLayer", "func_146976_a")) {

                // Objective:
                // Find: GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                // Replace With: GuiChestHook.color(1.0F, 1.0F, 1.0F, 1.0F, this.lowerChestInventory);

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKESTATIC) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode) abstractNode;
                        if (methodInsnNode.owner.equals(TransformerClass.GlStateManager.getNameRaw()) && methodInsnNode.name.equals(TransformerMethod.color.getName())) {
                            methodNode.instructions.insertBefore(abstractNode, new VarInsnNode(Opcodes.ALOAD, 0)); // this.lowerChestInventory
                            methodNode.instructions.insertBefore(abstractNode, TransformerField.lowerChestInventory.getField(TransformerClass.GuiChest));

                            methodNode.instructions.insertBefore(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiChestHook",
                                    "color", "(FFFF"+TransformerClass.IInventory.getName()+")V", false));
                            // GuiChestHook.color(1.0F, 1.0F, 1.0F, 1.0F);

                            iterator.remove(); // Remove the old line.
                            break;
                        }
                    }
                }
            } else if (nameMatches(methodName, "drawGuiContainerForegroundLayer", "func_146979_b")) {

                // Objective:
                // Find:
                // this.fontRendererObj.drawString(this.lowerChestInventory.getDisplayName().getUnformattedText(), 8, 6, 4210752);
                // this.fontRendererObj.drawString(this.upperChestInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
                //
                // Replace With:
                // GuiChestHook.drawString(this.fontRendererObj, this.lowerChestInventory.getDisplayName().getUnformattedText(), 8, 6, 4210752);
                // GuiChestHook.drawString(this.fontRendererObj, this.upperChestInventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);

                Iterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
                while (iterator.hasNext()) {
                    AbstractInsnNode abstractNode = iterator.next();
                    if (abstractNode instanceof MethodInsnNode && abstractNode.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                        MethodInsnNode methodInsnNode = (MethodInsnNode) abstractNode;
                        if (methodInsnNode.owner.equals(TransformerClass.FontRenderer.getNameRaw()) && methodInsnNode.name.equals(TransformerMethod.drawString.getName())) {
                            methodNode.instructions.insertBefore(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiChestHook",
                                    "drawString", "("+TransformerClass.FontRenderer.getName()+"Ljava/lang/String;III)I", false));
                            // GuiChestHook.drawString(this.fontRendererObj, this.lowerChestInventory.getDisplayName().getUnformattedText(), 8, ..., 4210752);

                            iterator.remove(); // Remove the old line. Don't break because we need to do this to two lines.
                        }
                    }
                }
            }
        }
    }

    private InsnList updateScreen() {
        InsnList list = new InsnList();

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiChestHook", "updateScreen",
                "()V", false)); // GuiChestHook.updateScreen();

        list.add(new InsnNode(Opcodes.RETURN));
        return list;
    }

    private InsnList onGuiClosed() {
        InsnList list = new InsnList();

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiChestHook", "onGuiClosed",
                "()V", false)); // GuiChestHook.onGuiClosed();

        list.add(new InsnNode(Opcodes.RETURN));
        return list;
    }

    private InsnList drawScreen() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
        list.add(new VarInsnNode(Opcodes.ILOAD, 1)); // mouseX
        list.add(new VarInsnNode(Opcodes.ILOAD, 2)); // mouseY
        list.add(new VarInsnNode(Opcodes.FLOAD, 3)); // super.drawScreen(mouseX, mouseY, partialTicks);
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, TransformerClass.GuiContainer.getNameRaw(), TransformerMethod.drawScreen.getName(), "(IIF)V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.guiLeft
        list.add(TransformerField.guiLeft.getField(TransformerClass.GuiChest));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.guiTop
        list.add(TransformerField.guiTop.getField(TransformerClass.GuiChest));

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiChestHook", "drawScreen",
                "(II)V", false)); // GuiChestHook.drawScreen(this.guiLeft, this.guiTop);

        list.add(new InsnNode(Opcodes.RETURN));
        return list;
    }

    private InsnList initGui() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // super.initGui();
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, TransformerClass.GuiContainer.getNameRaw(), TransformerMethod.initGui.getName(), "()V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.lowerChestInventory
        list.add(TransformerField.lowerChestInventory.getField(TransformerClass.GuiChest));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.guiLeft
        list.add(TransformerField.guiLeft.getField(TransformerClass.GuiChest));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.guiTop
        list.add(TransformerField.guiTop.getField(TransformerClass.GuiChest));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.fontRendererObj
        list.add(TransformerField.fontRendererObj.getField(TransformerClass.GuiChest));

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiChestHook", "initGui",
                "("+TransformerClass.IInventory.getName()+"II"+TransformerClass.FontRenderer.getName()+")V", false));
        // GuiChestHook.initGui(this.lowerChestInventory, this.guiLeft, this.guiTop, this.fontRendererObj);

        list.add(new InsnNode(Opcodes.RETURN));
        return list;
    }

    private InsnList keyTyped() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ILOAD, 1)); // typedChar
        list.add(new VarInsnNode(Opcodes.ILOAD, 2)); // keyCode

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiChestHook", "keyTyped",
                "(CI)Z", false)); // GuiChestHook.keyTyped(typedChar, keyCode)
        LabelNode notCancelled = new LabelNode();
        list.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new VarInsnNode(Opcodes.ILOAD, 1)); // typedChar
        list.add(new VarInsnNode(Opcodes.ILOAD, 2)); // keyCode
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, TransformerClass.GuiContainer.getNameRaw(), TransformerMethod.keyTyped.getName(), "(CI)V", false));

        list.add(notCancelled);
        list.add(new InsnNode(Opcodes.RETURN));
        return list;
    }

    private InsnList handleMouseClick() {
        InsnList list = new InsnList();

        list.add(new TypeInsnNode(Opcodes.NEW, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue"));
        list.add(new InsnNode(Opcodes.DUP)); // ReturnValue returnValue = new ReturnValue();
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "<init>", "()V", false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 5));

        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // slotIn

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.inventorySlots
        list.add(TransformerField.inventorySlots.getField(TransformerClass.GuiChest));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.lowerChestInventory
        list.add(TransformerField.lowerChestInventory.getField(TransformerClass.GuiChest));

        list.add(new VarInsnNode(Opcodes.ALOAD, 5)); // EntityPlayerSPHook.handleMouseClick(slotIn, this.inventorySlots, this.lowerChestInventory, returnValue)
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiChestHook", "handleMouseClick",
                "("+TransformerClass.Slot.getName()+TransformerClass.Container.getName()+TransformerClass.IInventory.getName()+"Lcodes/biscuit/skyblockaddons/asm/utils/ReturnValue;)V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 5));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "codes/biscuit/skyblockaddons/asm/utils/ReturnValue", "isCancelled",
                "()Z", false));
        LabelNode notCancelled = new LabelNode(); // if (returnValue.isCancelled())
        list.add(new JumpInsnNode(Opcodes.IFEQ, notCancelled));

        list.add(new InsnNode(Opcodes.RETURN)); // return;
        list.add(notCancelled);

        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // slotIn
        list.add(new VarInsnNode(Opcodes.ILOAD, 2)); // slotId
        list.add(new VarInsnNode(Opcodes.ILOAD, 3)); // clickedButton
        list.add(new VarInsnNode(Opcodes.ILOAD, 4)); // clickType // super.handleMouseClick(slotIn, slotId, clickedButton, clickType);
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, TransformerClass.GuiContainer.getNameRaw(), TransformerMethod.handleMouseClick.getName(), "("+TransformerClass.Slot.getName()+"III)V", false));

        list.add(new InsnNode(Opcodes.RETURN));
        return list;
    }

    private InsnList mouseClicked() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ILOAD, 1)); // mouseX
        list.add(new VarInsnNode(Opcodes.ILOAD, 2)); // mouseY
        list.add(new VarInsnNode(Opcodes.ILOAD, 3)); // mouseButton
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiChestHook", "mouseClicked",
                "(III)V", false)); // GuiChestHook.mouseClicked(mouseX, mouseY, mouseButton);

        list.add(new VarInsnNode(Opcodes.ALOAD, 0));
        list.add(new VarInsnNode(Opcodes.ILOAD, 1)); // mouseX
        list.add(new VarInsnNode(Opcodes.ILOAD, 2)); // mouseY
        list.add(new VarInsnNode(Opcodes.ILOAD, 3)); // mouseButton // super.mouseClicked(mouseX, mouseY, mouseButton);
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, TransformerClass.GuiContainer.getNameRaw(), TransformerMethod.mouseClicked.getName(), "(III)V", false));

        list.add(new InsnNode(Opcodes.RETURN));
        return list;
    }
}
