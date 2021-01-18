package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Arrays;
import java.util.List;

public class CompactCounter {
	
	/**
	 * Counts blocks left until next compact activation
	 * @Author Kmaf
	 */
	
	public enum State {
		guessing,
		compensating
	}
	
	@Getter private State state = State.guessing;
	private int offset = 0;
	private int compactLevel = 0;
	private int compactMined = 0;
	private int blocksTillActivation = 0;
	
	/**
	 * Checks if a tool has compact and if so calculates and renders the blocks remaining till it activates
	 */
	public void checkCompact() {
		SkyblockAddons main = SkyblockAddons.getInstance();
		ItemStack heldItem = Minecraft.getMinecraft().thePlayer.getHeldItem();
		if (heldItem == null) {
			return;
		}
		// Check Compact Status
		List<Item> pickaxes = Arrays.asList(Items.wooden_pickaxe, Items.stone_pickaxe, Items.iron_pickaxe, Items.golden_pickaxe, Items.diamond_pickaxe);
		boolean isPickaxe = false;
		for (Item pickaxe : pickaxes) {
			if (heldItem.getItem().equals(pickaxe)) {
				isPickaxe = true;
				break;
			}
		}
		if (isPickaxe) {
			NBTTagCompound extraAttributes = heldItem.getSubCompound("ExtraAttributes", false);
			int compactLevel = getCompactLevel(extraAttributes);
			if (compactLevel > 0) {
				int compactBlocksMined = getCompactMined(extraAttributes);
				if (compactBlocksMined > 0) {
					int blocksLeft = getBlocksTillActivation();
					main.getRenderListener().setBlocksLeftTillCompact(blocksLeft);
				}
			} else {
				main.getRenderListener().setBlocksLeftTillCompact(-1);
			}
		}

	}
	
	/**
	 * Returns the level of compact present on an item.
	 * @param extraAttributes The NBT Tag Compound 'ExtraAttributes'
	 * @return The level of compact level present or -1 if Compact is not present
	 */
	public int getCompactLevel(NBTTagCompound extraAttributes) {
		compactLevel = -1;
		if (extraAttributes.hasKey("enchantments")) {
			NBTTagCompound enchantments = extraAttributes.getCompoundTag("enchantments");
			if (enchantments.hasKey("compact")) {
				compactLevel = enchantments.getInteger("compact");
			}
		}
		return compactLevel;
	}
	
	/**
	 * Returns number of blocks mined on compact tool.
	 * @param extraAttributes The NBT Tag compound 'ExtraAttributes'
	 * @return The number of blocks mined with compact or -1 if tag is not present.
	 */
	public int getCompactMined(NBTTagCompound extraAttributes) {
		compactMined = -1;
		if (extraAttributes.hasKey("compact_blocks")) {
			compactMined = extraAttributes.getInteger("compact_blocks");
		}
		return compactMined;
	}
	
	/**
	 * Returns how many blocks till compact is next activated.
	 * @return The number of blocks till next compact ability or -1 if compound level is invalid.
	 */
	public int getBlocksTillActivation() {
		blocksTillActivation = getEnchantmentGoal(compactLevel) - ((compactMined + offset) % getEnchantmentGoal(compactLevel));
		return blocksTillActivation;
	}
	
	public int getEnchantmentGoal(int level) {
		switch (level) {
			case 1: {
				return 400;
			}
			case 2: {
				return 375;
			}
			case 3: {
				return 350;
			}
			case 4: {
				return 325;
			}
			case 5: {
				return 300;
			}
			case 6: {
				return 275;
			}
			case 7: {
				return  250;
			}
			case 8: {
				return 225;
			}
			case 9: {
				return 200;
			}
			case 10: {
				return 175;
			}
			default: {
				return 500;
			}
		}
	}
	
	/**
	 * Hypixel is weird so the value of blocks mined within the Pickaxe's lore is sometimes (most of the time) incorrect
	 * Added an offset that is calculated whenever the enchantment procs to correct any discrepancy
	 */
	public void setOffset() {
		state = State.compensating;
		checkCompact();
		if (blocksTillActivation != getEnchantmentGoal(compactLevel)) {
			offset = blocksTillActivation + offset;
			offset = offset % getEnchantmentGoal(compactLevel);
			SkyblockAddons.getInstance().getUtils().sendMessage("Compact Counter was off by: " + blocksTillActivation);
			SkyblockAddons.getInstance().getUtils().sendMessage("Set Offset to: " + offset);
		}
	}
}
