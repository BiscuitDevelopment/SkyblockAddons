package codes.biscuit.skyblockaddons.features.EntityOutlines;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.config.ConfigValues;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.ItemRarity;
import codes.biscuit.skyblockaddons.core.Location;
import codes.biscuit.skyblockaddons.events.RenderEntityOutlineEvent;
import codes.biscuit.skyblockaddons.events.RenderEntityOutlineEvent.Type;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Function;

/**
 * Controls the behavior of the {@link codes.biscuit.skyblockaddons.core.Feature#MAKE_DROPPED_ITEMS_GLOW} and {@link codes.biscuit.skyblockaddons.core.Feature#SHOW_GLOWING_ITEMS_ON_ISLAND} features
 */
public class FeatureItemOutlines {

    /**
     * List of skyblock locations where we might see items in showcases
     */
    private static final HashSet<Location> SHOWCASE_ITEM_LOCATIONS = new HashSet<>(Arrays.asList(
            Location.VILLAGE, Location.AUCTION_HOUSE, Location.BANK, Location.BAZAAR,
            Location.COAL_MINE, Location.LIBRARY, Location.JERRYS_WORKSHOP, Location.THE_END));



    /**
     * Cached value of the client's skyblock location
     */
    private static Location location;
    /**
     * Cached value of the client's config
     */
    private static ConfigValues config;


    /**
     * Entity-level predicate to determine whether a specific entity should be outlined, and if so, what color.
     * Should be used in conjunction with the global-level predicate, {@link #GLOBAL_TEST()}.
     * <p>
     * Return {@code null} if the entity should not be outlined, or the integer color of the entity to be outlined iff the entity should be outlined
     */
    private static final Function<Entity, Integer> OUTLINE_COLOR = e -> {
        // Only accept items that aren't showcase items
        if (e instanceof EntityItem && (!SHOWCASE_ITEM_LOCATIONS.contains(location) || !isShopShowcaseItem((EntityItem) e))) {
            ItemRarity itemRarity = ItemUtils.getRarity(((EntityItem) e).getEntityItem());
            if (itemRarity != null) {
                // Return the rarity color of the item
                return itemRarity.getColorCode().getColor();
            }
            // Return gray if the item doesn't have a rarity for some reason...
            return ColorCode.GRAY.getColor();
        }
        return null;
    };

    public FeatureItemOutlines() {
    }

    /**
     * Global-level predicate to determine whether any entities should outlined.
     * Should be used in conjunction with the entity-level predicate, {@link #OUTLINE_COLOR}.
     * <p>
     * Don't accept if the player is on a personal island and the
     *
     * @return {@code false} iff no entities should be outlined (i.e., accept if the player has item outlines enabled for the current skyblock location)
     */
    private static boolean GLOBAL_TEST() {
        return config.isEnabled(Feature.MAKE_DROPPED_ITEMS_GLOW) && (config.isEnabled(Feature.SHOW_GLOWING_ITEMS_ON_ISLAND) || location != Location.ISLAND);
    }

    /**
     * This method checks if the given EntityItem is an item being showcased in a shop.
     * It works by detecting glass case the item is in.
     *
     * @param entityItem the potential shop showcase item.
     * @return true iff the entity is a shop showcase item.
     */
    private static boolean isShopShowcaseItem(EntityItem entityItem) {
        for (EntityArmorStand entityArmorStand : entityItem.worldObj.getEntitiesWithinAABB(EntityArmorStand.class, entityItem.getEntityBoundingBox())) {
            if (entityArmorStand.isInvisible() && entityArmorStand.getEquipmentInSlot(4) != null &&
                    entityArmorStand.getEquipmentInSlot(4).getItem() == Item.getItemFromBlock(Blocks.glass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Queues items to be outlined that satisfy global- and entity-level predicates.
     *
     * @param e the outline event
     */
    @SubscribeEvent
    public void onRenderEntityOutlines(RenderEntityOutlineEvent e) {
        // Cache constants
        location = SkyblockAddons.getInstance().getUtils().getLocation();
        config = SkyblockAddons.getInstance().getConfigValues();

        if (e.getType() == Type.XRAY) {
            // Test whether we should add any entities at all
            if (GLOBAL_TEST()) {
                // Queue specific items for outlining
                e.queueEntitiesToOutline(OUTLINE_COLOR);
            }
        }
    }

}
