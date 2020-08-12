package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.utils.ReturnValue;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Location;
import codes.biscuit.skyblockaddons.core.Message;
import codes.biscuit.skyblockaddons.features.cooldowns.CooldownManager;
import codes.biscuit.skyblockaddons.utils.InventoryUtils;
import codes.biscuit.skyblockaddons.core.npc.NPCUtils;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class MinecraftHook {

    private static final ResourceLocation currentLocation = new ResourceLocation("skyblockaddons", "bars.png");

    private static long lastProfileMessage = -1;

    @Getter private static long lastLockedSlotItemChange = -1;

    private static final Set<Location> DEEP_CAVERNS_LOCATIONS = EnumSet.of(Location.DEEP_CAVERNS, Location.GUNPOWDER_MINES,
            Location.LAPIS_QUARRY, Location.PIGMAN_DEN, Location.SLIMEHILL, Location.DIAMOND_RESERVE, Location.OBSIDIAN_SANCTUARY);

    private static final Set<Block> DEEP_CAVERNS_MINEABLE_BLOCKS = new HashSet<>(Arrays.asList(Blocks.coal_ore, Blocks.iron_ore, Blocks.gold_ore, Blocks.redstone_ore, Blocks.emerald_ore,
            Blocks.diamond_ore, Blocks.diamond_block, Blocks.obsidian, Blocks.lapis_ore, Blocks.lit_redstone_ore));

    private static final Set<Block> NETHER_MINEABLE_BLOCKS = new HashSet<>(Arrays.asList(Blocks.glowstone, Blocks.quartz_ore, Blocks.nether_wart));

    private static final Set<Location> PARK_LOCATIONS = EnumSet.of(Location.BIRCH_PARK, Location.SPRUCE_WOODS, Location.SAVANNA_WOODLAND, Location.DARK_THICKET, Location.JUNGLE_ISLAND);

    private static final Set<Block> LOGS = new HashSet<>(Arrays.asList(Blocks.log, Blocks.log2));

    private static long lastStemMessage = -1;
    private static long lastUnmineableMessage = -1;

    public static void onRefreshResources(IReloadableResourceManager resourceManager) {
        boolean usingOldPackTexture = false;
        boolean usingDefaultTexture = true;
        try {
            IResource currentResource = resourceManager.getResource(currentLocation);
            String currentHash = DigestUtils.md5Hex(currentResource.getInputStream());

            InputStream oldStream = SkyblockAddons.class.getClassLoader().getResourceAsStream("assets/skyblockaddons/imperialoldbars.png");
            if (oldStream != null) {
                String oldHash = DigestUtils.md5Hex(oldStream);
                usingOldPackTexture = currentHash.equals(oldHash);
            }

            InputStream barsStream = SkyblockAddons.class.getClassLoader().getResourceAsStream("assets/skyblockaddons/bars.png");
            if (barsStream != null) {
                String barsHash = DigestUtils.md5Hex(barsStream);
                usingDefaultTexture = currentHash.equals(barsHash);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main != null) { // Minecraft reloads textures before and after mods are loaded. So only set the variable if sba was initialized.
            main.getUtils().setUsingOldSkyBlockTexture(usingOldPackTexture);
            main.getUtils().setUsingDefaultBarTextures(usingDefaultTexture);
        }
    }

    public static void rightClickMouse(ReturnValue<?> returnValue) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                Entity entityIn = mc.objectMouseOver.entityHit;

                if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && entityIn instanceof EntityItemFrame && ((EntityItemFrame)entityIn).getDisplayedItem() == null) {
                    int slot = mc.thePlayer.inventory.currentItem + 36;
                    if (main.getConfigValues().getLockedSlots().contains(slot) && slot >= 9) {
                        main.getUtils().playLoudSound("note.bass", 0.5);
                        main.getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.DROP_CONFIRMATION) + Message.MESSAGE_SLOT_LOCKED.getMessage());
                        returnValue.cancel();
                    }
                }
            }
        }
    }

    private static boolean isItemBow(ItemStack item) {
        return item != null && item.getItem() != null && item.getItem().equals(Items.bow);
    }

    public static void updatedCurrentItem() {
        Minecraft mc = Minecraft.getMinecraft();
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && (main.getUtils().isOnSkyblock() || main.getPlayerListener().aboutToJoinSkyblockServer())) {
            int slot = mc.thePlayer.inventory.currentItem + 36;
            if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && main.getConfigValues().getLockedSlots().contains(slot)
                    && (slot >= 9 || mc.thePlayer.openContainer instanceof ContainerPlayer && slot >= 5)) {

                MinecraftHook.lastLockedSlotItemChange = System.currentTimeMillis();
            }

            ItemStack heldItemStack = mc.thePlayer.getHeldItem();
            if (heldItemStack != null && main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) && !main.getUtils().isInDungeon()
                    && !main.getUtils().getItemDropChecker().canDropItem(heldItemStack, true, false)) {

                MinecraftHook.lastLockedSlotItemChange = System.currentTimeMillis();
            }
        }
    }

    public static void onClickMouse(ReturnValue<?> returnValue) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            return;
        }
        BlockPos blockPos = mc.objectMouseOver.getBlockPos();
        if (mc.theWorld.getBlockState(blockPos).getBlock().getMaterial() == Material.air) {
            return;
        }

        SkyblockAddons main = SkyblockAddons.getInstance();
        EntityPlayerSP p = mc.thePlayer;
        ItemStack heldItem = p.getHeldItem();
        if (heldItem != null) {
            Block block = mc.theWorld.getBlockState(blockPos).getBlock();
            long now = System.currentTimeMillis();

            if (main.getConfigValues().isEnabled(Feature.AVOID_BREAKING_STEMS) && (block.equals(Blocks.melon_stem) || block.equals(Blocks.pumpkin_stem))) {
                if (main.getConfigValues().isEnabled(Feature.ENABLE_MESSAGE_WHEN_BREAKING_STEMS) && now - lastStemMessage > 20000) {
                    lastStemMessage = now;
                    main.getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.AVOID_BREAKING_STEMS) + Message.MESSAGE_CANCELLED_STEM_BREAK.getMessage());
                }
                returnValue.cancel();
            } else if (main.getConfigValues().isEnabled(Feature.ONLY_MINE_ORES_DEEP_CAVERNS) && DEEP_CAVERNS_LOCATIONS.contains(main.getUtils().getLocation())
                    && ItemUtils.isPickaxe(heldItem.getItem()) && !DEEP_CAVERNS_MINEABLE_BLOCKS.contains(block)) {
                if (main.getConfigValues().isEnabled(Feature.ENABLE_MESSAGE_WHEN_MINING_DEEP_CAVERNS) && now - lastUnmineableMessage > 60000) {
                    lastUnmineableMessage = now;
                    main.getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.ONLY_MINE_ORES_DEEP_CAVERNS) + Message.MESSAGE_CANCELLED_NON_ORES_BREAK.getMessage());
                }
                returnValue.cancel();
            } else if (main.getConfigValues().isEnabled(Feature.ONLY_MINE_VALUABLES_NETHER) && Location.BLAZING_FORTRESS.equals(main.getUtils().getLocation()) &&
                    ItemUtils.isPickaxe(heldItem.getItem()) && !NETHER_MINEABLE_BLOCKS.contains(block)) {
                if (main.getConfigValues().isEnabled(Feature.ENABLE_MESSAGE_WHEN_MINING_NETHER) && now - lastUnmineableMessage > 60000) {
                    lastUnmineableMessage = now;
                    main.getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.ONLY_MINE_VALUABLES_NETHER) + Message.MESSAGE_CANCELLED_NON_ORES_BREAK.getMessage());
                }
                returnValue.cancel();
            } else if (main.getConfigValues().isEnabled(Feature.ONLY_BREAK_LOGS_PARK) && PARK_LOCATIONS.contains(main.getUtils().getLocation())
                    && main.getUtils().isAxe(heldItem.getItem()) && !LOGS.contains(block)) {
                if (main.getConfigValues().isEnabled(Feature.ENABLE_MESSAGE_WHEN_BREAKING_PARK) && now - lastUnmineableMessage > 60000) {
                    lastUnmineableMessage = now;
                    main.getUtils().sendMessage(main.getConfigValues().getRestrictedColor(Feature.ONLY_BREAK_LOGS_PARK) + Message.MESSAGE_CANCELLED_NON_LOGS_BREAK.getMessage());
                }
                returnValue.cancel();
            } else if (main.getConfigValues().isEnabled(Feature.JUNGLE_AXE_COOLDOWN)) {
                if ((block.equals(Blocks.log) || block.equals(Blocks.log2))
                        && p.getHeldItem() != null) {

                    final boolean holdingJungleAxeOnCooldown = InventoryUtils.JUNGLE_AXE_DISPLAYNAME.equals(p.getHeldItem().getDisplayName()) && CooldownManager.isOnCooldown(InventoryUtils.JUNGLE_AXE_DISPLAYNAME);
                    final boolean holdingTreecapitatorOnCooldown = InventoryUtils.TREECAPITATOR_DISPLAYNAME.equals(p.getHeldItem().getDisplayName()) && CooldownManager.isOnCooldown(InventoryUtils.TREECAPITATOR_DISPLAYNAME);

                    if (holdingJungleAxeOnCooldown || holdingTreecapitatorOnCooldown) {
                        returnValue.cancel();
                    }
                }
            }
        }
    }
}
