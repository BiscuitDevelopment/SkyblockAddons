package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.ButtonLocation;
import codes.biscuit.skyblockaddons.gui.ButtonSlider;
import codes.biscuit.skyblockaddons.gui.LocationEditGui;
import codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.client.GuiNotification;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

import static net.minecraft.client.gui.Gui.icons;

public class PlayerListener {

    public final static ItemStack BONE_ITEM = new ItemStack(Item.getItemById(352));
    private final static ResourceLocation BARS = new ResourceLocation("skyblockaddons", "bars.png");
    private final static ResourceLocation DEFENCE_VANILLA = new ResourceLocation("skyblockaddons", "defence.png");

    /**
     * Delay for item pickup logging to happen after changing a world in ms
     * to prevent the whole inventory being shown as just picked up
     */
    private final static int ITEM_PICKUP_LOG_DELAY = 1000;

    private boolean sentUpdate = false;
    private long lastWorldJoin = -1;
    private long lastBoss = -1;
    private int soundTick = 1;
    private int manaTick = 1;
    private long lastMinionSound = -1;
    private String cannotReachMobName;

    private boolean subtitleWarning = false;
    private boolean titleWarning = false;
    private Feature subtitleFeature = null;
    private Feature titleFeature = null;

    private boolean predictHealth = false;
    private boolean predictMana = false;
    private int defense = 0;
    private int health = 100;
    private int maxHealth = 100;
    private int mana = 0;
    private int maxMana = 100;

    private Map<String, ItemDiff> itemPickupLog = new HashMap<>();

//    private Feature.Accuracy magmaTimerAccuracy = null;
//    private long magmaTime = 7200;

    private boolean openMainGUI = false;

    private SkyblockAddons main;

    public PlayerListener(SkyblockAddons main) {
        this.main = main;
    }

    @SubscribeEvent()
    public void onWorldJoin(EntityJoinWorldEvent e) {
        if (e.entity == Minecraft.getMinecraft().thePlayer) {
            lastWorldJoin = System.currentTimeMillis();
            titleWarning = false;
            lastBoss = -1;
            soundTick = 1;
            manaTick = 1;
        }
    }

    @SubscribeEvent()
    public void onChatReceive(ClientChatReceivedEvent e) {
        String message = e.message.getUnformattedText();
        if (e.type == 2) {
            if (message.endsWith("\u270E Mana\u00A7r")) {
                try {
                    String returnMessage;
                    if (message.startsWith("\u00A7d\u00A7lTHE END RACE")) { // Might be doing the end race!
                        // Example Action Bar: '§d§lTHE END RACE §e00:52.370            §b147/147✎ Mana§r'
                        String[] messageSplit = message.split(" {12}");
                        String[] manaSplit = main.getUtils().getNumbersOnly(messageSplit[1]).split(Pattern.quote("/"));
                        mana = Integer.parseInt(manaSplit[0]);
                        maxMana = Integer.parseInt(manaSplit[1].trim());
                        predictMana = false;
                        predictHealth = true;
                        returnMessage = messageSplit[0];
                    } else {
                        // Example Action Bar: '§c586/586❤     §a247§a❈ Defense     §b173/173✎ Mana§r'
                        String[] splitMessage = message.split(" {5}");
                        String healthPart = splitMessage[0];
                        String defencePart = null;
                        String manaPart;
                        if (splitMessage.length > 2) {
                            defencePart = splitMessage[1];
                            manaPart = splitMessage[2];
                        } else {
                            manaPart = splitMessage[1];
                        }
                        String[] healthSplit = main.getUtils().getNumbersOnly(main.getUtils().stripColor(healthPart)).split(Pattern.quote("/"));
                        health = Integer.parseInt(healthSplit[0]);
                        maxHealth = Integer.parseInt(healthSplit[1]);
                        if (defencePart != null) {
                            defense = Integer.valueOf(main.getUtils().getNumbersOnly(defencePart).trim());
                        }
                        String[] manaSplit = main.getUtils().getNumbersOnly(manaPart).split(Pattern.quote("/"));
                        mana = Integer.parseInt(manaSplit[0]);
                        maxMana = Integer.parseInt(manaSplit[1].trim());
                        predictHealth = false;
                        predictMana = false;
                        StringBuilder newMessage = new StringBuilder();
                        boolean showHealth = main.getConfigValues().getHealthBarType() == Feature.BarType.OFF;
                        boolean showDefence = defencePart != null && main.getConfigValues().getDefenceIconType() == Feature.IconType.OFF;
                        boolean showMana = main.getConfigValues().getManaBarType() == Feature.BarType.OFF;
                        if (showHealth) {
                            newMessage.append(healthPart);
                        }
                        if (showDefence) {
                            if (showHealth) newMessage.append("     ");
                            newMessage.append(defencePart);
                        }
                        if (showMana) {
                            if (showHealth || showDefence) newMessage.append("     ");
                            newMessage.append(manaPart);
                        }
                        returnMessage = newMessage.toString();
                    }
                    if (returnMessage.length() == 0) {
                        returnMessage = " "; // This is to solve an issue with oof mod, which doesn't check if a string is empty before dealing with it (and it spams chat).
                    }
                    e.message = new ChatComponentText(returnMessage);
                    return;
                } catch (Exception ignored) {}
            }
            predictMana = true;
            predictHealth = true;
        } else {
            if (predictMana && message.startsWith("Used ") && message.endsWith("Mana)")) {
                int mana = Integer.parseInt(message.split(Pattern.quote("! ("))[1].split(Pattern.quote(" Mana)"))[0]);
                this.mana -= mana;
            }
        }
    }

