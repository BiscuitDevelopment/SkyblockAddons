package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.hooks.FontRendererHook;
import codes.biscuit.skyblockaddons.config.ConfigValues;
import codes.biscuit.skyblockaddons.core.*;
import codes.biscuit.skyblockaddons.core.dungeons.DungeonClass;
import codes.biscuit.skyblockaddons.core.dungeons.DungeonMilestone;
import codes.biscuit.skyblockaddons.core.dungeons.DungeonPlayer;
import codes.biscuit.skyblockaddons.features.*;
import codes.biscuit.skyblockaddons.features.dragontracker.DragonTracker;
import codes.biscuit.skyblockaddons.features.dragontracker.DragonType;
import codes.biscuit.skyblockaddons.features.dragontracker.DragonsSince;
import codes.biscuit.skyblockaddons.features.healingcircle.HealingCircleManager;
import codes.biscuit.skyblockaddons.features.powerorbs.PowerOrb;
import codes.biscuit.skyblockaddons.features.powerorbs.PowerOrbManager;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerBoss;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerDrop;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerTracker;
import codes.biscuit.skyblockaddons.features.spookyevent.CandyType;
import codes.biscuit.skyblockaddons.features.spookyevent.SpookyEventManager;
import codes.biscuit.skyblockaddons.features.tablist.TabListParser;
import codes.biscuit.skyblockaddons.features.tablist.TabListRenderer;
import codes.biscuit.skyblockaddons.features.tabtimers.TabEffect;
import codes.biscuit.skyblockaddons.features.tabtimers.TabEffectManager;
import codes.biscuit.skyblockaddons.gui.*;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonLocation;
import codes.biscuit.skyblockaddons.misc.Updater;
import codes.biscuit.skyblockaddons.misc.scheduler.Scheduler;
import codes.biscuit.skyblockaddons.shader.ShaderManager;
import codes.biscuit.skyblockaddons.shader.chroma.ChromaScreenTexturedShader;
import codes.biscuit.skyblockaddons.utils.*;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.client.GuiNotification;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector3d;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;

import static codes.biscuit.skyblockaddons.utils.TextUtils.NUMBER_FORMAT;
import static net.minecraft.client.gui.Gui.icons;

public class RenderListener {

    private static final ItemStack BONE_ITEM = new ItemStack(Items.bone);
    private static final ResourceLocation BARS = new ResourceLocation("skyblockaddons", "barsV2.png");
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
    private static final ResourceLocation FARM_ICON = new ResourceLocation("skyblockaddons", "icons/farm.png");

    private static final ResourceLocation CRITICAL = new ResourceLocation("skyblockaddons", "critical.png");

    private static final ItemStack WATER_BUCKET = new ItemStack(Items.water_bucket);
    private static final ItemStack IRON_SWORD = new ItemStack(Items.iron_sword);
    private static final ItemStack WARP_SKULL = ItemUtils.createSkullItemStack("§bFast Travel", null,  "9ae837fc-19da-3841-af06-7db55d51c815", "c9c8881e42915a9d29bb61a16fb26d059913204d265df5b439b3d792acd56");
    private static final ItemStack SKYBLOCK_MENU = ItemUtils.createItemStack(Items.nether_star, "§aSkyBlock Menu §7(Right Click)", "SKYBLOCK_MENU", false);
    private static final ItemStack PET_ROCK = ItemUtils.createSkullItemStack("§f§f§7[Lvl 100] §6Rock", null,  "1ed7c993-8190-3055-a48c-f70f71b17284", "cb2b5d48e57577563aca31735519cb622219bc058b1f34648b67b8e71bc0fa");
    private static final ItemStack DOLPHIN_PET = ItemUtils.createSkullItemStack("§f§f§7[Lvl 100] §6Dolphin", null,  "48f53ffe-a3f0-3280-aac0-11cc0d6121f4", "cefe7d803a45aa2af1993df2544a28df849a762663719bfefc58bf389ab7f5");
    private static final ItemStack CHEST = new ItemStack(Item.getItemFromBlock(Blocks.chest));
    private static final ItemStack SKULL = ItemUtils.createSkullItemStack("Skull", null, "c659cdd4-e436-4977-a6a7-d5518ebecfbb", "1ae3855f952cd4a03c148a946e3f812a5955ad35cbcb52627ea4acd47d3081");
    private static final ItemStack HYPERION = ItemUtils.createItemStack(Items.iron_sword,"§6Hyperion","HYPERION", false);
    private static final ItemStack VALKYRIE = ItemUtils.createItemStack(Items.iron_sword,"§6Valkyrie","VALKYRIE", false);
    private static final ItemStack ASTRAEA = ItemUtils.createItemStack(Items.iron_sword,"§6Astraea","ASTRAEA", false);
    private static final ItemStack SCYLLA = ItemUtils.createItemStack(Items.iron_sword,"§6Scylla","SCYLLA", false);
    private static final ItemStack SCPETRE = new ItemStack(Blocks.red_flower,1,2); //doesnt show sb texture pack cos blocks cant have and idk how

    private static final ItemStack GREEN_CANDY = ItemUtils.createSkullItemStack("Green Candy", "GREEN_CANDY", "0961dbb3-2167-3f75-92e4-ec8eb4f57e55", "ce0622d01cfdae386cc7dd83427674b422f46d0a57e67a20607e6ca4b9af3b01");
    private static final ItemStack PURPLE_CANDY = ItemUtils.createSkullItemStack("Purple Candy", "PURPLE_CANDY", "5b0e6bf0-6312-3476-b5f8-dbc9a8849a1f", "95d7aee4e97ad84095f55405ee1305d1fc8554c309edb12a1db863cde9c1ec80");

    private static final SlayerArmorProgress[] DUMMY_PROGRESSES = new SlayerArmorProgress[]{new SlayerArmorProgress(new ItemStack(Items.diamond_boots)), new SlayerArmorProgress(new ItemStack(Items.chainmail_leggings)), new SlayerArmorProgress(new ItemStack(Items.diamond_chestplate)), new SlayerArmorProgress(new ItemStack(Items.leather_helmet))};

    private static EntityArmorStand radiantDummyArmorStand;
    private static EntityZombie revenant;
    private static EntitySpider tarantula;
    private static EntityCaveSpider caveSpider;
    private static EntityWolf sven;
    private static EntityEnderman enderman;

    private final SkyblockAddons main = SkyblockAddons.getInstance();

    @Getter @Setter private boolean predictHealth;
    @Getter @Setter private boolean predictMana;

    @Setter private boolean updateMessageDisplayed;

    private Feature subtitleFeature;
    @Getter @Setter private Feature titleFeature;

    @Setter private int arrowsLeft;

    @Setter private String cannotReachMobName;

