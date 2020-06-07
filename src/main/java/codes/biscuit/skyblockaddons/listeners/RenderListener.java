package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Attribute;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Location;
import codes.biscuit.skyblockaddons.core.Message;
import codes.biscuit.skyblockaddons.gui.IslandWarpGui;
import codes.biscuit.skyblockaddons.gui.LocationEditGui;
import codes.biscuit.skyblockaddons.gui.SettingsGui;
import codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonLocation;
import codes.biscuit.skyblockaddons.utils.*;
import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import codes.biscuit.skyblockaddons.utils.nifty.reflection.MinecraftReflection;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.client.GuiNotification;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.*;

import static net.minecraft.client.gui.Gui.icons;

public class RenderListener {

    private final static ItemStack BONE_ITEM = new ItemStack(Items.bone);
    private final static ResourceLocation BARS = new ResourceLocation("skyblockaddons", "bars.png");
    private final static ResourceLocation DEFENCE_VANILLA = new ResourceLocation("skyblockaddons", "defence.png");
    private final static ResourceLocation IMPERIAL_BARS_FIX = new ResourceLocation("skyblockaddons", "imperialbarsfix.png");
    private final static ResourceLocation TICKER_SYMBOL = new ResourceLocation("skyblockaddons", "ticker.png");

    private final static ResourceLocation ENDERMAN_ICON = new ResourceLocation("skyblockaddons", "icons/enderman.png");
    private final static ResourceLocation ENDERMAN_GROUP_ICON = new ResourceLocation("skyblockaddons", "icons/endermangroup.png");
    private final static ResourceLocation MAGMA_BOSS_ICON = new ResourceLocation("skyblockaddons", "icons/magmaboss.png");
    private final static ResourceLocation SIRIUS_ICON = new ResourceLocation("skyblockaddons", "icons/sirius.png");
    private final static ResourceLocation SUMMONING_EYE_ICON = new ResourceLocation("skyblockaddons", "icons/summoningeye.png");
    private final static ResourceLocation ZEALOTS_PER_EYE_ICON = new ResourceLocation("skyblockaddons", "icons/zealotspereye.png");
    private final static ResourceLocation SLASH_ICON = new ResourceLocation("skyblockaddons", "icons/slash.png");
    private final static ResourceLocation IRON_GOLEM_ICON = new ResourceLocation("skyblockaddons", "icons/irongolem.png");
    private final static ResourceLocation WARP_ICON = new ResourceLocation("skyblockaddons", "icons/warp.png");

    private final static ItemStack WATER_BUCKET = new ItemStack(Items.water_bucket);
    private final static ItemStack IRON_SWORD = new ItemStack(Items.iron_sword);
    private final static ItemStack NETHER_STAR = new ItemStack(Items.nether_star);

    private SkyblockAddons main;

    @Getter @Setter private boolean predictHealth = false;
    @Getter @Setter private boolean predictMana = false;

    @Setter private boolean updateMessageDisplayed = false;

    private Feature subtitleFeature = null;
    @Getter @Setter private Feature titleFeature = null;

    @Setter private int arrowsLeft;

    @Setter private String cannotReachMobName = null;

    @Setter private long skillFadeOutTime = -1;
    @Setter private EnumUtils.SkillType skill = null;
    @Setter private String skillText = null;

    private EnumUtils.GUIType guiToOpen = null;
    private int guiPageToOpen = 1;
    private EnumUtils.GuiTab guiTabToOpen = EnumUtils.GuiTab.MAIN;
    private Feature guiFeatureToOpen = null;


    public RenderListener(SkyblockAddons main) {
        this.main = main;
    }

    /**
     * Render overlays and warnings for clients without labymod.
     */
    @SubscribeEvent()
    public void onRenderRegular(RenderGameOverlayEvent.Post e) {
        if ((!main.isUsingLabymod() || Minecraft.getMinecraft().ingameGUI instanceof GuiIngameForge)) {
            if (e.type == RenderGameOverlayEvent.ElementType.EXPERIENCE || e.type == RenderGameOverlayEvent.ElementType.JUMPBAR) {
                if (main.getUtils().isOnSkyblock()) {
                    renderOverlays();
                    renderWarnings(e.resolution);
                } else {
                    renderTimersOnly();
                }
                drawUpdateMessage();
            }
        }
    }

    /**
     * Render overlays and warnings for clients with labymod.
     * Labymod creates its own ingame gui and replaces the forge one, and changes the events that are called.
     * This is why the above method can't work for both.
     */
    @SubscribeEvent()
    public void onRenderLabyMod(RenderGameOverlayEvent e) {
        if (e.type == null && main.isUsingLabymod()) {
            if (main.getUtils().isOnSkyblock()) {
                renderOverlays();
                renderWarnings(e.resolution);
            } else {
                renderTimersOnly();
            }
            drawUpdateMessage();
        }
    }

    @SubscribeEvent()
    public void onRenderLiving(RenderLivingEvent.Specials.Pre<EntityLivingBase> e) {
        Entity entity = e.entity;
        if (main.getConfigValues().isEnabled(Feature.MINION_DISABLE_LOCATION_WARNING) && entity.hasCustomName()) {
            if (entity.getCustomNameTag().startsWith("§cThis location isn\'t perfect! :(")) {
                e.setCanceled(true);
            }
            if (entity.getCustomNameTag().startsWith("§c/!\\")) {
                for (Entity listEntity : Minecraft.getMinecraft().theWorld.loadedEntityList) {
                    if (listEntity.hasCustomName() && listEntity.getCustomNameTag().startsWith("§cThis location isn\'t perfect! :(") &&
                            listEntity.posX == entity.posX && listEntity.posZ == entity.posZ &&
                            listEntity.posY + 0.375 == entity.posY) {
                        e.setCanceled(true);
                        break;
                    }
                }
            }
        }
    }

