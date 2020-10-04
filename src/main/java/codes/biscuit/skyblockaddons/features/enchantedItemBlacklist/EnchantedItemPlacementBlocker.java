package codes.biscuit.skyblockaddons.features.enchantedItemBlacklist;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.ItemRarity;
import codes.biscuit.skyblockaddons.core.Location;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import com.google.common.collect.Lists;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;

/**
 * This class is the main class of the "Avoid Placing Enchanted Items" feature. Whenever a player tries to place an item
 * on their private island, this class is used to check if the action should be blocked or allowed.
 */
public class EnchantedItemPlacementBlocker {

    private static final ArrayList<Block> INTERACTIVE_BLOCKS = Lists.newArrayList(Blocks.acacia_door, Blocks.anvil, Blocks.birch_door,
            Blocks.brewing_stand, Blocks.chest, Blocks.crafting_table, Blocks.dark_oak_door, Blocks.enchanting_table, Blocks.furnace,
            Blocks.iron_door, Blocks.iron_trapdoor, Blocks.jungle_door, Blocks.lever, Blocks.lit_furnace, Blocks.oak_door, Blocks.stone_button,
            Blocks.trapdoor, Blocks.trapped_chest, Blocks.wooden_button);

    private static final ArrayList<Class<?>> CLASSES_OF_ITEMS_THAT_CAN_BE_PLACED = Lists.newArrayList(ItemBucket.class, ItemRedstone.class,
            ItemReed.class, ItemSeedFood.class, ItemSeeds.class, ItemSkull.class);

    @Setter private static EnchantedItemBlacklist blacklist;

    /**
     * Determine if the placement of this item should be blocked.
     *
     * @param itemStack the item being placed
     * @param interactEvent the {@code PlayerInteractEvent} that was triggered when the player used the item
     * @return {@code true} if the usage should be blocked, {@code false} otherwise.
     */
    public static boolean shouldBlockPlacement(ItemStack itemStack, PlayerInteractEvent interactEvent) {
        if (itemStack == null) {
            throw new NullPointerException();
        }

        String heldItemId = ItemUtils.getSkyBlockItemID(itemStack);

        // Don't block non-Skyblock items.
        if (heldItemId == null) {
            return false;
        }

        /*
        Block placing blocks only on the private island since that's the only place players can place blocks at.
        Also block both actions RIGHT_CLICK_BLOCK and RIGHT_CLICK_AIR because 2 events are sent,
        one with the first action and one with the second.
         */
        if (SkyblockAddons.getInstance().getUtils().getLocation() == Location.ISLAND && interactEvent.action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK
                && canBePlaced(itemStack.getItem())) {
            for (String blacklistItemId : blacklist.enchantedItemIds) {
                if (heldItemId.equals(blacklistItemId)) {
                    // If the item is a material for a recipe, placement will be blocked server-side.
                    if (!ItemUtils.isMaterialForRecipe(itemStack)) {
                        /*
                        Check if the player will use the held item or activate the clicked block if the player is
                        right clicking on a block.
                         */
                        if (interactEvent.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
                            Block clickedBlock = Minecraft.getMinecraft().theWorld.getBlockState(interactEvent.pos).getBlock();

                            /*
                             If the player right clicks on an interactive block like a chest, the item won't be used.
                             The player will activate the block instead.
                             */
                            return willNotActivateBlock(interactEvent.action, interactEvent.entityPlayer, clickedBlock);

                        } else {
                            return false;
                        }
                    }
                    else {
                        return false;
                    }
                }
            }

            ItemRarity rarity = ItemUtils.getRarity(itemStack);

            /*
             If this item isn't found in the blacklist, check if it's an enchanted block with a rarity above the minimum.
             ItemReed is included because it's the class of some blocks like flowerpots and repeaters.
             */
            if (rarity != null && interactEvent.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && itemStack.isItemEnchanted() &&
                    blacklist.rarityLimit.compareTo(rarity) <= 0) {
                /*
                 If the player right clicks on an interactive block like a chest, the item won't be used.
                 The player will activate the block instead.
                 */
                Block clickedBlock = Minecraft.getMinecraft().theWorld.getBlockState(interactEvent.pos).getBlock();
                return willNotActivateBlock(interactEvent.action, interactEvent.entityPlayer, clickedBlock);

            } else {
                return false;
            }
        }

        return false;
    }

    /*
    Checks if the given item can be placed down like a block.
     */
    private static boolean canBePlaced(Item item) {
        return Block.getBlockFromItem(item) != null || CLASSES_OF_ITEMS_THAT_CAN_BE_PLACED.contains(item.getClass());
    }

    /*
    Checks if the given action the given player is doing will not activate the given block.
     */
    private static boolean willNotActivateBlock(PlayerInteractEvent.Action action, EntityPlayer player, Block block) {
        if (block == null) {
            throw new IllegalArgumentException("Block cannot be null!");
        }

        return action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || player.isSneaking() || !INTERACTIVE_BLOCKS.contains(block);
    }
}