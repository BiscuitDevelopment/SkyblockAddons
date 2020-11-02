package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.core.EntityAggregate;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.AxisAlignedBB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JerryPresent extends EntityAggregate {

    private static final Pattern FROM_TO_PATTERN = Pattern.compile("(?:From:|To:) (?:\\[.*?] )?(?<name>\\w{1,16})");

    @Getter
    private static Map<UUID, JerryPresent> jerryPresents = new HashMap<>();

    @Getter
    private final boolean isForPlayer;
    @Getter
    private final boolean isFromPlayer;
    @Getter
    private final PresentColor presentColor;

    public JerryPresent(UUID present, UUID fromLine, UUID toLine, PresentColor color, boolean isFromPlayer, boolean isForPlayer) {
        super(present, fromLine, toLine);

        this.presentColor = color;
        this.isFromPlayer = isFromPlayer;
        this.isForPlayer = isForPlayer;
    }

    /**
     * Armor stand with the present-colored skull
     */
    public UUID getThePresent() {
        return this.getEntities().get(0);
    }

    /**
     * Armor stand with "From: [RANK] Username"
     */
    public UUID getLowerDisplay() {
        return this.getEntities().get(1);
    }

    /**
     * Armor stand with "CLICK TO OPEN" or "To: [RANK] Username"
     */
    public UUID getUpperDisplay() {
        return this.getEntities().get(2);
    }

    public boolean shouldHide() {
        return !isForPlayer && !isFromPlayer;
    }

    /**
     * Returns an instance of JerryPresent if this entity is in fact part of a jerry
     * present, or null if not.
     */
    public static JerryPresent getJerryPresent(Entity targetEntity) {
        if (!(targetEntity instanceof EntityArmorStand) || !targetEntity.isInvisible()) {
            return null;
        }

        // Check if this present already exists...
        for (JerryPresent present : jerryPresents.values()) {
            if (present.getEntities().contains(targetEntity.getUniqueID())) {
                return present;
            }
        }

        // Check a small range around...
        List<EntityArmorStand> stands = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(EntityArmorStand.class,
                new AxisAlignedBB(targetEntity.posX - 0.1, targetEntity.posY - 2, targetEntity.posZ - 0.1,
                        targetEntity.posX + 0.1, targetEntity.posY + 2, targetEntity.posZ + 0.1));

        EntityArmorStand present = null, fromLine = null, toLine = null;
        PresentColor presentColor = null;
        for (EntityArmorStand stand : stands) {
            if (!stand.isInvisible()) {
                continue;
            }

            if (stand.hasCustomName()) {
                String name = TextUtils.stripColor(stand.getCustomNameTag());

                // From line (middle)
                if (name.startsWith("From: ")) {
                    fromLine = stand;

                    // To line (top)
                } else if (name.equals("CLICK TO OPEN") || name.startsWith("To: ")) {
                    toLine = stand;
                }

            } else {
                String skullID = ItemUtils.getSkullOwnerID(stand.getEquipmentInSlot(4));
                if (skullID == null) {
                    continue;
                }

                PresentColor standColor = PresentColor.fromSkullID(skullID);
                if (standColor == null) {
                    continue;
                }

                // Present stand (bottom)
                present = stand;
                presentColor = standColor;
            }
        }
        // Verify that we've found all parts, and that the positions make sense
        if (present == null || fromLine == null || toLine == null || present.posY > fromLine.posY || fromLine.posY > toLine.posY) {
            return null;
        }

        Matcher matcher = FROM_TO_PATTERN.matcher(TextUtils.stripColor(fromLine.getCustomNameTag()));
        if (!matcher.matches()) {
            return null;
        }
        String name = matcher.group("name");

        boolean fromYou = name.equals(Minecraft.getMinecraft().thePlayer.getName());
        boolean forYou = TextUtils.stripColor(toLine.getCustomNameTag()).equals("CLICK TO OPEN");

        return new JerryPresent(present.getUniqueID(), fromLine.getUniqueID(), toLine.getUniqueID(), presentColor, fromYou, forYou);
    }

    private enum PresentColor {
        WHITE("7732c5e4-1800-3b90-a70f-727d2969254b"),
        GREEN("d5eb6a2a-3f10-3d6b-ba6a-4d46bb58a5cb"),
        RED("bc74cb05-2758-3395-93ec-70452a983604");

        private String skullID;

        PresentColor(String skullID) {
            this.skullID = skullID;
        }

        public static PresentColor fromSkullID(String skullID) {
            for (PresentColor presentColor : PresentColor.values()) {
                if (presentColor.skullID.equals(skullID)) {
                    return presentColor;
                }
            }

            return null;
        }
    }
}
