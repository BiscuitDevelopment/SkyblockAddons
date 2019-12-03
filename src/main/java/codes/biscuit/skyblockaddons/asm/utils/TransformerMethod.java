package codes.biscuit.skyblockaddons.asm.utils;

import codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public enum TransformerMethod {

    // GuiChest / GuiContainer / Gui
    updateScreen("updateScreen", "func_73876_c", "e", "()V"),
    onGuiClosed("onGuiClosed", "func_146281_b", "m", "()V"),
    drawScreen("drawScreen", "func_73863_a", "a", "(IIF)V"),
    initGui("initGui", "func_73866_w", "b", "()V"),
    keyTyped("keyTyped", "func_73869_a", "a", "(CI)V", true),
    handleMouseClick("handleMouseClick", "func_146984_a", "a", "(Lnet/minecraft/inventory/Slot;III)V", "("+TransformerClass.Slot.getName()+"III)V"),
    mouseClicked("mouseClicked", "func_73864_a", "a", "(III)V", true),
    drawGradientRect("drawGradientRect", "func_73733_a", "a", "(IIIIII)V"),
    drawSlot("drawSlot", "func_146977_a", "a", "(Lnet/minecraft/inventory/Slot;)V", "("+TransformerClass.Slot.getName()+")V"),
    checkHotbarKeys("checkHotbarKeys", "func_146983_a", "b", "(I)Z"),

    // RenderItem
    renderItemAndEffectIntoGUI("renderItemAndEffectIntoGUI", "func_180450_b", "b", "(Lnet/minecraft/item/ItemStack;II)V", "("+TransformerClass.ItemStack.getName()+"II)V"),

    //GlStateManager
    color("color", "func_179131_c", "c", "(FFFF)V"),
    drawString("drawString", "func_78276_b", "a", "(Ljava/lang/String;III)I"),

    //IChatComponent
    getUnformattedText("getUnformattedText", "func_150260_c", "c", "()Ljava/lang/String;"),

    //ItemStack
    isItemDamaged("isItemDamaged", "func_77951_h", "g", "()Z"),

    //SoundManager
    getNormalizedVolume("getNormalizedVolume", "func_148594_a", "a", "F"),

    //TileEntityEnderChestRenderer
    bindTexture("bindTexture", "func_147499_a", "a", "(Lnet/minecraft/util/ResourceLocation;)V", "("+TransformerClass.ResourceLocation.getName()+")V"),

    //ModelChest
    renderAll("renderAll", "func_78231_a", "a", "()V"),

    NULL(null,null,null,null,false);

    private String name;
    private String description;
    private String[] exceptions = null;

    TransformerMethod(String deobfMethod, String seargeMethod, String notchMethod18, String seargeDescription) {
        this(deobfMethod, seargeMethod, notchMethod18, seargeDescription, seargeDescription, false);
    }

    TransformerMethod(String deobfMethod, String seargeMethod, String notchMethod18, String seargeDescription, String notchDescription) {
        this(deobfMethod, seargeMethod, notchMethod18, seargeDescription, notchDescription, false);
    }

    TransformerMethod(String deobfMethod, String seargeMethod, String notchMethod18, String seargeDescription, boolean ioException) {
        this(deobfMethod, seargeMethod, notchMethod18, seargeDescription, seargeDescription, ioException);
    }

    TransformerMethod(String deobfMethod, String seargeMethod, String notchMethod18, String seargeDescription, String notchDescription, boolean ioException) {
        if (SkyblockAddonsTransformer.DEOBFUSCATED) {
            name = deobfMethod;
            description = seargeDescription;
        } else {
            if (SkyblockAddonsTransformer.NOTCH_MAPPINGS) {
                name = notchMethod18;
                description = notchDescription;
            } else {
                name = seargeMethod;
                description = seargeDescription;
            }
        }
        if (ioException) exceptions = new String[] {"java/io/IOException"};
    }

    public String getName() {
        return name;
    }

    public MethodNode createMethodNode() {
        return new MethodNode(Opcodes.ACC_PUBLIC, name, description, null, exceptions);
    }

    public boolean matches(MethodInsnNode methodInsnNode) {
        return this.name.equals(methodInsnNode.name) && this.description.equals(methodInsnNode.desc);
    }
}
