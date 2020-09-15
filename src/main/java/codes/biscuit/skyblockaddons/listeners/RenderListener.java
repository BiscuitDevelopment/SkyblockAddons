package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.*;
import codes.biscuit.skyblockaddons.features.*;
import codes.biscuit.skyblockaddons.features.dragontracker.DragonTracker;
import codes.biscuit.skyblockaddons.features.dragontracker.DragonType;
import codes.biscuit.skyblockaddons.features.dragontracker.DragonsSince;
import codes.biscuit.skyblockaddons.features.healingcircle.HealingCircle;
import codes.biscuit.skyblockaddons.features.healingcircle.HealingCircleParticle;
import codes.biscuit.skyblockaddons.features.powerorbs.PowerOrb;
import codes.biscuit.skyblockaddons.features.powerorbs.PowerOrbManager;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerBoss;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerDrop;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerTracker;
import codes.biscuit.skyblockaddons.features.tabtimers.TabEffect;
import codes.biscuit.skyblockaddons.features.tabtimers.TabEffectManager;
import codes.biscuit.skyblockaddons.gui.IslandWarpGui;
import codes.biscuit.skyblockaddons.gui.LocationEditGui;
import codes.biscuit.skyblockaddons.gui.SettingsGui;
import codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonLocation;
import codes.biscuit.skyblockaddons.misc.ChromaManager;
import codes.biscuit.skyblockaddons.misc.Updater;
import codes.biscuit.skyblockaddons.misc.scheduler.Scheduler;
import codes.biscuit.skyblockaddons.misc.scheduler.SkyblockRunnable;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import codes.biscuit.skyblockaddons.utils.Utils;
import codes.biscuit.skyblockaddons.utils.objects.IntPair;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.util.Vec4b;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.GuiNotification;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.geom.Point2D;
import java.math.BigDecimal;
import java.util.List;
import java.util.*;

import static net.minecraft.client.gui.Gui.icons;

public class RenderListener {

    private static final ItemStack BONE_ITEM = new ItemStack(Items.bone);
    private static final ResourceLocation BARS = new ResourceLocation("skyblockaddons", "bars.png");
    private static final ResourceLocation DEFENCE_VANILLA = new ResourceLocation("skyblockaddons", "defence.png");
    private static final ResourceLocation IMPERIAL_BARS_FIX = new ResourceLocation("skyblockaddons", "imperialbarsfix.png");
    private static final ResourceLocation TICKER_SYMBOL = new ResourceLocation("skyblockaddons", "ticker.png");

    private static final ResourceLocation ENDERMAN_ICON = new ResourceLocation("skyblockaddons", "icons/enderman.png");
    private static final ResourceLocation ENDERMAN_GROUP_ICON = new ResourceLocation("skyblockaddons", "icons/endermangroup.png");
    private static final ResourceLocation MAGMA_BOSS_ICON = new ResourceLocation("skyblockaddons", "icons/magmaboss.png");
    private static final ResourceLocation SIRIUS_ICON = new ResourceLocation("skyblockaddons", "icons/sirius.png");
    private static final ResourceLocation SUMMONING_EYE_ICON = new ResourceLocation("skyblockaddons", "icons/summoningeye.png");
    private static final ResourceLocation ZEALOTS_PER_EYE_ICON = new ResourceLocation("skyblockaddons", "icons/zealotspereye.png");
    private static final ResourceLocation SLASH_ICON = new ResourceLocation("skyblockaddons", "icons/slash.png");
    private static final ResourceLocation IRON_GOLEM_ICON = new ResourceLocation("skyblockaddons", "icons/irongolem.png");

    private static final ResourceLocation DUNGEON_MAP = new ResourceLocation("skyblockaddons", "dungeonsmap.png");

    private static final ResourceLocation CRITICAL = new ResourceLocation("skyblockaddons", "critical.png");

    private static final ItemStack WATER_BUCKET = new ItemStack(Items.water_bucket);
    private static final ItemStack IRON_SWORD = new ItemStack(Items.iron_sword);
    private static ItemStack WARP_SKULL;

    private static EntityArmorStand radiantDummyArmorStand;
    private static EntityZombie revenant;
    private static EntitySpider tarantula;
    private static EntityCaveSpider caveSpider;
    private static EntityWolf sven;

    private SkyblockAddons main = SkyblockAddons.getInstance();

    @Getter @Setter private boolean predictHealth;
    @Getter @Setter private boolean predictMana;

    @Setter private boolean updateMessageDisplayed;

    private Feature subtitleFeature;
    @Getter @Setter private Feature titleFeature;

    @Setter private int arrowsLeft;

    @Setter private String cannotReachMobName;

    @Setter private long skillFadeOutTime = -1;
    @Setter private EnumUtils.SkillType skill;
    @Setter private String skillText;

    private EnumUtils.GUIType guiToOpen;
    private int guiPageToOpen = 1;
    private EnumUtils.GuiTab guiTabToOpen = EnumUtils.GuiTab.MAIN;
    private Feature guiFeatureToOpen;

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

        int scaledWidth = scaledResolution.getScaledWidth();
        int scaledHeight = scaledResolution.getScaledHeight();
        if (titleFeature != null) {

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
                case LEGENDARY_SEA_CREATURE_WARNING:
                    message = Message.MESSAGE_LEGENDARY_SEA_CREATURE_WARNING;
                    break;
                case BOSS_APPROACH_ALERT:
                    message = Message.MESSAGE_BOSS_APPROACH_ALERT;
                    break;
            }
            if (message != null) {
                String text = message.getMessage();
                int stringWidth = mc.fontRendererObj.getStringWidth(text);

                float scale = 4; // Scale is normally 4, but if its larger than the screen, scale it down...
                if (stringWidth * scale > (scaledWidth * 0.9F)) {
                    scale = (scaledWidth * 0.9F) / (float) stringWidth;
                }

                GlStateManager.pushMatrix();
                GlStateManager.translate((float) (scaledWidth / 2), (float) (scaledHeight / 2), 0.0F);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, scale); // TODO Check if changing this scale breaks anything...

                ChromaManager.renderingText(titleFeature);
                mc.fontRendererObj.drawString(text, (float) (-mc.fontRendererObj.getStringWidth(text) / 2), -20.0F, main.getConfigValues().getColor(titleFeature).getRGB(), true);
                ChromaManager.doneRenderingText();

