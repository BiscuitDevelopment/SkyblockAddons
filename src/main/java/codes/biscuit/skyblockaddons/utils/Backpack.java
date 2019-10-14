package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.common.util.Constants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public class Backpack {

	private int x;
	private int y;
	private ItemStack[] items;
	private String backpackName;
	private BackpackColor backpackColor;

	public Backpack(ItemStack[] items, String backpackName, BackpackColor backpackColor) {
		this.items = items;
		this.backpackName = backpackName;
		this.backpackColor = backpackColor;
	}

	public Backpack(ItemStack[] items, String backpackName, BackpackColor backpackColor, int x, int y) {
		this(items, backpackName, backpackColor);
		this.x = x;
		this.y = y;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public ItemStack[] getItems() {
		return items;
	}

	public String getBackpackName() {
		return backpackName;
	}

	public BackpackColor getBackpackColor() {
		return backpackColor;
	}

	public static Backpack getFromItem(ItemStack stack) {
		if (stack == null)
			return null;

		if (stack.hasTagCompound()) {
			NBTTagCompound extraAttributes = stack.getTagCompound();

			if (extraAttributes.hasKey("ExtraAttributes")) {
				extraAttributes = extraAttributes.getCompoundTag("ExtraAttributes");
				String id = extraAttributes.getString("id");

				if (id.contains("BACKPACK")) {
					byte[] bytes = null;
					for (String key : extraAttributes.getKeySet()) {
						if (key.endsWith("backpack_data")) {
							bytes = extraAttributes.getByteArray(key);
							break;
						}
					}

					if (bytes == null)
						return null;

					try {
						NBTTagCompound nbtTagCompound = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
						NBTTagList list = nbtTagCompound.getTagList("i", Constants.NBT.TAG_COMPOUND);
						int length = list.tagCount();
						ItemStack[] items = new ItemStack[length];

						for (int i = 0; i < length; i++) {
							NBTTagCompound item = list.getCompoundTagAt(i);
							short itemID = item.getShort("id");
							boolean enchanted = false;
							String newItemId = Item.getItemById(itemID).getRegistryName().toString();

							if (item.hasKey("tag")) {
								NBTTagCompound tag = item.getCompoundTag("tag");

								if (tag.hasKey("ExtraAttributes")) {
									NBTTagCompound itemAttributes = tag.getCompoundTag("ExtraAttributes");
									String attributeID = itemAttributes.getString("id");

									if (attributeID.startsWith("ENCHANTED_")) {
										enchanted = true;

										// This fixes enchanted potatoes having the wrong id (potato block).
										if (itemID == 142 && "ENCHANTED_POTATO".equals(attributeID))
											item.setShort("id", (short)392);
									}
								}
							}

							// Convert item id from 1.8 to 1.12 format
							item.removeTag("id");
							item.setString("id", newItemId);
							ItemStack itemStack = new ItemStack(item);

							// Fix potions
							if ("minecraft:potion".equals(newItemId)) {
								NBTTagCompound tag = item.getCompoundTag("tag");
								ItemStack potion = new ItemStack(Items.POTIONITEM, 1);
								List<PotionEffect> effects = PotionUtils.getEffectsFromTag(tag);
								PotionUtils.appendEffects(potion, effects);

								if (!effects.isEmpty()) {
									int pid = Potion.getIdFromPotion(effects.get(0).getPotion());
									PotionType potionType = PotionType.REGISTRY.getObjectById(pid);
									PotionUtils.addPotionToItemStack(potion, potionType);
								}

								// TODO: Fix non-potion potions (awkward, water, etc.)
								itemStack = potion;
							}

							if (enchanted)
								itemStack.addEnchantment(Enchantments.PROTECTION, 1);

							items[i] = itemStack;
						}

						BackpackColor color = BackpackColor.WHITE;
						if (extraAttributes.hasKey("backpack_color")) {
							try {
								color = BackpackColor.valueOf(extraAttributes.getString("backpack_color"));
							} catch (IllegalArgumentException ignored) {}
						}
						return new Backpack(items, SkyblockAddons.getInstance().getUtils().stripColor(stack.getDisplayName()), color);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		return null;
	}

	private static PotionType getPotionType(String potionName) {
		PotionType potionType = PotionType.getPotionTypeForName(potionName);
		return potionType != null && !PotionTypes.EMPTY.equals(potionType) ? potionType : PotionTypes.WATER;
	}

}