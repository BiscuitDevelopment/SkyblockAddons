package codes.biscuit.skyblockaddons.listeners;

import static net.minecraft.client.gui.Gui.icons;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.lwjgl.opengl.GL11;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.ButtonSlider;
import codes.biscuit.skyblockaddons.gui.LocationEditGui;
import codes.biscuit.skyblockaddons.gui.SettingsGui;
import codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.utils.ConfigValues;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class PlayerListener {

	public final static ItemStack BONE = new ItemStack(Item.getItemById(352));
	public final static ResourceLocation MANA_BARS = new ResourceLocation("skyblockaddons", "manabars.png");

	private boolean sentUpdate = false;
	private boolean predictMana = false;
	private long lastWorldJoin = -1;
	private boolean fullInventoryWarning = false;
	private boolean bossWarning = false;
	private long lastBoss = -1;
	private int soundTick = 1;
	private int manaTick = 1;
	private long lastSound = -1;

	private int defense = 0;
	private int health = 100;
	private int maxHealth = 100;
	private int mana = 0;
	private int maxMana = 100;

	private boolean openMainGUI = false;
	private boolean openSettingsGUI = false;
//    private Map<Long, String> spawnLog = new HashMap<>();

	private SkyblockAddons main;

	public PlayerListener(SkyblockAddons main) {
		this.main = main;
	}
	
	@SubscribeEvent()
    public void onInventoryClick(GuiScreenEvent.MouseInputEvent e) {
        if(e.gui instanceof GuiContainer) {
            GuiContainer gui = (GuiContainer) e.gui;
            Slot bookSlot = gui.getSlotUnderMouse();
            if (bookSlot != null) {
                if (bookSlot.getSlotIndex() == 13) {
                	if (bookSlot.getHasStack()) {
                		// Get bottle of enchanting in the respective slots
                		List<ItemStack> enchantSlots = new ArrayList<ItemStack>();
                		enchantSlots.add(gui.inventorySlots.getSlot(29).getStack()); // low tier
                		enchantSlots.add(gui.inventorySlots.getSlot(31).getStack()); // medium tier
                		enchantSlots.add(gui.inventorySlots.getSlot(33).getStack()); // high tier
                		for (ItemStack slotItem : enchantSlots) {
                			if (slotItem.hasDisplayName() && slotItem.getDisplayName().startsWith(EnumChatFormatting.GREEN + "Enchant Item")) {
                    			Minecraft mc = Minecraft.getMinecraft();
                    			List<String> toolip = slotItem.getTooltip(mc.thePlayer, false);
                    			if (toolip.size() > 2) {
                    				String enchantLine = toolip.get(2);
                    				if (enchantLine.split(Pattern.quote("* "))[1].toLowerCase().contains(SkyblockAddons.INSTANCE.getConfigValues().getEnchantment().toLowerCase())) {
                    					e.setCanceled(true);
                    				}
                    			}
                    		}
						}            		
                	}
                }
            }
        }
    }

	@SubscribeEvent()
	public void onWorldJoin(EntityJoinWorldEvent e) {
		if (e.entity == Minecraft.getMinecraft().thePlayer) {
			lastWorldJoin = System.currentTimeMillis();
			bossWarning = false;
			lastBoss = -1;
			soundTick = 1;
			manaTick = 1;
		}
	}

	@SubscribeEvent()
	public void onRenderBossWarning(RenderGameOverlayEvent.Post e) {
		if (e.type == RenderGameOverlayEvent.ElementType.TEXT) { // Render a title-like warning.
			Minecraft mc = Minecraft.getMinecraft();
			ScaledResolution scaledresolution = e.resolution;
			int i = scaledresolution.getScaledWidth();
			if (bossWarning) {
				int j = scaledresolution.getScaledHeight();
				GlStateManager.pushMatrix();
				GlStateManager.translate((float) (i / 2), (float) (j / 2), 0.0F);
//            GlStateManager.enableBlend();
//            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
				GlStateManager.pushMatrix();
				GlStateManager.scale(4.0F, 4.0F, 4.0F);
				String text;
				text = main.getConfigValues().getColor(Feature.WARNING_COLOR).getChatFormatting()
						+ main.getConfigValues().getMessage(ConfigValues.Message.MESSAGE_MAGMA_BOSS_WARNING);
				mc.ingameGUI.getFontRenderer().drawString(text,
						(float) (-mc.ingameGUI.getFontRenderer().getStringWidth(text) / 2), -20.0F, 16777215, true);
				GlStateManager.popMatrix();
//            GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
			if (fullInventoryWarning
					&& !main.getConfigValues().getDisabledFeatures().contains(Feature.FULL_INVENTORY_WARNING)) {
				int j = scaledresolution.getScaledHeight();
				GlStateManager.pushMatrix();
				GlStateManager.translate((float) (i / 2), (float) (j / 2), 0.0F);
				GlStateManager.pushMatrix();
				GlStateManager.scale(4.0F, 4.0F, 4.0F);
				String text;
				text = main.getConfigValues().getColor(Feature.WARNING_COLOR).getChatFormatting()
						+ main.getConfigValues().getMessage(ConfigValues.Message.MESSAGE_FULL_INVENTORY);
				mc.ingameGUI.getFontRenderer().drawString(text,
						(float) (-mc.ingameGUI.getFontRenderer().getStringWidth(text) / 2), -20.0F, 16777215, true);
				GlStateManager.popMatrix();
				GlStateManager.popMatrix();
			}
			if (!main.getConfigValues().getDisabledFeatures().contains(Feature.MAGMA_BOSS_BAR)) {
				for (Entity entity : mc.theWorld.loadedEntityList) {
					if (entity instanceof EntityArmorStand) {
						String name = entity.getDisplayName().getFormattedText();
						if (name.contains("Magma Cube Boss ")) {
							name = name.split(Pattern.quote("Magma Cube Boss "))[1];
							mc.getTextureManager().bindTexture(icons);
							GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
							GlStateManager.enableBlend();
							int j = 182;
							int k = i / 2 - j / 2;
							int health = 1;
							int l = (int) (health * (float) (j + 1));
							int i1 = 12;
							mc.ingameGUI.drawTexturedModalRect(k, i1, 0, 74, j, 5);
							mc.ingameGUI.drawTexturedModalRect(k, i1, 0, 74, j, 5);

							if (l > 0) {
								mc.ingameGUI.drawTexturedModalRect(k, i1, 0, 79, l, 5);
							}
							mc.ingameGUI.getFontRenderer().drawStringWithShadow(name,
									(float) (i / 2 - mc.ingameGUI.getFontRenderer().getStringWidth(name) / 2),
									(float) (i1 - 10), 16777215);
							GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
							mc.getTextureManager().bindTexture(icons);
							GlStateManager.disableBlend();
						}
					}
				}
			}
		}
	}

	@SubscribeEvent()
	public void onChatReceive(ClientChatReceivedEvent e) {
		if (main.getConfigValues().getManaBarType() != Feature.ManaBarType.OFF) {
			String message = e.message.getUnformattedText();
			if (e.type == 2) {
				if (message.endsWith("\u270E Mana\u00A7r")) {
					try {
						String[] splitMessage = message.split(Pattern.quote("     "));
						String healthPart = splitMessage[0];
						String defencePart = null;
						String manaPart;
						if (splitMessage.length > 2) {
							defencePart = splitMessage[1];
							manaPart = splitMessage[2];
						} else {
							manaPart = splitMessage[1];
						}
						String[] healthSplit = main.getUtils().getNumbersOnly(healthPart).split(Pattern.quote("/"));
						health = Integer.parseInt(healthSplit[0]);
						maxHealth = Integer.parseInt(healthSplit[1]);
						if (defencePart != null) {
							defense = Integer.valueOf(main.getUtils().getNumbersOnly(defencePart).trim());
						}
						String[] manaSplit = main.getUtils().getNumbersOnly(manaPart).split(Pattern.quote("/"));
						mana = Integer.parseInt(manaSplit[0]);
						maxMana = Integer.parseInt(manaSplit[1].trim());
						predictMana = false;
						StringBuilder newMessage = new StringBuilder(healthPart).append("     ");
						if (defencePart != null) {
							newMessage.append(defencePart).append("     ");
						}
						e.message = new ChatComponentText(newMessage.toString());
						return;
					} catch (Exception ignored) {
					}
				}
				predictMana = true;
			} else {
				if (predictMana && message.startsWith("Used ") && message.endsWith("Mana)")) {
					int mana = Integer
							.parseInt(message.split(Pattern.quote("! ("))[1].split(Pattern.quote(" Mana)"))[0]);
					this.mana -= mana;
				}
			}
		}
	}

	@SubscribeEvent()
	public void onRenderRegular(RenderGameOverlayEvent.Post e) {
		if (!main.isUsingLabymod() && e.type == RenderGameOverlayEvent.ElementType.EXPERIENCE
				&& main.getUtils().isOnSkyblock()) {
			renderOverlays(e.resolution);
		}
	}

	@SubscribeEvent()
	public void onRenderLabyMod(RenderGameOverlayEvent e) {
		if (main.isUsingLabymod() && main.getUtils().isOnSkyblock()) {
			renderOverlays(e.resolution);
		}
	}

	private void renderOverlays(ScaledResolution sr) {
		Minecraft mc = Minecraft.getMinecraft();
		float scale = main.getUtils().denormalizeValue(main.getConfigValues().getGuiScale(), ButtonSlider.VALUE_MIN,
				ButtonSlider.VALUE_MAX, ButtonSlider.VALUE_STEP);
		float scaleMultiplier = 1F / scale;
		GlStateManager.pushMatrix();
		GlStateManager.scale(scale, scale, 1);
		if (main.getConfigValues().getManaBarType() != Feature.ManaBarType.OFF
				&& !(mc.currentScreen instanceof LocationEditGui)) {
			mc.getTextureManager().bindTexture(MANA_BARS);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.disableBlend();

			short barWidth = 92;
			if (main.getConfigValues().getManaBarType() == Feature.ManaBarType.BAR
					|| main.getConfigValues().getManaBarType() == Feature.ManaBarType.BAR_TEXT) {
				float manaFill = (float) mana / maxMana;
				if (manaFill > 1)
					manaFill = 1;
				int left = (int) (main.getConfigValues().getManaBarX() * sr.getScaledWidth()) + 14;
				int filled = (int) (manaFill * barWidth);
				int top = (int) (main.getConfigValues().getManaBarY() * sr.getScaledHeight()) + 10;
				// mc.ingameGUI.drawTexturedModalRect(left, top, 10, 84, barWidth, 5);
				int textureY = main.getConfigValues().getColor(Feature.MANA_BAR_COLOR).ordinal() * 10;
				mc.ingameGUI.drawTexturedModalRect(left * scaleMultiplier - 60, top * scaleMultiplier - 10, 0, textureY,
						barWidth, 5);
				if (filled > 0) {
//                        mc.ingameGUI.drawTexturedModalRect(left, top, 10, 89, filled, 5);
					mc.ingameGUI.drawTexturedModalRect(left * scaleMultiplier - 60, top * scaleMultiplier - 10, 0,
							textureY + 5, filled, 5);
				}
			}
			if (main.getConfigValues().getManaBarType() == Feature.ManaBarType.TEXT
					|| main.getConfigValues().getManaBarType() == Feature.ManaBarType.BAR_TEXT) {
				int color = main.getConfigValues().getColor(Feature.MANA_TEXT_COLOR).getColor(255);
				String text = mana + "/" + maxMana;
				int x = (int) (main.getConfigValues().getManaTextX() * sr.getScaledWidth()) + 60
						- mc.ingameGUI.getFontRenderer().getStringWidth(text) / 2;
				int y = (int) (main.getConfigValues().getManaTextY() * sr.getScaledHeight()) + 4;
				x += 25;
				y += 10;
				mc.ingameGUI.getFontRenderer().drawString(text, (int) (x * scaleMultiplier) - 60 + 1,
						(int) (y * scaleMultiplier) - 10, 0);
				mc.ingameGUI.getFontRenderer().drawString(text, (int) (x * scaleMultiplier) - 60 - 1,
						(int) (y * scaleMultiplier) - 10, 0);
				mc.ingameGUI.getFontRenderer().drawString(text, (int) (x * scaleMultiplier) - 60,
						(int) (y * scaleMultiplier) + 1 - 10, 0);
				mc.ingameGUI.getFontRenderer().drawString(text, (int) (x * scaleMultiplier) - 60,
						(int) (y * scaleMultiplier) - 1 - 10, 0);
				mc.ingameGUI.getFontRenderer().drawString(text, (int) (x * scaleMultiplier) - 60,
						(int) (y * scaleMultiplier) - 10, color);
				GlStateManager.enableBlend();
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			}
		}
		if ((!main.getConfigValues().getDisabledFeatures().contains(Feature.SKELETON_BAR))
				&& !(mc.currentScreen instanceof LocationEditGui) && main.getUtils().isWearingSkeletonHelmet()) {
			int width = (int) (main.getConfigValues().getSkeletonBarX() * sr.getScaledWidth());
			int height = (int) (main.getConfigValues().getSkeletonBarY() * sr.getScaledHeight());
			int bones = 0;
			for (Entity listEntity : mc.theWorld.loadedEntityList) {
				if (listEntity instanceof EntityItem && listEntity.ridingEntity instanceof EntityZombie
						&& listEntity.ridingEntity.isInvisible() && listEntity.getDistanceToEntity(mc.thePlayer) <= 8) {
					bones++;
				}
			}
			if (bones > 3)
				bones = 3;
			for (int boneCounter = 0; boneCounter < bones; boneCounter++) {
				mc.getRenderItem().renderItemIntoGUI(BONE, (int) ((width + boneCounter * 15 * scale) * scaleMultiplier),
						(int) ((height + 2) * scaleMultiplier));
			}
		}
		GlStateManager.popMatrix();
	}

	@SubscribeEvent()
	public void onRenderRemoveBars(RenderGameOverlayEvent.Pre e) {
		if (e.type == RenderGameOverlayEvent.ElementType.ALL) {
			if (main.getUtils().isOnSkyblock()
					&& !main.getConfigValues().getDisabledFeatures().contains(Feature.HIDE_FOOD_ARMOR_BAR)) {
				GuiIngameForge.renderFood = false;
				GuiIngameForge.renderArmor = false;
			}
		}
	}

//	@SubscribeEvent()
//	public void onGuiInteract(MouseInputEvent.Pre e) {
//		if (e.gui instanceof GuiInventory) {
//			GuiInventory gui = (GuiInventory) e.gui;
//			Slot slot = gui.getSlotUnderMouse();
//			Minecraft mc = Minecraft.getMinecraft();
//			ItemStack item = slot.getStack();
//			if (item != null && item.hasDisplayName()) {
//				if (item.getDisplayName().startsWith(EnumChatFormatting.GREEN + "Enchant Item")) {
//					List<String> toolTip = slot.getStack().getTooltip(mc.thePlayer, false);
//					if (toolTip.size() > 2) {
//						String enchantLine = toolTip.get(2);
//						if (enchantLine.split(Pattern.quote("* "))[1].toLowerCase().contains("sharpness".toLowerCase())) { // replace hard string with an object from textbox
//							mc.thePlayer.sendChatMessage("Got 'em");
//                        	mc.thePlayer.playSound("random.orb", 10, 0.5F);
//                        	e.setCanceled(true);
//                        }
//					}
//					mc.thePlayer.sendChatMessage(slot.getStack().getDisplayName());
//				}
//			}
//		}
//	}

	@SubscribeEvent()
	public void onInteract(PlayerInteractEvent e) {
		if (!main.getConfigValues().getDisabledFeatures().contains(Feature.DISABLE_EMBER_ROD)) {
			Minecraft mc = Minecraft.getMinecraft();
			ItemStack heldItem = e.entityPlayer.getHeldItem();
			if (e.entityPlayer == mc.thePlayer && heldItem != null) {
				if (heldItem.getItem().equals(Items.blaze_rod) && heldItem.isItemEnchanted()
						&& main.getUtils().isOnIsland()) {
					e.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent()
	public void onTickMana(TickEvent.ClientTickEvent e) {
		if (e.phase == TickEvent.Phase.START) {
			manaTick++;
			if (manaTick == 20) {
				if (predictMana) {
					mana += (maxMana / 50);
					if (mana > maxMana)
						mana = maxMana;
				}
			} else if (manaTick % 5 == 0) {
				Minecraft mc = Minecraft.getMinecraft();
				if (mc != null) {
					EntityPlayerSP p = mc.thePlayer;
					if (p != null) {
						main.getUtils().checkIfInventoryIsFull(mc, p);
						main.getUtils().checkIfWearingSkeletonHelmet(p);
					}
				}
			} else if (manaTick > 20) {
				main.getUtils().checkIfOnSkyblockAndIsland();
				Minecraft mc = Minecraft.getMinecraft();
				if (!sentUpdate && mc != null && mc.thePlayer != null && mc.theWorld != null) {
					main.getUtils().checkUpdates();
					sentUpdate = true;
				}
				manaTick = 1;
			}
		}
	}

	// Addition by Michael#3549
	@SubscribeEvent
	public void onEntityEvent(LivingEvent.LivingUpdateEvent e) {
		if (!main.getConfigValues().getDisabledFeatures().contains(Feature.MINION_STOP_WARNING)) {
			Entity entity = e.entity;
			if (entity instanceof EntityArmorStand && entity.hasCustomName()) {
				if (entity.getCustomNameTag().startsWith("\u00A7cI can\'t reach any ")) {
					long now = System.currentTimeMillis();
					if (now - lastSound > 5000) {
						lastSound = now;
						EntityPlayerSP p = Minecraft.getMinecraft().thePlayer;
						p.playSound("random.orb", 1, 1);
						String mobName = entity.getCustomNameTag().split(Pattern.quote("\u00A7cI can\'t reach any "))[1]
								.toLowerCase();
						if (mobName.lastIndexOf("s") == mobName.length() - 1) {
							mobName = mobName.substring(0, mobName.length() - 1);
						}
						p.addChatMessage(new ChatComponentText(EnumChatFormatting.RED + "A " + mobName
								+ " minion cannot reach and has stopped spawning!"));
					}
				}
			}
		}
	}

	@SubscribeEvent()
	public void onTickMagmaBossChecker(TickEvent.ClientTickEvent e) {
		if (e.phase == TickEvent.Phase.START
				&& !main.getConfigValues().getDisabledFeatures().contains(Feature.MAGMA_WARNING)) {
			Minecraft mc = Minecraft.getMinecraft();
			if (mc != null && mc.thePlayer != null) {
				if ((lastBoss == -1 || System.currentTimeMillis() - lastBoss > 1800000) && soundTick % 5 == 0) {
					for (Entity entity : mc.theWorld.loadedEntityList) { // Loop through all the entities.
						if (entity instanceof EntityMagmaCube) {
							EntitySlime magma = (EntitySlime) entity;
							int size = magma.getSlimeSize();
							if (size > 10) { // Find a big magma boss
								lastBoss = System.currentTimeMillis();
								bossWarning = true; // Enable warning and disable again in four seconds.
								soundTick = 16; // so the sound plays instantly
								new Timer().schedule(new TimerTask() {
									@Override
									public void run() {
										bossWarning = false;
									}
								}, main.getConfigValues().getWarningSeconds() * 1000); // 4 second warning.
//                                logServer(mc);
							}
						}
					}
				}
				if (bossWarning && soundTick % 4 == 0) { // Play sound every 4 ticks or 1/5 second.
					mc.thePlayer.playSound("random.orb", 1, 0.5F);
				}
			}
			soundTick++;
			if (soundTick > 20) {
				soundTick = 1;
			}
		}
	}

	@SubscribeEvent()
	public void onRender(TickEvent.RenderTickEvent e) {
		if (openMainGUI) {
			Minecraft.getMinecraft().displayGuiScreen(new SkyblockAddonsGui(main));
			openMainGUI = false;
		} else if (openSettingsGUI) {
			Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(main));
			openSettingsGUI = false;
		}
	}

	public boolean isBossWarning() {
		return bossWarning;
	}

	// private void logServer(Minecraft mc) { // for magma boss logs
//        if (mc.ingameGUI.getTabList().header != null) {
//            List<IChatComponent> siblings = mc.ingameGUI.getTabList().header.getSiblings(); // Bring back AT if doing this
//            if (siblings.size() > 2) {
//                String dateAndServer = siblings.get(siblings.size() - 3).getUnformattedText();
//                spawnLog.put(System.currentTimeMillis(), dateAndServer.split(Pattern.quote("  "))[1]);
//            }
//        }
//    }

	public void setOpenMainGUI(boolean openMainGUI) {
		this.openMainGUI = openMainGUI;
	}

	public void setOpenSettingsGUI(boolean openSettingsGUI) {
		this.openSettingsGUI = openSettingsGUI;
	}

	public void setFullInventoryWarning(boolean fullInventoryWarning) {
		this.fullInventoryWarning = fullInventoryWarning;
	}

	public boolean isFullInventoryWarning() {
		return fullInventoryWarning;
	}

	public long getLastWorldJoin() {
		return lastWorldJoin;
	}
}
