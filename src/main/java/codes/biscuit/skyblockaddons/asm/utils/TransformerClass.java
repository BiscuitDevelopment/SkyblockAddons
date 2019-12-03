package codes.biscuit.skyblockaddons.asm.utils;

import codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsTransformer;

public enum TransformerClass {

    Minecraft("net/minecraft/client/Minecraft", "ave"),
    EntityItem("net/minecraft/entity/item/EntityItem", "uz"),
    GuiChest("net/minecraft/client/gui/inventory/GuiChest", "ayr"),
    IInventory("net/minecraft/inventory/IInventory", "og"),
    FontRenderer("Lnet/minecraft/client/gui/FontRenderer", "avn"),
    GuiScreen("net/minecraft/client/GuiScreen", "axu"),
    GuiContainer("net/minecraft/client/gui/inventory/GuiContainer", "ayl"),
    ItemStack("net/minecraft/item/ItemStack", "zx"),
    GlStateManager("net/minecraft/client/renderer/GlStateManager", "bfl"),
    Container("net/minecraft/inventory/Container", "xi"),
    Slot("net/minecraft/inventory/Slot", "yg"),
    RenderItem("net/minecraft/client/renderer/entity/RenderItem", "bjh"),
    GuiInventory("net/minecraft/client/gui/inventory/GuiInventory", "azc"),
    IChatComponent("net/minecraft/util/IChatComponent", "eu"),
    IReloadableResourceManager("net/minecraft/client/resources/IReloadableResourceManager", "bng"),
    S2FPacketSetSlot("net/minecraft/network/play/server/S2FPacketSetSlot", "gf"),
    S30PacketWindowItems("net/minecraft/network/play/server/S30PacketWindowItems", "gd"),
    BlockPos("net/minecraft/util/BlockPos", "cj"),
    EntityPlayer("net/minecraft/entity/player/EntityPlayer", "wn"),
    EnumPlayerModelParts("net/minecraft/entity/player/EnumPlayerModelParts", "wo"),
    Entity("net/minecraft/entity/Entity", "pk"),
    SoundManager("net/minecraft/client/audio/SoundManager", "bpx"),
    ISound("net/minecraft/client/audio/ISound", "bpj"),
    SoundPoolEntry("net/minecraft/client/audio/SoundPoolEntry", "bpw"),
    SoundCategory("net/minecraft/client/audio/SoundCategory", "bpg"),
    TileEntityEnderChestRenderer("net/minecraft/client/renderer/tileentity/TileEntityEnderChestRenderer", "bhg"),
    ResourceLocation("net/minecraft/util/ResourceLocation", "jy"),
    ModelChest("net/minecraft/client/model/ModelChest", "baz"),

    NULL(null,null);

    private String name;

    TransformerClass(String seargeClass, String notchClass18) {
        if (SkyblockAddonsTransformer.DEOBFUSCATED || !SkyblockAddonsTransformer.NOTCH_MAPPINGS) {
            name = seargeClass;
        } else {
            name = notchClass18;
        }
    }

    public String getNameRaw() {
        return name;
    }

    public String getName() {
        return "L"+name+";";
    }
}
