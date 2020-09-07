package codes.biscuit.skyblockaddons.features.powerorbs;

import codes.biscuit.skyblockaddons.utils.TextUtils;
import codes.biscuit.skyblockaddons.utils.Utils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for managing active PowerOrbs around the player.
 * {@link #put(PowerOrb, int) Insert} orbs that get detected and {@link #getActivePowerOrb() get} the
 * active orb with the highest priority (enum ordinal).
 *
 * @author DidiSkywalker
 */
public class PowerOrbManager {

    private static final Pattern POWER_ORB_PATTERN = Pattern.compile("[A-Za-z ]* (?<seconds>[0-9]*)s");

    /** The PowerOrbManager instance. */
    @Getter private static final PowerOrbManager instance = new PowerOrbManager();

    /**
     * Entry displaying {@link PowerOrb#RADIANT} at 20 seconds for the edit screen
     */
    public static final PowerOrbEntry DUMMY_POWER_ORB_ENTRY = new PowerOrbEntry(PowerOrb.RADIANT, 20);

    private Map<PowerOrb, PowerOrbEntry> powerOrbEntryMap = new HashMap<>();

    @Getter private EntityArmorStand powerOrbArmorStand = null;

    /**
     * Put any detected orb into the list of active orbs.
     *
     * @param powerOrb Detected PowerOrb type
     * @param seconds Seconds the orb has left before running out
     */
    private void put(PowerOrb powerOrb, int seconds) {
        powerOrbEntryMap.put(powerOrb, new PowerOrbEntry(powerOrb, seconds));
    }

    /**
     * Get the active orb with the highest priority. Priority is based on enum value's ordinal
     * and the returned orb is guaranteed to have been active at least 100ms ago.
     *
     * @return Highest priority orb or null if none is around
     */
    public PowerOrbEntry getActivePowerOrb() {
        Optional<Map.Entry<PowerOrb, PowerOrbEntry>> max = powerOrbEntryMap.entrySet().stream()
                .filter(powerOrbEntryEntry -> powerOrbEntryEntry.getValue().timestamp + 100 > System.currentTimeMillis())
                .max(Map.Entry.comparingByKey());

        return max.map(Map.Entry::getValue).orElse(null);
    }

    /**
     * Detects a power orb from an entity, and puts it in this manager.
     *
     * @param entity The entity to detect whether it is a power orb or not.
     */
    public void detectPowerOrb(Entity entity) {
        String customNameTag = entity.getCustomNameTag();
        PowerOrb powerOrb = PowerOrb.getByDisplayname(customNameTag);

        if (powerOrb != null && powerOrb.isInRadius(entity.getDistanceSqToEntity(Minecraft.getMinecraft().thePlayer))) { // TODO Make sure this works...
            Matcher matcher = POWER_ORB_PATTERN.matcher(TextUtils.stripColor(customNameTag));

            if (matcher.matches()) {
                String secondsString = matcher.group("seconds");
                try {
                    // Apparently they don't have a second count for moment after spawning, that's what this try-catch is for
                    put(powerOrb, Integer.parseInt(secondsString));
                } catch (NumberFormatException ex) {
                    // It's okay, just don't add the power orb I guess...
                    return;
                }

                List<EntityArmorStand> surroundingArmorStands = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(EntityArmorStand.class,
                        new AxisAlignedBB(entity.posX - 0.1, entity.posY - 3, entity.posZ - 0.1, entity.posX + 0.1, entity.posY, entity.posZ + 0.1));
                if (!surroundingArmorStands.isEmpty()) {

                    EntityArmorStand orbArmorStand = null;

                    for (EntityArmorStand surroundingArmorStand : surroundingArmorStands) {
                        ItemStack helmet = surroundingArmorStand.getCurrentArmor(3);
                        if (helmet != null) {
                            orbArmorStand = surroundingArmorStand;
                        }
                    }

                    if (orbArmorStand != null && hasPowerOrbEntityChanged(orbArmorStand)) {
                        powerOrbArmorStand = createVirtualArmorStand(orbArmorStand);
                    }
                }
            }
        }
    }

    public boolean hasPowerOrbEntityChanged(EntityArmorStand newPowerOrbArmorStand) {
        if (powerOrbArmorStand == null) {
            return true;
        }

        return powerOrbArmorStand.getEquipmentInSlot(4) != newPowerOrbArmorStand.getEquipmentInSlot(4);
    }

    public EntityArmorStand createVirtualArmorStand(EntityArmorStand armorStandToClone) {
        EntityArmorStand virtualArmorStand = new EntityArmorStand(Utils.getDummyWorld());

        virtualArmorStand.setCurrentItemOrArmor(4, armorStandToClone.getEquipmentInSlot(4));

        return virtualArmorStand;
    }

    @Getter
    public static class PowerOrbEntry {
        /** The PowerOrb type. */
        private final PowerOrb powerOrb;

        /** Seconds the orb has left before running out */
        private final int seconds;

        private final long timestamp;

        private PowerOrbEntry(PowerOrb powerOrb, int seconds) {
            this.powerOrb = powerOrb;
            this.seconds = seconds;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
