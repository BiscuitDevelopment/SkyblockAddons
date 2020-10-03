package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.utils.DevUtils;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import lombok.Getter;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.util.Constants;
import scala.actors.threadpool.Arrays;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/*
An aggregate entity that stores a single present from Jerry
 */
public class JerryPresent extends EntityAggregate {

    private static final Pattern STRIP_TO_FROM = Pattern.compile("(From:)?(To:)?( \\[.*\\])? ");

    // Publicly accessable map of tracked JerryPresents...
    // Could have put in PlayerListener, but then would have had to import PlayerListener into RenderManagerHook...
    // Made more sense not to do that and instead placed it here
    public static EntityAggregateMap<JerryPresent> jerryPresentMap = new EntityAggregateMap();

    // Is the present for you
    @Getter private final boolean isForYou;
    // Is the present from you
    @Getter private final boolean isFromYou;
    // Color of present
    private final PresentColor presentColor;

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
    These methods access information specific to JerryPresent and thus they are here
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
     This will only happen once if it's called on a LivingSpawnEvent.

     Returns null if no present found
     */
    public static JerryPresent checkAndReturnJerryPresent(EntityArmorStand targetStand) {
        // Only accept invisible armorstands
        if (!targetStand.isInvisible()) return null;
        // Check a small enough range that it will be hard for two presents to get confused with each other
        List<EntityArmorStand> stands = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABB(EntityArmorStand.class,
                new AxisAlignedBB(  targetStand.posX - .5, targetStand.posY - 3, targetStand.posZ - .5,
                        targetStand.posX + .5, targetStand.posY + 3, targetStand.posZ + .5));
        // Since the method is called before the entity is actually added to the chunk entity list, we add it here
        stands.add(targetStand);
        // Try to identify present skull (bottom), middle text line (middle), and top text line (top)
        EntityArmorStand bottom = null, middle = null, top = null;
        String presentID = "";
        for (EntityArmorStand stand : stands) {
            if (!stand.isInvisible()) continue;
            // To/From armorstands
            if (stand.hasCustomName()) {
                String name = TextUtils.stripColor(stand.getCustomNameTag());
                if (name.matches("From:.*")) {
                    //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Middle: " + name));
                    middle = stand;
                } else if (name.equals("CLICK TO OPEN") || name.matches("To:.*")) {
                    //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Top " + name));
                    top = stand;
                }
            }
            // Skull armorstand -- try to get Hypixel's skull id to determine if it's the present
            else {
                presentID = tryToGetSkullIdFromArmorstand(stand);

                if (presentID == null || !PRESENT_TYPE_IDS.containsKey(presentID)) continue;
                //Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Bottom " + presentID));
                bottom = stand;
            }
        }
        // Check that we've found a bottom, middle, top, as well as that the positions make sense (might not be perfect)
        if (bottom == null || middle == null || top == null || bottom.posY > middle.posY || middle.posY > top.posY) return null;

        // Get the important present information
        PresentColor presentColor = PRESENT_TYPE_IDS.get(presentID);
        boolean fromYou = STRIP_TO_FROM.matcher(TextUtils.stripColor(middle.getCustomNameTag())).replaceAll("").
                equals(Minecraft.getMinecraft().thePlayer.getName());
        boolean forYou = TextUtils.stripColor(top.getCustomNameTag()).equals("CLICK TO OPEN");

        return new JerryPresent(bottom, middle, top, presentColor, fromYou, forYou);
    }

    private static String tryToGetSkullIdFromArmorstand(EntityArmorStand e) {
        NBTBase nbt = new NBTTagCompound();
        String s = "";
        e.writeEntityToNBT((NBTTagCompound)nbt);
        if (((NBTTagCompound)nbt).hasKey("Equipment")) {
            nbt = ((NBTTagCompound)nbt).getTag("Equipment");
            if (nbt.getId() == Constants.NBT.TAG_LIST && ((NBTTagList)nbt).tagCount() == 5) {
                nbt = ((NBTTagList)nbt).get(4);
                if (nbt.getId() == Constants.NBT.TAG_COMPOUND && ((NBTTagCompound)nbt).hasKey("tag")) {
                    nbt = ((NBTTagCompound)nbt).getTag("tag");
                    if (nbt.getId() == Constants.NBT.TAG_COMPOUND && ((NBTTagCompound)nbt).hasKey("SkullOwner")) {
                        nbt = ((NBTTagCompound)nbt).getTag("SkullOwner");
                        if (nbt.getId() == Constants.NBT.TAG_COMPOUND && ((NBTTagCompound)nbt).hasKey("Id")) {
                            s = ((NBTTagCompound)nbt).getString("Id");
                        }
                    }
                }
            }
        }
        return s.length() == 0 ? null : s;
    }

}
