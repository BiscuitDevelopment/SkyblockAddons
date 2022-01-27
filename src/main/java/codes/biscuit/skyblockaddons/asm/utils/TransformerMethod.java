package codes.biscuit.skyblockaddons.asm.utils;

import codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public enum TransformerMethod {

    // GuiChest / GuiContainer / Gui
    updateScreen("updateScreen", "func_73876_c", "e", "()V"),
    drawScreen("drawScreen", "func_73863_a", "a", "(IIF)V"),
    initGui("initGui", "func_73866_w", "b", "()V"),
    keyTyped("keyTyped", "func_73869_a", "a", "(CI)V", true),
    handleMouseClick("handleMouseClick", "func_146984_a", "a", "(Lnet/minecraft/inventory/Slot;III)V", "("+TransformerClass.Slot.getName()+"III)V"),
    mouseClicked("mouseClicked", "func_73864_a", "a", "(III)V", true),
    drawGradientRect("drawGradientRect", "func_73733_a", "a", "(IIIIII)V"),
    drawSlot("drawSlot", "func_146977_a", "a", "(Lnet/minecraft/inventory/Slot;)V", "("+TransformerClass.Slot.getName()+")V"),
    checkHotbarKeys("checkHotbarKeys", "func_146983_a", "b", "(I)Z"),
    actionPerformed("actionPerformed", "func_146284_a", "a", "(Lnet/minecraft/client/gui/GuiButton;)V", "("+TransformerClass.GuiButton.getName()+")V"),
    handleMouseInput("handleMouseInput", "func_178039_p", "p", "()V", true),
    mouseClickMove("mouseClickMove", "func_146273_a", "a", "(IIIJ)V"),
    mouseReleased("mouseReleased", "func_146286_b", "b", "(III)V"),

    // GuiChest
    drawGuiContainerBackgroundLayer("drawGuiContainerBackgroundLayer", "func_146976_a", "a", "(FII)V"),

    // GuiScreen
    renderToolTip("renderToolTip", "func_146285_a", "a", "(Lnet/minecraft/item/ItemStack;II)V", "("+TransformerClass.ItemStack.getName()+"II)V"),
    handleComponentClick("handleComponentClick", "func_175276_a", "a", "(Lnet/minecraft/util/IChatComponent;)Z", "("+TransformerClass.IChatComponent.getName()+")Z"),

    // RenderItem
    renderItemAndEffectIntoGUI("renderItemAndEffectIntoGUI", "func_180450_b", "b", "(Lnet/minecraft/item/ItemStack;II)V", "("+TransformerClass.ItemStack.getName()+"II)V"),
    drawGuiContainerForegroundLayer("drawGuiContainerForegroundLayer", "func_146979_b", "b", "(II)V"),
//    renderItemModelForEntity("renderItemModelForEntity", "func_175049_a", "a", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;)V",
//            "("+TransformerClass.ItemStack.getName()+TransformerClass.EntityLivingBase.getName()+TransformerClass.ItemCameraTransforms$TransformType.getName()+")V"),
    renderItemModelTransform("renderItemModelTransform", "func_175040_a", "a", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;)V",
        "("+TransformerClass.ItemStack.getName()+TransformerClass.IBakedModel.getName()+TransformerClass.ItemCameraTransforms$TransformType.getName()+")V"),
