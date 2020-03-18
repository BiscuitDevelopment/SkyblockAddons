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
    actionPerformed("actionPerformed", "func_146284_a", "a", "(Lnet/minecraft/client/gui/GuiButton;)V", "("+TransformerClass.GuiButton.getName()+")V"),

    // GuiChest
    drawGuiContainerBackgroundLayer("drawGuiContainerBackgroundLayer", "func_146976_a", "a", "(FII)V"),

    // GuiScreen
    renderToolTip("renderToolTip", "func_146285_a", "a", "(Lnet/minecraft/item/ItemStack;II)V", "("+TransformerClass.ItemStack.getName()+"II)V"),
    handleComponentClick("handleComponentClick", "func_175276_a", "a", "(Lnet/minecraft/util/IChatComponent;)Z", "("+TransformerClass.IChatComponent.getName()+")Z"),

    // RenderItem
    renderItemAndEffectIntoGUI("renderItemAndEffectIntoGUI", "func_180450_b", "b", "(Lnet/minecraft/item/ItemStack;II)V", "("+TransformerClass.ItemStack.getName()+"II)V"),
    drawGuiContainerForegroundLayer("drawGuiContainerForegroundLayer", "func_146979_b", "b", "(II)V"),

    // GlStateManager
    color("color", "func_179131_c", "c", "(FFFF)V"),
    drawString("drawString", "func_78276_b", "a", "(Ljava/lang/String;III)I"),

    // IChatComponent
    getUnformattedText("getUnformattedText", "func_150260_c", "c", "()Ljava/lang/String;"),

    // ItemStack
    isItemDamaged("isItemDamaged", "func_77951_h", "g", "()Z"),

    // SoundManager
    getNormalizedVolume("getNormalizedVolume", "func_148594_a", "a", "(Lnet/minecraft/client/audio/ISound;Lnet/minecraft/client/audio/SoundPoolEntry;Lnet/minecraft/client/audio/SoundCategory;)F", "("+TransformerClass.ISound.getName()+TransformerClass.SoundPoolEntry.getName()+TransformerClass.SoundCategory.getName()+")F"),

    // TileEntityEnderChestRenderer
    bindTexture("bindTexture", "func_147499_a", "a", "(Lnet/minecraft/util/ResourceLocation;)V", "("+TransformerClass.ResourceLocation.getName()+")V"),

    // ModelChest
    renderAll("renderAll", "func_78231_a", "a", "()V"),

    // EntityPlayerSP
    dropOneItem("dropOneItem", "func_71040_bB", "a", "(Z)Lnet/minecraft/entity/item/EntityItem;", "(Z)"+TransformerClass.EntityItem.getName()),

    // EntityRenderer
    getMouseOver("getMouseOver", "func_78473_a", "a", "(F)V"),
    getNightVisionBrightness("getNightVisionBrightness", "func_180438_a", "a", "("+TransformerClass.EntityLivingBase.getName()+"F)F", "(Lnet/minecraft/entity/EntityLivingBase;F)F"),

    // GuiNewChat
    printChatMessageWithOptionalDeletion("printChatMessageWithOptionalDeletion", "func_146234_a", "a", "(Lnet/minecraft/util/IChatComponent;I)V", "("+TransformerClass.IChatComponent.getName()+"I)V"),

    // Minecraft
    refreshResources("refreshResources", "func_110436_a", "e", "()V"),
    rightClickMouse("rightClickMouse", "func_147121_ag", "ax", "()V"),
    isIntegratedServerRunning("isIntegratedServerRunning", "func_71387_A", "E", "()Z"),

    // MouseHelper
    ungrabMouseCursor("ungrabMouseCursor", "func_74373_b", "b", "()V"),

    // NetHandlerPlayClient
    handleSetSlot("handleSetSlot", "func_147266_a", "a", "(Lnet/minecraft/network/play/server/S2FPacketSetSlot;)V", "("+TransformerClass.S2FPacketSetSlot.getName()+")V"),
    handleWindowItems("handleWindowItems", "func_147241_a", "a", "(Lnet/minecraft/network/play/server/S30PacketWindowItems;)V", "("+TransformerClass.S30PacketWindowItems.getName()+")V"),

    // PlayerControllerMP
    clickBlock("clickBlock", "func_180511_b", "b", "(Lnet/minecraft/util/BlockPos;Lnet/minecraft/util/EnumFacing;)Z", "("+TransformerClass.BlockPos.getName()+TransformerClass.EnumFacing.getName()+")Z"),
    onPlayerDestroyBlock("onPlayerDestroyBlock", "func_178888_a", "a", "(Lnet/minecraft/util/BlockPos;Lnet/minecraft/util/EnumFacing;)Z", "("+TransformerClass.BlockPos.getName()+TransformerClass.EnumFacing.getName()+")Z"),
    windowClick("windowClick", "func_78753_a", "a", "(IIIILnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;", "(IIII"+TransformerClass.EntityPlayer.getName()+")"+TransformerClass.ItemStack.getName()),

    // RendererLivingEntity
    rotateCorpse("rotateCorpse", "func_77043_a", "a", "(Lnet/minecraft/entity/EntityLivingBase;FFF)V", "("+TransformerClass.EntityLivingBase.getName()+"FFF)V"),
    isWearing("isWearing", "func_175148_a", "a", "(Lnet/minecraft/entity/player/EnumPlayerModelParts;)Z", "("+TransformerClass.EnumPlayerModelParts.getName()+")Z"),

    // RenderManager
    shouldRender("shouldRender", "func_178635_a", "a", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;DDD)Z", "("+TransformerClass.Entity.getName()+TransformerClass.ICamera.getName()+"DDD)Z"),

    // SoundManager
    playSound("playSound", "func_148611_c", "c", "(Lnet/minecraft/client/audio/ISound;)V", "("+TransformerClass.ISound.getName()+")V"),

    // TileEntityEnderChestRenderer
    renderTileEntityAt("renderTileEntityAt", "func_180535_a", "a", "(Lnet/minecraft/tileentity/TileEntity;DDDFI)V", "("+TransformerClass.TileEntity.getName()+"DDDFI)V"),

    // FontRenderer
    renderChar("renderChar", "func_181559_a", "a", "(CZ)F"),

    // Constructor
    init("<init>", "<init>", "<init>", "()V"),

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
        if (SkyblockAddonsTransformer.isDeobfuscated()) {
            name = deobfMethod;
            description = seargeDescription;
        } else {
            if (SkyblockAddonsTransformer.isUsingNotchMappings()) {
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

    public boolean matches(MethodNode methodNode) {
        return this.name.equals(methodNode.name) && (this.description.equals(methodNode.desc) || this == init);
    }
}
