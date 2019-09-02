package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.BackpackInfo;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Mixin(GuiScreen.class)
public abstract class MixinGuiScreen {

    @Inject(method = "renderToolTip", at = @At(value = "HEAD"), cancellable = true)
    private void shouldRenderRedirect(ItemStack stack, int x, int y, CallbackInfo ci) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (stack.getItem().equals(Items.skull) && main.getConfigValues().isEnabled(Feature.SHOW_BACKPACK_PREVIEW)) {
            if (main.getConfigValues().isEnabled(Feature.SHOW_BACKPACK_HOLDING_SHIFT) && !GuiScreen.isShiftKeyDown()) {
                return;
            }
            Container playerContainer = Minecraft.getMinecraft().thePlayer.openContainer;
            if (playerContainer instanceof ContainerChest) { // Avoid showing backpack preview in auction stuff.
                IInventory chest = ((ContainerChest)playerContainer).getLowerChestInventory();
                if (chest.hasCustomName() && chest.getDisplayName().getUnformattedText().contains("Auction")) {
                    return;
                }
            }
            if (stack.hasTagCompound()) {
                NBTTagCompound extraAttributes = stack.getTagCompound();
                if (extraAttributes.hasKey("ExtraAttributes")) {
                    extraAttributes = extraAttributes.getCompoundTag("ExtraAttributes");
                    String id = extraAttributes.getString("id");
                    if (!id.equals("")) {
                        byte[] bytes = null;
                        EnumUtils.Backpack backpack = null;
                        switch (id) {
                            case "SMALL_BACKPACK":
                                bytes = extraAttributes.getByteArray("small_backpack_data");
                                backpack = EnumUtils.Backpack.SMALL;
                                break;
                            case "MEDIUM_BACKPACK":
                                bytes = extraAttributes.getByteArray("medium_backpack_data");
                                backpack = EnumUtils.Backpack.MEDIUM;
                                break;
                            case "LARGE_BACKPACK":
                                bytes = extraAttributes.getByteArray("large_backpack_data");
                                backpack = EnumUtils.Backpack.LARGE;
                                break;
                        }
                        if (bytes == null) return;
                        NBTTagCompound nbtTagCompound;
                        try {
                            nbtTagCompound = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
                            NBTTagList list = nbtTagCompound.getTagList("i", Constants.NBT.TAG_COMPOUND);
                            int length = list.tagCount();
                            ItemStack[] items = new ItemStack[length];
                            for (int i = 0; i < length; i++) {
                                NBTTagCompound item = list.getCompoundTagAt(i);
                                // This fixes an issue in Hypixel where enchanted potatoes have the wrong id (potato block instead of item).
                                short itemID = item.getShort("id");
                                if (itemID == 142 && item.hasKey("tag")) {
                                    extraAttributes = item.getCompoundTag("tag");
                                    if (extraAttributes.hasKey("ExtraAttributes")) {
                                        extraAttributes = extraAttributes.getCompoundTag("ExtraAttributes");
                                        id = extraAttributes.getString("id");
                                        if (id.equals("ENCHANTED_POTATO")) {
                                            item.setShort("id", (short)392);
                                        }
                                    }
                                }
                                ItemStack itemStack = ItemStack.loadItemStackFromNBT(item);
                                items[i] = itemStack;
                            }
                            main.getUtils().setBackpackToRender(new BackpackInfo(x, y, items, backpack));
                            main.getPlayerListener().onItemTooltip(new ItemTooltipEvent(stack,
                                    null, null, false));
                            ci.cancel();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