//    renderItem("renderItem", "func_178099_a", "a", "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;)V",
//            "("+TransformerClass.EntityLivingBase.getName()+TransformerClass.ItemStack.getName()+TransformerClass.ItemCameraTransforms$TransformType.getName()+")V"),
    renderItem("renderItem", "func_180454_a", "a", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/resources/model/IBakedModel;)V",
        "("+TransformerClass.ItemStack.getName()+TransformerClass.IBakedModel.getName()+")V"),
    renderModel_IBakedModel_ItemStack("renderModel", "func_175036_a", "a", "(Lnet/minecraft/client/resources/model/IBakedModel;Lnet/minecraft/item/ItemStack;)V",
            "("+TransformerClass.IBakedModel.getName()+TransformerClass.ItemStack.getName()+")V"),
    renderModel_IBakedModel_I_ItemStack("renderModel", "func_175045_a", "a", "(Lnet/minecraft/client/resources/model/IBakedModel;ILnet/minecraft/item/ItemStack;)V",
            "("+TransformerClass.IBakedModel.getName()+"I"+TransformerClass.ItemStack.getName()+")V"),

    // GlStateManager
    color("color", "func_179131_c", "c", "(FFFF)V"),
    drawString("drawString", "func_78276_b", "a", "(Ljava/lang/String;III)I"),
    depthMask("depthMask", "func_179132_a", "a", "(Z)V"),
    alphaFunc("alphaFunc", "func_179092_a", "a", "(IF)V"),

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

    // EntityPlayer
    isPotionActive("isPotionActive", "func_70644_a", "a", "(Lnet/minecraft/potion/Potion;)Z","("+TransformerClass.Potion.getName()+")Z"),
    setCurrentItemOrArmor("setCurrentItemOrArmor", "func_70062_b", "c", "(ILnet/minecraft/item/ItemStack;)V", "(I" + TransformerClass.ItemStack.getName() + ")V"),

    // EntityPlayerSP
    dropOneItem("dropOneItem", "func_71040_bB", "a", "(Z)Lnet/minecraft/entity/item/EntityItem;", "(Z)"+TransformerClass.EntityItem.getName()),
    setPlayerSPHealth("setPlayerSPHealth", "func_71150_b", "n", "(F)V"),

    // EntityRenderer
    getMouseOver("getMouseOver", "func_78473_a", "a", "(F)V"),
    getNightVisionBrightness("getNightVisionBrightness", "func_180438_a", "a", "(Lnet/minecraft/entity/EntityLivingBase;F)F", "("+TransformerClass.EntityLivingBase.getName()+"F)F"),
    updateCameraAndRender("updateCameraAndRender", "func_181560_a", "a", "(FJ)V", "(FJ)V"),

    // GuiNewChat
    printChatMessageWithOptionalDeletion("printChatMessageWithOptionalDeletion", "func_146234_a", "a", "(Lnet/minecraft/util/IChatComponent;I)V", "("+TransformerClass.IChatComponent.getName()+"I)V"),

    // Minecraft
    refreshResources("refreshResources", "func_110436_a", "e", "()V"),
    rightClickMouse("rightClickMouse", "func_147121_ag", "ax", "()V"),
    isIntegratedServerRunning("isIntegratedServerRunning", "func_71387_A", "E", "()Z"),
    runTick("runTick", "func_71407_l", "s", "()V"),
    clickMouse("clickMouse", "func_147116_af", "aw", "()V"),
    sendClickBlockToController("sendClickBlockToController", "func_147115_a", "b", "(Z)V"),

    // MouseHelper
    ungrabMouseCursor("ungrabMouseCursor", "func_74373_b", "b", "()V"),

    // NetHandlerPlayClient
    handleSetSlot("handleSetSlot", "func_147266_a", "a", "(Lnet/minecraft/network/play/server/S2FPacketSetSlot;)V", "("+TransformerClass.S2FPacketSetSlot.getName()+")V"),
    handleWindowItems("handleWindowItems", "func_147241_a", "a", "(Lnet/minecraft/network/play/server/S30PacketWindowItems;)V", "("+TransformerClass.S30PacketWindowItems.getName()+")V"),

    // PlayerControllerMP
    clickBlock("clickBlock", "func_180511_b", "b", "(Lnet/minecraft/util/BlockPos;Lnet/minecraft/util/EnumFacing;)Z", "("+TransformerClass.BlockPos.getName()+TransformerClass.EnumFacing.getName()+")Z"),
    onPlayerDestroyBlock("onPlayerDestroyBlock", "func_178888_a", "a", "(Lnet/minecraft/util/BlockPos;Lnet/minecraft/util/EnumFacing;)Z", "("+TransformerClass.BlockPos.getName()+TransformerClass.EnumFacing.getName()+")Z"),
    windowClick("windowClick", "func_78753_a", "a", "(IIIILnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;", "(IIII"+TransformerClass.EntityPlayer.getName()+")"+TransformerClass.ItemStack.getName()),
    resetBlockRemoving("resetBlockRemoving", "func_78767_c", "c", "()V", "()V"),

    // RendererLivingEntity
    rotateCorpse("rotateCorpse", "func_77043_a", "a", "(Lnet/minecraft/entity/EntityLivingBase;FFF)V", "("+TransformerClass.EntityLivingBase.getName()+"FFF)V"),
    isWearing("isWearing", "func_175148_a", "a", "(Lnet/minecraft/entity/player/EnumPlayerModelParts;)Z", "("+TransformerClass.EnumPlayerModelParts.getName()+")Z"),
    renderModel_RendererLivingEntity("renderModel", "func_77036_a", "a", "(Lnet/minecraft/entity/EntityLivingBase;FFFFFF)V", "("+TransformerClass.EntityLivingBase.getName()+"FFFFFF)V"),
    setScoreTeamColor("setScoreTeamColor", "func_177088_c", "c", "(Lnet/minecraft/entity/EntityLivingBase;)Z", "("+TransformerClass.EntityLivingBase.getName()+")Z"),

    // RenderManager
    shouldRender("shouldRender", "func_178635_a", "a", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;DDD)Z", "("+TransformerClass.Entity.getName()+TransformerClass.ICamera.getName()+"DDD)Z"),

    // SoundManager
    playSound("playSound", "func_148611_c", "c", "(Lnet/minecraft/client/audio/ISound;)V", "("+TransformerClass.ISound.getName()+")V"),

    // TileEntityEnderChestRenderer
    renderTileEntityAt("renderTileEntityAt", "func_180535_a", "a", "(Lnet/minecraft/tileentity/TileEntityEnderChest;DDDFI)V", "("+TransformerClass.TileEntityEnderChest.getName()+"DDDFI)V"),

    // FontRenderer
    renderChar("renderChar", "func_181559_a", "a", "(CZ)F"),
    renderStringAtPos("renderStringAtPos", "func_78255_a", "a", "(Ljava/lang/String;Z)V"),
    resetStyles("resetStyles", "func_78265_b", "e", "()V"),

    // EntityLivingBase
    handleStatusUpdate("handleStatusUpdate", "func_70103_a", "a", "(B)V"),
    removePotionEffectClient("removePotionEffectClient", "func_70618_n", "l", "(I)V"),
    addPotionEffect("addPotionEffect", "func_70690_d", "c", "(Lnet/minecraft/potion/PotionEffect;)V", "(" + TransformerClass.PotionEffect.getName() +")V"),

    // Constructor
    init("<init>", "<init>", "<init>", "()V"),

    // InventoryPlayer
    changeCurrentItem("changeCurrentItem", "func_70453_c", "d", "(I)V"),

    // Render
    getEntityTexture_RenderEnderman("getEntityTexture", "func_110775_a", "a", "(Lnet/minecraft/entity/monster/EntityEnderman;)Lnet/minecraft/util/ResourceLocation;", "("+TransformerClass.EntityEnderman.getName()+")"+TransformerClass.ResourceLocation.getName()),

    // ModelBase
    render("render", "func_78088_a", "a", "(Lnet/minecraft/entity/Entity;FFFFFF)V", "("+TransformerClass.Entity.getName()+"FFFFFF)V"),

    // RenderGlobal
    isRenderEntityOutlines("isRenderEntityOutlines", "func_174985_d", "d", "()Z"),
    renderEntities("renderEntities", "func_180446_a", "a", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;F)V", "(" + TransformerClass.Entity.getName() + TransformerClass.ICamera.getName() + "F)V"),
    renderEntityOutlineFramebuffer("renderEntityOutlineFramebuffer", "func_174975_c", "c", "()V"),
    sendBlockBreakProgress("sendBlockBreakProgress", "func_180441_b", "b", "(I" + TransformerClass.BlockPos.getName() + "I)V"),

    // TileEntityItemStackRenderer
    renderByItem("renderByItem", "func_179022_a", "a", "(Lnet/minecraft/item/ItemStack;)V", "(" + TransformerClass.ItemStack.getName() + ")V"),

    // EffectRenderer
    addEffect("addEffect", "func_78873_a", "a", "(Lnet/minecraft/client/particle/EntityFX;)V", "(" + TransformerClass.EntityFX.getName() + ")V"),
    renderParticles("renderParticles", "func_78874_a", "a", "(" + TransformerClass.Entity.getName() + "F)V"),

    // WorldClient
    onEntityRemoved("onEntityRemoved", "func_72847_b", "b", "(Lnet/minecraft/entity/Entity;)V", "(" + TransformerClass.Entity.getName() + ")V"),
    invalidateRegionAndSetBlock("invalidateRegionAndSetBlock", "func_180503_b", "b", "(Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z", "(" + TransformerClass.BlockPos.getName() + TransformerClass.IBlockState.getName() + ")Z"),

    // ItemArmor
    onItemRightClick("onItemRightClick", "func_77659_a", "a", "(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;)Lnet/minecraft/item/ItemStack;", "(" + TransformerClass.ItemStack.getName() + TransformerClass.World.getName() + TransformerClass.EntityPlayer.getName() + ")" + TransformerClass.ItemStack.getName()),

    // BehaviorDefaultDispenseItem
    dispenseStack("dispenseStack", "func_82487_b", "b", "(Lnet/minecraft/dispenser/IBlockSource;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;", "(" + TransformerClass.IBlockSource.getName() + TransformerClass.ItemStack.getName() + ")" + TransformerClass.ItemStack.getName()),

    // Word
    playAuxSFX("playAuxSFX", "func_175718_b", "b", "(ILnet/minecraft/util/BlockPos;I)V", "(I" + TransformerClass.BlockPos.getName() + "I)V"),

    updateFramebufferSize("updateFramebufferSize", "func_147119_ah", "ay", "()V"),

    draw("draw", "func_181679_a", "a", "(Lnet/minecraft/client/renderer/WorldRenderer;)V", "(" + TransformerClass.WorldRenderer.getName() + ")V"),

    // EntityFX
    renderParticle("renderParticle", "func_180434_a", "a", "(Lnet/minecraft/client/renderer/WorldRenderer;Lnet/minecraft/entity/Entity;FFFFFF)V");

    private final String name;
    private final String description;
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

    public String getDescription() {
        return description;
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