    /**
     * I have an option so you can see the magma timer in other games so that's why.
     */
    private void renderTimersOnly() {
        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof LocationEditGui) && !(mc.currentScreen instanceof GuiNotification)) {
            GlStateManager.disableBlend();
            if (main.getConfigValues().isEnabled(Feature.MAGMA_BOSS_TIMER) && main.getConfigValues().isEnabled(Feature.SHOW_MAGMA_TIMER_IN_OTHER_GAMES) &&
                    main.getPlayerListener().getMagmaAccuracy() != EnumUtils.MagmaTimerAccuracy.NO_DATA) {
                float scale = main.getConfigValues().getGuiScale(Feature.MAGMA_BOSS_TIMER);
                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, 1);
                drawText(Feature.MAGMA_BOSS_TIMER, scale, mc, null);
                GlStateManager.popMatrix();
            }
            if (main.getConfigValues().isEnabled(Feature.DARK_AUCTION_TIMER) && main.getConfigValues().isEnabled(Feature.SHOW_DARK_AUCTION_TIMER_IN_OTHER_GAMES)) {
                float scale = main.getConfigValues().getGuiScale(Feature.DARK_AUCTION_TIMER);
                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, 1);
                drawText(Feature.DARK_AUCTION_TIMER, scale, mc, null);
                GlStateManager.popMatrix();
            }
        }
    }

    /**
     * This renders all the title/subtitle warnings from features.
     */
    private void renderWarnings(ScaledResolution scaledResolution) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null || !main.getUtils().isOnSkyblock()) {
            return;
        }

        int i = scaledResolution.getScaledWidth();
        if (titleFeature != null) {
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
                case SUMMONING_EYE_ALERT:
                    message = Message.MESSAGE_SUMMONING_EYE_FOUND;
                    break;
                case SPECIAL_ZEALOT_ALERT:
                    message = Message.MESSAGE_SPECIAL_ZEALOT_FOUND;
                    break;
            }
            if (message != null) {
                String text = message.getMessage();
                ChromaManager.renderingText(titleFeature);
                MinecraftReflection.FontRenderer.drawString(text, (float) (-MinecraftReflection.FontRenderer.getStringWidth(text) / 2), -20.0F,
                        main.getConfigValues().getColor(titleFeature).getRGB(), true);
                ChromaManager.doneRenderingText();
            }
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }
        if (subtitleFeature != null) {
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
                case NO_ARROWS_LEFT_ALERT:
                    message = Message.MESSAGE_NO_ARROWS_LEFT;
                    break;
            }
            if (message != null) {
                String text;
                if (message == Message.MESSAGE_MINION_CANNOT_REACH) {
                    text = message.getMessage(cannotReachMobName);
                } else if (message == Message.MESSAGE_NO_ARROWS_LEFT && arrowsLeft != -1) {
                    text = Message.MESSAGE_ONLY_FEW_ARROWS_LEFT.getMessage(Integer.toString(arrowsLeft));
                } else {
                    text = message.getMessage();
                }
                ChromaManager.renderingText(subtitleFeature);
                MinecraftReflection.FontRenderer.drawString(text, -MinecraftReflection.FontRenderer.getStringWidth(text) / 2F, -23.0F,
                        main.getConfigValues().getColor(subtitleFeature).getRGB(), true);
                ChromaManager.doneRenderingText();
            }
            GlStateManager.popMatrix();
            GlStateManager.popMatrix();
        }
    }

    /**
     * This renders all the gui elements (bars, icons, texts, skeleton bar, etc.).
     */
    private void renderOverlays() {
        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof LocationEditGui) && !(mc.currentScreen instanceof GuiNotification)) {
            GlStateManager.disableBlend();

            for (Feature feature : Feature.getGuiFeatures()) {
                if (main.getConfigValues().isEnabled(feature)) {
                    if (feature == Feature.SKELETON_BAR && !main.getInventoryUtils().isWearingSkeletonHelmet())
                        continue;
                    if (feature == Feature.HEALTH_UPDATES && main.getPlayerListener().getHealthUpdate() == null)
                        continue;

                    float scale = main.getConfigValues().getGuiScale(feature);
                    GlStateManager.pushMatrix();
                    GlStateManager.scale(scale, scale, 1);
                    feature.draw(scale, mc, null);
                    GlStateManager.popMatrix();
                }
            }
        }
    }

    /**
     * This renders both the bars.
     */
    public void drawBar(Feature feature, float scale, Minecraft mc, ButtonLocation buttonLocation) {
        mc.getTextureManager().bindTexture(BARS);

        if (main.getUtils().isUsingOldSkyBlockTexture()) {
            mc.getTextureManager().bindTexture(IMPERIAL_BARS_FIX);
        }

        // The height and width of this element (box not included)
        int barHeightExpansion = 2 * main.getConfigValues().getSizes(feature).getY();
        int height = 3 + barHeightExpansion;

        int barWidthExpansion = 10 * main.getConfigValues().getSizes(feature).getX();
        int width = 22 + barWidthExpansion;

        // The fill of the bar from 0 to 1
        float fill;
        if (feature == Feature.MANA_BAR) {
            fill = (float) getAttribute(Attribute.MANA) / getAttribute(Attribute.MAX_MANA);
        } else {
            fill = (float) getAttribute(Attribute.HEALTH) / getAttribute(Attribute.MAX_HEALTH);
        }
        if (fill > 1) fill = 1;
        int filled = Math.round(fill * width);

        float x = main.getConfigValues().getActualX(feature);
        float y = main.getConfigValues().getActualY(feature);
        Color color = main.getConfigValues().getColor(feature);

        if (feature == Feature.HEALTH_BAR && main.getConfigValues().isEnabled(Feature.CHANGE_BAR_COLOR_FOR_POTIONS)) {
            if (mc.thePlayer.isPotionActive(19/* Poison */)) {
                color = ChatFormatting.DARK_GREEN.getColor();
            } else if (mc.thePlayer.isPotionActive(20/* Wither */)) {
                color = ChatFormatting.DARK_GRAY.getColor();
            }
        }

        // Put the x & y to scale, remove half the width and height to center this element.
        x /= scale;
        y /= scale;
        x -= (float) width / 2;
        y -= (float) height / 2;
        int intX = Math.round(x);
        int intY = Math.round(y);
        if (buttonLocation == null) {
            drawModularBar(mc, color, false, intX, intY + barHeightExpansion / 2, null, feature, filled, width);
            if (filled > 0) {
                drawModularBar(mc, color, true, intX, intY + barHeightExpansion / 2, null, feature, filled, width);
            }
        } else {
            int boxXOne = intX - 4;
            int boxXTwo = intX + width + 5;
            int boxYOne = intY - 3;
            int boxYTwo = intY + height + 4;
            buttonLocation.checkHoveredAndDrawBox(boxXOne, boxXTwo, boxYOne, boxYTwo, scale);
            drawModularBar(mc, color, false, intX, intY + barHeightExpansion / 2, buttonLocation, feature, filled, width);
            if (filled > 0) {
                drawModularBar(mc, color, true, intX, intY + barHeightExpansion / 2, buttonLocation, feature, filled, width);
            }
        }
    }

    private void drawModularBar(Minecraft mc, Color color, boolean filled, int x, int y, ButtonLocation buttonLocation, Feature feature, int fillWidth, int maxWidth) {
        Gui gui = mc.ingameGUI;
        if (buttonLocation != null) {
            gui = buttonLocation;
        }
        if (color.getRGB() == ChatFormatting.BLACK.getRGB()) {
            GlStateManager.color(0.25F, 0.25F, 0.25F); // too dark normally
        } else { // a little darker for contrast
            GlStateManager.color(((float) color.getRed() / 255) * 0.9F, ((float) color.getGreen() / 255) * 0.9F, ((float) color.getBlue() / 255) * 0.9F);
        }
        CoordsPair sizes = main.getConfigValues().getSizes(feature);
        if (!filled) fillWidth = maxWidth;
        drawBarStart(gui, x, y, filled, sizes.getX(), sizes.getY(), fillWidth, color, maxWidth);
    }

    private void drawBarStart(Gui gui, int x, int y, boolean filled, int barWidth, int barHeight, int fillWidth, Color color, int maxWidth) {
        int baseTextureY = filled ? 0 : 6;

//        drawMiddleThreeRows(gui,x+10,y,barHeight,22,baseTextureY,2, fillWidth, 2); // these two lines just fill some gaps in the bar
//        drawMiddleThreeRows(gui,x+11+(barWidth*9),y,barHeight,22,baseTextureY,2, fillWidth, 2);

        drawAllFiveRows(gui, x, y, barHeight, 0, baseTextureY, 11, fillWidth);

        drawBarSeparators(gui, x + 11, y, baseTextureY, barWidth, barHeight, fillWidth);

        if (fillWidth < maxWidth-1 && fillWidth > 0 && // This just draws a dark line to easily distinguish where the bar's progress is.
                main.getUtils().isUsingDefaultBarTextures()) { // It doesn't always work out nicely when using like custom textures though.
            GlStateManager.color(((float) color.getRed() / 255) * 0.8F, ((float) color.getGreen() / 255) * 0.8F, ((float) color.getBlue() / 255) * 0.8F);
            drawMiddleThreeRows(gui, x + fillWidth, y, barHeight, 11, 6, 2, fillWidth, 2);
        }
    }

    private void drawMiddleBarParts(Gui gui, int x, int y, int baseTextureY, int barWidth, int barHeight, int fillWidth) {
        int endBarX = 0;
        for (int i = 0; i < barWidth; i++) {
            endBarX = x + (i * 10);
            drawAllFiveRows(gui, endBarX, y, barHeight, 12, baseTextureY, 9, fillWidth - 11 - (i * 10));
        }
        drawBarEnd(gui, endBarX + 10, y, baseTextureY, barWidth, barHeight, fillWidth);
    }

    private void drawBarSeparators(Gui gui, int x, int y, int baseTextureY, int barWidth, int barHeight, int fillWidth) {
        for (int i = 0; i <= barWidth; i++) {
            drawMiddleThreeRows(gui, x + (i * 10), y, barHeight, 11, baseTextureY, 1, fillWidth - 11 - (i * 10), 2);
        }
        drawMiddleBarParts(gui, x + 1, y, baseTextureY, barWidth, barHeight, fillWidth);
    }

    private void drawBarEnd(Gui gui, int x, int y, int baseTextureY, int barWidth, int barHeight, int fillWidth) {
        drawAllFiveRows(gui, x, y, barHeight, 22, baseTextureY, 11, fillWidth - 11 - (barWidth * 10));
    }

    private void drawAllFiveRows(Gui gui, int x, int y, int barHeight, int textureX, int baseTextureY, int width, int fillWidth) {
        if (fillWidth > width || baseTextureY >= 8) fillWidth = width;
        gui.drawTexturedModalRect(x, y + 1 - barHeight, textureX, baseTextureY, fillWidth, 1);

        drawMiddleThreeRows(gui, x, y, barHeight, textureX, baseTextureY, width, fillWidth, 1);

        gui.drawTexturedModalRect(x, y + 3 + barHeight, textureX, baseTextureY + 4, fillWidth, 1);
    }

    private void drawMiddleThreeRows(Gui gui, int x, int y, int barHeight, int textureX, int baseTextureY, int width, int fillWidth, int rowHeight) {
        if (fillWidth > width || baseTextureY >= 8) fillWidth = width;
        for (int i = 0; i < barHeight; i++) {
            if (rowHeight == 2) { //this means its drawing bar separators, and its a little different
                gui.drawTexturedModalRect(x, y - i, textureX, baseTextureY, fillWidth, rowHeight);
            } else {
                gui.drawTexturedModalRect(x, y + 1 - i, textureX, baseTextureY + 1, fillWidth, rowHeight);
            }
        }

        gui.drawTexturedModalRect(x, y + 2, textureX, baseTextureY + 2, fillWidth, 1);

        for (int i = 0; i < barHeight; i++) {
            gui.drawTexturedModalRect(x, y + 3 + i, textureX, baseTextureY + 3, fillWidth, rowHeight);
        }
    }

    /**
     * Renders the messages from the SkyblockAddons Updater
     */
    private void drawUpdateMessage() {
        Updater updater = main.getUpdater();
        String message = updater.getMessageToRender();

        if (updater.hasUpdate() && message != null && !updateMessageDisplayed) {
            Minecraft mc = Minecraft.getMinecraft();
            String[] textList = main.getUtils().wrapSplitText(message, 36);

            int halfWidth = new ScaledResolution(mc).getScaledWidth() / 2;
            Gui.drawRect(halfWidth - 110, 20, halfWidth + 110, 53 + textList.length * 10, main.getUtils().getDefaultBlue(140));
            String title = SkyblockAddons.MOD_NAME;
            GlStateManager.pushMatrix();
            float scale = 1.5F;
            GlStateManager.scale(scale, scale, 1);
            MinecraftReflection.FontRenderer.drawString(title, (int) (halfWidth / scale) - MinecraftReflection.FontRenderer.getStringWidth(title) / 2, (int) (30 / scale), ChatFormatting.WHITE);
            GlStateManager.popMatrix();
            int y = 45;
            for (String line : textList) {
                MinecraftReflection.FontRenderer.drawString(line, halfWidth - MinecraftReflection.FontRenderer.getStringWidth(line) / 2, y, ChatFormatting.WHITE);
                y += 10;
            }

            main.getScheduler().schedule(Scheduler.CommandType.ERASE_UPDATE_MESSAGE, 10);

            main.getUpdater().sendUpdateMessage();
        }
    }

    /**
     * This renders a bar for the skeleton hat bones bar.
     */
    public void drawSkeletonBar(Minecraft mc, float scale, ButtonLocation buttonLocation) {
        float x = main.getConfigValues().getActualX(Feature.SKELETON_BAR);
        float y = main.getConfigValues().getActualY(Feature.SKELETON_BAR);
        int bones = 0;
        if (!(mc.currentScreen instanceof LocationEditGui)) {
            for (Entity listEntity : mc.theWorld.loadedEntityList) {
                if (listEntity instanceof EntityItem &&
                        listEntity.ridingEntity instanceof EntityArmorStand && listEntity.ridingEntity.isInvisible() && listEntity.getDistanceToEntity(mc.thePlayer) <= 8) {
                    bones++;
                }
            }
        } else {
            bones = 3;
        }
        if (bones > 3) bones = 3;

        float height = 16;
        float width = 3 * 15;
        x -= Math.round(width * scale / 2);
        y -= Math.round(height * scale / 2);
        x /= scale;
        y /= scale;
        if (buttonLocation != null) {
            int boxXOne = Math.round(x - 4);
            int boxXTwo = Math.round(x + width + 4);
            int boxYOne = Math.round(y - 4);
            int boxYTwo = Math.round(y + height + 4);
            buttonLocation.checkHoveredAndDrawBox(boxXOne, boxXTwo, boxYOne, boxYTwo, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        for (int boneCounter = 0; boneCounter < bones; boneCounter++) {
            mc.getRenderItem().renderItemIntoGUI(BONE_ITEM, Math.round((x + boneCounter * 15)), Math.round(y));
        }
    }

    /**
     * This renders the skeleton bar.
     */
    public void drawScorpionFoilTicker(Minecraft mc, float scale, ButtonLocation buttonLocation) {
        if (buttonLocation != null || main.getPlayerListener().getTickers() != -1) {
            float x = main.getConfigValues().getActualX(Feature.TICKER_CHARGES_DISPLAY);
            float y = main.getConfigValues().getActualY(Feature.TICKER_CHARGES_DISPLAY);

            float height = 9;
            float width = 4 * 11;
            x -= Math.round(width * scale / 2);
            y -= Math.round(height * scale / 2);
            x /= scale;
            y /= scale;
            if (buttonLocation != null) {
                int boxXOne = Math.round(x - 4);
                int boxXTwo = Math.round(x + width + 2);
                int boxYOne = Math.round(y - 4);
                int boxYTwo = Math.round(y + height + 4);
                buttonLocation.checkHoveredAndDrawBox(boxXOne, boxXTwo, boxYOne, boxYTwo, scale);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }

            int maxTickers = (buttonLocation == null) ? main.getPlayerListener().getMaxTickers() : 4;
            for (int tickers = 0; tickers < maxTickers; tickers++) {
                mc.getTextureManager().bindTexture(TICKER_SYMBOL);
                GlStateManager.enableAlpha();
                if (tickers < (buttonLocation == null ? main.getPlayerListener().getTickers() : 3)) {
                    Gui.drawModalRectWithCustomSizedTexture(Math.round(x + tickers * 11), Math.round(y), 0, 0, 9, 9, 18, 9);
                } else {
                    Gui.drawModalRectWithCustomSizedTexture(Math.round(x + tickers * 11), Math.round(y), 9, 0, 9, 9, 18, 9);
                }
            }
        }
    }


    /**
     * This renders the defence icon.
     */
    public void drawIcon(float scale, Minecraft mc, ButtonLocation buttonLocation) {
        if (main.getConfigValues().isDisabled(Feature.USE_VANILLA_TEXTURE_DEFENCE)) {
            mc.getTextureManager().bindTexture(icons);
        } else {
            mc.getTextureManager().bindTexture(DEFENCE_VANILLA);
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        // The height and width of this element (box not included)
        int height = 9;
        int width = 9;
        float x = main.getConfigValues().getActualX(Feature.DEFENCE_ICON);
        float y = main.getConfigValues().getActualY(Feature.DEFENCE_ICON);
        if (buttonLocation == null) {
            float newScale = scale * 1.5F;
            GlStateManager.pushMatrix();
            GlStateManager.scale(newScale, newScale, 1);
            newScale *= scale;
            x -= Math.round((float) width * newScale / 2);
            y -= Math.round((float) height * newScale / 2);
            mc.ingameGUI.drawTexturedModalRect(x / newScale, y / newScale, 34, 9, width, height);
            GlStateManager.popMatrix();
        } else {
            scale *= (scale / 1.5);
            x -= Math.round((float) width * scale / 2);
            y -= Math.round((float) height * scale / 2);
            x /= scale;
            y /= scale;
            int intX = Math.round(x);
            int intY = Math.round(y);
            int boxXOne = intX - 2;
            int boxXTwo = intX + width + 2;
            int boxYOne = intY - 2;
            int boxYTwo = intY + height + 2;
            buttonLocation.checkHoveredAndDrawBox(boxXOne, boxXTwo, boxYOne, boxYTwo, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            buttonLocation.drawTexturedModalRect(intX, intY, 34, 9, width, height);
        }
    }

    /**
     * This renders all the different types gui text elements.
     */
    public void drawText(Feature feature, float scale, Minecraft mc, ButtonLocation buttonLocation) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        String text;
        int color = main.getConfigValues().getColor(feature).getRGB();
        float textAlpha = 1;
        if (feature == Feature.MANA_TEXT) {
            text = getAttribute(Attribute.MANA) + "/" + getAttribute(Attribute.MAX_MANA);
        } else if (feature == Feature.HEALTH_TEXT) {
            text = getAttribute(Attribute.HEALTH) + "/" + getAttribute(Attribute.MAX_HEALTH);
        } else if (feature == Feature.DEFENCE_TEXT) {
            text = String.valueOf(getAttribute(Attribute.DEFENCE));
        } else if (feature == Feature.DEFENCE_PERCENTAGE) {
            double doubleDefence = getAttribute(Attribute.DEFENCE);
            double percentage = ((doubleDefence / 100) / ((doubleDefence / 100) + 1)) * 100; //Taken from https://hypixel.net/threads/how-armor-works-and-the-diminishing-return-of-higher-defence.2178928/
            BigDecimal bigDecimal = new BigDecimal(percentage).setScale(1, BigDecimal.ROUND_HALF_UP);
            text = bigDecimal.toString() + "%";
        } else if (feature == Feature.SPEED_PERCENTAGE) {
            String walkSpeed = String.valueOf(Minecraft.getMinecraft().thePlayer.capabilities.getWalkSpeed() * 1000);
            text = walkSpeed.substring(0, Math.min(walkSpeed.length(), 3));

            if (text.endsWith(".")) text = text.substring(0, text.indexOf('.')); //remove trailing periods

            text += "%";
        } else if (feature == Feature.HEALTH_UPDATES) {
            Integer healthUpdate = main.getPlayerListener().getHealthUpdate();
            if (buttonLocation == null) {
                if (healthUpdate != null) {
                    color = healthUpdate > 0 ? ChatFormatting.GREEN.getRGB() : ChatFormatting.RED.getRGB();
                    text = (healthUpdate > 0 ? "+" : "-") + Math.abs(healthUpdate);
                } else {
                    return;
                }
            } else {
                text = "+123";
                color = ChatFormatting.GREEN.getRGB();
            }
        } else if (feature == Feature.DARK_AUCTION_TIMER) { // The timezone of the server, to avoid problems with like timezones that are 30 minutes ahead or whatnot.
            Calendar nextDarkAuction = Calendar.getInstance(TimeZone.getTimeZone("EST"));
            if (nextDarkAuction.get(Calendar.MINUTE) >= 55) {
                nextDarkAuction.add(Calendar.HOUR_OF_DAY, 1);
            }
            nextDarkAuction.set(Calendar.MINUTE, 55);
            nextDarkAuction.set(Calendar.SECOND, 0);
            int difference = (int) (nextDarkAuction.getTimeInMillis() - System.currentTimeMillis());
            int minutes = difference / 60000;
            int seconds = (int) Math.round((double) (difference % 60000) / 1000);
            StringBuilder timestamp = new StringBuilder();
            if (minutes < 10) {
                timestamp.append("0");
            }
            timestamp.append(minutes).append(":");
            if (seconds < 10) {
                timestamp.append("0");
            }
            timestamp.append(seconds);
            text = timestamp.toString();
        } else if (feature == Feature.MAGMA_BOSS_TIMER) {
            StringBuilder magmaBuilder = new StringBuilder();
            magmaBuilder.append(main.getPlayerListener().getMagmaAccuracy().getSymbol());
            EnumUtils.MagmaTimerAccuracy ma = main.getPlayerListener().getMagmaAccuracy();
            if (ma == EnumUtils.MagmaTimerAccuracy.ABOUT || ma == EnumUtils.MagmaTimerAccuracy.EXACTLY) {
                if (buttonLocation == null) {
                    int totalSeconds = main.getPlayerListener().getMagmaTime();
                    if (totalSeconds < 0) totalSeconds = 0;
                    int hours = totalSeconds / 3600;
                    int minutes = totalSeconds / 60 % 60;
                    int seconds = totalSeconds % 60;
                    if (Math.abs(hours) >= 10) hours = 10;
                    magmaBuilder.append(hours).append(":");
                    if (minutes < 10) {
                        magmaBuilder.append("0");
                    }
                    magmaBuilder.append(minutes).append(":");
                    if (seconds < 10) {
                        magmaBuilder.append("0");
                    }
                    magmaBuilder.append(seconds);
                } else {
                    magmaBuilder.append("1:23:45");
                }
            }
            text = magmaBuilder.toString();
        } else if (feature == Feature.SKILL_DISPLAY) {
            if (buttonLocation == null) {
                text = skillText;
                if (text == null) return;
            } else {
                text = "+10 (20,000/50,000)";
            }
            if (buttonLocation == null) {
                int remainingTime = (int) (skillFadeOutTime - System.currentTimeMillis());
                if (remainingTime < 0) {
                    if (remainingTime < -2000) {
                        return; // Will be invisible, no need to render.
                    }

                    textAlpha = (float) 1 - ((float) -remainingTime / 2000);
                    color = main.getConfigValues().getColor(feature, Math.round(textAlpha * 255 >= 4 ? textAlpha * 255 : 4)).getRGB(); // so it fades out, 0.016 is the minimum alpha
                }
            }
        } else if(feature == Feature.ZEALOT_COUNTER) {
        	if(main.getUtils().getLocation() != Location.DRAGONS_NEST && buttonLocation == null) return;
        	text = String.valueOf(main.getPersistentValues().getKills());
        } else if(feature == Feature.SHOW_TOTAL_ZEALOT_COUNT) {
            if(main.getUtils().getLocation() != Location.DRAGONS_NEST && buttonLocation == null) return;
            if (main.getPersistentValues().getTotalKills() <= 0) {
                text = String.valueOf(main.getPersistentValues().getKills());
            } else {
                text = String.valueOf(main.getPersistentValues().getTotalKills()+main.getPersistentValues().getKills());
            }
        } else if(feature == Feature.SHOW_SUMMONING_EYE_COUNT) {
            if(main.getUtils().getLocation() != Location.DRAGONS_NEST && buttonLocation == null) return;
            text = String.valueOf(main.getPersistentValues().getSummoningEyeCount());
        } else if(feature == Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE) {
            if(main.getUtils().getLocation() != Location.DRAGONS_NEST && buttonLocation == null) return;
            int summoningEyeCount = main.getPersistentValues().getSummoningEyeCount();

            if (summoningEyeCount > 0) {
                text = String.valueOf(Math.round(main.getPersistentValues().getTotalKills() / (double)main.getPersistentValues().getSummoningEyeCount()));
            } else {
                text = "0"; // Avoid zero division.
            }
        } else if (feature == Feature.BIRCH_PARK_RAINMAKER_TIMER) {
            long rainmakerTime = main.getPlayerListener().getRainmakerTimeEnd();

            if ((main.getUtils().getLocation() != Location.BIRCH_PARK || rainmakerTime == -1) && buttonLocation == null) {
                return;
            }

            int totalSeconds = (int)(rainmakerTime-System.currentTimeMillis())/1000;
            if (rainmakerTime != -1 && totalSeconds > 0) {
                StringBuilder timerBuilder = new StringBuilder();

                int hours = totalSeconds / 3600;
                int minutes = totalSeconds / 60 % 60;
                int seconds = totalSeconds % 60;

                if (hours > 0) {
                    timerBuilder.append(hours).append(":");
                }
                if (minutes < 10 && hours > 0) {
                    timerBuilder.append("0");
                }
                timerBuilder.append(minutes).append(":");
                if (seconds < 10) {
                    timerBuilder.append("0");
                }
                timerBuilder.append(seconds);

                text = timerBuilder.toString();
            } else {
                if (buttonLocation == null) {
                    return;
                }

                text = "1:23";
            }
        } else if (feature == Feature.COMBAT_TIMER_DISPLAY) {
            long lastDamaged = main.getUtils().getLastDamaged()+5000;
            int combatSeconds = (int)Math.ceil((lastDamaged-System.currentTimeMillis())/1000D);

            if (combatSeconds <= 0 && buttonLocation == null) {
                return;
            }

            text = "IN COMBAT";
        } else if (feature == Feature.ENDSTONE_PROTECTOR_DISPLAY) {
            if (((main.getUtils().getLocation() != Location.THE_END && main.getUtils().getLocation() != Location.DRAGONS_NEST)
                    || EndstoneProtectorManager.getMinibossStage() == null || !EndstoneProtectorManager.isCanDetectSkull()) && buttonLocation == null) {
                return;
            }

            EndstoneProtectorManager.Stage stage = EndstoneProtectorManager.getMinibossStage();

            if (buttonLocation != null && stage == null) {
                stage = EndstoneProtectorManager.Stage.STAGE_3;
            }

            int stageNum = Math.min(stage.ordinal(), 5);
            text = Message.MESSAGE_STAGE.getMessage(String.valueOf(stageNum));
        } else {
            return;
        }
        float x = main.getConfigValues().getActualX(feature);
        float y = main.getConfigValues().getActualY(feature);

        int height = 7;
        int width = MinecraftReflection.FontRenderer.getStringWidth(text);

        // Constant width ovverrides for some features.
        if (feature == Feature.ZEALOT_COUNTER || feature == Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE) {
            width = MinecraftReflection.FontRenderer.getStringWidth("500");
        } else if (feature == Feature.SHOW_TOTAL_ZEALOT_COUNT) {
            width = MinecraftReflection.FontRenderer.getStringWidth("30000");
        } else if (feature == Feature.SHOW_SUMMONING_EYE_COUNT) {
            width = MinecraftReflection.FontRenderer.getStringWidth("100");
        }

        x -= Math.round(width * scale / 2);
        y -= Math.round(height * scale / 2);
        x /= scale;
        y /= scale;
        int intX = Math.round(x);
        int intY = Math.round(y);
        if (buttonLocation != null) {
            int boxXOne = intX - 4;
            int boxXTwo = intX + width + 4;
            int boxYOne = intY - 4;
            int boxYTwo = intY + height + 4;
            if (feature == Feature.MAGMA_BOSS_TIMER || feature == Feature.DARK_AUCTION_TIMER || feature == Feature.ZEALOT_COUNTER || feature == Feature.SKILL_DISPLAY
            || feature == Feature.SHOW_TOTAL_ZEALOT_COUNT || feature == Feature.SHOW_SUMMONING_EYE_COUNT || feature == Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE ||
            feature == Feature.BIRCH_PARK_RAINMAKER_TIMER || feature == Feature.COMBAT_TIMER_DISPLAY || feature == Feature.ENDSTONE_PROTECTOR_DISPLAY) {
                boxXOne -= 18;
                boxYOne -= 2;
            }

            if (feature == Feature.ENDSTONE_PROTECTOR_DISPLAY) {
                boxXTwo += 16+2+mc.fontRendererObj.getStringWidth(String.valueOf(EndstoneProtectorManager.getZealotCount()));
            }

            if (feature == Feature.COMBAT_TIMER_DISPLAY) {
                boxYTwo += 15;
            }
            buttonLocation.checkHoveredAndDrawBox(boxXOne, boxXTwo, boxYOne, boxYTwo, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        GlStateManager.enableBlend();
        ChromaManager.renderingText(feature);
        main.getUtils().drawTextWithStyle(text, intX, intY, color, textAlpha);
        ChromaManager.doneRenderingText();

        GlStateManager.color(1, 1, 1, 1);
        if (feature == Feature.DARK_AUCTION_TIMER) {
            mc.getTextureManager().bindTexture(SIRIUS_ICON);
            Gui.drawModalRectWithCustomSizedTexture(intX - 18, intY - 5, 0, 0, 16, 16, 16, 16);
        } else if (feature == Feature.MAGMA_BOSS_TIMER) {
            mc.getTextureManager().bindTexture(MAGMA_BOSS_ICON);
            Gui.drawModalRectWithCustomSizedTexture(intX - 18, intY - 5, 0, 0, 16, 16, 16, 16);
        } else if (feature == Feature.ZEALOT_COUNTER) {
            mc.getTextureManager().bindTexture(ENDERMAN_ICON);
            Gui.drawModalRectWithCustomSizedTexture(intX - 18, intY - 5, 0, 0, 16, 16, 16, 16);
        } else if (feature == Feature.SHOW_TOTAL_ZEALOT_COUNT) {
            mc.getTextureManager().bindTexture(ENDERMAN_GROUP_ICON);
            Gui.drawModalRectWithCustomSizedTexture(intX - 18, intY - 5, 0, 0, 16, 16, 16, 16);
        } else if (feature == Feature.SHOW_SUMMONING_EYE_COUNT) {
            mc.getTextureManager().bindTexture(SUMMONING_EYE_ICON);
            Gui.drawModalRectWithCustomSizedTexture(intX - 18, intY - 5, 0, 0, 16, 16, 16, 16);
        } else if (feature == Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE) {
            mc.getTextureManager().bindTexture(ZEALOTS_PER_EYE_ICON);
            Gui.drawModalRectWithCustomSizedTexture(intX - 18, intY - 5, 0, 0, 16, 16, 16, 16);
            mc.getTextureManager().bindTexture(SLASH_ICON);
            main.getUtils().bindRGBColor(color);
            Gui.drawModalRectWithCustomSizedTexture(intX - 18, intY - 5, 0, 0, 16, 16, 16, 16);
        } else if (feature == Feature.SKILL_DISPLAY && ((skill != null && skill.getItem() != null) || buttonLocation != null)) {
            GlStateManager.enableRescaleNormal();
            RenderHelper.enableGUIStandardItemLighting();
            if (!(mc.currentScreen instanceof GuiChat)) {
                if (buttonLocation != null || textAlpha > 0.1) {
                    mc.getRenderItem().renderItemIntoGUI(buttonLocation == null ? skill.getItem() : EnumUtils.SkillType.FARMING.getItem(), intX - 18, intY - 5);
                }
            }
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
        } else if (feature == Feature.BIRCH_PARK_RAINMAKER_TIMER) {
            GlStateManager.enableRescaleNormal();
            RenderHelper.enableGUIStandardItemLighting();

            mc.getRenderItem().renderItemIntoGUI(WATER_BUCKET, intX - 18, intY - 5);

            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
        } else if (feature == Feature.COMBAT_TIMER_DISPLAY) {
            GlStateManager.enableRescaleNormal();
            RenderHelper.enableGUIStandardItemLighting();

            long lastDamaged = main.getUtils().getLastDamaged()+5000;
            int combatSeconds = (int)Math.ceil((lastDamaged-System.currentTimeMillis())/1000D);

            if (buttonLocation != null) {
                combatSeconds = 5;
            }

            int iconX = intX - 18;

            mc.getRenderItem().renderItemIntoGUI(IRON_SWORD, iconX, intY - 5);
            intY += 15;
            int totalWidth = width + 16 + 2; // 2 spacer and 16 sword icon.

            String warpTimeRemaining = combatSeconds+"s";
            String menuTimeRemaining = (combatSeconds-2)+"s";
            if (combatSeconds <= 2) {
                menuTimeRemaining = "✔";
            }
            int menuTimeRemainingWidth = MinecraftReflection.FontRenderer.getStringWidth(menuTimeRemaining);

            int spacerBetweenBothItems = 4;
            int spacerBetweenItemsAndText = 2;

            mc.getRenderItem().renderItemIntoGUI(NETHER_STAR, iconX + totalWidth/2 - 16-menuTimeRemainingWidth - spacerBetweenItemsAndText - spacerBetweenBothItems/2, intY - 5);
            ChromaManager.renderingText(feature);
            main.getUtils().drawTextWithStyle(menuTimeRemaining, iconX + totalWidth/2-menuTimeRemainingWidth - spacerBetweenBothItems/2, intY, color, textAlpha);
            ChromaManager.doneRenderingText();

            GlStateManager.color(1,1,1,1);
            mc.getTextureManager().bindTexture(WARP_ICON);
            Gui.drawModalRectWithCustomSizedTexture(iconX + totalWidth/2 + spacerBetweenBothItems/2, intY - 3, 0, 0, 13, 13, 13, 13);
            ChromaManager.renderingText(feature);
            main.getUtils().drawTextWithStyle(warpTimeRemaining, iconX + totalWidth/2 + spacerBetweenBothItems/2+13+spacerBetweenItemsAndText, intY, color, textAlpha);
            ChromaManager.doneRenderingText();
        } else if (feature == Feature.ENDSTONE_PROTECTOR_DISPLAY) {
            mc.getTextureManager().bindTexture(IRON_GOLEM_ICON);
            Gui.drawModalRectWithCustomSizedTexture(intX - 18, intY - 5, 0, 0, 16, 16, 16, 16);

            intX += mc.fontRendererObj.getStringWidth(text)+2;

            mc.getTextureManager().bindTexture(ENDERMAN_GROUP_ICON);
            Gui.drawModalRectWithCustomSizedTexture(intX, intY - 5, 0, 0, 16, 16, 16, 16);

            int count = EndstoneProtectorManager.getZealotCount();

            ChromaManager.renderingText(feature);
            main.getUtils().drawTextWithStyle(String.valueOf(count), intX+16+2, intY, color, textAlpha);
            ChromaManager.doneRenderingText();
        }
    }

    private static final SlayerArmorProgress[] DUMMY_PROGRESSES = new SlayerArmorProgress[]{new SlayerArmorProgress(new ItemStack(Items.diamond_boots)),
            new SlayerArmorProgress(new ItemStack(Items.chainmail_leggings)), new SlayerArmorProgress(new ItemStack(Items.diamond_chestplate)), new SlayerArmorProgress(new ItemStack(Items.leather_helmet))};

    public void drawRevenantIndicator(float scale, Minecraft mc, ButtonLocation buttonLocation) {
        float x = main.getConfigValues().getActualX(Feature.SLAYER_INDICATOR);
        float y = main.getConfigValues().getActualY(Feature.SLAYER_INDICATOR);

        int longest = -1;
        SlayerArmorProgress[] progresses = main.getInventoryUtils().getSlayerArmorProgresses();
        if (buttonLocation != null) progresses = DUMMY_PROGRESSES;
        for (SlayerArmorProgress progress : progresses) {
            if (progress == null) continue;

            int textWidth = MinecraftReflection.FontRenderer.getStringWidth(progress.getPercent()+"% ("+progress.getDefence()+")");
            if (textWidth > longest) {
                longest = textWidth;
            }
        }
        if (longest == -1) return;

        int height = 15 * 4;
        int width = longest + 15;
        x -= Math.round(width * scale / 2);
        y -= Math.round(height * scale / 2);
        x /= scale;
        y /= scale;
        int intX = Math.round(x);
        int intY = Math.round(y);
        if (buttonLocation != null) {
            int boxXOne = intX - 4;
            int boxXTwo = intX + width + 4;
            int boxYOne = intY - 4;
            int boxYTwo = intY + height + 4;
            buttonLocation.checkHoveredAndDrawBox(boxXOne, boxXTwo, boxYOne, boxYTwo, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        EnumUtils.AnchorPoint anchorPoint = main.getConfigValues().getAnchorPoint(Feature.SLAYER_INDICATOR);
        boolean downwards = (anchorPoint == EnumUtils.AnchorPoint.TOP_LEFT || anchorPoint == EnumUtils.AnchorPoint.TOP_RIGHT);

        int color = main.getConfigValues().getColor(Feature.SLAYER_INDICATOR).getRGB();

        int drawnCount = 0;
        for (int armorPiece = 3; armorPiece >= 0; armorPiece--) {
            SlayerArmorProgress progress = progresses[downwards ? armorPiece : 3 - armorPiece];
            if (progress == null) continue;

            int fixedY;
            if (downwards) {
                fixedY = intY + drawnCount * 15;
            } else {
                fixedY = (intY + 45) - drawnCount * 15;
            }
            drawItemStack(mc, progress.getItemStack(), intX - 2, fixedY);

            int currentX = intX + 17;
            ChromaManager.renderingText(Feature.SLAYER_INDICATOR);
            main.getUtils().drawTextWithStyle(progress.getPercent()+"% (", currentX, fixedY + 5, color);
            ChromaManager.doneRenderingText();

            currentX += MinecraftReflection.FontRenderer.getStringWidth(progress.getPercent()+"% (");
            main.getUtils().drawTextWithStyle(progress.getDefence(), currentX, fixedY + 5, 0xFFFFFFFF);

            currentX += MinecraftReflection.FontRenderer.getStringWidth(progress.getDefence());
            ChromaManager.renderingText(Feature.SLAYER_INDICATOR);
            main.getUtils().drawTextWithStyle(")", currentX, fixedY + 5, color);
            ChromaManager.doneRenderingText();

            drawnCount++;
        }
    }

    public void drawPotionEffectTimers(float scale, ButtonLocation buttonLocation){
        float x = main.getConfigValues().getActualX(Feature.TAB_EFFECT_TIMERS);
        float y = main.getConfigValues().getActualY(Feature.TAB_EFFECT_TIMERS);

        TabEffectManager tabEffect = TabEffectManager.getInstance();

        List<TabEffect> potionTimers = tabEffect.getPotionTimers();
        List<TabEffect> powerupTimers = tabEffect.getPowerupTimers();

        if(potionTimers.isEmpty() && powerupTimers.isEmpty()) {
            if (buttonLocation == null) {
                return;
            } else { // When editing GUI draw dummy timers.
                potionTimers = TabEffectManager.getDummyPotionTimers();
                powerupTimers = TabEffectManager.getDummyPowerupTimers();
            }
        }

        EnumUtils.AnchorPoint anchorPoint = main.getConfigValues().getAnchorPoint(Feature.TAB_EFFECT_TIMERS);
        boolean topDown = (anchorPoint == EnumUtils.AnchorPoint.TOP_LEFT || anchorPoint == EnumUtils.AnchorPoint.TOP_RIGHT);

        int totalEffects = potionTimers.size() + powerupTimers.size();
        int spacer = (!potionTimers.isEmpty() && !powerupTimers.isEmpty()) ? 3 : 0;

        int height = (totalEffects * 9) + spacer; //9 px per effect + 3px spacer between Potions and Powerups if both exist
        int width = 156; //String width of "Enchanting XP Boost III 1:23:45"
        x-=Math.round(width*scale/2);
        y-=Math.round(height*scale/2)-(totalEffects * 4);
        x/=scale;
        y/=scale;
        int intX = Math.round(x);
        int intY = Math.round(y);
        if (buttonLocation != null) {
            int boxXOne = intX-4;
            int boxXTwo = intX+width+4;
            int boxYOffset = topDown ? 0 : (1-totalEffects) * 9;
            int boxYOne = intY-4 + boxYOffset;
            int boxYTwo = intY+4+height - 3 + boxYOffset;
            buttonLocation.checkHoveredAndDrawBox(boxXOne, boxXTwo, boxYOne, boxYTwo, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        boolean alignRight = (anchorPoint == EnumUtils.AnchorPoint.TOP_RIGHT || anchorPoint == EnumUtils.AnchorPoint.BOTTOM_RIGHT);

        Color color = main.getConfigValues().getColor(Feature.TAB_EFFECT_TIMERS);

        int drawnCount = 0;
        for(TabEffect potion : potionTimers){
            int fixedY = intY + (topDown ? 0 : spacer) + (drawnCount * 9);

            String effect = potion.getEffect();
            String duration = potion.getDurationForDisplay();

            if (alignRight) {
                ChromaManager.renderingText(Feature.TAB_EFFECT_TIMERS);
                main.getUtils().drawTextWithStyle(duration+" ", intX + width - MinecraftReflection.FontRenderer.getStringWidth(duration+" ")
                        - MinecraftReflection.FontRenderer.getStringWidth(effect.trim()), fixedY, color.getRGB());
                ChromaManager.doneRenderingText();
                main.getUtils().drawTextWithStyle(effect.trim(), intX + width - MinecraftReflection.FontRenderer.getStringWidth(effect.trim()), fixedY, color.getRGB());
            } else {
                main.getUtils().drawTextWithStyle(effect, intX, fixedY, color.getRGB());
                ChromaManager.renderingText(Feature.TAB_EFFECT_TIMERS);
                main.getUtils().drawTextWithStyle(duration, intX+MinecraftReflection.FontRenderer.getStringWidth(effect), fixedY, color.getRGB());
                ChromaManager.doneRenderingText();
            }
            drawnCount += topDown ? 1 : -1;
        }
        for(TabEffect powerUp : powerupTimers){
            int fixedY = intY + (topDown ? spacer : 0) + (drawnCount * 9);

            String effect = powerUp.getEffect();
            String duration = powerUp.getDurationForDisplay();

            if (alignRight) {
                ChromaManager.renderingText(Feature.TAB_EFFECT_TIMERS);
                main.getUtils().drawTextWithStyle(duration+" ", intX + width - MinecraftReflection.FontRenderer.getStringWidth(duration+" ")
                        - MinecraftReflection.FontRenderer.getStringWidth(effect.trim()), fixedY, color.getRGB());
                ChromaManager.doneRenderingText();
                main.getUtils().drawTextWithStyle(effect, intX + width - MinecraftReflection.FontRenderer.getStringWidth(effect.trim()), fixedY, color.getRGB());
            } else {
                main.getUtils().drawTextWithStyle(effect, intX, fixedY, color.getRGB());
                ChromaManager.renderingText(Feature.TAB_EFFECT_TIMERS);
                main.getUtils().drawTextWithStyle(duration, intX+MinecraftReflection.FontRenderer.getStringWidth(effect), fixedY, color.getRGB());
                ChromaManager.doneRenderingText();
            }
            drawnCount += topDown ? 1 : -1;
        }
    }

    private void drawItemStack(Minecraft mc, ItemStack item, int x, int y) {
        RenderHelper.enableGUIStandardItemLighting();
        mc.getRenderItem().renderItemIntoGUI(item, x, y);
        RenderHelper.disableStandardItemLighting();
    }

    private static List<ItemDiff> DUMMY_PICKUP_LOG = new ArrayList<>(Arrays.asList(new ItemDiff(ChatFormatting.DARK_PURPLE + "Forceful Ember Chestplate", 1),
            new ItemDiff("Boat", -1), new ItemDiff(ChatFormatting.BLUE + "Aspect of the End", 1)));

    public void drawItemPickupLog(float scale, ButtonLocation buttonLocation) {
        float x = main.getConfigValues().getActualX(Feature.ITEM_PICKUP_LOG);
        float y = main.getConfigValues().getActualY(Feature.ITEM_PICKUP_LOG);

        EnumUtils.AnchorPoint anchorPoint = main.getConfigValues().getAnchorPoint(Feature.ITEM_PICKUP_LOG);
        boolean downwards = anchorPoint == EnumUtils.AnchorPoint.TOP_RIGHT || anchorPoint == EnumUtils.AnchorPoint.TOP_LEFT;

        int height = 8 * 3;
        int width = MinecraftReflection.FontRenderer.getStringWidth("+ 1x Forceful Ember Chestplate");
        x -= Math.round(width * scale / 2);
        y -= Math.round(height * scale / 2);
        x /= scale;
        y /= scale;
        int intX = Math.round(x);
        int intY = Math.round(y);
        if (buttonLocation != null) {
            int boxXOne = intX - 4;
            int boxXTwo = intX + width + 4;
            int boxYOne = intY - 4;
            int boxYTwo = intY + height + 4;
            buttonLocation.checkHoveredAndDrawBox(boxXOne, boxXTwo, boxYOne, boxYTwo, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
        int i = 0;
        Collection<ItemDiff> log = main.getInventoryUtils().getItemPickupLog();
        if (buttonLocation != null) {
            log = DUMMY_PICKUP_LOG;
        }
        for (ItemDiff itemDiff : log) {
            String text = String.format("%s %sx §r%s", itemDiff.getAmount() > 0 ? "§a+" : "§c-",
                    Math.abs(itemDiff.getAmount()), itemDiff.getDisplayName());
            int stringY = intY + (i * MinecraftReflection.FontRenderer.getFontHeight());
            if (!downwards) {
                stringY = intY - (i * MinecraftReflection.FontRenderer.getFontHeight());
                stringY += 18;
            }

            GlStateManager.enableBlend();
            main.getUtils().drawTextWithStyle(text, intX, stringY, ChatFormatting.WHITE);
            i++;
        }
    }

    public void drawPowerOrbStatus(Minecraft mc, float scale, ButtonLocation buttonLocation) {
        PowerOrbManager.PowerOrbEntry activePowerOrb = PowerOrbManager.getInstance().getActivePowerOrb();
        if (buttonLocation != null) {
            activePowerOrb = PowerOrbManager.DUMMY_POWER_ORB_ENTRY;
        }
        if (activePowerOrb != null) {
            PowerOrb powerOrb = activePowerOrb.getPowerOrb();
            int seconds = activePowerOrb.getSeconds();

            EnumUtils.PowerOrbDisplayStyle displayStyle = main.getConfigValues().getPowerOrbDisplayStyle();
            if (displayStyle == EnumUtils.PowerOrbDisplayStyle.DETAILED) {
                drawDetailedPowerOrbStatus(mc, scale, buttonLocation, powerOrb, seconds);
            } else {
                drawCompactPowerOrbStatus(mc, scale, buttonLocation, powerOrb, seconds);
            }
        }
    }

    /**
     * Displays the power orb display in a compact way with only the amount of seconds to the right of the icon.
     *
     *  --
     * |  | XXs
     *  --
     */
    private void drawCompactPowerOrbStatus(Minecraft mc, float scale, ButtonLocation buttonLocation, PowerOrb powerOrb, int seconds) {
        float x = main.getConfigValues().getActualX(Feature.POWER_ORB_STATUS_DISPLAY);
        float y = main.getConfigValues().getActualY(Feature.POWER_ORB_STATUS_DISPLAY);

        String secondsString = String.format("§e%ss", seconds);
        int spacing = 1;
        int iconSize = MinecraftReflection.FontRenderer.getFontHeight() * 3; // 3 because it looked the best
        int width = iconSize + spacing + MinecraftReflection.FontRenderer.getStringWidth(secondsString);
        // iconSize also acts as height
        x -= Math.round(width * scale / 2);
        y -= Math.round(iconSize * scale / 2);
        x /= scale;
        y /= scale;
        int intX = Math.round(x);
        int intY = Math.round(y);

        if (buttonLocation != null) {
            int boxXOne = intX - 4;
            int boxXTwo = intX + width + 4;
            int boxYOne = intY - 4;
            int boxYTwo = intY + iconSize + 4;
            buttonLocation.checkHoveredAndDrawBox(boxXOne, boxXTwo, boxYOne, boxYTwo, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(powerOrb.getResourceLocation());
        GlStateManager.color(1, 1, 1, 1F);
        Gui.drawModalRectWithCustomSizedTexture(intX, intY, 0, 0, iconSize, iconSize, iconSize, iconSize);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();

        main.getUtils().drawTextWithStyle(secondsString, intX + iconSize, intY + (iconSize / 2) - (MinecraftReflection.FontRenderer.getFontHeight() / 2), ChatFormatting.WHITE.getColor(255).getRGB());
    }

    /**
     * Displays the power orb with detailed stats about the boost you're receiving.
     *
     *  --  +X ❤/s
     * |  | +X ✎/s
     *  --  +X ❁
     *  XXs
     */
    private void drawDetailedPowerOrbStatus(Minecraft mc, float scale, ButtonLocation buttonLocation, PowerOrb powerOrb, int seconds) {
        float x = main.getConfigValues().getActualX(Feature.POWER_ORB_STATUS_DISPLAY);
        float y = main.getConfigValues().getActualY(Feature.POWER_ORB_STATUS_DISPLAY);

        int maxHealth = main.getUtils().getAttributes().get(Attribute.MAX_HEALTH).getValue();
        double healthRegen = maxHealth * powerOrb.getHealthRegen();
        double healIncrease = powerOrb.getHealIncrease() * 100;

        List<String> display = new LinkedList<>();
        display.add(String.format("§c+%s ❤/s", TextUtils.formatDouble(healthRegen)));
        if(powerOrb.getManaRegen() > 0) {
            int maxMana = main.getUtils().getAttributes().get(Attribute.MAX_MANA).getValue();
            double manaRegen = maxMana / 50;
            manaRegen = manaRegen + manaRegen * powerOrb.getManaRegen();
            display.add(String.format("§b+%s ✎/s", TextUtils.formatDouble(manaRegen)));
        }
        if (powerOrb.getStrength() > 0) {
            display.add(String.format("§4+%d ❁", powerOrb.getStrength()));
        }
        if (healIncrease > 0) {
            display.add(String.format("§2+%s%% Healing", TextUtils.formatDouble(healIncrease)));
        }

        Optional<String> longestLine = display.stream().max(Comparator.comparingInt(String::length));

        int spacingBetweenLines = 1;
        int iconSize = MinecraftReflection.FontRenderer.getFontHeight() * 3; // 3 because it looked the best
        int iconAndSecondsHeight = iconSize + MinecraftReflection.FontRenderer.getFontHeight();

        int effectsHeight = (MinecraftReflection.FontRenderer.getFontHeight() + spacingBetweenLines) * display.size();
        int width = iconSize + longestLine.map(MinecraftReflection.FontRenderer::getStringWidth)
                .orElseGet(() -> MinecraftReflection.FontRenderer.getStringWidth(display.get(0)));
        int height = Math.max(effectsHeight, iconAndSecondsHeight);
        x -= Math.round(width * scale / 2);
        y -= Math.round(24 * scale / 2);
        x /= scale;
        y /= scale;
        int intX = Math.round(x);
        int intY = Math.round(y);

        if (buttonLocation != null) {
            int boxXOne = intX - 4;
            int boxXTwo = intX + width + 4;
            int boxYOne = intY - 4;
            int boxYTwo = intY + height + 4;
            buttonLocation.checkHoveredAndDrawBox(boxXOne, boxXTwo, boxYOne, boxYTwo, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        mc.getTextureManager().bindTexture(powerOrb.getResourceLocation());
        GlStateManager.color(1, 1, 1, 1F);
        Gui.drawModalRectWithCustomSizedTexture(intX, intY, 0, 0, iconSize, iconSize, iconSize, iconSize);
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();

        String secondsString = String.format("§e%ss", seconds);
        main.getUtils().drawTextWithStyle(secondsString, intX + (iconSize / 2) - (MinecraftReflection.FontRenderer.getStringWidth(secondsString) / 2), intY + iconSize, ChatFormatting.WHITE.getColor(255).getRGB());

        int startY = Math.round(intY + (iconAndSecondsHeight / 2f) - (effectsHeight / 2f));
        for (int i = 0; i < display.size(); i++) {
            main.getUtils().drawTextWithStyle(display.get(i), intX + iconSize + 3, startY + (i * (MinecraftReflection.FontRenderer.getFontHeight() + spacingBetweenLines)), ChatFormatting.WHITE.getColor(255).getRGB());
        }
    }

    /**
     * Displays the bait list. Only shows bait with count > 0. Copied and modified from <code>drawCompactPowerOrbStatus</code>
     *  ---
     * |min| count
     *  ---
     *  ---
     * |fis| count
     *  ---
     *  etc
     */
    public void drawBaitList(Minecraft mc, float scale, ButtonLocation buttonLocation) {

        int boxXOne = -4261;
        int boxXTwo = -4261;
        int boxYOne = -4261;
        int boxYTwo = -4261;

        if (BaitListManager.getInstance().holdingRod())
            for (Map.Entry<BaitListManager.BaitType, BaitListManager.Set<Integer, Integer>> entry : BaitListManager.getInstance().baitsInInventory.entrySet())
            {
                float x = main.getConfigValues().getActualX(Feature.BAIT_LIST);
                float y = main.getConfigValues().getActualY(Feature.BAIT_LIST);

                String count = "§e" + entry.getValue().getO2();
                int spacing = 1;
                int iconSize = MinecraftReflection.FontRenderer.getFontHeight() * 2;
                int width = iconSize;// + spacing + MinecraftReflection.FontRenderer.getStringWidth(count);

                x -= Math.round(width * scale / 2);
                y -= Math.round(iconSize * scale / 2);
                x /= scale;
                y /= scale;
                int intX = Math.round(x);
                int intY = Math.round(y) + entry.getValue().getO1() * 20;

                if (boxXOne == -4261 || boxXOne > intX - 4)
                    boxXOne = intX - 4;
                if (boxXTwo == -4261 || boxXTwo < intX + width + 4)
                    boxXTwo = intX + width + 4;
                if (boxYOne == -4261 || boxYOne > intY - 4)
                    boxYOne = intY - 4;
                if (boxYTwo == -4261 || boxYTwo < intY + iconSize + 4)
                    boxYTwo = intY + iconSize + 4;

                GlStateManager.disableDepth();
                GlStateManager.enableBlend();
                mc.getTextureManager().bindTexture(entry.getKey().resourceLocation);
                GlStateManager.color(1, 1, 1, 1F);
                Gui.drawModalRectWithCustomSizedTexture(intX, intY, 0, 0, iconSize, iconSize, iconSize, iconSize);
                GlStateManager.disableBlend();
                GlStateManager.enableDepth();

                main.getUtils().drawTextWithStyle(count, intX + spacing + iconSize + 4, intY + (iconSize / 2) - (MinecraftReflection.FontRenderer.getFontHeight() / 2), ChatFormatting.WHITE.getColor(255).getRGB());
            }

        if (buttonLocation != null) {
            if (!BaitListManager.getInstance().holdingRod() || BaitListManager.getInstance().baitsInInventory.isEmpty()) {
                float x = main.getConfigValues().getActualX(Feature.BAIT_LIST);
                float y = main.getConfigValues().getActualY(Feature.BAIT_LIST);

                String count = "§e4261";
                int spacing = 1;
                int iconSize = MinecraftReflection.FontRenderer.getFontHeight() * 2;
                int width = iconSize + spacing + MinecraftReflection.FontRenderer.getStringWidth(count);

                x -= Math.round(width * scale / 2);
                y -= Math.round(iconSize * scale / 2);
                x /= scale;
                y /= scale;
                int intX = Math.round(x);
                int intY = Math.round(y) + 20;

                boxXOne = intX - 4;
                boxXTwo = intX + width + 4;
                boxYOne = intY - 4;
                boxYTwo = intY + iconSize + 4;

                GlStateManager.disableDepth();
                GlStateManager.enableBlend();
                mc.getTextureManager().bindTexture(BaitListManager.BaitType.BLESSED.resourceLocation);
                GlStateManager.color(1, 1, 1, 1F);
                Gui.drawModalRectWithCustomSizedTexture(intX, intY, 0, 0, iconSize, iconSize, iconSize, iconSize);
                GlStateManager.disableBlend();
                GlStateManager.enableDepth();

                main.getUtils().drawTextWithStyle(count, intX + iconSize + 4, intY + (iconSize / 2) - (MinecraftReflection.FontRenderer.getFontHeight() / 2), ChatFormatting.WHITE.getColor(255).getRGB());
            }

            buttonLocation.checkHoveredAndDrawBox(boxXOne, boxXTwo, boxYOne, boxYTwo, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    /**
     * Easily grab an attribute from utils.
     */
    private int getAttribute(Attribute attribute) {
        return main.getUtils().getAttributes().get(attribute).getValue();
    }

    @SubscribeEvent()
    public void onRenderRemoveBars(RenderGameOverlayEvent.Pre e) {
        if (e.type == RenderGameOverlayEvent.ElementType.ALL) {
            if (main.getUtils().isOnSkyblock()) {
                if (main.getConfigValues().isEnabled(Feature.HIDE_FOOD_ARMOR_BAR)) {
                    GuiIngameForge.renderFood = false;
                    GuiIngameForge.renderArmor = false;
                }
                if (main.getConfigValues().isEnabled(Feature.HIDE_HEALTH_BAR)) {
                    GuiIngameForge.renderHealth = false;
                }
                if (main.getConfigValues().isEnabled(Feature.HIDE_PET_HEALTH_BAR)) {
                    GuiIngameForge.renderHealthMount = false;
                }
            } else {
                if (main.getConfigValues().isEnabled(Feature.HIDE_HEALTH_BAR)) {
                    GuiIngameForge.renderHealth = true;
                }
                if (main.getConfigValues().isEnabled(Feature.HIDE_FOOD_ARMOR_BAR)) {
                    GuiIngameForge.renderArmor = true;
                }
            }
        }
    }

    @SubscribeEvent()
    public void onRender(TickEvent.RenderTickEvent e) {
        if (guiToOpen == EnumUtils.GUIType.MAIN) {
            Minecraft.getMinecraft().displayGuiScreen(new SkyblockAddonsGui(main, guiPageToOpen, guiTabToOpen));
        } else if (guiToOpen == EnumUtils.GUIType.EDIT_LOCATIONS) {
            Minecraft.getMinecraft().displayGuiScreen(new LocationEditGui(main, guiPageToOpen, guiTabToOpen));
        } else if (guiToOpen == EnumUtils.GUIType.SETTINGS) {
            Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(main, guiFeatureToOpen, 1, guiPageToOpen, guiTabToOpen, guiFeatureToOpen.getSettings()));
        } else if (guiToOpen == EnumUtils.GUIType.WARP) {
            Minecraft.getMinecraft().displayGuiScreen(new IslandWarpGui());
        }
        guiToOpen = null;
    }


    public void setGuiToOpen(EnumUtils.GUIType guiToOpen) {
        this.guiToOpen = guiToOpen;
    }

    public void setGuiToOpen(EnumUtils.GUIType guiToOpen, int page, EnumUtils.GuiTab tab) {
        this.guiToOpen = guiToOpen;
        guiPageToOpen = page;
        guiTabToOpen = tab;
    }

    public void setGuiToOpen(EnumUtils.GUIType guiToOpen, int page, EnumUtils.GuiTab tab, Feature feature) {
        setGuiToOpen(guiToOpen,page,tab);
        guiFeatureToOpen = feature;
    }

    public void setSubtitleFeature(Feature subtitleFeature) {
        this.subtitleFeature = subtitleFeature; // TODO: check, does this break anything? (arrow)
    }
}
