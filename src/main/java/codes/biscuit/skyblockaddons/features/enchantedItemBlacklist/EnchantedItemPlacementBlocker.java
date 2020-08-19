package codes.biscuit.skyblockaddons.features.enchantedItemBlacklist;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Location;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
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
    private static final SkyblockAddons MAIN = SkyblockAddons.getInstance();
    private static final ArrayList<Block> INTERACTIVE_BLOCKS = new ArrayList<>();
    private static final ArrayList<Class<? extends Item>> CLASSES_OF_ITEMS_THAT_CAN_BE_PLACED = new ArrayList<>();

    @Setter
    private static EnchantedItemBlacklist blacklist;

    static {
        CLASSES_OF_ITEMS_THAT_CAN_BE_PLACED.add(ItemBucket.class);
        CLASSES_OF_ITEMS_THAT_CAN_BE_PLACED.add(ItemRedstone.class);
        CLASSES_OF_ITEMS_THAT_CAN_BE_PLACED.add(ItemReed.class);
        CLASSES_OF_ITEMS_THAT_CAN_BE_PLACED.add(ItemSeedFood.class);
        CLASSES_OF_ITEMS_THAT_CAN_BE_PLACED.add(ItemSeeds.class);
        CLASSES_OF_ITEMS_THAT_CAN_BE_PLACED.add(ItemSkull.class);

        INTERACTIVE_BLOCKS.add(Blocks.acacia_door);
        INTERACTIVE_BLOCKS.add(Blocks.anvil);
        INTERACTIVE_BLOCKS.add(Blocks.birch_door);
        INTERACTIVE_BLOCKS.add(Blocks.brewing_stand);
        INTERACTIVE_BLOCKS.add(Blocks.chest);
        INTERACTIVE_BLOCKS.add(Blocks.crafting_table);
        INTERACTIVE_BLOCKS.add(Blocks.dark_oak_door);
        INTERACTIVE_BLOCKS.add(Blocks.enchanting_table);
        INTERACTIVE_BLOCKS.add(Blocks.furnace);
        INTERACTIVE_BLOCKS.add(Blocks.iron_door);
        INTERACTIVE_BLOCKS.add(Blocks.iron_trapdoor);
        INTERACTIVE_BLOCKS.add(Blocks.jungle_door);
        INTERACTIVE_BLOCKS.add(Blocks.lever);
        INTERACTIVE_BLOCKS.add(Blocks.lit_furnace);
        INTERACTIVE_BLOCKS.add(Blocks.oak_door);
        INTERACTIVE_BLOCKS.add(Blocks.stone_button);
        INTERACTIVE_BLOCKS.add(Blocks.trapdoor);
        INTERACTIVE_BLOCKS.add(Blocks.trapped_chest);
        INTERACTIVE_BLOCKS.add(Blocks.wooden_button);
    }

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
        if (MAIN.getUtils().getLocation() == Location.ISLAND && interactEvent.action != PlayerInteractEvent.Action.LEFT_CLICK_BLOCK
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
                        }
                        else {
                            return false;
                        }
                    }
                    else {
                        return false;
                    }
                }
            }

            /*
             If this item isn't found in the blacklist, check if it's an enchanted block with a rarity above the minimum.
             ItemReed is included because it's the class of some blocks like flowerpots and repeaters.
             */
            if (interactEvent.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && itemStack.isItemEnchanted() &&
                    blacklist.rarityLimit.compareTo(ItemUtils.getRarity(itemStack)) <= 0) {
                Block clickedBlock = Minecraft.getMinecraft().theWorld.getBlockState(interactEvent.pos).getBlock();

                /*
                 If the player right clicks on an interactive block like a chest, the item won't be used.
                 The player will activate the block instead.
                 */
                return willNotActivateBlock(interactEvent.action, interactEvent.entityPlayer, clickedBlock);
            }
            else {
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