    @Setter private long skillFadeOutTime = -1;
    @Setter private SkillType skill;
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
        //TODO: very hacky way to accomplish update every frame. Fix in feature refactor?
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null) {
            EntityPlayerSP p = mc.thePlayer;
            if (p != null && main.getConfigValues().isEnabled(Feature.HEALTH_PREDICTION)) { //Reverse calculate the player's health by using the player's vanilla hearts. Also calculate the health change for the gui item.
                float newHealth = getAttribute(Attribute.HEALTH) > getAttribute(Attribute.MAX_HEALTH) ?
                        getAttribute(Attribute.HEALTH) : Math.round(getAttribute(Attribute.MAX_HEALTH) * ((p.getHealth()) / p.getMaxHealth()));
                main.getUtils().getAttributes().get(Attribute.HEALTH).setValue(newHealth);
            }
        }

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
        if (entity.hasCustomName()) {
            if (main.getConfigValues().isEnabled(Feature.MINION_DISABLE_LOCATION_WARNING)) {
                if (entity.getCustomNameTag().startsWith("§cThis location isn't perfect! :(")) {
                    e.setCanceled(true);
                }
                if (entity.getCustomNameTag().startsWith("§c/!\\")) {
                    for (Entity listEntity : Minecraft.getMinecraft().theWorld.loadedEntityList) {
                        if (listEntity.hasCustomName() && listEntity.getCustomNameTag().startsWith("§cThis location isn't perfect! :(") &&
                                listEntity.posX == entity.posX && listEntity.posZ == entity.posZ && listEntity.posY + 0.375 == entity.posY) {
                            e.setCanceled(true);
                            break;
                        }
                    }
                }
            }

            if (main.getConfigValues().isEnabled(Feature.HIDE_SVEN_PUP_NAMETAGS)) {
                if (entity instanceof EntityArmorStand && entity.hasCustomName() && entity.getCustomNameTag().contains("Sven Pup")) {
                    e.setCanceled(true);
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
            if (main.getConfigValues().isEnabled(Feature.FARM_EVENT_TIMER) && main.getConfigValues().isEnabled(Feature.SHOW_FARM_EVENT_TIMER_IN_OTHER_GAMES)) {
                float scale = main.getConfigValues().getGuiScale(Feature.FARM_EVENT_TIMER);
                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, 1);
                drawText(Feature.FARM_EVENT_TIMER, scale, mc, null);
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
                case WARN_WHEN_FETCHUR_CHANGES:
                    message = Message.MESSAGE_FETCHUR_WARNING;
                    break;
                case BROOD_MOTHER_ALERT:
                    message = Message.MESSAGE_BROOD_MOTHER_WARNING;
                    break;
                case BAL_BOSS_ALERT:
                    message = Message.MESSAGE_BAL_BOSS_WARNING;
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

                FontRendererHook.setupFeatureFont(titleFeature);
                DrawUtils.drawText(text, (float) (-mc.fontRendererObj.getStringWidth(text) / 2), -20.0F, main.getConfigValues().getColor(titleFeature));
                FontRendererHook.endFeatureFont();

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

                FontRendererHook.setupFeatureFont(subtitleFeature);
                DrawUtils.drawText(text, -mc.fontRendererObj.getStringWidth(text) / 2F, -23.0F, main.getConfigValues().getColor(subtitleFeature));
                FontRendererHook.endFeatureFont();

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
     * This draws all Skyblock Addons Bars, including the Health, Mana, Drill, and Skill XP bars
     *
     * @param feature        for which to render the bars
     * @param scale          the scale of the feature
     * @param mc             link to the minecraft session
     * @param buttonLocation the resizing gui, if present
     */
    public void drawBar(Feature feature, float scale, Minecraft mc, ButtonLocation buttonLocation) {
        // The fill of the bar from 0 to 1
        float fill;
        if (feature == Feature.MANA_BAR) {
            fill = getAttribute(Attribute.MANA) / getAttribute(Attribute.MAX_MANA);
        } else if (feature == Feature.DRILL_FUEL_BAR) {
            fill = getAttribute(Attribute.FUEL) / getAttribute(Attribute.MAX_FUEL);
        } else if (feature == Feature.SKILL_PROGRESS_BAR) {
            ActionBarParser parser = main.getPlayerListener().getActionBarParser();
            if (buttonLocation == null) {
                if (parser.getPercent() == 0 || parser.getPercent() == 100) {
                    return;
                } else {
                    fill = parser.getPercent() / 100;
                }
            } else {
                fill = 0.40F;
            }
        } else {
            fill = getAttribute(Attribute.HEALTH) / getAttribute(Attribute.MAX_HEALTH);
        }
        if (fill > 1) fill = 1;

        float x = main.getConfigValues().getActualX(feature);
        float y = main.getConfigValues().getActualY(feature);
        float scaleX = main.getConfigValues().getSizesX(feature);
        float scaleY = main.getConfigValues().getSizesY(feature);
        GlStateManager.scale(scaleX, scaleY, 1);

        x = transformXY(x, 71, scale * scaleX);
        y = transformXY(y, 5, scale * scaleY);

        // Render the button resize box if necessary
        if (buttonLocation != null) {
            buttonLocation.checkHoveredAndDrawBox(x, x + 71, y, y + 5, scale, scaleX, scaleY);
        }

        SkyblockColor color = ColorUtils.getDummySkyblockColor(main.getConfigValues().getColor(feature), main.getConfigValues().getChromaFeatures().contains(feature));

        if (feature == Feature.SKILL_PROGRESS_BAR && buttonLocation == null) {
            int remainingTime = (int) (skillFadeOutTime - System.currentTimeMillis());

            if (remainingTime < 0) {
                if (remainingTime < -2000) {
                    return; // Will be invisible, no need to render.
                }

                int textAlpha = Math.round(255 - (-remainingTime / 2000F * 255F));
                color = ColorUtils.getDummySkyblockColor(main.getConfigValues().getColor(feature, textAlpha), main.getConfigValues().getChromaFeatures().contains(feature)); // so it fades out, 0.016 is the minimum alpha
            }
        }

        if (feature == Feature.DRILL_FUEL_BAR && buttonLocation == null && !ItemUtils.isDrill(mc.thePlayer.getHeldItem())) {
            return;
        }

        if (feature == Feature.HEALTH_BAR && main.getConfigValues().isEnabled(Feature.CHANGE_BAR_COLOR_FOR_POTIONS)) {
            if (mc.thePlayer.isPotionActive(19/* Poison */)) {
                color = ColorUtils.getDummySkyblockColor(ColorCode.DARK_GREEN.getColor(), main.getConfigValues().getChromaFeatures().contains(feature));
            } else if (mc.thePlayer.isPotionActive(20/* Wither */)) {
                color = ColorUtils.getDummySkyblockColor(ColorCode.DARK_GRAY.getColor(), main.getConfigValues().getChromaFeatures().contains(feature));
            }
        }

        main.getUtils().enableStandardGLOptions();
        // Draw the actual bar
        drawMultiLayeredBar(mc, color, x, y, fill);

        main.getUtils().restoreGLOptions();
    }

    /**
     * Draws a multitextured bar:
     * Begins by coloring and rendering the empty bar.
     * Then, colors and renders the full bar up to the fraction {@param fill}.
     * Then, overlays (and does not color) an additional texture centered on the current progress of the bar.
     * Then, overlays (and does not color) a final style texture over the bar
     * @param mc link to the current minecraft session
     * @param color the color with which to render the bar
     * @param x the x position of the bar
     * @param y the y position of the bar
     * @param fill the fraction (from 0 to 1) of the bar that's full
     */
    private void drawMultiLayeredBar(Minecraft mc, SkyblockColor color, float x, float y, float fill) {
        int barHeight = 5, barWidth = 71;
        float barFill = barWidth * fill;
        mc.getTextureManager().bindTexture(BARS);
        if (color.getColor() == ColorCode.BLACK.getColor()) {
            GlStateManager.color(0.25F, 0.25F, 0.25F, ColorUtils.getAlpha(color.getColor()) / 255F); // too dark normally
        } else { // A little darker for contrast...
            ColorUtils.bindColor(color.getColor(), 0.9F);
        }
        // If chroma, draw the empty bar much darker than the filled bar
        if (color.drawMulticolorUsingShader()) {
            GlStateManager.color(.5F, .5F, .5F);
            ShaderManager.getInstance().enableShader(ChromaScreenTexturedShader.class);
        }
        // Empty bar first
        DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 1, 1, barWidth, barHeight, 80, 50);

        if (color.drawMulticolorUsingShader()) {
            ColorUtils.bindWhite();
            ShaderManager.getInstance().enableShader(ChromaScreenTexturedShader.class);
        }

        // Filled bar next
        if (fill != 0) {
            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 1, 7, barFill, barHeight, 80, 50);
        }
        // Disable coloring
        if (color.drawMulticolorUsingShader()) {
            ShaderManager.getInstance().disableShader();
        }
        ColorUtils.bindWhite();

        // Overlay uncolored progress indicator next (texture packs can use this to overlay their own static bar colors)
        if (fill > 0 && fill < 1) {
            // Make sure that the overlay doesn't go outside the bounds of the bar.
            // It's 4 pixels wide, so ensure we only render the texture between 0 <= x <= barWidth
            // Start rendering at x => 0 (for small fill values, also don't render before the bar starts)
            // Adding padding ensures that no green bar gets rendered from the texture...?
            float padding = .01F;
            float oneSide = 2 - padding;
            float startX = Math.max(0, barFill - oneSide);
            // Start texture at x >= 0 (for small fill values, also start the texture so indicator is always centered)
            float startTexX = Math.max(padding, oneSide - barFill);
            // End texture at x <= barWidth and 4 <= startTexX + endTexX (total width of overlay texture). Cut off for large fill values.
            float endTexX = Math.min(2 * oneSide - startTexX, barWidth - barFill + oneSide);
            DrawUtils.drawModalRectWithCustomSizedTexture(x + startX, y, 1 + startTexX, 24, endTexX, barHeight, 80, 50);
        }
        // Overlay uncolored bar display next (texture packs can use this to overlay their own static bar colors)
        DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 1, 13, barWidth, barHeight, 80, 50);
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
            DrawUtils.drawCenteredText(title, (int) (halfWidth / scale), (int) (30 / scale), ColorCode.WHITE.getColor());
            GlStateManager.popMatrix();
            int y = 45;
            for (String line : textList) {
                DrawUtils.drawCenteredText(line, halfWidth, y, ColorCode.WHITE.getColor());
                y += 10;
            }

            main.getScheduler().schedule(Scheduler.CommandType.ERASE_UPDATE_MESSAGE, 10);

            if (!main.getUpdater().hasSentUpdateMessage()) {
                main.getUpdater().sendUpdateMessage();
            }
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
                    DrawUtils.drawModalRectWithCustomSizedTexture(x + tickers * 11, y, 0, 0, 9, 9, 18, 9, false);
                } else {
                    DrawUtils.drawModalRectWithCustomSizedTexture(x + tickers * 11, y, 9, 0, 9, 9, 18, 9, false);
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
        x = main.getRenderListener().transformXY(x, width, scale);
        y = main.getRenderListener().transformXY(y, height, scale);

        main.getUtils().enableStandardGLOptions();

        if (buttonLocation == null) {
            mc.ingameGUI.drawTexturedModalRect(x, y, 34, 9, width, height);
        } else {
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
        int color = main.getConfigValues().getColor(feature);
        if (feature == Feature.MANA_TEXT) {
            text = NUMBER_FORMAT.format(getAttribute(Attribute.MANA)) + "/" + NUMBER_FORMAT.format(getAttribute(Attribute.MAX_MANA));

        } else if (feature == Feature.OVERFLOW_MANA) {
            if (getAttribute(Attribute.OVERFLOW_MANA) != 0 || buttonLocation != null) {
                text = getAttribute(Attribute.OVERFLOW_MANA) + "ʬ";
            } else {
                return;
            }
        } else if (feature == Feature.HEALTH_TEXT) {
            if (mc.thePlayer.isPotionActive(22/* Absorption */)) {
                color = ColorUtils.getDummySkyblockColor(ColorCode.GOLD.getColor(), main.getConfigValues().getChromaFeatures().contains(feature)).getColor();
            }
            text = NUMBER_FORMAT.format(getAttribute(Attribute.HEALTH)) + "/" + NUMBER_FORMAT.format(getAttribute(Attribute.MAX_HEALTH));

        } else if (feature == Feature.DEFENCE_TEXT) {
            text = NUMBER_FORMAT.format(getAttribute(Attribute.DEFENCE));

        } else if (feature == Feature.OTHER_DEFENCE_STATS) {
            text = main.getPlayerListener().getActionBarParser().getOtherDefense();
            if (buttonLocation != null && (text == null || text.length() == 0)) {
                text = "|||  T3!";
            }
            if (text == null || text.length() == 0) {
                return;
            }

        } else if (feature == Feature.EFFECTIVE_HEALTH_TEXT) {
            text = NUMBER_FORMAT.format(Math.round(getAttribute(Attribute.HEALTH) * (1 + getAttribute(Attribute.DEFENCE) / 100F)));

        } else if (feature == Feature.DRILL_FUEL_TEXT) {
            if (!ItemUtils.isDrill(mc.thePlayer.getHeldItem())) {
                return;
            }
            text = (getAttribute(Attribute.FUEL) + "/" + getAttribute(Attribute.MAX_FUEL)).replaceAll("000$", "k");
        } else if (feature == Feature.DEFENCE_PERCENTAGE) {
            double doubleDefence = getAttribute(Attribute.DEFENCE);
            double percentage = ((doubleDefence / 100) / ((doubleDefence / 100) + 1)) * 100; //Taken from https://hypixel.net/threads/how-armor-works-and-the-diminishing-return-of-higher-defence.2178928/
            BigDecimal bigDecimal = new BigDecimal(percentage).setScale(1, RoundingMode.HALF_UP);
            text = bigDecimal + "%";

        } else if (feature == Feature.SPEED_PERCENTAGE) {
            String walkSpeed = NUMBER_FORMAT.format(Minecraft.getMinecraft().thePlayer.capabilities.getWalkSpeed() * 1000);
            text = walkSpeed.substring(0, Math.min(walkSpeed.length(), 3));

            if (text.endsWith(".")) text = text.substring(0, text.indexOf('.')); //remove trailing periods

            text += "%";

        } else if (feature == Feature.HEALTH_UPDATES) {
            Float healthUpdate = main.getPlayerListener().getHealthUpdate();
            if (buttonLocation == null) {
                if (healthUpdate != null) {
                    color = healthUpdate > 0 ? ColorCode.GREEN.getColor() : ColorCode.RED.getColor();
                    text = (healthUpdate > 0 ? "+" : "-") + Math.abs(healthUpdate);
                } else {
                    return;
                }
            } else {
                text = "+123";
                color = ColorCode.GREEN.getColor();
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
        } else if (feature == Feature.FARM_EVENT_TIMER) { // The timezone of the server, to avoid problems with like timezones that are 30 minutes ahead or whatnot.
            Calendar nextFarmEvent = Calendar.getInstance(TimeZone.getTimeZone("EST"));
            if (nextFarmEvent.get(Calendar.MINUTE) >= 15) {
                nextFarmEvent.add(Calendar.HOUR_OF_DAY, 1);
            }
            nextFarmEvent.set(Calendar.MINUTE, 15);
            nextFarmEvent.set(Calendar.SECOND, 0);
            int difference = (int) (nextFarmEvent.getTimeInMillis() - System.currentTimeMillis());
            int minutes = difference / 60000;
            int seconds = (int) Math.round((double) (difference % 60000) / 1000);
            if (minutes < 40) {
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
            } else{
                StringBuilder timestampActive = new StringBuilder();
                timestampActive.append("Active: ");
                if (minutes-40 < 10) {
                    timestampActive.append("0");
                }
                timestampActive.append(minutes-40).append(":");
                if (seconds < 10) {
                    timestampActive.append("0");
                }
                timestampActive.append(seconds);
                text = timestampActive.toString();
            }

        } else if (feature == Feature.SKILL_DISPLAY) {
            if (buttonLocation == null) {
                text = skillText;
                if (text == null) return;
            } else {
                StringBuilder previewBuilder = new StringBuilder();
                if (main.getConfigValues().isEnabled(Feature.SHOW_SKILL_XP_GAINED)) {
                    previewBuilder.append("+123 ");
                }
                if (main.getConfigValues().isEnabled(Feature.SHOW_SKILL_PERCENTAGE_INSTEAD_OF_XP)) {
                    previewBuilder.append("40% ");
                } else {
                    previewBuilder.append("(2000/5000) ");
                }
                if (main.getConfigValues().isEnabled(Feature.SKILL_ACTIONS_LEFT_UNTIL_NEXT_LEVEL)) {
                    previewBuilder.append(" - ").append(Translations.getMessage("messages.actionsLeft", 3000)).append(" ");
                }
                previewBuilder.setLength(previewBuilder.length() - 1);
                text = previewBuilder.toString();
            }
            if (buttonLocation == null) {
                int remainingTime = (int) (skillFadeOutTime - System.currentTimeMillis());

                if (remainingTime < 0) {
                    if (remainingTime < -1968) {
                        return; // Will be invisible, no need to render.
                    }

                    int textAlpha = Math.round(255 - (-remainingTime / 2000F * 255F));
                    color = main.getConfigValues().getColor(feature, textAlpha); // so it fades out, 0.016 is the minimum alpha
                }
            }

        } else if (feature == Feature.ZEALOT_COUNTER) {
            if (main.getConfigValues().isEnabled(Feature.ZEALOT_COUNTER_NEST_ONLY) && main.getUtils().getLocation() != Location.DRAGONS_NEST && buttonLocation == null) {
                return;
            }
            text = String.valueOf(main.getPersistentValuesManager().getPersistentValues().getKills());

        } else if (feature == Feature.SHOW_TOTAL_ZEALOT_COUNT) {
            if (main.getConfigValues().isEnabled(Feature.SHOW_TOTAL_ZEALOT_COUNT_NEST_ONLY) && main.getUtils().getLocation() != Location.DRAGONS_NEST && buttonLocation == null) {
                return;
            }
            if (main.getPersistentValuesManager().getPersistentValues().getTotalKills() <= 0) {
                text = String.valueOf(main.getPersistentValuesManager().getPersistentValues().getKills());
            } else {
                text = String.valueOf(main.getPersistentValuesManager().getPersistentValues().getTotalKills() + main.getPersistentValuesManager().getPersistentValues().getKills());
            }

        } else if (feature == Feature.SHOW_SUMMONING_EYE_COUNT) {
            if (main.getConfigValues().isEnabled(Feature.SHOW_SUMMONING_EYE_COUNT_NEST_ONLY) && main.getUtils().getLocation() != Location.DRAGONS_NEST && buttonLocation == null) {
                return;
            }
            text = String.valueOf(main.getPersistentValuesManager().getPersistentValues().getSummoningEyeCount());

        } else if (feature == Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE) {
            if (main.getConfigValues().isEnabled(Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE_NEST_ONLY) && main.getUtils().getLocation() != Location.DRAGONS_NEST && buttonLocation == null) {
                return;
            }
            int summoningEyeCount = main.getPersistentValuesManager().getPersistentValues().getSummoningEyeCount();

            if (summoningEyeCount > 0) {
                text = String.valueOf(Math.round(main.getPersistentValuesManager().getPersistentValues().getTotalKills() / (double) main.getPersistentValuesManager().getPersistentValues().getSummoningEyeCount()));
            } else {
                text = "0"; // Avoid zero division.
            }

        } else if (feature == Feature.BIRCH_PARK_RAINMAKER_TIMER) {
            long rainmakerTime = main.getPlayerListener().getRainmakerTimeEnd();

            if ((main.getUtils().getLocation() != Location.BIRCH_PARK || rainmakerTime == -1) && buttonLocation == null) {
                return;
            }

            int totalSeconds = (int) (rainmakerTime - System.currentTimeMillis()) / 1000;

            if (TabListParser.getParsedRainTime() != null) {
                text = TabListParser.getParsedRainTime();
            } else if (rainmakerTime != -1 && totalSeconds > 0) {
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

            DungeonMilestone dungeonMilestone = main.getDungeonManager().getDungeonMilestone();
            if (dungeonMilestone == null) {
                if (buttonLocation != null) {
                    dungeonMilestone = new DungeonMilestone(DungeonClass.HEALER);
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
            int deaths = main.getDungeonManager().getDeathCount();

            if (buttonLocation == null) {
                if (!main.getUtils().isInDungeon()) {
                    return;
                }
            }
            text = Integer.toString(deaths);

        } else if (feature == Feature.ROCK_PET_TRACKER) {
            text = String.valueOf(main.getPersistentValuesManager().getPersistentValues().getOresMined());

        } else if (feature == Feature.DOLPHIN_PET_TRACKER) {
            text = String.valueOf(main.getPersistentValuesManager().getPersistentValues().getSeaCreaturesKilled());

        } else if (feature == Feature.DUNGEONS_SECRETS_DISPLAY) {
            if (buttonLocation == null && !main.getUtils().isInDungeon()) {
                return;
            }

            text = "Secrets";
        } else if (feature == Feature.SPIRIT_SCEPTRE_DISPLAY) {
            ItemStack holdingItem = mc.thePlayer.getCurrentEquippedItem();
            ItemStack held = Minecraft.getMinecraft().thePlayer.getHeldItem();
            String skyblockItemID = ItemUtils.getSkyblockItemID(held);
            if (buttonLocation != null) {
                text = "Hyperion";
            } else if (holdingItem == null || skyblockItemID == null) {
                return;
            } else if (skyblockItemID.equals("HYPERION") || skyblockItemID.equals("VALKYRIE") || skyblockItemID.equals("ASTRAEA") || skyblockItemID.equals("SCYLLA") || skyblockItemID.equals("BAT_WAND")) {
                text = holdingItem.getDisplayName().replaceAll("§[a-f0-9]?✪", "");
            } else {
                return;
            }

        } else if (feature == Feature.CANDY_POINTS_COUNTER) {
            if (buttonLocation == null && !SpookyEventManager.isActive()) {
                return;
            }

            text = "Test";
        } else if (feature == Feature.FETCHUR_TODAY) {
            FetchurManager.FetchurItem fetchurItem = FetchurManager.getInstance().getCurrentFetchurItem();
            if (!FetchurManager.getInstance().hasFetchedToday() || buttonLocation != null) {
                if (main.getConfigValues().isEnabled(Feature.SHOW_FETCHUR_ITEM_NAME)) {
                    text = Message.MESSAGE_FETCHUR_TODAY.getMessage(fetchurItem.getItemStack().stackSize + "x " + fetchurItem.getItemText());
                } else {
                    text = Message.MESSAGE_FETCHUR_TODAY.getMessage("");
                }
            } else {
                text = ""; // If it has made fetchur, then no need for text
            }
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

        if (feature == Feature.MAGMA_BOSS_TIMER || feature == Feature.DARK_AUCTION_TIMER || feature == Feature.FARM_EVENT_TIMER || feature == Feature.ZEALOT_COUNTER || feature == Feature.SKILL_DISPLAY
                || feature == Feature.SHOW_TOTAL_ZEALOT_COUNT || feature == Feature.SHOW_SUMMONING_EYE_COUNT || feature == Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE ||
                feature == Feature.BIRCH_PARK_RAINMAKER_TIMER || feature == Feature.COMBAT_TIMER_DISPLAY || feature == Feature.ENDSTONE_PROTECTOR_DISPLAY ||
                feature == Feature.DUNGEON_DEATH_COUNTER || feature == Feature.DOLPHIN_PET_TRACKER || feature == Feature.ROCK_PET_TRACKER) {
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
            width += 16 + 2;
            height += 10;
        }

        if (feature == Feature.DUNGEONS_SECRETS_DISPLAY) {
            width += 16 + 2;
            height += 12;
        }

        if (feature == Feature.FETCHUR_TODAY) {
            if (main.getConfigValues().isDisabled(Feature.SHOW_FETCHUR_ITEM_NAME)) {
                width += 18;
                height += 9;
            }
        }

        if (feature == Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY) {
            int maxNumberWidth = mc.fontRendererObj.getStringWidth("99");
            width = 18 + 2 + maxNumberWidth + 5 + 18 + 2 + maxNumberWidth;
            height = 18 * (int) Math.ceil(EssenceType.values().length / 2F);
        }
        if (feature == Feature.SPIRIT_SCEPTRE_DISPLAY) {
            int maxNumberWidth = mc.fontRendererObj.getStringWidth("12345");
            width += 18 + maxNumberWidth;
            height += 20;
        }


        if (feature == Feature.CANDY_POINTS_COUNTER) {
            width = 0;

            Map<CandyType, Integer> candyCounts = SpookyEventManager.getCandyCounts();
            if (!SpookyEventManager.isActive()) {
                if (buttonLocation == null) {
                    return;
                }

                candyCounts = SpookyEventManager.getDummyCandyCounts();
            }

            int green = candyCounts.get(CandyType.GREEN);
            int purple = candyCounts.get(CandyType.PURPLE);
            if (buttonLocation != null || green > 0) {
                width += 16 + 1 + mc.fontRendererObj.getStringWidth(String.valueOf(green));
            }
            if (buttonLocation != null || purple > 0) {
                if (green > 0) {
                    width += 1;
                }

                width += 16 + 1 + mc.fontRendererObj.getStringWidth(String.valueOf(purple)) + 1;
            }
            height = 16 + 8;
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
            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

            FontRendererHook.setupFeatureFont(feature);
            DrawUtils.drawText(text, x + 18, y + 4, color);
            FontRendererHook.endFeatureFont();

        } else if (feature == Feature.FARM_EVENT_TIMER) {
            mc.getTextureManager().bindTexture(FARM_ICON);
            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

            FontRendererHook.setupFeatureFont(feature);
            DrawUtils.drawText(text, x + 18, y + 4, color);
            FontRendererHook.endFeatureFont();
        } else if (feature == Feature.MAGMA_BOSS_TIMER) {
            mc.getTextureManager().bindTexture(MAGMA_BOSS_ICON);
            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

            FontRendererHook.setupFeatureFont(feature);
            DrawUtils.drawText(text, x + 18, y + 4, color);
            FontRendererHook.endFeatureFont();

        } else if (feature == Feature.ZEALOT_COUNTER) {
            mc.getTextureManager().bindTexture(ENDERMAN_ICON);
            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

            FontRendererHook.setupFeatureFont(feature);
            DrawUtils.drawText(text, x + 18, y + 4, color);
            FontRendererHook.endFeatureFont();

        } else if (feature == Feature.SHOW_TOTAL_ZEALOT_COUNT) {
            mc.getTextureManager().bindTexture(ENDERMAN_GROUP_ICON);
            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

            FontRendererHook.setupFeatureFont(feature);
            DrawUtils.drawText(text, x + 18, y + 4, color);
            FontRendererHook.endFeatureFont();

        } else if (feature == Feature.SHOW_SUMMONING_EYE_COUNT) {
            mc.getTextureManager().bindTexture(SUMMONING_EYE_ICON);
            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

            FontRendererHook.setupFeatureFont(feature);
            DrawUtils.drawText(text, x + 18, y + 4, color);
            FontRendererHook.endFeatureFont();

        } else if (feature == Feature.SHOW_AVERAGE_ZEALOTS_PER_EYE) {
            mc.getTextureManager().bindTexture(ZEALOTS_PER_EYE_ICON);
            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);
            mc.getTextureManager().bindTexture(SLASH_ICON);
            ColorUtils.bindColor(color);
            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16, true);

            FontRendererHook.setupFeatureFont(feature);
            DrawUtils.drawText(text, x + 18, y + 4, color);
            FontRendererHook.endFeatureFont();

        } else if (feature == Feature.SKILL_DISPLAY && ((skill != null && skill.getItem() != null) || buttonLocation != null)) {
            renderItem(buttonLocation == null ? skill.getItem() : SkillType.FARMING.getItem(), x, y);

            FontRendererHook.setupFeatureFont(feature);
            DrawUtils.drawText(text, x + 18, y + 4, color);
            FontRendererHook.endFeatureFont();

        } else if (feature == Feature.BIRCH_PARK_RAINMAKER_TIMER) {
            renderItem(WATER_BUCKET, x, y);

            FontRendererHook.setupFeatureFont(feature);
            DrawUtils.drawText(text, x + 18, y + 4, color);
            FontRendererHook.endFeatureFont();

        } else if (feature == Feature.COMBAT_TIMER_DISPLAY) {
            long lastDamaged = main.getUtils().getLastDamaged() + 5000;
            int combatSeconds = (int) Math.ceil((lastDamaged - System.currentTimeMillis()) / 1000D);

            if (buttonLocation != null) {
                combatSeconds = 5;
            }

            renderItem(IRON_SWORD, x, y);

            FontRendererHook.setupFeatureFont(feature);
            DrawUtils.drawText(text, x + 18, y + 4, color);
            FontRendererHook.endFeatureFont();

            y += 20;

            String warpTimeRemaining = combatSeconds + "s";
            String menuTimeRemaining = (combatSeconds - 2) + "s";
            if (combatSeconds <= 2) {
                menuTimeRemaining = "✔";
            }
            int menuTimeRemainingWidth = mc.fontRendererObj.getStringWidth(menuTimeRemaining);

            int spacerBetweenBothItems = 4;
            int spacerBetweenItemsAndText = 2;

            renderItem(SKYBLOCK_MENU, x + width / 2F - 16 - menuTimeRemainingWidth - spacerBetweenItemsAndText - spacerBetweenBothItems / 2F, y - 5);

            FontRendererHook.setupFeatureFont(feature);
            DrawUtils.drawText(menuTimeRemaining, x + width / 2F - menuTimeRemainingWidth - spacerBetweenBothItems / 2F, y, color);
            FontRendererHook.endFeatureFont();

            GlStateManager.color(1, 1, 1, 1);
            renderItem(WARP_SKULL, x + width / 2F + spacerBetweenBothItems / 2F, y - 5);
            FontRendererHook.setupFeatureFont(feature);
            DrawUtils.drawText(warpTimeRemaining, x + width / 2F + spacerBetweenBothItems / 2F + 13 + spacerBetweenItemsAndText, y, color);
            FontRendererHook.endFeatureFont();

        } else if (feature == Feature.ENDSTONE_PROTECTOR_DISPLAY) {
            mc.getTextureManager().bindTexture(IRON_GOLEM_ICON);
            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

            FontRendererHook.setupFeatureFont(feature);
            DrawUtils.drawText(text, x + 18, y + 4, color);
            FontRendererHook.endFeatureFont();

            x += 16 + 2 + mc.fontRendererObj.getStringWidth(text) + 2;

            GlStateManager.color(1, 1, 1, 1);
            mc.getTextureManager().bindTexture(ENDERMAN_GROUP_ICON);
            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, 16, 16, 16, 16);

            int count = EndstoneProtectorManager.getZealotCount();

            FontRendererHook.setupFeatureFont(feature);
            DrawUtils.drawText(String.valueOf(count), x + 16 + 2, y + 4, color);
            FontRendererHook.endFeatureFont();

        } else if (feature == Feature.SHOW_DUNGEON_MILESTONE) {
            DungeonMilestone dungeonMilestone = main.getDungeonManager().getDungeonMilestone();
            if (buttonLocation != null) {
                dungeonMilestone = new DungeonMilestone(DungeonClass.HEALER);
            }

            renderItem(dungeonMilestone.getDungeonClass().getItem(), x, y);
            FontRendererHook.setupFeatureFont(feature);
            DrawUtils.drawText(text, x + 18, y, color);
            double amount = Double.parseDouble(dungeonMilestone.getValue());
            DecimalFormat formatter = new DecimalFormat("#,###");
            DrawUtils.drawText(formatter.format(amount), x + 18 + mc.fontRendererObj.getStringWidth(text) / 2F
                    - mc.fontRendererObj.getStringWidth(formatter.format(amount)) / 2F, y + 9, color);
            FontRendererHook.endFeatureFont();

        } else if (feature == Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY) {
            this.drawCollectedEssences(x, y, buttonLocation != null, true);

        } else if (feature == Feature.DUNGEON_DEATH_COUNTER) {
            renderItem(SKULL, x, y);
            FontRendererHook.setupFeatureFont(feature);
            DrawUtils.drawText(text, x + 18, y + 4, color);
            FontRendererHook.endFeatureFont();
        } else if (feature == Feature.ROCK_PET_TRACKER) {
            renderItem(PET_ROCK, x, y);

            FontRendererHook.setupFeatureFont(feature);
            DrawUtils.drawText(text, x + 18, y + 4, color);
            FontRendererHook.endFeatureFont();

        } else if (feature == Feature.DOLPHIN_PET_TRACKER) {
            renderItem(DOLPHIN_PET, x, y);

            FontRendererHook.setupFeatureFont(feature);
            DrawUtils.drawText(text, x + 18, y + 4, color);
            FontRendererHook.endFeatureFont();

        } else if (feature == Feature.DUNGEONS_SECRETS_DISPLAY) {
            int secrets = main.getDungeonManager().getSecrets();
            int maxSecrets = main.getDungeonManager().getMaxSecrets();

            FontRendererHook.setupFeatureFont(feature);
            DrawUtils.drawText(text, x + 16 + 2, y, color);
            FontRendererHook.endFeatureFont();

            if (secrets == -1 && buttonLocation != null) {
                secrets = 5;
                maxSecrets = 10;
            }

            if (secrets == -1 | maxSecrets == 0) {
                FontRendererHook.setupFeatureFont(feature);
                String none = Translations.getMessage("messages.none");
                DrawUtils.drawText(none, x + 16 + 2 + mc.fontRendererObj.getStringWidth(text) / 2F - mc.fontRendererObj.getStringWidth(none) / 2F, y + 10, color);
                FontRendererHook.endFeatureFont();
            } else {
                if (secrets > maxSecrets) {
                    // Assume the max secrets equals to found secrets
                    maxSecrets = secrets;
                }

                float percent = secrets / (float) maxSecrets;
                if (percent < 0) {
                    percent = 0;
                } else if (percent > 1) {
                    percent = 1;
                }

                float r;
                float g;
                if (percent <= 0.5) { // Fade from red -> yellow
                    r = 1;
                    g = (percent * 2) * 0.66F + 0.33F;
                } else { // Fade from yellow -> green
                    r = (1 - percent) * 0.66F + 0.33F;
                    g = 1;
                }
                int secretsColor = new Color(Math.min(1, r), g, 0.33F).getRGB();

                float secretsWidth = mc.fontRendererObj.getStringWidth(String.valueOf(secrets));
                float slashWidth = mc.fontRendererObj.getStringWidth("/");
                float maxSecretsWidth = mc.fontRendererObj.getStringWidth(String.valueOf(maxSecrets));

                float totalWidth = secretsWidth + slashWidth + maxSecretsWidth;

                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText("/", x + 16 + 2 + mc.fontRendererObj.getStringWidth(text) / 2F - totalWidth / 2F + secretsWidth, y + 11, color);
                FontRendererHook.endFeatureFont();

                DrawUtils.drawText(String.valueOf(secrets), x + 16 + 2 + mc.fontRendererObj.getStringWidth(text) / 2F - totalWidth / 2F, y + 11, secretsColor);
                DrawUtils.drawText(String.valueOf(maxSecrets), x + 16 + 2 + mc.fontRendererObj.getStringWidth(text) / 2F - totalWidth / 2F + secretsWidth + slashWidth, y + 11, secretsColor);
            }

            GlStateManager.color(1, 1, 1, 1);
            renderItem(CHEST, x, y);

        } else if (feature == Feature.SPIRIT_SCEPTRE_DISPLAY) {
            int hitEnemies = main.getPlayerListener().getSpiritSceptreHitEnemies();
            float dealtDamage = main.getPlayerListener().getSpiritSceptreDealtDamage();
            FontRendererHook.setupFeatureFont(feature);
            DrawUtils.drawText(text, x + 16 + 2, y, color);
            if (hitEnemies == 1) {
                DrawUtils.drawText(String.format("%d enemy hit", hitEnemies), x + 16 + 2, y + 9, color);
            }
            else {
                DrawUtils.drawText(String.format("%d enemies hit", hitEnemies), x + 16 + 2, y + 9, color);
            }
            DrawUtils.drawText(String.format("%,d damage dealt", Math.round(dealtDamage)), x + 16 + 2, y + 18, color);
            FontRendererHook.endFeatureFont();
            ItemStack held = Minecraft.getMinecraft().thePlayer.getHeldItem();
            String skyblockItemID = ItemUtils.getSkyblockItemID(held);
            if (buttonLocation != null) {
                renderItem(HYPERION, x, y);
            } else if (skyblockItemID.equals("HYPERION")) {
                renderItem(HYPERION, x, y);
            } else if (skyblockItemID.equals("VALKYRIE")) {
                renderItem(VALKYRIE, x, y);
            } else if (skyblockItemID.equals("ASTRAEA")) {
                renderItem(ASTRAEA, x, y);
            } else if (skyblockItemID.equals("SCYLLA")) {
                renderItem(SCYLLA, x, y);
            } else if (skyblockItemID.equals("BAT_WAND")) {
                renderItem(SCPETRE, x, y);
            }

        } else if (feature == Feature.CANDY_POINTS_COUNTER) {
            Map<CandyType, Integer> candyCounts = SpookyEventManager.getCandyCounts();
            if (!SpookyEventManager.isActive()) {
                candyCounts = SpookyEventManager.getDummyCandyCounts();
            }
            int green = candyCounts.get(CandyType.GREEN);
            int purple = candyCounts.get(CandyType.PURPLE);

            int points = SpookyEventManager.getPoints();
            if (!SpookyEventManager.isActive()) {
                points = 5678;
            }

            float currentX = x;
            if (buttonLocation != null || green > 0) {
                renderItem(GREEN_CANDY, currentX, y);

                currentX += 16 + 1;
                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(String.valueOf(green), currentX, y + 4, color);
                FontRendererHook.endFeatureFont();
            }
            if (buttonLocation != null || purple > 0) {
                if (buttonLocation != null || green > 0) {
                    currentX += mc.fontRendererObj.getStringWidth(String.valueOf(green)) + 1;
                }

                renderItem(PURPLE_CANDY, currentX, y);

                currentX += 16 + 1;
                FontRendererHook.setupFeatureFont(feature);
                DrawUtils.drawText(String.valueOf(purple), currentX, y + 4, color);
                FontRendererHook.endFeatureFont();
            }

            FontRendererHook.setupFeatureFont(feature);
            text = points + " Points";
            DrawUtils.drawText(text, x + width / 2F - mc.fontRendererObj.getStringWidth(text) / 2F, y + 16, color);
            FontRendererHook.endFeatureFont();

        } else if (feature == Feature.FETCHUR_TODAY) {
            boolean showDwarven = main.getConfigValues().isDisabled(Feature.SHOW_FETCHUR_ONLY_IN_DWARVENS) ||
                    LocationUtils.isInDwarvenMines(main.getUtils().getLocation().getScoreboardName());
            boolean showInventory = main.getConfigValues().isDisabled(Feature.SHOW_FETCHUR_INVENTORY_OPEN_ONLY) ||
                    Minecraft.getMinecraft().currentScreen != null;
            FetchurManager.FetchurItem fetchurItem = FetchurManager.getInstance().getCurrentFetchurItem();
            // Show if it's the gui button position, or the player hasn't given Fetchur, and it shouldn't be hidden b/c of dwarven mines or inventory
            if (fetchurItem != null && (buttonLocation != null ||
                    (!FetchurManager.getInstance().hasFetchedToday() && showDwarven && showInventory))) {

                FontRendererHook.setupFeatureFont(feature);

                if (main.getConfigValues().isDisabled(Feature.SHOW_FETCHUR_ITEM_NAME)) {
                    DrawUtils.drawText(text, x + 1, y + 4, color); // Line related to the "Fetchur wants" text
                    float offsetX = Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
                    renderItemAndOverlay(fetchurItem.getItemStack(), String.valueOf(fetchurItem.getItemStack().stackSize), x + offsetX, y);
                } else {
                    DrawUtils.drawText(text, x, y, color); // Line related to the "Fetchur wants" text
                }
                FontRendererHook.endFeatureFont();
            }
        } else {
            FontRendererHook.setupFeatureFont(feature);
            DrawUtils.drawText(text, x, y, color);
            FontRendererHook.endFeatureFont();
        }

        main.getUtils().restoreGLOptions();
    }

    public void drawCollectedEssences(float x, float y, boolean usePlaceholders, boolean hideZeroes) {
        Minecraft mc = Minecraft.getMinecraft();

        float currentX = x;
        float currentY;

        int maxNumberWidth = mc.fontRendererObj.getStringWidth("99");

        int color = main.getConfigValues().getColor(Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY);

        int count = 0;
        if (main.getConfigValues().isEnabled(Feature.SHOW_SALVAGE_ESSENCES_COUNTER)) {
            for (EssenceType essenceType : EssenceType.values()) {
                int value;

                if (main.getInventoryUtils().getInventoryType() == InventoryType.SALVAGING) {
                    value = main.getDungeonManager().getSalvagedEssences().getOrDefault(essenceType, 0);
                } else {
                    value = main.getDungeonManager().getCollectedEssences().getOrDefault(essenceType, 0);
                }

                if (usePlaceholders) {
                    value = 99;
                } else if (value <= 0 && hideZeroes) {
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
                DrawUtils.drawModalRectWithCustomSizedTexture(currentX, currentY, 0, 0, 16, 16, 16, 16);

                FontRendererHook.setupFeatureFont(Feature.DUNGEONS_COLLECTED_ESSENCES_DISPLAY);
                DrawUtils.drawText(String.valueOf(value), currentX + 18 + 2, currentY + 5, color);
                FontRendererHook.endFeatureFont();

                count++;
            }
        }
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
            renderItem(entry.getKey().getItemStack(), x, y);

            int color = main.getConfigValues().getColor(Feature.BAIT_LIST);
            FontRendererHook.setupFeatureFont(Feature.BAIT_LIST);
            DrawUtils.drawText(String.valueOf(entry.getValue()), x + iconSize + spacing, y + (iconSize / 2F) - (8 / 2F), color);
            FontRendererHook.endFeatureFont();

            y += iconSize;
        }

        main.getUtils().restoreGLOptions();
    }

    public void drawSlayerTrackers(Feature feature, Minecraft mc, float scale, ButtonLocation buttonLocation) {
        boolean colorByRarity;
        boolean textMode;
        SlayerBoss slayerBoss;
        EnumUtils.SlayerQuest quest = main.getUtils().getSlayerQuest();
        Location location = main.getUtils().getLocation();
        ConfigValues config = main.getConfigValues();
        if (feature == Feature.REVENANT_SLAYER_TRACKER) {
            if (buttonLocation == null && config.isEnabled(Feature.HIDE_WHEN_NOT_IN_CRYPTS) &&
                    (quest != EnumUtils.SlayerQuest.REVENANT_HORROR || location != Location.GRAVEYARD && location != Location.COAL_MINE)) {
                return;
            }

            colorByRarity = config.isEnabled(Feature.REVENANT_COLOR_BY_RARITY);
            textMode = config.isEnabled(Feature.REVENANT_TEXT_MODE);
            slayerBoss = SlayerBoss.REVENANT;
        } else if (feature == Feature.TARANTULA_SLAYER_TRACKER) {
            if (buttonLocation == null && config.isEnabled(Feature.HIDE_WHEN_NOT_IN_SPIDERS_DEN) &&
                    (quest != EnumUtils.SlayerQuest.TARANTULA_BROODFATHER || location != Location.SPIDERS_DEN)) {
                return;
            }

            colorByRarity = config.isEnabled(Feature.TARANTULA_COLOR_BY_RARITY);
            textMode = config.isEnabled(Feature.TARANTULA_TEXT_MODE);
            slayerBoss = SlayerBoss.TARANTULA;
        } else if (feature == Feature.SVEN_SLAYER_TRACKER) {
            if (buttonLocation == null && config.isEnabled(Feature.HIDE_WHEN_NOT_IN_CASTLE) &&
                    (quest != EnumUtils.SlayerQuest.SVEN_PACKMASTER || (location != Location.RUINS && location != Location.HOWLING_CAVE))) {
                return;
            }

            colorByRarity = config.isEnabled(Feature.SVEN_COLOR_BY_RARITY);
            textMode = config.isEnabled(Feature.SVEN_TEXT_MODE);
            slayerBoss = SlayerBoss.SVEN;
        } else if (feature == Feature.VOIDGLOOM_SLAYER_TRACKER) {
            if (buttonLocation == null && config.isEnabled(Feature.HIDE_WHEN_NOT_IN_END) &&
                    (quest != EnumUtils.SlayerQuest.VOIDGLOOM_SERAPH || (location != Location.THE_END && location != Location.DRAGONS_NEST && location != Location.VOID_SEPULTURE))) {
                return;
            }

            colorByRarity = config.isEnabled(Feature.ENDERMAN_COLOR_BY_RARITY);
            textMode = config.isEnabled(Feature.ENDERMAN_TEXT_MODE);
            slayerBoss = SlayerBoss.VOIDGLOOM;
        } else {
            return;
        }

        float x = config.getActualX(feature);
        float y = config.getActualY(feature);
        int color = config.getColor(feature);

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

            FontRendererHook.setupFeatureFont(feature);

            DrawUtils.drawText(slayerBoss.getDisplayName(), x, y, color);
            y += lineHeight + spacer;
            DrawUtils.drawText(Translations.getMessage("slayerTracker.bossesKilled"), x, y, color);
            String text = String.valueOf(SlayerTracker.getInstance().getSlayerKills(slayerBoss));
            DrawUtils.drawText(text, x + width - mc.fontRendererObj.getStringWidth(text), y, color);
            y += lineHeight + spacer;

            FontRendererHook.endFeatureFont();

            for (SlayerDrop slayerDrop : slayerBoss.getDrops()) {

                int currentColor = color;
                if (colorByRarity) {
                    currentColor = slayerDrop.getRarity().getColorCode().getColor();
                } else {
                    FontRendererHook.setupFeatureFont(feature);
                }

                DrawUtils.drawText(slayerDrop.getDisplayName(), x, y, currentColor);

                if (!colorByRarity) {
                    FontRendererHook.endFeatureFont();
                }

                FontRendererHook.setupFeatureFont(feature);
                text = String.valueOf(SlayerTracker.getInstance().getDropCount(slayerDrop));
                DrawUtils.drawText(text, x + width - mc.fontRendererObj.getStringWidth(text), y, currentColor);
                FontRendererHook.endFeatureFont();

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
            } else if (feature == Feature.VOIDGLOOM_SLAYER_TRACKER) {
                entityRenderY = 25;
                textCenterX = 20;
            } else {
                entityRenderY = 36;
                textCenterX = 15;
            }

            int iconWidth = 16;

            int entityWidth = textCenterX * 2;
            int entityIconSpacingHorizontal = 2;
            int iconTextOffset = -2;
            int row = 0;
            int column = 0;
            int maxItemsPerRow = (int) Math.ceil(slayerBoss.getDrops().size() / 3.0);
            int[] maxTextWidths = new int[maxItemsPerRow];
            for (SlayerDrop slayerDrop : slayerBoss.getDrops()) {
                int width = mc.fontRendererObj.getStringWidth(TextUtils.abbreviate(SlayerTracker.getInstance().getDropCount(slayerDrop)));

                maxTextWidths[column] = Math.max(maxTextWidths[column], width);

                column++;
                if (column == maxItemsPerRow) {
                    column = 0;
                    row++;
                }
            }

            int totalColumnWidth = 0;
            for (int i : maxTextWidths) {
                totalColumnWidth += i;
            }
            int iconSpacingVertical = 4;

            int width = entityWidth + entityIconSpacingHorizontal + maxItemsPerRow * iconWidth + totalColumnWidth + iconTextOffset;
            int height = (iconWidth + iconSpacingVertical) * 3 - iconSpacingVertical;

            x = transformXY(x, width, scale);
            y = transformXY(y, height, scale);

            if (buttonLocation != null) {
                buttonLocation.checkHoveredAndDrawBox(x, x + width, y, y + height, scale);
            }

            if (feature == Feature.REVENANT_SLAYER_TRACKER) {
                if (revenant == null) {
                    revenant = new EntityZombie(Utils.getDummyWorld());

                    revenant.getInventory()[0] = ItemUtils.createItemStack(Items.diamond_hoe, true);
                    revenant.getInventory()[1] = ItemUtils.createItemStack(Items.diamond_boots, false);
                    revenant.getInventory()[2] = ItemUtils.createItemStack(Items.diamond_leggings, true);
                    revenant.getInventory()[3] = ItemUtils.createItemStack(Items.diamond_chestplate, true);
                    revenant.getInventory()[4] = ItemUtils.createSkullItemStack(null, null, "45012ee3-29fd-42ed-908b-648c731c7457", "1fc0184473fe882d2895ce7cbc8197bd40ff70bf10d3745de97b6c2a9c5fc78f");
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

            } else if (feature == Feature.VOIDGLOOM_SLAYER_TRACKER) {
                if (enderman == null) {
                    enderman = new EntityEnderman(Utils.getDummyWorld());

                    enderman.setHeldBlockState(Blocks.beacon.getBlockState().getBaseState());
                }
                GlStateManager.color(1, 1, 1, 1);
                enderman.ticksExisted = (int) main.getNewScheduler().getTotalTicks();
                GlStateManager.scale(.7, .7, 1);
                drawEntity(enderman, (x + 15) / .7F, (y + 51) / .7F, -30);
                GlStateManager.scale(1 / .7, 1 / .7, 1);

            } else {
                if (sven == null) {
                    sven = new EntityWolf(Utils.getDummyWorld());
                    sven.setAngry(true);
                }
                GlStateManager.color(1, 1, 1, 1);
                drawEntity(sven, x + 17, y + 38, -35);
            }

            GlStateManager.disableDepth();
            FontRendererHook.setupFeatureFont(feature);
            String text = TextUtils.abbreviate(SlayerTracker.getInstance().getSlayerKills(slayerBoss)) + " Kills";
            DrawUtils.drawText(text, x + textCenterX - mc.fontRendererObj.getStringWidth(text) / 2F, y + entityRenderY, color);
            FontRendererHook.endFeatureFont();

            row = 0;
            column = 0;
            float currentX = x + entityIconSpacingHorizontal + entityWidth;
            for (SlayerDrop slayerDrop : slayerBoss.getDrops()) {
                if (column > 0) {
                    currentX += iconWidth + maxTextWidths[column - 1];
                }

                float currentY = y + row * (iconWidth + iconSpacingVertical);

                GlStateManager.color(1, 1, 1, 1);
                renderItem(slayerDrop.getItemStack(), currentX, currentY);

                GlStateManager.disableDepth();

                int currentColor = color;
                if (colorByRarity) {
                    currentColor = slayerDrop.getRarity().getColorCode().getColor();
                } else {
                    FontRendererHook.setupFeatureFont(feature);
                }

                DrawUtils.drawText(TextUtils.abbreviate(SlayerTracker.getInstance().getDropCount(slayerDrop)), currentX + iconWidth + iconTextOffset, currentY + 8, currentColor);
                if (!colorByRarity) {
                    FontRendererHook.endFeatureFont();
                }

                column++;
                if (column == maxItemsPerRow) {
                    currentX = x + entityIconSpacingHorizontal + entityWidth;
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
            recentDragons = DragonTracker.getDummyDragons();
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

        int color = main.getConfigValues().getColor(Feature.DRAGON_STATS_TRACKER);

        if (textMode) {
            FontRendererHook.setupFeatureFont(Feature.DRAGON_STATS_TRACKER);
            DrawUtils.drawText(Translations.getMessage("dragonTracker.recentDragons"), x, y, color);
            y += 8 + spacerHeight;
            FontRendererHook.endFeatureFont();

            for (DragonType dragon : recentDragons) {
                int currentColor = color;
                if (colorByRarity) {
                    currentColor = dragon.getColor().getColor();
                } else {
                    FontRendererHook.setupFeatureFont(Feature.DRAGON_STATS_TRACKER);
                }

                DrawUtils.drawText(dragon.getDisplayName(), x, y, currentColor);

                if (!colorByRarity) {
                    FontRendererHook.endFeatureFont();
                }

                y += 8;
            }
            y += spacerHeight;

            FontRendererHook.setupFeatureFont(Feature.DRAGON_STATS_TRACKER);
            color = main.getConfigValues().getColor(Feature.DRAGON_STATS_TRACKER);
            DrawUtils.drawText(Translations.getMessage("dragonTracker.dragonsSince"), x, y, color);
            y += 8 + spacerHeight;
            FontRendererHook.endFeatureFont();

            for (DragonsSince dragonsSince : DragonsSince.values()) {
                GlStateManager.disableDepth();
                GlStateManager.enableBlend();
                GlStateManager.color(1, 1, 1, 1F);
                GlStateManager.disableBlend();
                GlStateManager.enableDepth();

                int currentColor = color;
                if (colorByRarity) {
                    currentColor = dragonsSince.getItemRarity().getColorCode().getColor();
                } else {
                    FontRendererHook.setupFeatureFont(Feature.DRAGON_STATS_TRACKER);
                }

                DrawUtils.drawText(dragonsSince.getDisplayName(), x, y, currentColor);

                if (!colorByRarity) {
                    FontRendererHook.endFeatureFont();
                }

                FontRendererHook.setupFeatureFont(Feature.DRAGON_STATS_TRACKER);
                int dragonsSinceValue = DragonTracker.getInstance().getDragsSince(dragonsSince);
                String text = dragonsSinceValue == 0 ? never : String.valueOf(dragonsSinceValue);
                DrawUtils.drawText(text, x + width - mc.fontRendererObj.getStringWidth(text), y, color);
                y += 8;
                FontRendererHook.endFeatureFont();
            }
        }
    }

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

        int color = main.getConfigValues().getColor(Feature.SLAYER_INDICATOR);

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
            FontRendererHook.setupFeatureFont(Feature.SLAYER_INDICATOR);
            DrawUtils.drawText(progress.getPercent() + "% (", currentX, fixedY + 5, color);
            FontRendererHook.endFeatureFont();

            currentX += mc.fontRendererObj.getStringWidth(progress.getPercent()+"% (");
            DrawUtils.drawText(progress.getDefence(), currentX, fixedY + 5, 0xFFFFFFFF);

            currentX += mc.fontRendererObj.getStringWidth(progress.getDefence());
            FontRendererHook.setupFeatureFont(Feature.SLAYER_INDICATOR);
            DrawUtils.drawText(")", currentX, fixedY + 5, color);
            FontRendererHook.endFeatureFont();

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
            if (potionTimers.isEmpty() && powerupTimers.isEmpty() && TabEffectManager.getInstance().getEffectCount() == 0) {
                return;
            }
        } else { // When editing GUI draw dummy timers.
            potionTimers = TabEffectManager.getDummyPotionTimers();
            powerupTimers = TabEffectManager.getDummyPowerupTimers();
        }

        EnumUtils.AnchorPoint anchorPoint = main.getConfigValues().getAnchorPoint(Feature.TAB_EFFECT_TIMERS);
        boolean topDown = (anchorPoint == EnumUtils.AnchorPoint.TOP_LEFT || anchorPoint == EnumUtils.AnchorPoint.TOP_RIGHT);

        int totalEffects = TabEffectManager.getDummyPotionTimers().size() + TabEffectManager.getDummyPowerupTimers().size() + 1; // + 1 to account for the "x Effects Active" line
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

        int color = main.getConfigValues().getColor(Feature.TAB_EFFECT_TIMERS);

        Minecraft mc = Minecraft.getMinecraft();

        // Draw the "x Effects Active" line
        FontRendererHook.setupFeatureFont(Feature.TAB_EFFECT_TIMERS);
        int effectCount = TabEffectManager.getInstance().getEffectCount();
        String text = effectCount == 1 ? Message.MESSAGE_ONE_EFFECT_ACTIVE.getMessage() :
                Message.MESSAGE_EFFECTS_ACTIVE.getMessage(String.valueOf(effectCount));
        float lineY;
        if (topDown) {
            lineY = y;
        } else {
            lineY = y + height - 8;
        }
        if (alignRight) {
            DrawUtils.drawText(text, x + width - mc.fontRendererObj.getStringWidth(text), lineY, color);
        } else {
            DrawUtils.drawText(text, x, lineY, color);
        }
        FontRendererHook.endFeatureFont();

        int drawnCount = 1; // 1 to account for the line above
        for (TabEffect potion : potionTimers) {
            if (topDown) {
                lineY = y + drawnCount * lineHeight;
            } else {
                lineY = y + height - drawnCount * lineHeight - 8;
            }

            String effect = potion.getEffect();
            String duration = potion.getDurationForDisplay();

            if (alignRight) {
                FontRendererHook.setupFeatureFont(Feature.TAB_EFFECT_TIMERS);
                DrawUtils.drawText(duration+" ", x + width - mc.fontRendererObj.getStringWidth(duration+" ")
                        - mc.fontRendererObj.getStringWidth(effect.trim()), lineY, color);
                FontRendererHook.endFeatureFont();
                DrawUtils.drawText(effect.trim(), x + width - mc.fontRendererObj.getStringWidth(effect.trim()), lineY, color);
            } else {
                DrawUtils.drawText(effect, x, lineY, color);
                FontRendererHook.setupFeatureFont(Feature.TAB_EFFECT_TIMERS);
                DrawUtils.drawText(duration, x+mc.fontRendererObj.getStringWidth(effect), lineY, color);
                FontRendererHook.endFeatureFont();
            }
            drawnCount++;
        }
        for (TabEffect powerUp : powerupTimers) {
            if (topDown) {
                lineY = y + spacer + drawnCount * lineHeight;
            } else {
                lineY = y + height - drawnCount * lineHeight - spacer - 8;
            }

            String effect = powerUp.getEffect();
            String duration = powerUp.getDurationForDisplay();

            if (alignRight) {
                FontRendererHook.setupFeatureFont(Feature.TAB_EFFECT_TIMERS);
                DrawUtils.drawText(duration+" ", x + width - mc.fontRendererObj.getStringWidth(duration+" ")
                        - mc.fontRendererObj.getStringWidth(effect.trim()), lineY, color);
                FontRendererHook.endFeatureFont();
                DrawUtils.drawText(effect, x + width - mc.fontRendererObj.getStringWidth(effect.trim()), lineY, color);
            } else {
                DrawUtils.drawText(effect, x, lineY, color);
                FontRendererHook.setupFeatureFont(Feature.TAB_EFFECT_TIMERS);
                DrawUtils.drawText(duration, x+mc.fontRendererObj.getStringWidth(effect), lineY, color);
                FontRendererHook.endFeatureFont();
            }
            drawnCount++;
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

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
    }

    private void renderItemAndOverlay(ItemStack item, String name, float x, float y) {
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableDepth();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(item, 0, 0);
        Minecraft.getMinecraft().getRenderItem().renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRendererObj, item, 0, 0, name);
        GlStateManager.popMatrix();

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

            DrawUtils.drawText(text, x, stringY, 0xFFFFFFFF);
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

        Entity entity = null;
        if (PowerOrbManager.getInstance().getActivePowerOrb() != null && PowerOrbManager.getInstance().getActivePowerOrb().getUuid() != null) {
            entity = Utils.getEntityByUUID(PowerOrbManager.getInstance().getActivePowerOrb().getUuid());
        }

        if (entity == null && buttonLocation != null) {
            entity = getRadiantDummyArmorStand();
        }

        main.getUtils().enableStandardGLOptions();

        if (entity instanceof EntityArmorStand) {
            drawPowerOrbArmorStand((EntityArmorStand) entity, x + 1, y + 4);
        } else {
            mc.getTextureManager().bindTexture(powerOrb.getResourceLocation());
            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, iconSize, iconSize, iconSize, iconSize);
        }

        DrawUtils.drawText(secondsString, x + spacing + iconSize, y + (iconSize / 2F) - (8 / 2F), ColorCode.WHITE.getColor(255));

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

        float maxHealth = main.getUtils().getAttributes().get(Attribute.MAX_HEALTH).getValue();
        float healthRegen = (float) (maxHealth * powerOrb.getHealthRegen());
        if (main.getUtils().getSlayerQuest() == EnumUtils.SlayerQuest.TARANTULA_BROODFATHER && main.getUtils().getSlayerQuestLevel() >= 2) {
            healthRegen *= 0.5; // Tarantula boss 2+ reduces healing by 50%.
        }
        double healIncrease = powerOrb.getHealIncrease() * 100;

        List<String> display = new LinkedList<>();
        display.add(String.format("§c+%s ❤/s", TextUtils.formatDouble(healthRegen)));
        if (powerOrb.getManaRegen() > 0) {
            float maxMana = main.getUtils().getAttributes().get(Attribute.MAX_MANA).getValue();
            float manaRegen = (float) Math.floor(maxMana / 50);
            manaRegen = (float) (manaRegen + manaRegen * powerOrb.getManaRegen());
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

        Entity entity = null;
        if (PowerOrbManager.getInstance().getActivePowerOrb() != null && PowerOrbManager.getInstance().getActivePowerOrb().getUuid() != null) {
            entity = Utils.getEntityByUUID(PowerOrbManager.getInstance().getActivePowerOrb().getUuid());
        }

        if (entity == null && buttonLocation != null) {
            entity = getRadiantDummyArmorStand();
        }

        main.getUtils().enableStandardGLOptions();

        if (entity instanceof EntityArmorStand) {
            drawPowerOrbArmorStand((EntityArmorStand) entity, x + 1, y + 4);
        } else {
            mc.getTextureManager().bindTexture(powerOrb.getResourceLocation());
            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, iconSize, iconSize, iconSize, iconSize);
        }

        String secondsString = String.format("§e%ss", seconds);
        DrawUtils.drawText(secondsString, Math.round(x + (iconSize / 2F) - (mc.fontRendererObj.getStringWidth(secondsString) / 2F)), y + iconSize, ColorCode.WHITE.getColor(255));

        float startY = Math.round(y + (iconAndSecondsHeight / 2f) - (effectsHeight / 2f));
        for (int i = 0; i < display.size(); i++) {
            DrawUtils.drawText(display.get(i), x + iconSize + 2, startY + (i * (mc.fontRendererObj.FONT_HEIGHT + spacingBetweenLines)), ColorCode.WHITE.getColor(255));
        }

        main.getUtils().restoreGLOptions();
    }

    /**
     * Easily grab an attribute from utils.
     */
    private float getAttribute(Attribute attribute) {
        return main.getUtils().getAttributes().get(attribute).getValue();
    }

    @SubscribeEvent()
    public void onRenderRemoveBars(RenderGameOverlayEvent.Pre e) {
        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.COMPACT_TAB_LIST)) {
            if (e.type == RenderGameOverlayEvent.ElementType.PLAYER_LIST) {
                if (TabListParser.getRenderColumns() != null) {
                    e.setCanceled(true);
                    TabListRenderer.render();
                }
            }
        }

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
            if (guiFeatureToOpen == Feature.ENCHANTMENT_LORE_PARSING) {
                Minecraft.getMinecraft().displayGuiScreen(new EnchantmentSettingsGui(guiFeatureToOpen, 1, guiPageToOpen, guiTabToOpen, guiFeatureToOpen.getSettings()));
            } else {
                Minecraft.getMinecraft().displayGuiScreen(new SettingsGui(guiFeatureToOpen, 1, guiPageToOpen, guiTabToOpen, guiFeatureToOpen.getSettings()));
            }
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
        this.subtitleFeature = subtitleFeature;
    }

    public float transformXY(float xy, int widthHeight, float scale) {
        float minecraftScale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        xy -= widthHeight / 2F * scale;
        xy = Math.round(xy * minecraftScale) / minecraftScale;
        return xy / scale;
    }

    @SubscribeEvent()
    public void onRenderWorld(RenderWorldLastEvent e) {
        Minecraft mc = Minecraft.getMinecraft();
        float partialTicks = e.partialTicks;

        HealingCircleManager.renderHealingCircleOverlays(partialTicks);

        if (main.getUtils().isOnSkyblock() && main.getUtils().isInDungeon() &&
                (main.getConfigValues().isEnabled(Feature.SHOW_CRITICAL_DUNGEONS_TEAMMATES) || main.getConfigValues().isEnabled(Feature.SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY))) {
            Entity renderViewEntity = mc.getRenderViewEntity();

            Vector3d viewPosition = Utils.getPlayerViewPosition();

            int iconSize = 25;

            for (EntityPlayer entity : mc.theWorld.playerEntities) {
                if (renderViewEntity == entity) {
                    continue;
                }

                if (!main.getDungeonManager().getTeammates().containsKey(entity.getName())) {
                    continue;
                }

                DungeonPlayer dungeonPlayer = main.getDungeonManager().getTeammates().get(entity.getName());

                double x = MathUtils.interpolateX(entity, partialTicks);
                double y = MathUtils.interpolateY(entity, partialTicks);
                double z = MathUtils.interpolateZ(entity, partialTicks);

                x -= viewPosition.x;
                y -= viewPosition.y;
                z -= viewPosition.z;

                if (main.getConfigValues().isEnabled(Feature.SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY)) {
                    y += 0.35F;
                }

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

                if (main.getConfigValues().isEnabled(Feature.SHOW_CRITICAL_DUNGEONS_TEAMMATES)
                        && (!dungeonPlayer.isGhost() && (dungeonPlayer.isCritical() || dungeonPlayer.isLow()))) {
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
                }

                if (!dungeonPlayer.isGhost() && main.getConfigValues().isEnabled(Feature.SHOW_DUNGEON_TEAMMATE_NAME_OVERLAY)) {
                    final String nameOverlay = ColorCode.YELLOW + "[" + dungeonPlayer.getDungeonClass().getFirstLetter() +  "] " + ColorCode.GREEN + entity.getName();
                    mc.fontRendererObj.drawString(nameOverlay, -mc.fontRendererObj.getStringWidth(nameOverlay) / 2F, iconSize / 2F + 13, -1, true);
                }

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
        float prevRenderYawOffset = powerOrbArmorStand.renderYawOffset;
        float prevPrevRenderYawOffset = powerOrbArmorStand.prevRenderYawOffset;

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

        powerOrbArmorStand.renderYawOffset = prevRenderYawOffset;
        powerOrbArmorStand.prevRenderYawOffset = prevPrevRenderYawOffset;
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

        ItemStack orbItemStack = ItemUtils.createSkullItemStack(null, null, "3ae3572b-2679-40b4-ba50-14dd58cbbbf7", "7ab4c4d6ee69bc24bba2b8faf67b9f704a06b01aa93f3efa6aef7a9696c4feef");

        radiantDummyArmorStand.setCurrentItemOrArmor(4, orbItemStack);

        return radiantDummyArmorStand;
    }
}
