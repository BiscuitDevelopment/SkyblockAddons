package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.core.EntityAggregate;
import codes.biscuit.skyblockaddons.core.EntityAggregateMap;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.AxisAlignedBB;
import lombok.Getter;
import net.minecraft.util.ChatComponentText;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/*
An aggregate entity that stores a single present from Jerry
 */
public class JerryPresent extends EntityAggregate {

    // Strips the "To:/From: [Rank] " and leaves just the name
    private static final Pattern STRIP_TO_FROM = Pattern.compile("(From:)?(To:)?( \\[.*])? ");

    // Publicly accessible map of tracked JerryPresents
    // If more of these entity-tracker maps are used in the future, it may make more sense to store in a separate file
    public static EntityAggregateMap<JerryPresent> jerryPresentMap = new EntityAggregateMap<>();

    // Is the present for you
    @Getter private final boolean isForYou;
    // Is the present from you
    @Getter private final boolean isFromYou;
    // Color of present
    @Getter private final PresentColor presentColor;

    // The different present colors
    private enum PresentColor {
        WHITE, GREEN, RED, UNKNOWN
    }

    // Map of NBT tag IDs to present colors
    private static final HashMap<String, PresentColor> PRESENT_TYPE_IDS;
    static {
        PRESENT_TYPE_IDS = new HashMap<>();
        PRESENT_TYPE_IDS.put("7732c5e4-1800-3b90-a70f-727d2969254b", PresentColor.WHITE); // White
        PRESENT_TYPE_IDS.put("d5eb6a2a-3f10-3d6b-ba6a-4d46bb58a5cb", PresentColor.GREEN); // Green
        PRESENT_TYPE_IDS.put("bc74cb05-2758-3395-93ec-70452a983604", PresentColor.RED); // Red
    }


    public JerryPresent(EntityArmorStand present, EntityArmorStand displayLower, EntityArmorStand displayUpper,
                        PresentColor color, boolean fromYou, boolean forYou) {
        // Create an EntityAggregate with 3 parts
        super(present, displayLower, displayUpper);
        this.presentColor = color;
        this.isFromYou = fromYou;
        this.isForYou = forYou;
    }

    /*
    These methods access information in EntityAggregate that is specific to JerryPresent
     */

    // This is the armorstand with the present-colored skull
    public EntityArmorStand getThePresent() {
        return (EntityArmorStand)(this.getEntityParts().get(0));
    }

    // This is the armorstand with "From: [RANK] Username"
    public EntityArmorStand getLowerDisplay() {
        return (EntityArmorStand)(this.getEntityParts().get(1));
    }

    // This is the armorstand with "CLICK TO OPEN" or "To: [RANK] Username"
    public EntityArmorStand getUpperDisplay() {
        return (EntityArmorStand)(this.getEntityParts().get(2));
    }

    // When the feature is turned on, we only render the presents of importance to the player
    public boolean shouldRender() {
        return isForYou || isFromYou;
    }

    public String toString() {
        return presentColor.name() + " from " + (isFromYou ? "you" : "other") + " to " + (isForYou ? "you" : "other:");
    }


    /*
     Returns a Jerry Present if the entity is the present and we see text above it
     The idea here is that we only return a present if all three armorstands are present
     The function should only succeed (create a present) once for a given set of armorstands
     But this relies on separately tracking which entities have already succeeded

     Returns null if no present found
     */
    public static JerryPresent checkAndReturnJerryPresent(Entity targetEntity) {
        // Only accept invisible armorstands
        if (!(targetEntity instanceof EntityArmorStand) || !targetEntity.isInvisible()) return null;
        // Check a small enough range that it will be hard for two presents to get confused with each other
        List<EntityArmorStand> stands = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(EntityArmorStand.class,
                new AxisAlignedBB(targetEntity.posX - .2, targetEntity.posY - 2, targetEntity.posZ - .2,
                        targetEntity.posX + .2, targetEntity.posY + 2, targetEntity.posZ + .2));
        // Try to identify present skull (bottom), middle text line (middle), and top text line (top)
        EntityArmorStand bottom = null, middle = null, top = null;
        String presentID = "";
        for (EntityArmorStand stand : stands) {
            if (!stand.isInvisible()) continue;
            // To/From armorstands
            if (stand.hasCustomName()) {
                String name = TextUtils.stripColor(stand.getCustomNameTag());
                if (name.matches("From:.*")) {
                    middle = stand;
                } else if (name.equals("CLICK TO OPEN") || name.matches("To:.*")) {
                    top = stand;
                }
            }

            else {
                // Skull armorstand -- try to get Hypixel's skull id to determine if it's a present
                presentID = ItemUtils.getSkullOwnerID(stand.getEquipmentInSlot(4));

                if (presentID == null || !PRESENT_TYPE_IDS.containsKey(presentID)) continue;
                bottom = stand;
            }
        }
        // Check that we've found a bottom, middle, top, as well as that the positions make sense
        if (bottom == null || middle == null || top == null || bottom.posY > middle.posY || middle.posY > top.posY) return null;

        // Get the important present information
        PresentColor presentColor = PRESENT_TYPE_IDS.get(presentID);
        boolean fromYou = STRIP_TO_FROM.matcher(TextUtils.stripColor(middle.getCustomNameTag())).replaceAll("").
                equals(Minecraft.getMinecraft().thePlayer.getName());
        boolean forYou = TextUtils.stripColor(top.getCustomNameTag()).equals("CLICK TO OPEN");

        return new JerryPresent(bottom, middle, top, presentColor, fromYou, forYou);
    }
}
