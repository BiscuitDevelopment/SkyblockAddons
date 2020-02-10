package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.utils.ReturnValue;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;

public class EntityPlayerSPHook {

    private static String lastItemName = null;
    private static long lastDrop = Minecraft.getSystemTime();

    public static EntityItem dropOneItemConfirmation(ReturnValue returnValue) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Minecraft mc = Minecraft.getMinecraft();
        ItemStack heldItemStack = mc.thePlayer.getHeldItem();

        if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && (main.getUtils().isOnSkyblock() || main.getPlayerListener().aboutToJoinSkyblockServer() || !main.getPlayerListener().didntRecentlyJoinWorld())) {
            int slot = mc.thePlayer.inventory.currentItem + 36;
            if (main.getConfigValues().getLockedSlots().contains(slot)
                    && (slot >= 9 || mc.thePlayer.openContainer instanceof ContainerPlayer && slot >= 5)) {
                main.getUtils().playLoudSound("note.bass", 0.5);
                SkyblockAddons.getInstance().getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.DROP_CONFIRMATION) +
                        Message.MESSAGE_SLOT_LOCKED.getMessage());
                returnValue.cancel();
                return null;
            }
        }

        if (heldItemStack != null) {
            if(main.getUtils().isOnSkyblock()
                    || main.getPlayerListener().aboutToJoinSkyblockServer()
                    || !main.getPlayerListener().didntRecentlyJoinWorld()) {
                EnumUtils.Rarity rarity = EnumUtils.Rarity.getRarity(heldItemStack);

                if (rarity != EnumUtils.Rarity.INVALID  && main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) &&
                        main.getUtils().cantDropItem(heldItemStack, rarity, true)) {
                    main.getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.STOP_DROPPING_SELLING_RARE_ITEMS)
                            + Message.MESSAGE_CANCELLED_DROPPING.getMessage());
                    returnValue.cancel();
                    return null;
                }
            }

            if (main.getConfigValues().isEnabled(Feature.DROP_CONFIRMATION) && (main.getUtils().isOnSkyblock() ||
                    main.getConfigValues().isEnabled(Feature.DOUBLE_DROP_IN_OTHER_GAMES))) {

                lastDrop = Minecraft.getSystemTime();

                String heldItemName = heldItemStack.hasDisplayName() ? heldItemStack.getDisplayName() : heldItemStack.getUnlocalizedName();

                if (lastItemName == null || !lastItemName.equals(heldItemName) || Minecraft.getSystemTime() - lastDrop >= 3000L) {
                    SkyblockAddons.getInstance().getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.DROP_CONFIRMATION) +
                            Message.MESSAGE_DROP_CONFIRMATION.getMessage());
                    lastItemName = heldItemName;
                    returnValue.cancel();
                }
            }
        }

        return null;
    }
}