    @SubscribeEvent()
    public void onRenderRegular(RenderGameOverlayEvent.Post e) {
        if ((!main.isUsingLabymod() || Minecraft.getMinecraft().ingameGUI instanceof GuiIngameForge) && main.getUtils().isOnSkyblock()) {
//            if (e.type == RenderGameOverlayEvent.ElementType.CROSSHAIRS) {
//                renderOverlays(e.resolution);
//                renderWarnings(e.resolution);
//            }
            if (e.type == RenderGameOverlayEvent.ElementType.EXPERIENCE) {
                renderOverlays(e.resolution);
                renderWarnings(e.resolution);
            }// else if (e.type == RenderGameOverlayEvent.ElementType.CHAT) {
            //    renderWarnings(e.resolution);
            //}
        }
    }

    @SubscribeEvent()
    public void onRenderLabyMod(RenderGameOverlayEvent e) {
        if (e.type == null && main.isUsingLabymod() && main.getUtils().isOnSkyblock()) {
            renderOverlays(e.resolution);
            renderWarnings(e.resolution);
        }
    }

    private void renderWarnings(ScaledResolution scaledResolution) {
        Minecraft mc = Minecraft.getMinecraft();
        int i = scaledResolution.getScaledWidth();
        if (titleWarning) {
            int j = scaledResolution.getScaledHeight();
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) (i / 2), (float) (j / 2), 0.0F);
//            GlStateManager.enableBlend();
//            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.pushMatrix();
            GlStateManager.scale(4.0F, 4.0F, 4.0F);
            Message message = null;
            switch (titleFeature) {
                case MAGMA_WARNING:
                    message = Message.MESSAGE_MAGMA_BOSS_WARNING;
                    break;
                case FULL_INVENTORY_WARNING:
                    message = Message.MESSAGE_FULL_INVENTORY;
                    break;
            }
            if (message != null) {
                String text;
                text = main.getConfigValues().getMessage(message);
                mc.ingameGUI.getFontRenderer().drawString(text, (float) (-mc.ingameGUI.getFontRenderer().getStringWidth(text) / 2), -20.0F,
                        main.getConfigValues().getColor(titleFeature).getColor(255), true);
            }
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }
        if (subtitleWarning) {
            int j = scaledResolution.getScaledHeight();
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) (i / 2), (float) (j / 2), 0.0F);
            GlStateManager.pushMatrix();
            GlStateManager.scale(2.0F, 2.0F, 2.0F);
            Message message = null;
            switch (subtitleFeature) {
                case MINION_STOP_WARNING:
                    message = Message.MESSAGE_MINION_CANNOT_REACH;
                    break;
                case MINION_FULL_WARNING:
                    message = Message.MESSAGE_MINION_IS_FULL;
                    break;
            }
            if (message != null) {
                String text;
                if (message == Message.MESSAGE_MINION_CANNOT_REACH) {
                    text = main.getConfigValues().getMessage(message, cannotReachMobName);
                } else {
                    text = main.getConfigValues().getMessage(message);
                }
                mc.ingameGUI.getFontRenderer().drawString(text, (float) (-mc.ingameGUI.getFontRenderer().getStringWidth(text) / 2), -23.0F,
                        main.getConfigValues().getColor(subtitleFeature).getColor(255), true);
            }
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
                        mc.ingameGUI.getFontRenderer().drawStringWithShadow(name, (float) (i / 2 - mc.ingameGUI.getFontRenderer().getStringWidth(name) / 2), (float) (i1 - 10), 16777215);
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        GlStateManager.disableBlend();
                    }
                }
            }
        }

    }

    private void renderOverlays(ScaledResolution sr) {
        Minecraft mc = Minecraft.getMinecraft();
        float scale = main.getUtils().denormalizeValue(main.getConfigValues().getGuiScale(), ButtonSlider.VALUE_MIN, ButtonSlider.VALUE_MAX, ButtonSlider.VALUE_STEP);
        float scaleMultiplier = 1F/scale;
        GlStateManager.disableBlend();
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);
        if (!(mc.currentScreen instanceof LocationEditGui) && !(mc.currentScreen instanceof GuiNotification)) {
            Feature.IconType iconType = main.getConfigValues().getDefenceIconType();
            Feature.BarType manaBarType = main.getConfigValues().getManaBarType();
            Feature.BarType healthBarType = main.getConfigValues().getHealthBarType();
            if ((!main.getConfigValues().getDisabledFeatures().contains(Feature.SKELETON_BAR)) && main.getUtils().isWearingSkeletonHelmet()) {
                CoordsPair coordsPair = main.getConfigValues().getCoords(Feature.SKELETON_BAR);
                int width = (int)(coordsPair.getX()*sr.getScaledWidth());
                int height = (int)(coordsPair.getY()*sr.getScaledHeight());
                int bones = 0;
                for (Entity listEntity : mc.theWorld.loadedEntityList) {
                    if (listEntity instanceof EntityItem &&
                            listEntity.ridingEntity instanceof EntityZombie && listEntity.ridingEntity.isInvisible() && listEntity.getDistanceToEntity(mc.thePlayer) <= 8) {
                        bones++;
                    }
                }
                if (bones > 3) bones = 3;
                for (int boneCounter = 0; boneCounter < bones; boneCounter++) {
                    mc.getRenderItem().renderItemIntoGUI(BONE_ITEM, (int)((width+boneCounter*15*scale)*scaleMultiplier), (int)((height+2)*scaleMultiplier));
                }
            }
            if (manaBarType == Feature.BarType.BAR
                    || manaBarType == Feature.BarType.BAR_TEXT) {
                drawBar(Feature.MANA_BAR, scaleMultiplier, mc, sr, Feature.MANA_BAR_COLOR);
            }
            if (healthBarType == Feature.BarType.BAR
                    || healthBarType == Feature.BarType.BAR_TEXT) {
                drawBar(Feature.HEALTH_BAR, scaleMultiplier, mc, sr, Feature.HEALTH_BAR_COLOR);
            }
            if (iconType == Feature.IconType.ICON || iconType == Feature.IconType.ICON_DEFENCE || iconType == Feature.IconType.ICON_PERCENTAGE
                    || iconType == Feature.IconType.ICON_DEFENCE_PERCENTAGE) {
                drawIcon(scale, mc, sr, null);
            }
            if (iconType == Feature.IconType.DEFENCE || iconType == Feature.IconType.ICON_DEFENCE || iconType == Feature.IconType.DEFENCE_PERCENTAGE
                    || iconType == Feature.IconType.ICON_DEFENCE_PERCENTAGE) {
                drawText(Feature.DEFENCE_TEXT, scaleMultiplier, mc, sr, Feature.DEFENCE_TEXT_COLOR);
            }
            if (iconType == Feature.IconType.PERCENTAGE || iconType == Feature.IconType.ICON_PERCENTAGE || iconType == Feature.IconType.DEFENCE_PERCENTAGE
                    || iconType == Feature.IconType.ICON_DEFENCE_PERCENTAGE) {
                drawText(Feature.DEFENCE_PERCENTAGE, scaleMultiplier, mc, sr, Feature.DEFENCE_PERCENTAGE_COLOR);
            }
            if (manaBarType == Feature.BarType.TEXT
                    || manaBarType == Feature.BarType.BAR_TEXT) {
                drawText(Feature.MANA_TEXT, scaleMultiplier, mc, sr, Feature.MANA_TEXT_COLOR);
            }
            if (healthBarType == Feature.BarType.TEXT
                    || healthBarType == Feature.BarType.BAR_TEXT) {
                drawText(Feature.HEALTH_TEXT, scaleMultiplier, mc, sr, Feature.HEALTH_TEXT_COLOR);
            }
            if(!main.getConfigValues().getDisabledFeatures().contains(Feature.ITEM_PICKUP_LOG)) {
                drawPickupLog(scaleMultiplier, mc, sr);
            }
        }
        GlStateManager.popMatrix();
    }

    private void drawBar(Feature feature, float scaleMultiplier, Minecraft mc, ScaledResolution sr, Feature colorFeature) {
        drawBar(feature, scaleMultiplier, mc, sr, colorFeature, null);
    }

    public void drawBar(Feature feature, float scaleMultiplier, Minecraft mc, ScaledResolution sr, Feature colorFeature, ButtonLocation buttonLocation) {
        mc.getTextureManager().bindTexture(PlayerListener.BARS);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        short barWidth = 92;
        float fill;
        if (feature == Feature.MANA_BAR) {
            fill = (float) mana / maxMana;
        } else {
            fill = (float) health / maxHealth;
        }
        if (fill > 1) fill = 1;
        float x;
        float y;
        if (feature == Feature.MANA_BAR) {
            CoordsPair coordsPair = main.getConfigValues().getCoords(Feature.MANA_BAR);
            x = coordsPair.getX();
            y = coordsPair.getY();
        } else {
            CoordsPair coordsPair = main.getConfigValues().getCoords(Feature.HEALTH_BAR);
            x = coordsPair.getX();
            y = coordsPair.getY();
        }
        int left = (int) (x * sr.getScaledWidth()) + 14;
        int filled = (int) (fill * barWidth);
        int top = (int) (y * sr.getScaledHeight()) + 10;
        int textureY = main.getConfigValues().getColor(colorFeature).ordinal()*10;
        if (buttonLocation == null) {
            mc.ingameGUI.drawTexturedModalRect(left * scaleMultiplier - 60, top * scaleMultiplier - 10, 0, textureY, barWidth, 5);
            if (filled > 0) {
                mc.ingameGUI.drawTexturedModalRect(left * scaleMultiplier - 60, top * scaleMultiplier - 10, 0, textureY + 5, filled, 5);
            }
        } else {
            buttonLocation.drawTexturedModalRect(left * scaleMultiplier - 60, top * scaleMultiplier - 10, 0, textureY, barWidth, 5);
            if (filled > 0) {
                buttonLocation.drawTexturedModalRect(left * scaleMultiplier - 60, top * scaleMultiplier - 10, 0, textureY+5, filled, 5);
            }
        }
    }

    public void drawIcon(float scale, Minecraft mc, ScaledResolution sr, ButtonLocation buttonLocation) {
        if (main.getConfigValues().getDisabledFeatures().contains(Feature.USE_VANILLA_TEXTURE_DEFENCE)) {
            mc.getTextureManager().bindTexture(icons);
        } else {
            mc.getTextureManager().bindTexture(PlayerListener.DEFENCE_VANILLA);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        float x;
        float y;
        CoordsPair coordsPair = main.getConfigValues().getCoords(Feature.DEFENCE_ICON);
        x = coordsPair.getX();
        y = coordsPair.getY();
        int left = (int) (x * sr.getScaledWidth());
        int top = (int) (y * sr.getScaledHeight());
        float scaleMultiplier;
        if (buttonLocation == null) {
            float newScale = scale*1.5F;
            scaleMultiplier = 1F/newScale;
            GlStateManager.pushMatrix();
            GlStateManager.scale(newScale, newScale, 1);
            scaleMultiplier/=scale;
            mc.ingameGUI.drawTexturedModalRect(left*scaleMultiplier, top* scaleMultiplier, 34, 9, 9, 9);
            GlStateManager.popMatrix();
        } else {
            scale *= (scale/1.5);
            scaleMultiplier = 1F/scale;
            buttonLocation.drawTexturedModalRect(left*scaleMultiplier, top*scaleMultiplier, 34, 9, 9, 9);
        }
    }

    public void drawText(Feature feature, float scaleMultiplier, Minecraft mc, ScaledResolution sr, Feature colorFeature) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int color = main.getConfigValues().getColor(colorFeature).getColor(255);
        String text;
        float textX;
        float textY;
        if (feature == Feature.MANA_TEXT) {
            CoordsPair coordsPair = main.getConfigValues().getCoords(Feature.MANA_TEXT);
            textX = coordsPair.getX();
            textY = coordsPair.getY();
            text = mana + "/" + maxMana;
        } else if (feature == Feature.HEALTH_TEXT) {
            CoordsPair coordsPair = main.getConfigValues().getCoords(Feature.HEALTH_TEXT);
            textX = coordsPair.getX();
            textY = coordsPair.getY();
            text = health + "/" + maxHealth;
        } else if (feature == Feature.DEFENCE_TEXT) {
            CoordsPair coordsPair = main.getConfigValues().getCoords(Feature.DEFENCE_TEXT);
            textX = coordsPair.getX();
            textY = coordsPair.getY();
            text = String.valueOf(defense);
        } else if(feature == Feature.ITEM_PICKUP_LOG) {
            CoordsPair coordsPair = main.getConfigValues().getCoords(Feature.ITEM_PICKUP_LOG);
            textX = coordsPair.getX();
            textY = coordsPair.getY();
            color = ConfigColor.WHITE.getColor(255);
            text = "Pickup Log";
        } else {
            CoordsPair coordsPair = main.getConfigValues().getCoords(Feature.DEFENCE_PERCENTAGE);
            textX = coordsPair.getX();
            textY = coordsPair.getY();
            double doubleDefence = (double)defense;
            double percentage = ((doubleDefence/100)/((doubleDefence/100)+1))*100; //Formula taken from https://hypixel.net/threads/how-armor-works-and-the-diminishing-return-of-higher-defence.2178928/
            BigDecimal bigDecimal = new BigDecimal(percentage).setScale(1, BigDecimal.ROUND_HALF_UP);
            text = bigDecimal.toString()+"%";
        }
        int x = (int) (textX * sr.getScaledWidth()) + 60 - mc.fontRendererObj.getStringWidth(text) / 2;
        int y = (int) (textY * sr.getScaledHeight()) + 4;
        x+=25;
        y+=10;
        mc.fontRendererObj.drawString(text, (int)(x*scaleMultiplier)-60+1, (int)(y*scaleMultiplier)-10, 0);
        mc.fontRendererObj.drawString(text, (int)(x*scaleMultiplier)-60-1, (int)(y*scaleMultiplier)-10, 0);
        mc.fontRendererObj.drawString(text, (int)(x*scaleMultiplier)-60, (int)(y*scaleMultiplier)+1-10, 0);
        mc.fontRendererObj.drawString(text, (int)(x*scaleMultiplier)-60, (int)(y*scaleMultiplier)-1-10, 0);
        mc.fontRendererObj.drawString(text, (int)(x*scaleMultiplier)-60, (int)(y*scaleMultiplier)-10, color);
//        mc.ingameGUI.drawString(mc.fontRendererObj, text, (int)(x*scaleMultiplier)-60, (int)(y*scaleMultiplier)-10, color);
    }

    /**
     * Draw the list of picked up items
     */
    private void drawPickupLog(float scaleMultiplier, Minecraft mc, ScaledResolution sr) {
        CoordsPair coordsPair = main.getConfigValues().getCoords(Feature.ITEM_PICKUP_LOG);
        float textX = coordsPair.getX();
        float textY = coordsPair.getY();
        int x = (int) (textX * sr.getScaledWidth()) + 60;
        int y = (int) (textY * sr.getScaledHeight()) + 4;
        int i = 0;
        for (ItemDiff itemDiff : itemPickupLog.values()) {
            String text = String.format("%s %sx \u00A7r%s", itemDiff.getAmount() > 0 ? "\u00A7a+":"\u00A7c-",
                    Math.abs(itemDiff.getAmount()), itemDiff.getDisplayName());
            if(itemDiff.getAmount() != 0) { // don't draw if the difference is 0
                mc.fontRendererObj.drawString(text,
                        (int) (x * scaleMultiplier) - 60,
                        (int) (y * scaleMultiplier) - 10 + (i * mc.fontRendererObj.FONT_HEIGHT),
                        ConfigColor.WHITE.getColor(255));
                i++;
            }
        }
    }

    @SubscribeEvent()
    public void onRenderRemoveBars(RenderGameOverlayEvent.Pre e) {
        if (e.type == RenderGameOverlayEvent.ElementType.ALL) {
            if (main.getUtils().isOnSkyblock()) {
                if (!main.getConfigValues().getDisabledFeatures().contains(Feature.HIDE_FOOD_ARMOR_BAR)) {
                    GuiIngameForge.renderFood = false;
                    GuiIngameForge.renderArmor = false;
                }
                if (!main.getConfigValues().getDisabledFeatures().contains(Feature.HIDE_HEALTH_BAR)) {
                    GuiIngameForge.renderHealth = false;
                }
            } else {
                if (!main.getConfigValues().getDisabledFeatures().contains(Feature.HIDE_HEALTH_BAR)) {
                    GuiIngameForge.renderHealth = true;
                }
                if (!main.getConfigValues().getDisabledFeatures().contains(Feature.HIDE_FOOD_ARMOR_BAR)) {
                    GuiIngameForge.renderArmor = true;
                }
            }
        }
    }

    @SubscribeEvent()
    public void onInteract(PlayerInteractEvent e) {
        if (!main.getConfigValues().getDisabledFeatures().contains(Feature.DISABLE_EMBER_ROD)) {
            Minecraft mc = Minecraft.getMinecraft();
            ItemStack heldItem = e.entityPlayer.getHeldItem();
            if (e.entityPlayer == mc.thePlayer && heldItem != null) {
                if (heldItem.getItem().equals(Items.blaze_rod) && heldItem.isItemEnchanted() && main.getUtils().getLocation() == Feature.Location.ISLAND) {
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
                    mana += (maxMana/50);
                    if (mana>maxMana) mana = maxMana;
                } if (predictHealth) {
                    Minecraft mc = Minecraft.getMinecraft();
                    if (mc != null) {
                        EntityPlayerSP p = mc.thePlayer;
                        if (p != null) { //Reverse calculate the player's health by using the player's vanilla hearts.
                            health = (int)(maxHealth*(p.getHealth()/p.getMaxHealth()));
                        }
                    }
                }
            } else if (manaTick % 5 == 0) {
                Minecraft mc = Minecraft.getMinecraft();
                if (mc != null) {
                    EntityPlayerSP p = mc.thePlayer;
                    if (p != null) {
                        main.getUtils().checkIfInventoryIsFull(mc, p);
                        main.getUtils().checkIfWearingSkeletonHelmet(p);
                        if(mc.currentScreen == null) {
                            List<ItemDiff> diffs = main.getUtils().getInventoryDifference(p);

                            // Don't add the difference to the displayed log right after a world join so
                            // it won't happen to detect the whole inventory as just picked up.
                            if(getLastWorldJoin()+ITEM_PICKUP_LOG_DELAY <= System.currentTimeMillis()) {
                                for (ItemDiff diff : diffs) {
                                    if (itemPickupLog.containsKey(diff.getDisplayName())) {
                                        itemPickupLog.get(diff.getDisplayName()).add(diff.getAmount());
                                    } else {
                                        itemPickupLog.put(diff.getDisplayName(), diff);
                                    }
                                }
                            }
                        }
                    }
                }

                List<String> logItemsToRemove = new LinkedList<>();
                itemPickupLog.forEach((displayName, itemDiff) -> {
                    if(itemDiff.getLifetime() > ItemDiff.LIFESPAN) {
                        logItemsToRemove.add(displayName);
                    }
                });
                logItemsToRemove.forEach(name -> itemPickupLog.remove(name));

            } else if (manaTick > 20) {
                main.getUtils().checkGameAndLocation();
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
        if (main.getUtils().isOnSkyblock() && main.getUtils().getLocation() == Feature.Location.ISLAND) {
            Entity entity = e.entity;
            if (entity instanceof EntityArmorStand && entity.hasCustomName()) {
                int cooldown = main.getConfigValues().getWarningSeconds()*1000+5000;
                if (!main.getConfigValues().getDisabledFeatures().contains(Feature.MINION_FULL_WARNING) &&
                        entity.getCustomNameTag().equals("\u00A7cMy storage is full! :(")) {
                    long now = System.currentTimeMillis();
                    if (now - lastMinionSound > cooldown) { //this just spams message...
                        lastMinionSound = now;
                        main.getUtils().playSound("random.pop", 1);
                        main.getPlayerListener().setSubtitleFeature(Feature.MINION_FULL_WARNING);
                        main.getPlayerListener().setSubtitleWarning(true);
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                main.getPlayerListener().setSubtitleWarning(false);
                            }
                        }, main.getConfigValues().getWarningSeconds() * 1000);
                    }
                } else if (!main.getConfigValues().getDisabledFeatures().contains(Feature.MINION_STOP_WARNING) &&
                        entity.getCustomNameTag().startsWith("\u00A7cI can\'t reach any ")) {
                    long now = System.currentTimeMillis();
                    if (now - lastMinionSound > cooldown) {
                        lastMinionSound = now;
                        main.getUtils().playSound("random.orb", 1);
                        String mobName = entity.getCustomNameTag().split(Pattern.quote("\u00A7cI can\'t reach any "))[1].toLowerCase();
                        if (mobName.lastIndexOf("s") == mobName.length() - 1) {
                            mobName = mobName.substring(0, mobName.length() - 1);
                        }
                        cannotReachMobName = mobName;
                        main.getPlayerListener().setSubtitleFeature(Feature.MINION_STOP_WARNING);
                        main.getPlayerListener().setSubtitleWarning(true);
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                main.getPlayerListener().setSubtitleWarning(false);
                            }
                        }, main.getConfigValues().getWarningSeconds() * 1000);
                    }
                }
            }
        }
    }