                GlStateManager.popMatrix();
                GlStateManager.popMatrix();
            }
        }
        if (subtitleFeature != null) {
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
                int stringWidth = mc.fontRendererObj.getStringWidth(text);

                float scale = 2; // Scale is normally 2, but if its larger than the screen, scale it down...
                if (stringWidth * scale > (scaledWidth * 0.9F)) {
                    scale = (scaledWidth * 0.9F) / (float) stringWidth;
                }

                GlStateManager.pushMatrix();
                GlStateManager.translate((float) (scaledWidth / 2), (float) (scaledHeight / 2), 0.0F);
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, scale);  // TODO Check if changing this scale breaks anything...

                ChromaManager.renderingText(subtitleFeature);
                mc.fontRendererObj.drawString(text, -mc.fontRendererObj.getStringWidth(text) / 2F, -23.0F,
                        main.getConfigValues().getColor(subtitleFeature).getRGB(), true);
                ChromaManager.doneRenderingText();

                GlStateManager.popMatrix();
                GlStateManager.popMatrix();
            }
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
        int width = 23 + barWidthExpansion;

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
                color = ColorCode.DARK_GREEN.getColor();
            } else if (mc.thePlayer.isPotionActive(20/* Wither */)) {
                color = ColorCode.DARK_GRAY.getColor();
            }
        }

        x = transformXY(x, width, scale);
        y = transformXY(y, height, scale);

        main.getUtils().enableStandardGLOptions();

        if (buttonLocation == null) {
            drawModularBar(mc, color, false, x, y + barHeightExpansion / 2F, null, feature, filled, width);
            if (filled > 0) {
                drawModularBar(mc, color, true, x, y + barHeightExpansion / 2F, null, feature, filled, width);
            }
        } else {
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            drawModularBar(mc, color, false, x, y + barHeightExpansion / 2F, buttonLocation, feature, filled, width);
            if (filled > 0) {
                drawModularBar(mc, color, true, x, y + barHeightExpansion / 2F, buttonLocation, feature, filled, width);
            }
        }

        main.getUtils().restoreGLOptions();
    }

    private void drawModularBar(Minecraft mc, Color color, boolean filled, float x, float y, ButtonLocation buttonLocation, Feature feature, int fillWidth, int maxWidth) {
        Gui gui = mc.ingameGUI;
        if (buttonLocation != null) {
            gui = buttonLocation;
        }
        if (color.getRGB() == ColorCode.BLACK.getRGB()) {
            GlStateManager.color(0.25F, 0.25F, 0.25F); // too dark normally
        } else { // a little darker for contrast
            GlStateManager.color(((float) color.getRed() / 255) * 0.9F, ((float) color.getGreen() / 255) * 0.9F, ((float) color.getBlue() / 255) * 0.9F);
        }
        IntPair sizes = main.getConfigValues().getSizes(feature);
        if (!filled) fillWidth = maxWidth;
        drawBarStart(gui, x, y, filled, sizes.getX(), sizes.getY(), fillWidth, color, maxWidth);
    }

    private void drawBarStart(Gui gui, float x, float y, boolean filled, int barWidth, int barHeight, int fillWidth, Color color, int maxWidth) {
        int baseTextureY = filled ? 0 : 6;

//        drawMiddleThreeRows(gui,x+10,y,barHeight,22,baseTextureY,2, fillWidth, 2); // these two lines just fill some gaps in the bar
//        drawMiddleThreeRows(gui,x+11+(barWidth*9),y,barHeight,22,baseTextureY,2, fillWidth, 2);

        drawAllFiveRows(gui, x, y, barHeight, 0, baseTextureY, 11, fillWidth); // This draws the first segment- including the first separator.

        drawBarSeparators(gui, x + 11, y, baseTextureY, barWidth, barHeight, fillWidth); // This draws the rest of the bar, not sure why it's named this...

        if (fillWidth < maxWidth - 1 && fillWidth > 0 && // This just draws a dark line to easily distinguish where the bar's progress is.
                main.getUtils().isUsingDefaultBarTextures()) { // It doesn't always work out nicely when using like custom textures though.
            GlStateManager.color(((float) color.getRed() / 255) * 0.8F, ((float) color.getGreen() / 255) * 0.8F, ((float) color.getBlue() / 255) * 0.8F);
            drawMiddleThreeRows(gui, x + fillWidth, y, barHeight, 11, 6, 2, fillWidth, 2);
        }
    }

    private void drawMiddleBarParts(Gui gui, float x, float y, int baseTextureY, int barWidth, int barHeight, int fillWidth) {
        float endBarX = 0;
        for (int i = 0; i < barWidth; i++) {
            endBarX = x + (i * 10);
            drawAllFiveRows(gui, endBarX, y, barHeight, 12, baseTextureY, 9, fillWidth - 11 - (i * 10));
        }
        drawBarEnd(gui, endBarX + 10, y, baseTextureY, barWidth, barHeight, fillWidth);
    }

    private void drawBarSeparators(Gui gui, float x, float y, int baseTextureY, int barWidth, int barHeight, int fillWidth) {
        for (int i = 0; i <= barWidth; i++) {
            drawMiddleThreeRows(gui, x + (i * 10), y, barHeight, 11, baseTextureY, 1, fillWidth - 11 - (i * 10), 2);
        }
        drawMiddleBarParts(gui, x + 1, y, baseTextureY, barWidth, barHeight, fillWidth);
    }

    private void drawBarEnd(Gui gui, float x, float y, int baseTextureY, int barWidth, int barHeight, int fillWidth) {
        drawAllFiveRows(gui, x, y, barHeight, 22, baseTextureY, 11, fillWidth - 11 - (barWidth * 10));
    }

    private void drawAllFiveRows(Gui gui, float x, float y, int barHeight, int textureX, int baseTextureY, int width, int fillWidth) {
        if (fillWidth > width || baseTextureY >= 8) fillWidth = width;
        gui.drawTexturedModalRect(x, y - barHeight, textureX, baseTextureY, fillWidth, 1);

        drawMiddleThreeRows(gui, x, y, barHeight, textureX, baseTextureY, width, fillWidth, 1);

        gui.drawTexturedModalRect(x, y + 2 + barHeight, textureX, baseTextureY + 4, fillWidth, 1);
    }

    private void drawMiddleThreeRows(Gui gui, float x, float y, int barHeight, int textureX, int baseTextureY, int width, int fillWidth, int rowHeight) {
        if (fillWidth > width || baseTextureY >= 8) fillWidth = width;
        for (int i = 0; i < barHeight; i++) {
            if (rowHeight == 2) { //this means its drawing bar separators, and its a little different
                gui.drawTexturedModalRect(x, y - 1 - i, textureX, baseTextureY, fillWidth, rowHeight);
            } else {
                gui.drawTexturedModalRect(x, y - i, textureX, baseTextureY + 1, fillWidth, rowHeight);
            }
        }

        gui.drawTexturedModalRect(x, y + 1, textureX, baseTextureY + 2, fillWidth, 1);

        for (int i = 0; i < barHeight; i++) {
            gui.drawTexturedModalRect(x, y + 2 + i, textureX, baseTextureY + 3, fillWidth, rowHeight);
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
            main.getUtils().drawCenteredString(title, (int) (halfWidth / scale), (int) (30 / scale), ColorCode.WHITE.getRGB());
            GlStateManager.popMatrix();
            int y = 45;
            for (String line : textList) {
                main.getUtils().drawCenteredString(line, halfWidth, y, ColorCode.WHITE.getRGB());
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

        int height = 16;
        int width = 3 * 16;

        x = transformXY(x, width, scale);
        y = transformXY(y, height, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        main.getUtils().enableStandardGLOptions();

        for (int boneCounter = 0; boneCounter < bones; boneCounter++) {
            renderItem(BONE_ITEM, x + boneCounter * 16, y);
        }

        main.getUtils().restoreGLOptions();
    }

    /**
     * This renders the skeleton bar.
     */
    public void drawScorpionFoilTicker(Minecraft mc, float scale, ButtonLocation buttonLocation) {
        if (buttonLocation != null || main.getPlayerListener().getTickers() != -1) {
            float x = main.getConfigValues().getActualX(Feature.TICKER_CHARGES_DISPLAY);
            float y = main.getConfigValues().getActualY(Feature.TICKER_CHARGES_DISPLAY);

            int height = 9;
            int width = 3 * 11 + 9;

            x = transformXY(x, width, scale);
            y = transformXY(y, height, scale);

            if (buttonLocation != null) {
                buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            }

            main.getUtils().enableStandardGLOptions();

            int maxTickers = (buttonLocation == null) ? main.getPlayerListener().getMaxTickers() : 4;
            for (int tickers = 0; tickers < maxTickers; tickers++) {
                mc.getTextureManager().bindTexture(TICKER_SYMBOL);
                GlStateManager.enableAlpha();
                if (tickers < (buttonLocation == null ? main.getPlayerListener().getTickers() : 3)) {
                    main.getUtils().drawModalRectWithCustomSizedTexture(x + tickers * 11, y, 0, 0, 9, 9, 18, 9, false);
                } else {
                    main.getUtils().drawModalRectWithCustomSizedTexture(x + tickers * 11, y, 9, 0, 9, 9, 18, 9, false);
                }
            }

            main.getUtils().restoreGLOptions();
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

        main.getUtils().enableStandardGLOptions();

        if (buttonLocation == null) {
            float newScale = scale * 1.5F;
            GlStateManager.pushMatrix();
            GlStateManager.scale(newScale, newScale, 1);
            newScale *= scale;
            x -= (float) width * newScale / 2;
            y -= (float) height * newScale / 2;
            mc.ingameGUI.drawTexturedModalRect(x / newScale, y / newScale, 34, 9, width, height);
            GlStateManager.popMatrix();
        } else {
            scale *= (scale / 1.5);
            x -= Math.round((float) width * scale / 2);
            y -= Math.round((float) height * scale / 2);
            x /= scale;
            y /= scale;
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            buttonLocation.drawTexturedModalRect(x, y, 34, 9, width, height);
        }

        main.getUtils().restoreGLOptions();
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
                    color = healthUpdate > 0 ? ColorCode.GREEN.getRGB() : ColorCode.RED.getRGB();
                    text = (healthUpdate > 0 ? "+" : "-") + Math.abs(healthUpdate);
                } else {
                    return;
                }
            } else {
                text = "+123";
                color = ColorCode.GREEN.getRGB();
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
                text = "+10 (20,000/50,000)" + (main.getConfigValues().isEnabled(Feature.ACTIONS_UNTIL_NEXT_LEVEL) ? " - " + Translations.getMessage("messages.actionsLeft", 3000) : "");
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
        } else if (feature == Feature.ZEALOT_COUNTER) {
            if (main.getConfigValues().isEnabled(Feature.ZEALOT_COUNTER_NEST_ONLY) && main.getUtils().getLocation() != Location.DRAGONS_NEST && buttonLocation == null) {
                return;
            }
            text = String.valueOf(main.getPersistentValues().getKills());
        } else if (feature == Feature.SHOW_TOTAL_ZEALOT_COUNT) {
            if (main.getConfigValues().isEnabled(Feature.SHOW_TOTAL_ZEALOT_COUNT_NEST_ONLY) && main.getUtils().getLocation() != Location.DRAGONS_NEST && buttonLocation == null) {
                return;
            }
            if (main.getPersistentValues().getTotalKills() <= 0) {
                text = String.valueOf(main.getPersistentValues().getKills());
            } else {
                text = String.valueOf(main.getPersistentValues().getTotalKills() + main.getPersistentValues().getKills());
            }
        } else if (feature == Feature.SHOW_SUMMONING_EYE_COUNT) {
            if (main.getConfigValues().isEnabled(Feature.SHOW_SUMMONING_EYE_COUNT_NEST_ONLY) && main.getUtils().getLocation() != Location.DRAGONS_NEST && buttonLocation == null) {
                return;
            }
            text = String.valueOf(main.getPersistentValues().getSummoningEyeCount());
        } else if (feature == Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE) {
            if (main.getConfigValues().isEnabled(Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE_NEST_ONLY) && main.getUtils().getLocation() != Location.DRAGONS_NEST && buttonLocation == null) {
                return;
            }
            int summoningEyeCount = main.getPersistentValues().getSummoningEyeCount();

            if (summoningEyeCount > 0) {
                text = String.valueOf(Math.round(main.getPersistentValues().getTotalKills() / (double) main.getPersistentValues().getSummoningEyeCount()));
            } else {
                text = "0"; // Avoid zero division.
            }
        } else if (feature == Feature.BIRCH_PARK_RAINMAKER_TIMER) {
            long rainmakerTime = main.getPlayerListener().getRainmakerTimeEnd();

            if ((main.getUtils().getLocation() != Location.BIRCH_PARK || rainmakerTime == -1) && buttonLocation == null) {
                return;
            }

            int totalSeconds = (int) (rainmakerTime - System.currentTimeMillis()) / 1000;
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
            long lastDamaged = main.getUtils().getLastDamaged() + 5000;
            int combatSeconds = (int) Math.ceil((lastDamaged - System.currentTimeMillis()) / 1000D);

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
        } else if (feature == Feature.SHOW_DUNGEON_MILESTONE) {
            if (buttonLocation == null && !main.getUtils().isInDungeon()) {
                return;
            }

            DungeonMilestone dungeonMilestone = main.getDungeonUtils().getDungeonMilestone();
            if (dungeonMilestone == null) {
                if (buttonLocation != null) {
                    dungeonMilestone = DungeonMilestone.getZeroMilestone(DungeonClass.HEALER);
                } else {
                    return;
                }
            }

            text = "Milestone " + dungeonMilestone.getLevel();
        } else if (feature == Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY) {
            if (buttonLocation == null && !main.getUtils().isInDungeon()) {
                return;
            }

            text = "";
        } else if (feature == Feature.DUNGEON_DEATH_COUNTER) {
            int deaths = 0;

            if (buttonLocation == null) {
                if (!main.getUtils().isInDungeon()) {
                    return;
                } else {
                    deaths = main.getDungeonUtils().getDeathCounter().getDeaths();

                    if (deaths == 0) {
                        return;
                    }
                }
            }

            text = Integer.toString(deaths);
        } else {
            return;
        }
        float x = main.getConfigValues().getActualX(feature);
        float y = main.getConfigValues().getActualY(feature);

        int height = 7;
        int width = mc.fontRendererObj.getStringWidth(text);

        // Constant width overrides for some features.
        if (feature == Feature.ZEALOT_COUNTER || feature == Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE) {
            width = mc.fontRendererObj.getStringWidth("500");
        } else if (feature == Feature.SHOW_TOTAL_ZEALOT_COUNT) {
            width = mc.fontRendererObj.getStringWidth("30000");
        } else if (feature == Feature.SHOW_SUMMONING_EYE_COUNT) {
            width = mc.fontRendererObj.getStringWidth("100");
        }

        if (feature == Feature.MAGMA_BOSS_TIMER || feature == Feature.DARK_AUCTION_TIMER || feature == Feature.ZEALOT_COUNTER || feature == Feature.SKILL_DISPLAY
                || feature == Feature.SHOW_TOTAL_ZEALOT_COUNT || feature == Feature.SHOW_SUMMONING_EYE_COUNT || feature == Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE ||
                feature == Feature.BIRCH_PARK_RAINMAKER_TIMER || feature == Feature.COMBAT_TIMER_DISPLAY || feature == Feature.ENDSTONE_PROTECTOR_DISPLAY ||
                feature == Feature.DUNGEON_DEATH_COUNTER) {
            width += 18;
            height += 9;
        }

        if (feature == Feature.ENDSTONE_PROTECTOR_DISPLAY) {
            width += 2 + 16 + 2 + mc.fontRendererObj.getStringWidth(String.valueOf(EndstoneProtectorManager.getZealotCount()));
        }

        if (feature == Feature.COMBAT_TIMER_DISPLAY) {
            height += 15;
        }

        if (feature == Feature.SHOW_DUNGEON_MILESTONE) {
            width += 18 + 2;
            height += 10;
        }

        if (feature == Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY) {
            int maxNumberWidth = mc.fontRendererObj.getStringWidth("99");
            width = 18 + 2 + maxNumberWidth + 5 + 18 + 2 + maxNumberWidth;
            height = 18 * (int) Math.ceil(EssenceType.values().length / 2F);
        }

        x = transformXY(x, width, scale);
        y = transformXY(y, height, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        main.getUtils().enableStandardGLOptions();

        if (feature == Feature.DARK_AUCTION_TIMER) {
            mc.getTextureManager().bindTexture(SIRIUS_ICON);
            main.getUtils().drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

            ChromaManager.renderingText(feature);
            main.getUtils().drawTextWithStyle(text, x + 18, y + 4, color);
            ChromaManager.doneRenderingText();

        } else if (feature == Feature.MAGMA_BOSS_TIMER) {
            mc.getTextureManager().bindTexture(MAGMA_BOSS_ICON);
            main.getUtils().drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

            ChromaManager.renderingText(feature);
            main.getUtils().drawTextWithStyle(text, x + 18, y + 4, color);
            ChromaManager.doneRenderingText();

        } else if (feature == Feature.ZEALOT_COUNTER) {
            mc.getTextureManager().bindTexture(ENDERMAN_ICON);
            main.getUtils().drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

            ChromaManager.renderingText(feature);
            main.getUtils().drawTextWithStyle(text, x + 18, y + 4, color);
            ChromaManager.doneRenderingText();

        } else if (feature == Feature.SHOW_TOTAL_ZEALOT_COUNT) {
            mc.getTextureManager().bindTexture(ENDERMAN_GROUP_ICON);
            main.getUtils().drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

            ChromaManager.renderingText(feature);
            main.getUtils().drawTextWithStyle(text, x + 18, y + 4, color);
            ChromaManager.doneRenderingText();

        } else if (feature == Feature.SHOW_SUMMONING_EYE_COUNT) {
            mc.getTextureManager().bindTexture(SUMMONING_EYE_ICON);
            main.getUtils().drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

            ChromaManager.renderingText(feature);
            main.getUtils().drawTextWithStyle(text, x + 18, y + 4, color);
            ChromaManager.doneRenderingText();

        } else if (feature == Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE) {
            mc.getTextureManager().bindTexture(ZEALOTS_PER_EYE_ICON);
            main.getUtils().drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);
            mc.getTextureManager().bindTexture(SLASH_ICON);
            main.getUtils().bindRGBColor(color);
            main.getUtils().drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16, true);

            ChromaManager.renderingText(feature);
            main.getUtils().drawTextWithStyle(text, x + 18, y + 4, color);
            ChromaManager.doneRenderingText();

        } else if (feature == Feature.SKILL_DISPLAY && ((skill != null && skill.getItem() != null) || buttonLocation != null)) {
            renderItem(buttonLocation == null ? skill.getItem() : EnumUtils.SkillType.FARMING.getItem(), x, y);

            ChromaManager.renderingText(feature);
            main.getUtils().drawTextWithStyle(text, x + 18, y + 4, color);
            ChromaManager.doneRenderingText();

        } else if (feature == Feature.BIRCH_PARK_RAINMAKER_TIMER) {
            renderItem(WATER_BUCKET, x, y);

            ChromaManager.renderingText(feature);
            main.getUtils().drawTextWithStyle(text, x + 18, y + 4, color);
            ChromaManager.doneRenderingText();

        } else if (feature == Feature.COMBAT_TIMER_DISPLAY) {
            long lastDamaged = main.getUtils().getLastDamaged() + 5000;
            int combatSeconds = (int) Math.ceil((lastDamaged - System.currentTimeMillis()) / 1000D);

            if (buttonLocation != null) {
                combatSeconds = 5;
            }

            renderItem(IRON_SWORD, x, y);

            ChromaManager.renderingText(feature);
            main.getUtils().drawTextWithStyle(text, x + 18, y + 4, color);
            ChromaManager.doneRenderingText();

            y += 20;

            String warpTimeRemaining = combatSeconds + "s";
            String menuTimeRemaining = (combatSeconds - 2) + "s";
            if (combatSeconds <= 2) {
                menuTimeRemaining = "✔";
            }
            int menuTimeRemainingWidth = mc.fontRendererObj.getStringWidth(menuTimeRemaining);

            int spacerBetweenBothItems = 4;
            int spacerBetweenItemsAndText = 2;

            renderItem(getNetherStar(), x + width / 2F - 16 - menuTimeRemainingWidth - spacerBetweenItemsAndText - spacerBetweenBothItems / 2F, y - 5);

            ChromaManager.renderingText(feature);
            main.getUtils().drawTextWithStyle(menuTimeRemaining, x + width / 2F - menuTimeRemainingWidth - spacerBetweenBothItems / 2F, y, color);
            ChromaManager.doneRenderingText();

            GlStateManager.color(1, 1, 1, 1);
            renderItem(getWarpSkull(), x + width / 2F + spacerBetweenBothItems / 2F, y - 5);
            ChromaManager.renderingText(feature);
            main.getUtils().drawTextWithStyle(warpTimeRemaining, x + width / 2F + spacerBetweenBothItems / 2F + 13 + spacerBetweenItemsAndText, y, color);
            ChromaManager.doneRenderingText();

        } else if (feature == Feature.ENDSTONE_PROTECTOR_DISPLAY) {
            mc.getTextureManager().bindTexture(IRON_GOLEM_ICON);
            main.getUtils().drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

            ChromaManager.renderingText(feature);
            main.getUtils().drawTextWithStyle(text, x + 18, y + 4, color);
            ChromaManager.doneRenderingText();

            x += 16 + 2 + mc.fontRendererObj.getStringWidth(text) + 2;

            GlStateManager.color(1, 1, 1, 1);
            mc.getTextureManager().bindTexture(ENDERMAN_GROUP_ICON);
            main.getUtils().drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

            int count = EndstoneProtectorManager.getZealotCount();

            ChromaManager.renderingText(feature);
            main.getUtils().drawTextWithStyle(String.valueOf(count), x + 16 + 2, y + 4, color);
            ChromaManager.doneRenderingText();

        } else if (feature == Feature.SHOW_DUNGEON_MILESTONE) {
            DungeonMilestone dungeonMilestone = main.getDungeonUtils().getDungeonMilestone();
            if (buttonLocation != null) {
                dungeonMilestone = DungeonMilestone.getZeroMilestone(DungeonClass.HEALER);
            }

            renderItem(dungeonMilestone.getDungeonClass().getItem(), x, y);
            ChromaManager.renderingText(feature);
            main.getUtils().drawTextWithStyle(text, x + 18, y, color);
            main.getUtils().drawTextWithStyle(dungeonMilestone.getValue(), x + 18 + mc.fontRendererObj.getStringWidth(text) / 2F
                    - mc.fontRendererObj.getStringWidth(dungeonMilestone.getValue()) / 2F, y + 9, color);
            ChromaManager.doneRenderingText();

        } else if (feature == Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY) {
            Map<EssenceType, Integer> collectedEssences = main.getDungeonUtils().getCollectedEssences();

            float currentX = x;
            float currentY;

            int maxNumberWidth = mc.fontRendererObj.getStringWidth("99");

            int count = 0;
            for (EssenceType essenceType : EssenceType.values()) {
                int value = collectedEssences.getOrDefault(essenceType, 0);
                if (buttonLocation != null) {
                    value = 99;
                } else if (value <= 0) {
                    continue;
                }

                int column = count % 2;
                int row = count / 2;

                if (column == 0) {
                    currentX = x;
                } else if (column == 1) {
                    currentX = x + 18 + 2 + maxNumberWidth + 5;
                }
                currentY = y + row * 18;

                GlStateManager.color(1, 1, 1, 1);
                mc.getTextureManager().bindTexture(essenceType.getResourceLocation());
                main.getUtils().drawModalRectWithCustomSizedTexture(currentX, currentY, 0, 0, 16, 16, 16, 16);

                ChromaManager.renderingText(feature);
                main.getUtils().drawTextWithStyle(String.valueOf(value), currentX + 18 + 2, currentY + 5, color);
                ChromaManager.doneRenderingText();

                count++;
            }
        } else if (feature == Feature.DUNGEON_DEATH_COUNTER) {
            renderItem(DungeonDeathCounter.SKULL_ITEM, x, y);
            ChromaManager.renderingText(feature);
            main.getUtils().drawTextWithStyle(text, x + 18, y + 4, color);
            ChromaManager.doneRenderingText();
        } else {
            ChromaManager.renderingText(feature);
            main.getUtils().drawTextWithStyle(text, x, y, color);
            ChromaManager.doneRenderingText();
        }

        main.getUtils().restoreGLOptions();
    }

    /**
     * Displays the bait list. Only shows bait with count > 0.
     */
    public void drawBaitList(Minecraft mc, float scale, ButtonLocation buttonLocation) {
        if (!BaitManager.getInstance().isHoldingRod() && buttonLocation == null) return;

        Map<BaitManager.BaitType, Integer> baits = BaitManager.getInstance().getBaitsInInventory();
        if (buttonLocation != null) {
            baits = BaitManager.DUMMY_BAITS;
        }

        int longestLineWidth = 0;
        for (Map.Entry<BaitManager.BaitType, Integer> entry : baits.entrySet()) {
            longestLineWidth = Math.max(longestLineWidth, Minecraft.getMinecraft().fontRendererObj.getStringWidth(String.valueOf(entry.getValue())));
        }

        float x = main.getConfigValues().getActualX(Feature.BAIT_LIST);
        float y = main.getConfigValues().getActualY(Feature.BAIT_LIST);

        int spacing = 1;
        int iconSize = 16;
        int width = iconSize + spacing + longestLineWidth;
        int height = iconSize * baits.size();

        x = transformXY(x, width, scale);
        y = transformXY(y, height, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
        }

        main.getUtils().enableStandardGLOptions();

        for (Map.Entry<BaitManager.BaitType, Integer> entry : baits.entrySet()) {
            if (entry.getValue() == 0) continue;

            GlStateManager.color(1, 1, 1, 1F);
            mc.getTextureManager().bindTexture(entry.getKey().getResourceLocation());
            main.getUtils().drawModalRectWithCustomSizedTexture(x, y, 0, 0, iconSize, iconSize, iconSize, iconSize);

            int color = main.getConfigValues().getColor(Feature.BAIT_LIST).getRGB();
            ChromaManager.renderingText(Feature.BAIT_LIST);
            main.getUtils().drawTextWithStyle(String.valueOf(entry.getValue()), x + iconSize + spacing, y + (iconSize / 2F) - (8 / 2F), color);
            ChromaManager.doneRenderingText();

            y += iconSize;
        }

        main.getUtils().restoreGLOptions();
    }

    public void drawSlayerTrackers(Feature feature, Minecraft mc, float scale, ButtonLocation buttonLocation) {
        boolean colorByRarity;
        boolean textMode;
        SlayerBoss slayerBoss;
        if (feature == Feature.REVENANT_SLAYER_TRACKER) {
            if (buttonLocation == null && main.getConfigValues().isEnabled(Feature.HIDE_WHEN_NOT_IN_CRYPTS) && main.getUtils().getSlayerQuest() != EnumUtils.SlayerQuest.REVENANT_HORROR &&
                    main.getUtils().getLocation() != Location.GRAVEYARD && main.getUtils().getLocation() != Location.COAL_MINE) {
                return;
            }

            colorByRarity = main.getConfigValues().isEnabled(Feature.REVENANT_COLOR_BY_RARITY);
            textMode = main.getConfigValues().isEnabled(Feature.REVENANT_TEXT_MODE);
            slayerBoss = SlayerBoss.REVENANT;
        } else if (feature == Feature.TARANTULA_SLAYER_TRACKER) {
            if (buttonLocation == null && main.getConfigValues().isEnabled(Feature.HIDE_WHEN_NOT_IN_SPIDERS_DEN) &&
                    main.getUtils().getSlayerQuest() != EnumUtils.SlayerQuest.TARANTULA_BROODFATHER && main.getUtils().getLocation() != Location.SPIDERS_DEN) {
                return;
            }

            colorByRarity = main.getConfigValues().isEnabled(Feature.TARANTULA_COLOR_BY_RARITY);
            textMode = main.getConfigValues().isEnabled(Feature.TARANTULA_TEXT_MODE);
            slayerBoss = SlayerBoss.TARANTULA;
        } else if (feature == Feature.SVEN_SLAYER_TRACKER) {
            if (buttonLocation == null && main.getConfigValues().isEnabled(Feature.HIDE_WHEN_NOT_IN_CASTLE) &&
            main.getUtils().getSlayerQuest() != EnumUtils.SlayerQuest.SVEN_PACKMASTER && main.getUtils().getLocation() != Location.RUINS) {
                return;
            }

            colorByRarity = main.getConfigValues().isEnabled(Feature.SVEN_COLOR_BY_RARITY);
            textMode = main.getConfigValues().isEnabled(Feature.SVEN_TEXT_MODE);
            slayerBoss = SlayerBoss.SVEN;
        } else {
            return;
        }

        float x = main.getConfigValues().getActualX(feature);
        float y = main.getConfigValues().getActualY(feature);
        int color = main.getConfigValues().getColor(feature).getRGB();

        if (textMode) {
            int lineHeight = 8;
            int spacer = 3;

            int lines = 0;
            int spacers = 0;

            int longestLineWidth = mc.fontRendererObj.getStringWidth(slayerBoss.getDisplayName());
            lines++;
            spacers++;

            int longestSlayerDropLineWidth = mc.fontRendererObj.getStringWidth(Translations.getMessage("slayerTracker.bossesKilled"));
            int longestCount = mc.fontRendererObj.getStringWidth(String.valueOf(SlayerTracker.getInstance().getSlayerKills(slayerBoss)));
            lines++;
            spacers++;

            for (SlayerDrop drop : slayerBoss.getDrops()) {
                longestSlayerDropLineWidth = Math.max(longestSlayerDropLineWidth, mc.fontRendererObj.getStringWidth(drop.getDisplayName()));
                longestCount = Math.max(longestCount, mc.fontRendererObj.getStringWidth(String.valueOf(SlayerTracker.getInstance().getDropCount(drop))));
                lines++;
            }

            int width = Math.max(longestLineWidth, longestSlayerDropLineWidth + 8 + longestCount);
            int height = lines * 8 + spacer * spacers;

            x = transformXY(x, width, scale);
            y = transformXY(y, height, scale);

            if (buttonLocation != null) {
                buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            }

            ChromaManager.renderingText(feature);

            main.getUtils().drawTextWithStyle(slayerBoss.getDisplayName(), x, y, color);
            y += lineHeight + spacer;
            main.getUtils().drawTextWithStyle(Translations.getMessage("slayerTracker.bossesKilled"), x, y, color);
            String text = String.valueOf(SlayerTracker.getInstance().getSlayerKills(slayerBoss));
            main.getUtils().drawTextWithStyle(text, x + width - mc.fontRendererObj.getStringWidth(text), y, color);
            y += lineHeight + spacer;

            ChromaManager.doneRenderingText();

            for (SlayerDrop slayerDrop : slayerBoss.getDrops()) {

                int currentColor = color;
                if (colorByRarity) {
                    currentColor = slayerDrop.getRarity().getColorCode().getRGB();
                } else {
                    ChromaManager.renderingText(feature);
                }

                main.getUtils().drawTextWithStyle(slayerDrop.getDisplayName(), x, y, currentColor);

                if (!colorByRarity) {
                    ChromaManager.doneRenderingText();
                }

                ChromaManager.renderingText(feature);
                text = String.valueOf(SlayerTracker.getInstance().getDropCount(slayerDrop));
                main.getUtils().drawTextWithStyle(text, x + width - mc.fontRendererObj.getStringWidth(text), y, currentColor);
                ChromaManager.doneRenderingText();

                y += lineHeight;
            }

        } else {
            int entityRenderY;
            int textCenterX;
            if (feature == Feature.REVENANT_SLAYER_TRACKER) {
                entityRenderY = 30;
                textCenterX = 15;
            } else if (feature == Feature.TARANTULA_SLAYER_TRACKER) {
                entityRenderY = 36;
                textCenterX = 28;
            } else {
                entityRenderY = 36;
                textCenterX = 15;
            }

            int iconWidth = 16;

            int entityWidth = textCenterX * 2;
            int entityIconSpacingHorizontal = 2;
            int iconTextOffset = -2;
            int columnOneMaxTextWidth = 0;
            int columnTwoMaxTextWidth = 0;
            int columnThreeMaxTextWidth = 0;
            int row = 0;
            int column = 0;
            for (SlayerDrop slayerDrop : slayerBoss.getDrops()) {
                int width = mc.fontRendererObj.getStringWidth(TextUtils.abbreviate(SlayerTracker.getInstance().getDropCount(slayerDrop)));

                if (column == 0) {
                    columnOneMaxTextWidth = Math.max(columnOneMaxTextWidth, width);
                } else if (column == 1) {
                    columnTwoMaxTextWidth = Math.max(columnTwoMaxTextWidth, width);
                } else if (column == 2) {
                    columnThreeMaxTextWidth = Math.max(columnThreeMaxTextWidth, width);
                }

                column++;
                if (column == 3) {
                    column = 0;
                    row++;
                }
            }

            int iconSpacingVertical = 4;

            int width = entityWidth + entityIconSpacingHorizontal + 3 * iconWidth + columnOneMaxTextWidth + columnTwoMaxTextWidth + columnThreeMaxTextWidth + iconTextOffset;
            int height = (iconWidth + iconSpacingVertical) * 3 - iconSpacingVertical;

            x = transformXY(x, width, scale);
            y = transformXY(y, height, scale);

            if (buttonLocation != null) {
                buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            }

            if (feature == Feature.REVENANT_SLAYER_TRACKER) {
                if (revenant == null) {
                    revenant = new EntityZombie(Utils.getDummyWorld());

                    revenant.getInventory()[0] = main.getUtils().createItemStack(Items.diamond_hoe, true);
                    revenant.getInventory()[1] = main.getUtils().createItemStack(Items.diamond_boots, false);
                    revenant.getInventory()[2] = main.getUtils().createItemStack(Items.diamond_leggings, true);
                    revenant.getInventory()[3] = main.getUtils().createItemStack(Items.diamond_chestplate, true);
                    revenant.getInventory()[4] = main.getUtils().createSkullItemStack(null, null, "45012ee3-29fd-42ed-908b-648c731c7457", "1fc0184473fe882d2895ce7cbc8197bd40ff70bf10d3745de97b6c2a9c5fc78f");
                }
                GlStateManager.color(1, 1, 1, 1);
                revenant.ticksExisted = (int) main.getNewScheduler().getTotalTicks();
                drawEntity(revenant, x + 15, y + 53, -15); // left is 35

            } else if (feature == Feature.TARANTULA_SLAYER_TRACKER) {
                if (tarantula == null) {
                    tarantula = new EntitySpider(Utils.getDummyWorld());

                    caveSpider = new EntityCaveSpider(Utils.getDummyWorld());

                    tarantula.riddenByEntity = caveSpider;
                    caveSpider.ridingEntity = tarantula;
                }
                GlStateManager.color(1, 1, 1, 1);
                drawEntity(tarantula, x + 28, y + 38, -30);
                drawEntity(caveSpider, x + 25, y + 23, -30);

            } else {
                if (sven == null) {
                    sven = new EntityWolf(Utils.getDummyWorld());
                    sven.setAngry(true);
                }
                GlStateManager.color(1, 1, 1, 1);
                drawEntity(sven, x + 17, y + 38, -35);
            }

            GlStateManager.disableDepth();
            ChromaManager.renderingText(feature);
            String text = TextUtils.abbreviate(SlayerTracker.getInstance().getSlayerKills(slayerBoss)) + " Kills";
            main.getUtils().drawTextWithStyle(text, x + textCenterX - mc.fontRendererObj.getStringWidth(text) / 2F, y + entityRenderY, color);
            ChromaManager.doneRenderingText();

            row = 0;
            column = 0;
            for (SlayerDrop slayerDrop : slayerBoss.getDrops()) {
                float currentX = x + entityIconSpacingHorizontal + entityWidth + column * iconWidth;
                if (column > 0) {
                    currentX += columnOneMaxTextWidth;
                }
                if (column > 1) {
                    currentX += columnTwoMaxTextWidth;
                }
                float currentY = y + row * (iconWidth + iconSpacingVertical);

                GlStateManager.color(1, 1, 1, 1);
                renderItem(slayerDrop.getItemStack(), currentX, currentY);

                GlStateManager.disableDepth();

                int currentColor = color;
                if (colorByRarity) {
                    currentColor = slayerDrop.getRarity().getColorCode().getRGB();
                } else {
                    ChromaManager.renderingText(feature);
                }

                main.getUtils().drawTextWithStyle(TextUtils.abbreviate(SlayerTracker.getInstance().getDropCount(slayerDrop)), currentX + iconWidth + iconTextOffset, currentY + 8, currentColor);
                if (!colorByRarity) {
                    ChromaManager.doneRenderingText();
                }

                column++;
                if (column == 3) {
                    column = 0;
                    row++;
                }
            }
            GlStateManager.enableDepth();
        }
    }

    public void drawDragonTrackers(Minecraft mc, float scale, ButtonLocation buttonLocation) {
        if (main.getConfigValues().isEnabled(Feature.DRAGON_STATS_TRACKER_NEST_ONLY) && main.getUtils().getLocation() != Location.DRAGONS_NEST && buttonLocation == null) {
            return;
        }

        List<DragonType> recentDragons = DragonTracker.getInstance().getRecentDragons();
        if (recentDragons.isEmpty() && buttonLocation != null) {
            recentDragons = Lists.newLinkedList();
            recentDragons.add(DragonType.PROTECTOR);
            recentDragons.add(DragonType.SUPERIOR);
            recentDragons.add(DragonType.WISE);
        }

        boolean colorByRarity = main.getConfigValues().isEnabled(Feature.DRAGON_STATS_TRACKER_COLOR_BY_RARITY);
        boolean textMode = main.getConfigValues().isEnabled(Feature.DRAGON_STATS_TRACKER_TEXT_MODE);

        int spacerHeight = 3;

        String never = Translations.getMessage("dragonTracker.never");

        int width;
        int height;
        if (textMode) {
            int lines = 0;
            int spacers = 0;

            int longestLineWidth = mc.fontRendererObj.getStringWidth(Translations.getMessage("dragonTracker.recentDragons"));
            lines++;
            spacers++;

            spacers++;
            longestLineWidth = Math.max(longestLineWidth, mc.fontRendererObj.getStringWidth(Translations.getMessage("dragonTracker.dragonsSince")));
            lines++;
            spacers++;

            for (DragonType dragon : recentDragons) {
                longestLineWidth = Math.max(longestLineWidth, mc.fontRendererObj.getStringWidth(dragon.getDisplayName()));
                lines++;
            }

            int longestCount = 0;
            int longestDragonsSinceLineWidth = 0;
            for (DragonsSince dragonsSince : DragonsSince.values()) {
                longestDragonsSinceLineWidth = Math.max(longestDragonsSinceLineWidth, mc.fontRendererObj.getStringWidth(dragonsSince.getDisplayName()));
                int dragonsSinceValue = DragonTracker.getInstance().getDragsSince(dragonsSince);
                longestCount = Math.max(longestCount, mc.fontRendererObj.getStringWidth(dragonsSinceValue == 0 ? never : String.valueOf(dragonsSinceValue)));
                lines++;
            }
            width = Math.max(longestLineWidth, longestDragonsSinceLineWidth + 8 + longestCount);

            height = lines * 8 + spacerHeight * spacers;
        } else {
            width = 100;
            height = 100;
        }

        float x = main.getConfigValues().getActualX(Feature.DRAGON_STATS_TRACKER);
        float y = main.getConfigValues().getActualY(Feature.DRAGON_STATS_TRACKER);
        x = transformXY(x, width, scale);
        y = transformXY(y, height, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
        }

        int color = main.getConfigValues().getColor(Feature.DRAGON_STATS_TRACKER).getRGB();

        if (textMode) {
            ChromaManager.renderingText(Feature.DRAGON_STATS_TRACKER);
            main.getUtils().drawTextWithStyle(Translations.getMessage("dragonTracker.recentDragons"), x, y, color);
            y += 8 + spacerHeight;
            ChromaManager.doneRenderingText();

            for (DragonType dragon : recentDragons) {
                int currentColor = color;
                if (colorByRarity) {
                    currentColor = dragon.getColor().getRGB();
                } else {
                    ChromaManager.renderingText(Feature.DRAGON_STATS_TRACKER);
                }

                main.getUtils().drawTextWithStyle(dragon.getDisplayName(), x, y, currentColor);

                if (!colorByRarity) {
                    ChromaManager.doneRenderingText();
                }

                y += 8;
            }
            y += spacerHeight;

            ChromaManager.renderingText(Feature.DRAGON_STATS_TRACKER);
            color = main.getConfigValues().getColor(Feature.DRAGON_STATS_TRACKER).getRGB();
            main.getUtils().drawTextWithStyle(Translations.getMessage("dragonTracker.dragonsSince"), x, y, color);
            y += 8 + spacerHeight;
            ChromaManager.doneRenderingText();

            for (DragonsSince dragonsSince : DragonsSince.values()) {
                GlStateManager.disableDepth();
                GlStateManager.enableBlend();
                GlStateManager.color(1, 1, 1, 1F);
                GlStateManager.disableBlend();
                GlStateManager.enableDepth();

                int currentColor = color;
                if (colorByRarity) {
                    currentColor = dragonsSince.getItemRarity().getColorCode().getRGB();
                } else {
                    ChromaManager.renderingText(Feature.DRAGON_STATS_TRACKER);
                }

                main.getUtils().drawTextWithStyle(dragonsSince.getDisplayName(), x, y, currentColor);

                if (!colorByRarity) {
                    ChromaManager.doneRenderingText();
                }

                ChromaManager.renderingText(Feature.DRAGON_STATS_TRACKER);
                int dragonsSinceValue = DragonTracker.getInstance().getDragsSince(dragonsSince);
                String text = dragonsSinceValue == 0 ? never : String.valueOf(dragonsSinceValue);
                main.getUtils().drawTextWithStyle(text, x + width - mc.fontRendererObj.getStringWidth(text), y, color);
                y += 8;
                ChromaManager.doneRenderingText();
            }
        } else {

        }
    }

    private static final SlayerArmorProgress[] DUMMY_PROGRESSES = new SlayerArmorProgress[]{new SlayerArmorProgress(new ItemStack(Items.diamond_boots)), new SlayerArmorProgress(new ItemStack(Items.chainmail_leggings)), new SlayerArmorProgress(new ItemStack(Items.diamond_chestplate)), new SlayerArmorProgress(new ItemStack(Items.leather_helmet))};
    private static ItemStack NETHER_STAR;

    public void drawRevenantIndicator(float scale, Minecraft mc, ButtonLocation buttonLocation) {
        float x = main.getConfigValues().getActualX(Feature.SLAYER_INDICATOR);
        float y = main.getConfigValues().getActualY(Feature.SLAYER_INDICATOR);

        int longest = -1;
        SlayerArmorProgress[] progresses = main.getInventoryUtils().getSlayerArmorProgresses();
        if (buttonLocation != null) progresses = DUMMY_PROGRESSES;
        for (SlayerArmorProgress progress : progresses) {
            if (progress == null) continue;

            int textWidth = mc.fontRendererObj.getStringWidth(progress.getPercent()+"% ("+progress.getDefence()+")");
            if (textWidth > longest) {
                longest = textWidth;
            }
        }
        if (longest == -1) return;

        int height = 15 * 4;
        int width = 16 + 2 + longest;

        x = transformXY(x, width, scale);
        y = transformXY(y, height, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        main.getUtils().enableStandardGLOptions();

        EnumUtils.AnchorPoint anchorPoint = main.getConfigValues().getAnchorPoint(Feature.SLAYER_INDICATOR);
        boolean downwards = (anchorPoint == EnumUtils.AnchorPoint.TOP_LEFT || anchorPoint == EnumUtils.AnchorPoint.TOP_RIGHT);

        int color = main.getConfigValues().getColor(Feature.SLAYER_INDICATOR).getRGB();

        int drawnCount = 0;
        for (int armorPiece = 3; armorPiece >= 0; armorPiece--) {
            SlayerArmorProgress progress = progresses[downwards ? armorPiece : 3 - armorPiece];
            if (progress == null) continue;

            float fixedY;
            if (downwards) {
                fixedY = y + drawnCount * 15;
            } else {
                fixedY = (y + 45) - drawnCount * 15;
            }
            renderItem(progress.getItemStack(), x, fixedY);

            float currentX = x + 19;
            ChromaManager.renderingText(Feature.SLAYER_INDICATOR);
            main.getUtils().drawTextWithStyle(progress.getPercent() + "% (", currentX, fixedY + 5, color);
            ChromaManager.doneRenderingText();

            currentX += mc.fontRendererObj.getStringWidth(progress.getPercent()+"% (");
            main.getUtils().drawTextWithStyle(progress.getDefence(), currentX, fixedY + 5, 0xFFFFFFFF);

            currentX += mc.fontRendererObj.getStringWidth(progress.getDefence());
            ChromaManager.renderingText(Feature.SLAYER_INDICATOR);
            main.getUtils().drawTextWithStyle(")", currentX, fixedY + 5, color);
            ChromaManager.doneRenderingText();

            drawnCount++;
        }

        main.getUtils().restoreGLOptions();
    }

    public void drawPotionEffectTimers(float scale, ButtonLocation buttonLocation) {
        float x = main.getConfigValues().getActualX(Feature.TAB_EFFECT_TIMERS);
        float y = main.getConfigValues().getActualY(Feature.TAB_EFFECT_TIMERS);

        TabEffectManager tabEffect = TabEffectManager.getInstance();

        List<TabEffect> potionTimers = tabEffect.getPotionTimers();
        List<TabEffect> powerupTimers = tabEffect.getPowerupTimers();

        if (buttonLocation == null) {
            if (potionTimers.isEmpty() && powerupTimers.isEmpty()) {
                return;
            }
        } else { // When editing GUI draw dummy timers.
            potionTimers = TabEffectManager.getDummyPotionTimers();
            powerupTimers = TabEffectManager.getDummyPowerupTimers();
        }

        EnumUtils.AnchorPoint anchorPoint = main.getConfigValues().getAnchorPoint(Feature.TAB_EFFECT_TIMERS);
        boolean topDown = (anchorPoint == EnumUtils.AnchorPoint.TOP_LEFT || anchorPoint == EnumUtils.AnchorPoint.TOP_RIGHT);

        int totalEffects = TabEffectManager.getDummyPotionTimers().size() + TabEffectManager.getDummyPowerupTimers().size();
        int spacer = (!TabEffectManager.getDummyPotionTimers().isEmpty() && !TabEffectManager.getDummyPowerupTimers().isEmpty()) ? 3 : 0;

        int lineHeight = 8 + 1; // 1 pixel between each line.

        //9 px per effect + 3px spacer between Potions and Powerups if both exist.
        int height = (totalEffects * lineHeight) + spacer - 1; // -1 Because last line doesn't need a pixel under.
        int width = 156; //String width of "Enchanting XP Boost III 1:23:45"

        x = transformXY(x, width, scale);
        y = transformXY(y, height, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        main.getUtils().enableStandardGLOptions();

        boolean alignRight = (anchorPoint == EnumUtils.AnchorPoint.TOP_RIGHT || anchorPoint == EnumUtils.AnchorPoint.BOTTOM_RIGHT);

        Color color = main.getConfigValues().getColor(Feature.TAB_EFFECT_TIMERS);

        Minecraft mc = Minecraft.getMinecraft();

        int drawnCount = 0;
        for (TabEffect potion : potionTimers) {
            float lineY;
            if (topDown) {
                lineY = y + drawnCount * lineHeight;
            } else {
                lineY = y + height + drawnCount * lineHeight - 8;
            }

            String effect = potion.getEffect();
            String duration = potion.getDurationForDisplay();

            if (alignRight) {
                ChromaManager.renderingText(Feature.TAB_EFFECT_TIMERS);
                main.getUtils().drawTextWithStyle(duration+" ", x + width - mc.fontRendererObj.getStringWidth(duration+" ")
                        - mc.fontRendererObj.getStringWidth(effect.trim()), lineY, color.getRGB());
                ChromaManager.doneRenderingText();
                main.getUtils().drawTextWithStyle(effect.trim(), x + width - mc.fontRendererObj.getStringWidth(effect.trim()), lineY, color.getRGB());
            } else {
                main.getUtils().drawTextWithStyle(effect, x, lineY, color.getRGB());
                ChromaManager.renderingText(Feature.TAB_EFFECT_TIMERS);
                main.getUtils().drawTextWithStyle(duration, x+mc.fontRendererObj.getStringWidth(effect), lineY, color.getRGB());
                ChromaManager.doneRenderingText();
            }
            drawnCount += topDown ? 1 : -1;
        }
        for (TabEffect powerUp : powerupTimers) {
            float lineY;
            if (topDown) {
                lineY = y + spacer + drawnCount * lineHeight;
            } else {
                lineY = y + height + drawnCount * lineHeight - spacer - 8;
            }

            String effect = powerUp.getEffect();
            String duration = powerUp.getDurationForDisplay();

            if (alignRight) {
                ChromaManager.renderingText(Feature.TAB_EFFECT_TIMERS);
                main.getUtils().drawTextWithStyle(duration+" ", x + width - mc.fontRendererObj.getStringWidth(duration+" ")
                        - mc.fontRendererObj.getStringWidth(effect.trim()), lineY, color.getRGB());
                ChromaManager.doneRenderingText();
                main.getUtils().drawTextWithStyle(effect, x + width - mc.fontRendererObj.getStringWidth(effect.trim()), lineY, color.getRGB());
            } else {
                main.getUtils().drawTextWithStyle(effect, x, lineY, color.getRGB());
                ChromaManager.renderingText(Feature.TAB_EFFECT_TIMERS);
                main.getUtils().drawTextWithStyle(duration, x+mc.fontRendererObj.getStringWidth(effect), lineY, color.getRGB());
                ChromaManager.doneRenderingText();
            }
            drawnCount += topDown ? 1 : -1;
        }

        main.getUtils().restoreGLOptions();
    }

    private void renderItem(ItemStack item, float x, float y) {
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(item, 0, 0);
        GlStateManager.popMatrix();

//        GlStateManager.disableDepth();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
    }

    private static final List<ItemDiff> DUMMY_PICKUP_LOG = new ArrayList<>(Arrays.asList(new ItemDiff(ColorCode.DARK_PURPLE + "Forceful Ember Chestplate", 1),
            new ItemDiff("Boat", -1), new ItemDiff(ColorCode.BLUE + "Aspect of the End", 1)));

    public void drawItemPickupLog(float scale, ButtonLocation buttonLocation) {
        float x = main.getConfigValues().getActualX(Feature.ITEM_PICKUP_LOG);
        float y = main.getConfigValues().getActualY(Feature.ITEM_PICKUP_LOG);

        EnumUtils.AnchorPoint anchorPoint = main.getConfigValues().getAnchorPoint(Feature.ITEM_PICKUP_LOG);
        boolean downwards = anchorPoint == EnumUtils.AnchorPoint.TOP_RIGHT || anchorPoint == EnumUtils.AnchorPoint.TOP_LEFT;

        int lineHeight = 8 + 1; // 1 pixel spacer
        int height = lineHeight * 3 - 1;
        int width = Minecraft.getMinecraft().fontRendererObj.getStringWidth("+ 1x Forceful Ember Chestplate");

        x = transformXY(x, width, scale);
        y = transformXY(y, height, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        main.getUtils().enableStandardGLOptions();

        int i = 0;
        Collection<ItemDiff> log = main.getInventoryUtils().getItemPickupLog();
        if (buttonLocation != null) {
            log = DUMMY_PICKUP_LOG;
        }
        for (ItemDiff itemDiff : log) {
            String text = String.format("%s %sx §r%s", itemDiff.getAmount() > 0 ? "§a+" : "§c-",
                    Math.abs(itemDiff.getAmount()), itemDiff.getDisplayName());
            float stringY = y + (i * lineHeight);
            if (!downwards) {
                stringY = y + height - (i * lineHeight) - 8;
            }

            main.getUtils().drawTextWithStyle(text, x, stringY, 0xFFFFFFFF);
            i++;
        }

        main.getUtils().restoreGLOptions();
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
     * <p>
     * --
     * |  | XXs
     * --
     */
    private void drawCompactPowerOrbStatus(Minecraft mc, float scale, ButtonLocation buttonLocation, PowerOrb powerOrb, int seconds) {
        float x = main.getConfigValues().getActualX(Feature.POWER_ORB_STATUS_DISPLAY);
        float y = main.getConfigValues().getActualY(Feature.POWER_ORB_STATUS_DISPLAY);

        String secondsString = String.format("§e%ss", seconds);
        int spacing = 1;
        int iconSize = mc.fontRendererObj.FONT_HEIGHT * 3; // 3 because it looked the best
        int width = iconSize + spacing + mc.fontRendererObj.getStringWidth(secondsString);

        x = transformXY(x, width, scale);
        y = transformXY(y, iconSize, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + iconSize, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        EntityArmorStand powerOrbArmorStand;
        if (buttonLocation != null) {
            powerOrbArmorStand = getRadiantDummyArmorStand();
        } else {
            powerOrbArmorStand = PowerOrbManager.getInstance().getPowerOrbArmorStand();
        }

        main.getUtils().enableStandardGLOptions();

        if (powerOrbArmorStand != null) {
            drawPowerOrbArmorStand(powerOrbArmorStand, x + 1, y + 4);
        } else {
            mc.getTextureManager().bindTexture(powerOrb.getResourceLocation());
            main.getUtils().drawModalRectWithCustomSizedTexture(x, y, 0, 0, iconSize, iconSize, iconSize, iconSize);
        }

        main.getUtils().drawTextWithStyle(secondsString, x + spacing + iconSize, y + (iconSize / 2F) - (8 / 2F), ColorCode.WHITE.getColor(255).getRGB());

        main.getUtils().restoreGLOptions();
    }

    /**
     * Displays the power orb with detailed stats about the boost you're receiving.
     * <p>
     * --  +X ❤/s
     * |  | +X ✎/s
     * --  +X ❁
     * XXs
     */
    private void drawDetailedPowerOrbStatus(Minecraft mc, float scale, ButtonLocation buttonLocation, PowerOrb powerOrb, int seconds) {
        float x = main.getConfigValues().getActualX(Feature.POWER_ORB_STATUS_DISPLAY);
        float y = main.getConfigValues().getActualY(Feature.POWER_ORB_STATUS_DISPLAY);

        int maxHealth = main.getUtils().getAttributes().get(Attribute.MAX_HEALTH).getValue();
        double healthRegen = maxHealth * powerOrb.getHealthRegen();
        if (main.getUtils().getSlayerQuest() == EnumUtils.SlayerQuest.TARANTULA_BROODFATHER && main.getUtils().getSlayerQuestLevel() >= 2) {
            healthRegen *= 0.5; // Tarantula boss 2+ reduces healing by 50%.
        }
        double healIncrease = powerOrb.getHealIncrease() * 100;

        List<String> display = new LinkedList<>();
        display.add(String.format("§c+%s ❤/s", TextUtils.formatDouble(healthRegen)));
        if (powerOrb.getManaRegen() > 0) {
            int maxMana = main.getUtils().getAttributes().get(Attribute.MAX_MANA).getValue();
            double manaRegen = Math.floorDiv(maxMana, 50);
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
        int iconSize = mc.fontRendererObj.FONT_HEIGHT * 3; // 3 because it looked the best
        int iconAndSecondsHeight = iconSize + mc.fontRendererObj.FONT_HEIGHT;

        int effectsHeight = (mc.fontRendererObj.FONT_HEIGHT + spacingBetweenLines) * display.size();
        int width = iconSize + 2 + longestLine.map(mc.fontRendererObj::getStringWidth)
                .orElseGet(() -> mc.fontRendererObj.getStringWidth(display.get(0)));
        int height = Math.max(effectsHeight, iconAndSecondsHeight);

        x = transformXY(x, width, scale);
        y = transformXY(y, height, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        EntityArmorStand powerOrbArmorStand;
        if (buttonLocation != null) {
            powerOrbArmorStand = getRadiantDummyArmorStand();
        } else {
            powerOrbArmorStand = PowerOrbManager.getInstance().getPowerOrbArmorStand();
        }

        main.getUtils().enableStandardGLOptions();

        if (powerOrbArmorStand != null) {
            drawPowerOrbArmorStand(powerOrbArmorStand, x + 1, y + 4);
        } else {
            mc.getTextureManager().bindTexture(powerOrb.getResourceLocation());
            main.getUtils().drawModalRectWithCustomSizedTexture(x, y, 0, 0, iconSize, iconSize, iconSize, iconSize);
        }

        String secondsString = String.format("§e%ss", seconds);
        main.getUtils().drawTextWithStyle(secondsString, Math.round(x + (iconSize / 2F) - (mc.fontRendererObj.getStringWidth(secondsString) / 2F)), y + iconSize, ColorCode.WHITE.getColor(255).getRGB());

        float startY = Math.round(y + (iconAndSecondsHeight / 2f) - (effectsHeight / 2f));
        for (int i = 0; i < display.size(); i++) {
            main.getUtils().drawTextWithStyle(display.get(i), x + iconSize + 2, startY + (i * (mc.fontRendererObj.FONT_HEIGHT + spacingBetweenLines)), ColorCode.WHITE.getColor(255).getRGB());
        }

        main.getUtils().restoreGLOptions();
    }

    private MapData mapData;

    @Getter private float mapStartX = -1;
    @Getter private float mapStartZ = -1;

    private Vec3 lastSecondVector;

    public void drawDungeonsMap(Minecraft mc, float scale, ButtonLocation buttonLocation) {
        if (buttonLocation == null && !main.getUtils().isInDungeon()) {
            mapStartX = -1;
            mapStartZ = -1;
            mapData = null;
        }

        ItemStack possibleMapItemStack = mc.thePlayer.inventory.getStackInSlot(8);
        if (buttonLocation == null && (possibleMapItemStack == null || possibleMapItemStack.getItem() != Items.filled_map ||
                !possibleMapItemStack.hasDisplayName()) && mapData == null) {
            return;
        }
        boolean isScoreSummary = false;
        if (buttonLocation == null && possibleMapItemStack != null && possibleMapItemStack.getItem() == Items.filled_map) {
            isScoreSummary = possibleMapItemStack.getDisplayName().contains("Your Score Summary");

            if (!possibleMapItemStack.getDisplayName().contains("Magical Map") && !isScoreSummary) {
                return;
            }
        }

        float x = main.getConfigValues().getActualX(Feature.DUNGEONS_MAP_DISPLAY);
        float y = main.getConfigValues().getActualY(Feature.DUNGEONS_MAP_DISPLAY);

        GlStateManager.pushMatrix();

        int originalSize = 128;
        float initialScaleFactor = 0.5F;

        int size = (int) (originalSize * initialScaleFactor);

        int minecraftScale = new ScaledResolution(mc).getScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(Math.round((x - size * scale / 2F)*minecraftScale),
                mc.displayHeight-Math.round((y + size * scale / 2F)*minecraftScale), Math.round(size * minecraftScale * scale), Math.round(size * minecraftScale * scale));

        x = transformXY(x, size, scale);
        y = transformXY(y, size, scale);

        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x+size, y, y+size, scale);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        Color color = main.getConfigValues().getColor(Feature.DUNGEONS_MAP_DISPLAY);
        main.getUtils().drawRect(x, y, x+size, y+size, 0x55000000);
        ChromaManager.renderingText(Feature.DUNGEONS_MAP_DISPLAY);
        main.getUtils().drawRectOutline(x, y, size, size, 1, color.getRGB(), main.getConfigValues().getChromaFeatures().contains(Feature.DUNGEONS_MAP_DISPLAY));
        ChromaManager.doneRenderingText();
        GlStateManager.color(1,1,1,1);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        main.getUtils().enableStandardGLOptions();

        GlStateManager.color(1,1,1,1);

        float rotation = 180 - MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw);

        float zoomScaleFactor = main.getUtils().denormalizeScale(main.getConfigValues().getMapZoom().getValue(), 0.5F, 5, 0.1F);
        if (isScoreSummary) {
            zoomScaleFactor = 1;
        }

        float totalScaleFactor = initialScaleFactor * zoomScaleFactor;

        float mapSize = (originalSize * totalScaleFactor);

        GlStateManager.scale(totalScaleFactor, totalScaleFactor, 1);
        x /= totalScaleFactor;
        y /= totalScaleFactor;
        GlStateManager.translate(x, y, 0);

        float rotationCenterX = originalSize * initialScaleFactor;
        float rotationCenterY = originalSize * initialScaleFactor;

        float centerOffset = -((mapSize-size)/zoomScaleFactor);
        GlStateManager.translate(centerOffset, centerOffset, 0);

        boolean rotate = main.getConfigValues().isEnabled(Feature.ROTATE_MAP);
        boolean rotateOnPlayer = main.getConfigValues().isEnabled(Feature.CENTER_ROTATION_ON_PLAYER);

        if (isScoreSummary) {
            rotate = false;
        }

        if (buttonLocation == null) {
            try {
                boolean foundMapData = false;
                MapData newMapData = null;
                if (possibleMapItemStack != null) {
                    newMapData = Items.filled_map.getMapData(possibleMapItemStack, mc.theWorld);
                }
                if (newMapData != null) {
                    mapData = newMapData;
                    foundMapData = true;
                }

                if (mapData != null) {
                    float playerX = (float) mc.thePlayer.posX;
                    float playerZ = (float) mc.thePlayer.posZ;

                    Vec3 currentVector = mc.thePlayer.getPositionVector();
                    main.getNewScheduler().scheduleDelayedTask(new SkyblockRunnable() {
                        @Override
                        public void run() {
                            lastSecondVector = currentVector;
                        }
                    }, 20);


                    double lastSecondTravel = -1;
                    if (lastSecondVector != null) {
                        lastSecondTravel = lastSecondVector.distanceTo(currentVector);
                    }
                    if (foundMapData && ((this.mapStartX == -1 || this.mapStartZ == -1) || lastSecondTravel == 0)) {
                        if (mapData.mapDecorations != null) {
                            for (Map.Entry<String, Vec4b> entry : mapData.mapDecorations.entrySet()) {
                                // Icon type 1 is the green player marker...
                                if (entry.getValue().func_176110_a() == 1) {
                                    float mapMarkerX = entry.getValue().func_176112_b() / 2.0F + 64.0F;
                                    float mapMarkerZ = entry.getValue().func_176113_c() / 2.0F + 64.0F;

                                    // 1 pixel on Hypixel map represents 1.5 blocks...
                                    float mapStartX = playerX - mapMarkerX * 1.5F;
                                    float mapStartZ = playerZ - mapMarkerZ * 1.5F;

                                    this.mapStartX = Math.round(mapStartX / 16F) * 16F;
                                    this.mapStartZ = Math.round(mapStartZ / 16F) * 16F;

//                                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(String.valueOf(this.mapStartX)));
                                }
                            }
                        }
                    }

                    float playerMarkerX = (playerX - mapStartX) / 1.5F;
                    float playerMarkerZ = (playerZ - mapStartZ) / 1.5F;

                    if (rotate && rotateOnPlayer) {
                        rotationCenterX = playerMarkerX;
                        rotationCenterY = playerMarkerZ;
                    }

                    if (rotate) {
                        if (rotateOnPlayer) {
                            GlStateManager.translate(size - rotationCenterX, size - rotationCenterY, 0);
                        }

                        GlStateManager.translate(rotationCenterX, rotationCenterY, 0);
                        GlStateManager.rotate(rotation, 0, 0, 1);
                        GlStateManager.translate(-rotationCenterX, -rotationCenterY, 0);
                    }

                    MapItemRenderer.Instance instance = mc.entityRenderer.getMapItemRenderer().getMapRendererInstance(mapData);
                    main.getUtils().drawMapEdited(instance, isScoreSummary, zoomScaleFactor);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            if (rotate) {
                long ticks = System.currentTimeMillis() % 18000 / 50;

                GlStateManager.translate(rotationCenterX, rotationCenterY, 0);
                GlStateManager.rotate(ticks, 0, 0, 1);
                GlStateManager.translate(-rotationCenterX, -rotationCenterY, 0);
            }

            mc.getTextureManager().bindTexture(DUNGEON_MAP);
            main.getUtils().drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 128,128, 128, 128);
        }
//        main.getUtils().drawRect(rotationCenterX-2, rotationCenterY-2, rotationCenterX+2, rotationCenterY+2, 0xFFFF0000);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        GlStateManager.popMatrix();
//        main.getUtils().drawRect(mapCenterX-2, mapCenterY-2, mapCenterX+2, mapCenterY+2, 0xFF00FF00);

        main.getUtils().restoreGLOptions();
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
            Minecraft.getMinecraft().displayGuiScreen(new SkyblockAddonsGui(guiPageToOpen, guiTabToOpen));
        } else if (guiToOpen == EnumUtils.GUIType.EDIT_LOCATIONS) {
            Minecraft.getMinecraft().displayGuiScreen(new LocationEditGui(guiPageToOpen, guiTabToOpen));
        } else if (guiToOpen == EnumUtils.GUIType.SETTINGS) {
            Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(guiFeatureToOpen, 1, guiPageToOpen, guiTabToOpen, guiFeatureToOpen.getSettings()));
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
        setGuiToOpen(guiToOpen, page, tab);
        guiFeatureToOpen = feature;
    }

    public void setSubtitleFeature(Feature subtitleFeature) {
        this.subtitleFeature = subtitleFeature; // TODO: check, does this break anything? (arrow)
    }

    private ItemStack getNetherStar() {
        if (NETHER_STAR != null) return NETHER_STAR;

        NETHER_STAR = new ItemStack(Items.nether_star);

        NBTTagCompound extraAttributes = new NBTTagCompound();
        extraAttributes.setString("id", "SKYBLOCK_MENU");

        NBTTagCompound nbtTag = new NBTTagCompound();
        nbtTag.setTag("ExtraAttributes", extraAttributes);

        NETHER_STAR.setTagCompound(nbtTag);

        return NETHER_STAR;
    }

    private ItemStack getWarpSkull() {
        if (WARP_SKULL != null) return WARP_SKULL;

        WARP_SKULL = new ItemStack(Items.skull, 1, 3);

        NBTTagCompound texture = new NBTTagCompound();
        texture.setString("Value", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzljODg4MWU0MjkxNWE5ZDI5YmI2MWExNmZiMjZkMDU5OTEzMjA0ZDI2NWRmNWI0MzliM2Q3OTJhY2Q1NiJ9fX0=");

        NBTTagList textures = new NBTTagList();
        textures.appendTag(texture);

        NBTTagCompound properties = new NBTTagCompound();
        properties.setTag("textures", textures);

        NBTTagCompound skullOwner = new NBTTagCompound();
        skullOwner.setString("Id", "9ae837fc-19da-3841-af06-7db55d51c815");
        skullOwner.setTag("Properties", properties);

        NBTTagCompound nbtTag = new NBTTagCompound();
        nbtTag.setTag("SkullOwner", skullOwner);

        WARP_SKULL.setTagCompound(nbtTag);

        return WARP_SKULL;
    }

    public float transformXY(float xy, int widthHeight, float scale) {
        float minecraftScale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        xy -= widthHeight * scale / 2F;
        xy = Math.round(xy * minecraftScale) / minecraftScale;
        return xy / scale;
    }

    @Getter private Set<HealingCircleParticle> healingCircleParticles = new HashSet<>();

    @SubscribeEvent()
    public void onRenderWorld(RenderWorldLastEvent e) {
        Minecraft mc = Minecraft.getMinecraft();
        float partialTicks = e.partialTicks;

        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.SHOW_HEALING_CIRCLE_WALL)) {
            healingCircleParticles.removeIf(healingCircleParticle -> System.currentTimeMillis() - healingCircleParticle.getCreation() > 10000);

            Set<HealingCircle> healingCircles = new HashSet<>();

            for (HealingCircleParticle healingCircleParticle : healingCircleParticles) {
                HealingCircle nearbyHealingCircle = null;
                for (HealingCircle healingCircle : healingCircles) {
                    if (healingCircle.getTotalParticles() > 50) {
                        Point2D.Double circleCenter = healingCircle.getCircleCenter();
                        if (healingCircleParticle.getPoint().distance(circleCenter.getX(), circleCenter.getY()) < 6) {
                            nearbyHealingCircle = healingCircle;
                            break;
                        }
                    } else {
                        if (healingCircleParticle.getPoint().distance(healingCircle.getAverageX(), healingCircle.getAverageZ()) < 12) {
                            nearbyHealingCircle = healingCircle;
                            break;
                        }
                    }
                }

                if (nearbyHealingCircle != null) {
                    nearbyHealingCircle.addPoint(healingCircleParticle);
                } else {
                    healingCircles.add(new HealingCircle(healingCircleParticle));
                }
            }

            for (HealingCircle healingCircle : healingCircles) {
                if (healingCircle.getParticlesPerSecond() < 10) {
                    if (System.currentTimeMillis() - healingCircle.getOldestParticle() > 1000) {
                        healingCircleParticles.removeAll(healingCircle.getHealingCircleParticles());
                        continue;
                    }
                }

                GlStateManager.pushMatrix();
                GL11.glNormal3f(0.0F, 1.0F, 0.0F);

                GlStateManager.disableLighting();
                GlStateManager.depthMask(false);
                GlStateManager.enableDepth();
                GlStateManager.enableBlend();
                GlStateManager.depthFunc(GL11.GL_LEQUAL);
                GlStateManager.disableCull();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.enableAlpha();
                GlStateManager.disableTexture2D();

                Color color = main.getConfigValues().getColor(Feature.SHOW_HEALING_CIRCLE_WALL);
                GlStateManager.color(color.getRed()/255F, color.getGreen()/255F, color.getBlue()/255F, 0.2F);
                Point2D.Double circleCenter = healingCircle.getCircleCenter();
                if (circleCenter != null && !Double.isNaN(circleCenter.getX()) && !Double.isNaN(circleCenter.getY())) {
                    main.getUtils().drawCylinder(circleCenter.getX(), 0, circleCenter.getY(), 10 / 2F, 255, partialTicks);
                }

                GlStateManager.enableCull();
                GlStateManager.enableTexture2D();
                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
                GlStateManager.enableLighting();
                GlStateManager.disableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.popMatrix();
            }
        }

        if (main.getUtils().isOnSkyblock() && main.getUtils().isInDungeon() && main.getConfigValues().isEnabled(Feature.SHOW_CRITICAL_DUNGEONS_TEAMMATES)) {
            Entity renderViewEntity = mc.getRenderViewEntity();

            double viewX = renderViewEntity.prevPosX + (renderViewEntity.posX - renderViewEntity.prevPosX) * (double) partialTicks;
            double viewY = renderViewEntity.prevPosY + (renderViewEntity.posY - renderViewEntity.prevPosY) * (double) partialTicks;
            double viewZ = renderViewEntity.prevPosZ + (renderViewEntity.posZ - renderViewEntity.prevPosZ) * (double) partialTicks;

            int iconSize = 25;

            for (EntityPlayer entity : mc.theWorld.playerEntities) {
                if (renderViewEntity == entity) {
                    continue;
                }

                if (!main.getDungeonUtils().getPlayers().containsKey(entity.getName())) {
                    continue;
                }

                DungeonPlayer dungeonPlayer = main.getDungeonUtils().getPlayers().get(entity.getName());
                if (dungeonPlayer.isGhost() || (!dungeonPlayer.isCritical() && !dungeonPlayer.isLow())) {
                    continue;
                }

                double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
                double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
                double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;

                x -= viewX;
                y -= viewY;
                z -= viewZ;

                if (entity.isSneaking()) {
                    y -= 0.65F;
                }

                double distanceScale = Math.max(1, renderViewEntity.getPositionVector().distanceTo(entity.getPositionVector()) / 10F);

                if (main.getConfigValues().isEnabled(Feature.MAKE_DUNGEON_TEAMMATES_GLOW)) {
                    y += entity.height + 0.75F + (iconSize * distanceScale) / 40F;
                } else {
                    y += entity.height / 2F + 0.25F;
                }

                float f = 1.6F;
                float f1 = 0.016666668F * f;
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, z);
                GL11.glNormal3f(0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
                GlStateManager.scale(-f1, -f1, f1);

                GlStateManager.scale(distanceScale, distanceScale, distanceScale);

                GlStateManager.disableLighting();
                GlStateManager.depthMask(false);
                GlStateManager.disableDepth();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.enableTexture2D();
                GlStateManager.color(1, 1, 1, 1);
                GlStateManager.enableAlpha();

                Tessellator tessellator = Tessellator.getInstance();
                WorldRenderer worldrenderer = tessellator.getWorldRenderer();

                mc.getTextureManager().bindTexture(CRITICAL);
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
                worldrenderer.pos(-iconSize / 2F, -iconSize / 2f, 0).tex(0, 0).endVertex();
                worldrenderer.pos(-iconSize / 2F, iconSize / 2F, 0).tex(0, 1).endVertex();
                worldrenderer.pos(iconSize / 2F, iconSize / 2F, 0).tex(1, 1).endVertex();
                worldrenderer.pos(iconSize / 2F, -iconSize / 2F, 0).tex(1, 0).endVertex();
                tessellator.draw();

                String text = "";
                if (dungeonPlayer.isLow()) {
                    text = "LOW";
                } else if (dungeonPlayer.isCritical()) {
                    text = "CRITICAL";
                }

                mc.fontRendererObj.drawString(text, -mc.fontRendererObj.getStringWidth(text) / 2F, iconSize / 2F + 2, -1, true);

                GlStateManager.enableDepth();
                GlStateManager.depthMask(true);
                GlStateManager.enableLighting();
                GlStateManager.disableBlend();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.popMatrix();
            }
        }
    }

    private void drawPowerOrbArmorStand(EntityArmorStand powerOrbArmorStand, float x, float y) {
        GlStateManager.pushMatrix();

        GlStateManager.enableDepth();
        GlStateManager.enableColorMaterial();

        GlStateManager.translate(x + 12.5F, y + 50F, 50F);
        GlStateManager.scale(-25F, 25F, 25F);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(22.0F, 1.0F, 0.0F, 0.0F);

        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        boolean shadowsEnabled = rendermanager.isRenderShadow();
        rendermanager.setRenderShadow(false);

        powerOrbArmorStand.setInvisible(true);
        float yaw = System.currentTimeMillis() % 1750 / 1750F * 360F;
        powerOrbArmorStand.renderYawOffset = yaw;
        powerOrbArmorStand.prevRenderYawOffset = yaw;

        rendermanager.renderEntityWithPosYaw(powerOrbArmorStand, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        rendermanager.setRenderShadow(shadowsEnabled);

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);

        GlStateManager.popMatrix();
    }

    private void drawEntity(EntityLivingBase entity, float x, float y, float yaw) {
        GlStateManager.pushMatrix();

        GlStateManager.enableDepth();
        GlStateManager.translate(x, y, 50F);
        GlStateManager.scale(-25, 25, 25);
        GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
        GlStateManager.rotate(15F, 1, 0, 0);
        RenderHelper.enableGUIStandardItemLighting();

        entity.renderYawOffset = yaw;
        entity.prevRenderYawOffset = yaw;
        entity.rotationYawHead = yaw;
        entity.prevRotationYawHead = yaw;

        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0f);
        boolean shadowsEnabled = rendermanager.isRenderShadow();
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        rendermanager.setRenderShadow(shadowsEnabled);

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.popMatrix();
    }

    public EntityArmorStand getRadiantDummyArmorStand() {
        if (radiantDummyArmorStand != null) {
            return radiantDummyArmorStand;
        }

        radiantDummyArmorStand = new EntityArmorStand(Utils.getDummyWorld());

        ItemStack orbItemStack = new ItemStack(Items.skull, 1, 3);

        NBTTagCompound texture = new NBTTagCompound(); // This is the texture URL of the radiant orb
        texture.setString("Value", TextUtils.encodeSkinTextureURL("http://textures.minecraft.net/texture/7ab4c4d6ee69bc24bba2b8faf67b9f704a06b01aa93f3efa6aef7a9696c4feef"));

        NBTTagList textures = new NBTTagList();
        textures.appendTag(texture);

        NBTTagCompound properties = new NBTTagCompound();
        properties.setTag("textures", textures);

        NBTTagCompound skullOwner = new NBTTagCompound(); // The id of the radiant orb (not sure if it means anything)
        skullOwner.setString("Id", "3ae3572b-2679-40b4-ba50-14dd58cbbbf7");
        skullOwner.setTag("Properties", properties);

        NBTTagCompound nbtTag = new NBTTagCompound();
        nbtTag.setTag("SkullOwner", skullOwner);

        orbItemStack.setTagCompound(nbtTag);

        radiantDummyArmorStand.setCurrentItemOrArmor(4, orbItemStack);

        return radiantDummyArmorStand;
    }
}
