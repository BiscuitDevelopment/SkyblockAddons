package codes.biscuit.skyblockaddons.asm;

import codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsTransformer;
import codes.biscuit.skyblockaddons.tweaker.transformer.ITransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;
import java.util.List;

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

        MethodNode updateScreen = new MethodNode(Opcodes.ACC_PUBLIC, SkyblockAddonsTransformer.DEOBFUSCATED ? "updateScreen" : "func_73876_c", "()V", null, null);
        updateScreen.instructions.add(updateScreen());
        classNode.methods.add(updateScreen);

        // Objective: Add:
        //
        // @Override
        // public updateScreen() {
        //     GuiChestHook.updateScreen();
        // }

        MethodNode onGuiClosed = new MethodNode(Opcodes.ACC_PUBLIC, SkyblockAddonsTransformer.DEOBFUSCATED ? "onGuiClosed" : "func_146281_b", "()V", null, null);
        onGuiClosed.instructions.add(onGuiClosed());
        classNode.methods.add(onGuiClosed);

        // Objective: Add:
        //
        // @Override
        // public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        //     super.drawScreen(mouseX, mouseY, partialTicks);
        //     GuiChestHook.drawScreen(this.guiLeft, this.guiTop);
        // }

        MethodNode drawScreen = new MethodNode(Opcodes.ACC_PUBLIC, SkyblockAddonsTransformer.DEOBFUSCATED ? "drawScreen" : "func_73863_a", "(IIF)V", null, null);
        drawScreen.instructions.add(drawScreen());
        classNode.methods.add(drawScreen);

        // Objective: Add:
        //
        // @Override
        // public initGui() {
        //     super.initGui();
        //     GuiChestHook.initGui(this.lowerChestInventory, this.guiLeft, this.guiTop, this.fontRendererObj);
        // }

        MethodNode initGui = new MethodNode(Opcodes.ACC_PUBLIC, SkyblockAddonsTransformer.DEOBFUSCATED ? "initGui" : "func_73866_w_", "()V", null, null);
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

        MethodNode keyTyped = new MethodNode(Opcodes.ACC_PUBLIC, SkyblockAddonsTransformer.DEOBFUSCATED ? "keyTyped" : "func_73869_a", "(CI)V",
                null, new String[]{"java/io/IOException"});
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

        MethodNode handleMouseClick = new MethodNode(Opcodes.ACC_PUBLIC, SkyblockAddonsTransformer.DEOBFUSCATED ? "handleMouseClick" : "func_146984_a", "(Lnet/minecraft/inventory/Slot;III)V", null, null);
        handleMouseClick.instructions.add(handleMouseClick());
        classNode.methods.add(handleMouseClick);

        // Objective: Add:
        //
        // @Override
        // public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        //     GuiChestHook.mouseClicked(mouseX, mouseY, mouseButton);
        //     super.mouseClicked(mouseX, mouseY, mouseButton);
        // }

        MethodNode mouseClicked = new MethodNode(Opcodes.ACC_PUBLIC, SkyblockAddonsTransformer.DEOBFUSCATED ? "mouseClicked" : "func_73864_a", "(III)V", null,
                new String[]{"java/io/IOException"});
        mouseClicked.instructions.add(mouseClicked());
        classNode.methods.add(mouseClicked);

        for (MethodNode methodNode : (List<MethodNode>)classNode.methods) { // Loop through all methods inside of the class.

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
                        if (nameMatches(methodInsnNode.owner,"net/minecraft/client/renderer/GlStateManager", "bfl") && nameMatches(methodInsnNode.name,"color", "func_179131_c", "c")) {
                            methodNode.instructions.insertBefore(abstractNode, new VarInsnNode(Opcodes.ALOAD, 0)); // this.lowerChestInventory
                            methodNode.instructions.insertBefore(abstractNode, new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/gui/inventory/GuiChest", SkyblockAddonsTransformer.DEOBFUSCATED ?
                                    "lowerChestInventory" : "field_147015_w", "Lnet/minecraft/inventory/IInventory;"));

                            methodNode.instructions.insertBefore(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiChestHook",
                                    "color", SkyblockAddonsTransformer.DEOBFUSCATED ? "(FFFFLnet/minecraft/inventory/IInventory;)V" :"(FFFFLog;)V", false));
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
                        if (nameMatches(methodInsnNode.owner, "net/minecraft/client/gui/FontRenderer", "avn") && nameMatches(methodInsnNode.name,"drawString", "func_78276_b", "a")) {
                            methodNode.instructions.insertBefore(abstractNode, new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiChestHook",
                                    "drawString", SkyblockAddonsTransformer.DEOBFUSCATED ? "(Lnet/minecraft/client/gui/FontRenderer;Ljava/lang/String;III)I"
                                    : "(Lavn;Ljava/lang/String;III)I", false));
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
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/client/gui/inventory/GuiContainer", "drawScreen", "(IIF)V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.guiLeft
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/gui/inventory/GuiChest", SkyblockAddonsTransformer.DEOBFUSCATED ? "guiLeft" : "field_147003_i", "I"));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.guiTop
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/gui/inventory/GuiChest", SkyblockAddonsTransformer.DEOBFUSCATED ? "guiTop" : "field_147009_r", "I"));

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiChestHook", "drawScreen",
                "(II)V", false)); // GuiChestHook.drawScreen(this.guiLeft, this.guiTop);

        list.add(new InsnNode(Opcodes.RETURN));
        return list;
    }

    private InsnList initGui() {
        InsnList list = new InsnList();

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // super.initGui();
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/client/gui/inventory/GuiContainer", "initGui", "()V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.lowerChestInventory
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/gui/inventory/GuiChest", SkyblockAddonsTransformer.DEOBFUSCATED ? "lowerChestInventory" : "field_147015_w", "Lnet/minecraft/inventory/IInventory;"));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.guiLeft
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/gui/inventory/GuiChest", SkyblockAddonsTransformer.DEOBFUSCATED ? "guiLeft" : "field_147003_i", "I"));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.guiTop
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/gui/inventory/GuiChest", SkyblockAddonsTransformer.DEOBFUSCATED ? "guiTop" : "field_147009_r", "I"));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.fontRendererObj
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/gui/inventory/GuiChest", SkyblockAddonsTransformer.DEOBFUSCATED ? "fontRendererObj" : "field_71466_p", "Lnet/minecraft/client/gui/FontRenderer;"));

        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiChestHook", "initGui",
                "(Lnet/minecraft/inventory/IInventory;IILnet/minecraft/client/gui/FontRenderer;)V", false)); // GuiChestHook.initGui(this.lowerChestInventory, this.guiLeft, this.guiTop, this.fontRendererObj);

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
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/client/gui/inventory/GuiContainer", SkyblockAddonsTransformer.DEOBFUSCATED ? "keyTyped" :
                "func_73869_a", "(CI)V", false));

        list.add(notCancelled);
        list.add(new InsnNode(Opcodes.RETURN));
        return list;
    }

    private InsnList handleMouseClick() {
        InsnList list = new InsnList();

        list.add(new TypeInsnNode(Opcodes.NEW, "codes/biscuit/skyblockaddons/asm/hooks/ReturnValue"));
        list.add(new InsnNode(Opcodes.DUP)); // ReturnValue returnValue = new ReturnValue();
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "codes/biscuit/skyblockaddons/asm/hooks/ReturnValue", "<init>", "()V", false));
        list.add(new VarInsnNode(Opcodes.ASTORE, 5));

        list.add(new VarInsnNode(Opcodes.ALOAD, 1)); // slotIn

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.inventorySlots
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/gui/inventory/GuiChest", SkyblockAddonsTransformer.DEOBFUSCATED ? "inventorySlots" : "field_147002_h", "Lnet/minecraft/inventory/Container;"));

        list.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this.lowerChestInventory
        list.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/gui/inventory/GuiChest", SkyblockAddonsTransformer.DEOBFUSCATED ? "lowerChestInventory" : "field_147015_w", "Lnet/minecraft/inventory/IInventory;"));

        list.add(new VarInsnNode(Opcodes.ALOAD, 5)); // EntityPlayerSPHook.handleMouseClick(slotIn, this.inventorySlots, this.lowerChestInventory, returnValue)
        list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "codes/biscuit/skyblockaddons/asm/hooks/GuiChestHook", "handleMouseClick",
                "(Lnet/minecraft/inventory/Slot;Lnet/minecraft/inventory/Container;Lnet/minecraft/inventory/IInventory;Lcodes/biscuit/skyblockaddons/asm/hooks/ReturnValue;)V", false));

        list.add(new VarInsnNode(Opcodes.ALOAD, 5));
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "codes/biscuit/skyblockaddons/asm/hooks/ReturnValue", "isCancelled",
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
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/client/gui/inventory/GuiContainer", SkyblockAddonsTransformer.DEOBFUSCATED ? "handleMouseClick" : "func_146984_a", "(Lnet/minecraft/inventory/Slot;III)V", false));

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
        list.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "net/minecraft/client/gui/inventory/GuiContainer", SkyblockAddonsTransformer.DEOBFUSCATED ? "mouseClicked" : "func_73864_a", "(III)V", false));

        list.add(new InsnNode(Opcodes.RETURN));
        return list;
    }
}