//    public static Set<BlockPos> particleBlocks = new HashSet<>();

//    @SubscribeEvent //TODO remove this after
//    public void RenderWorldLastEvent(RenderWorldLastEvent event)
//    {
////        List<EntityFX>[][] fxLayers = Minecraft.getMinecraft().effectRenderer.fxLayers;
//////        List<EntityFX> particles = new ArrayList<>();
////        Set<BlockPos> particleBlocks = new HashSet<>();
////        for (int i = 0; i < fxLayers.length; i++) {
////            for (int j = 0; j < fxLayers[i].length; j++) {
//////                if (entity instanceof EntityFX) {
////                List<EntityFX> list = fxLayers[i][j];
////                for (EntityFX entityFX : list) {
////                    particleBlocks.add(new BlockPos(entityFX.getPosition()));
////                }
//////                particles.add((EntityFX)entity);
//////                }
////            }
////        }
//        float partialTickTime = event.partialTicks;
//        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
//        double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTickTime;
//        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTickTime;
//        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTickTime;
//
//        GL11.glPushMatrix();
//        GL11.glTranslated(-x, -y, -z); //go from cartesian x,y,z coordinates to in-world x,y,z coordinates
//        GL11.glEnable(GL11.GL_DEPTH_TEST);
//
//        GL11.glBegin(GL11.GL_QUADS); //begin drawing lines defined by 2 vertices
//
//        GL11.glColor4f(1f, 0, 0, 0.5f); //alpha must be > 0.1
//        int bx = 5;
//        int by = 101;
//        int bz = 14;
//        GL11.glVertex3d(bx, by+1, bz+1); //top
//        GL11.glVertex3d(bx, by+1, bz);
//        GL11.glVertex3d(bx+1, by+1, bz);
//        GL11.glVertex3d(bx+1, by+1, bz+1);
//
//        GL11.glVertex3d(bx+1, by+1, bz);
//        GL11.glVertex3d(bx+1, by+1, bz+1);
//        GL11.glVertex3d(bx+1, by, bz+1);
//        GL11.glVertex3d(bx+1, by, bz);
//
//        GL11.glEnd();
////        for (BlockPos blockPos : particleBlocks) {
////            Block block = Minecraft.getMinecraft().theWorld.getBlockState(blockPos).getBlock();
////            if (block.equals(Blocks.obsidian) || block.equals(Blocks.end_stone)) {
//////                double minX = blockPos.getX() - 1;
//////                double maxX = blockPos.getX() + 1;
//////                double maxY = blockPos.getY() + 0.02;
//////                double minZ = blockPos.getZ() - 1;
//////                double maxZ = blockPos.getZ() + 1;
////                double minX = blockPos.getX() + 0.02;
////                double maxX = blockPos.getX()+1 - 0.02;
////                double maxY = blockPos.getY()  + 0.02 + 2;
////                double minZ = blockPos.getZ() + 0.02;
////                double maxZ = blockPos.getZ()+1 - 0.02;
////
////                //render an "X" using 2 lines at (0, 10, 0) in game
////                GL11.glBegin(GL11.GL_QUADS); //begin drawing lines defined by 2 vertices
////
////                GL11.glColor4f(1f, 0, 0, 1f); //alpha must be > 0.1
////                GL11.glVertex3d(maxX, maxY, maxZ);
////                GL11.glVertex3d(minX, maxY, minZ);
////                GL11.glVertex3d(maxX, maxY, minZ);
////                GL11.glVertex3d(minX, maxY, maxZ);
////
////                GL11.glEnd();
////            }
////        }
////        particleBlocks.clear();
//
//        //cleanup
//        GL11.glEnable(GL11.GL_TEXTURE_2D);
//        GL11.glPopMatrix();
//    }

    @SubscribeEvent()
    public void onTickMagmaBossChecker(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START && !main.getConfigValues().getDisabledFeatures().contains(Feature.MAGMA_WARNING) && main.getUtils().isOnSkyblock()) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc != null && mc.thePlayer != null) {
                if ((lastBoss == -1 || System.currentTimeMillis() - lastBoss > 1800000) && soundTick % 5 == 0) {
                    for (Entity entity : mc.theWorld.loadedEntityList) { // Loop through all the entities.
                        if (entity instanceof EntityMagmaCube) {
                            EntitySlime magma = (EntitySlime) entity;
                            int size = magma.getSlimeSize();
                            if (size > 10) { // Find a big magma boss
                                lastBoss = System.currentTimeMillis();
                                titleFeature = Feature.MAGMA_WARNING;
                                titleWarning = true; // Enable warning and disable again in four seconds.
                                soundTick = 16; // so the sound plays instantly
                                new Timer().schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        titleWarning = false;
                                    }
                                }, main.getConfigValues().getWarningSeconds()*1000); // 4 second warning.
//                                logServer(mc);
                            }
                        }
                    }
                }
                if (titleWarning && titleFeature == Feature.MAGMA_WARNING && soundTick % 4 == 0) { // Play sound every 4 ticks or 1/5 second.
                    main.getUtils().playSound("random.orb", 0.5);
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
            Minecraft.getMinecraft().displayGuiScreen(new SkyblockAddonsGui(main, 1));
            openMainGUI = false;
        }
    }

    public void setTitleWarning(boolean titleWarning) {
        this.titleWarning = titleWarning;
    }

    public void setTitleFeature(Feature titleFeature) {
        this.titleFeature = titleFeature;
    }

    private void setSubtitleFeature(Feature subtitleFeature) {
        this.subtitleFeature = subtitleFeature;
    }

    //    @SubscribeEvent() // apparently the music disc is random so cant use it
//    public void onPlaySound(PlaySoundEvent e) {
//        if (main.getUtils().getLocation() == Feature.Location.BLAZING_FORTRESS && e.sound.getSoundLocation().getResourcePath().equals("records.13")) {
////            magmaTime
//        }
//    }

    public void setOpenMainGUI(boolean openMainGUI) {
        this.openMainGUI = openMainGUI;
    }

    private void setSubtitleWarning(boolean subtitleWarning) {
        this.subtitleWarning = subtitleWarning;
    }

    public long getLastWorldJoin() {
        return lastWorldJoin;
    }
}
