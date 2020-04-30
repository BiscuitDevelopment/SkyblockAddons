package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.utils.ReturnValue;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;

import javax.lang.model.type.NullType;
import java.util.List;

public class EntityPlayerSPHook {

    private static String lastItemName = null;
    private static long lastDrop = Minecraft.getSystemTime();

    public static EntityItem dropOneItemConfirmation(ReturnValue<NullType> returnValue) {
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

                if (main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) &&
                        !main.getUtils().getItemDropChecker().canDropItem(heldItemStack, true)) {
                    main.getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.STOP_DROPPING_SELLING_RARE_ITEMS)
                            + Message.MESSAGE_CANCELLED_DROPPING.getMessage());
                    returnValue.cancel();
                    return null;
                }
            }
            else {
                if (!main.getUtils().getItemDropChecker().canDropItem(heldItemStack)) {
                    returnValue.cancel();
                    return null;
                }
            }
        }

        return null;
    }

    private static float lastUpdate = -1;

    public static void healthPlayerUpdate(float health) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (!main.getUtils().isOnSkyblock() || !main.getConfigValues().isEnabled(Feature.COMBAT_TIMER_DISPLAY)) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (health < lastUpdate || health == lastUpdate && mc.thePlayer.isPotionActive(Potion.absorption)) {

            List<Entity> nearEntities = mc.theWorld.getEntitiesWithinAABB(Entity.class,
                    new AxisAlignedBB(mc.thePlayer.posX - 2, mc.thePlayer.posY-2, mc.thePlayer.posZ - 2, mc.thePlayer.posX + 2, mc.thePlayer.posY + 2, mc.thePlayer.posZ + 2));
            boolean foundPossibleAttacker = false;

            for (Entity entity : nearEntities) {
                if (entity instanceof EntityMob || entity instanceof EntityWolf || entity instanceof IProjectile) {
                    foundPossibleAttacker = true;
                    break;
                }
            }

            if (foundPossibleAttacker) {
                SkyblockAddons.getInstance().getUtils().setLastDamaged(System.currentTimeMillis());
            }
        }
        lastUpdate = health;
    }
}
