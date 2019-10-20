package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.LocationEditGui;
import codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonLocation;
import codes.biscuit.skyblockaddons.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.client.GuiNotification;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.TimeZone;

import static net.minecraft.client.gui.Gui.icons;

public class RenderListener {

    private SkyblockAddons main;
    private final static ItemStack BONE_ITEM = new ItemStack(Item.getItemById(352));
    private final ResourceLocation BARS = new ResourceLocation("skyblockaddons", "bars.png");
    private final ResourceLocation DEFENCE_VANILLA = new ResourceLocation("skyblockaddons", "defence.png");
    private final ResourceLocation TEXT_ICONS = new ResourceLocation("skyblockaddons", "icons.png");
    public static final ResourceLocation LOCK = new ResourceLocation("skyblockaddons", "lock.png");

    private boolean predictHealth = false;
    private boolean predictMana = false;

    private DownloadInfo downloadInfo;

    private Feature subtitleFeature = null;
    private Feature titleFeature = null;
    private String cannotReachMobName = null;

    private PlayerListener.GUIType guiToOpen = null;
    private int guiPageToOpen = 1;
    private EnumUtils.GuiTab guiTabToOpen = EnumUtils.GuiTab.FEATURES;

    public RenderListener(SkyblockAddons main) {
        this.main = main;
        downloadInfo = new DownloadInfo(main);
    }
    /**
     * Render overlays and warnings for clients without labymod.
     */
    @SubscribeEvent()
    public void onRenderRegular(RenderGameOverlayEvent.Post e) {
        if ((!main.isUsingLabymod() || Minecraft.getMinecraft().ingameGUI instanceof GuiIngameForge)) {
            if (e.type == RenderGameOverlayEvent.ElementType.EXPERIENCE) {
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
    public void onRenderLiving(RenderLivingEvent.Specials.Pre e) {
        Entity entity = e.entity;
        if (main.getConfigValues().isEnabled(Feature.MINION_DISABLE_LOCATION_WARNING)) {
            if (entity.getCustomNameTag().startsWith("§cThis location isn\'t perfect! :(")) {
                e.setCanceled(true);
            }
            if (entity.getCustomNameTag().startsWith("§c/!\\")) {
                for (Entity listEntity : Minecraft.getMinecraft().theWorld.loadedEntityList) {
                    if (listEntity.getCustomNameTag().startsWith("§cThis location isn\'t perfect! :(") &&
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
            }
            if (message != null) {
                String text = message.getMessage();
                mc.ingameGUI.getFontRenderer().drawString(text, (float) (-mc.ingameGUI.getFontRenderer().getStringWidth(text) / 2), -20.0F,
                        main.getConfigValues().getColor(titleFeature).getColor(), true);
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
            }
            if (message != null) {
                String text;
                if (message == Message.MESSAGE_MINION_CANNOT_REACH) {
                    text = message.getMessage(cannotReachMobName);
                } else {
                    text = message.getMessage();
                }
                mc.ingameGUI.getFontRenderer().drawString(text, (float) (-mc.ingameGUI.getFontRenderer().getStringWidth(text) / 2), -23.0F,
                        main.getConfigValues().getColor(subtitleFeature).getColor(), true);
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
            if ((main.getConfigValues().isEnabled(Feature.SKELETON_BAR)) && main.getInventoryUtils().isWearingSkeletonHelmet()) {
                float scale = main.getConfigValues().getGuiScale(Feature.SKELETON_BAR);
                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, 1);
                drawSkeletonBar(scale, mc, null);
                GlStateManager.popMatrix();
            }
            Feature[] bars = {Feature.MANA_BAR, Feature.HEALTH_BAR};
            for (Feature feature : bars) {
                if (main.getConfigValues().isEnabled(feature)) {
                    float scale = main.getConfigValues().getGuiScale(feature);
                    GlStateManager.pushMatrix();
                    GlStateManager.scale(scale, scale, 1);
                    drawBar(feature, scale, mc);
                    GlStateManager.popMatrix();
                }
            }

            if (main.getConfigValues().isEnabled(Feature.DEFENCE_ICON)) {
                float scale = main.getConfigValues().getGuiScale(Feature.DEFENCE_ICON);
                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, 1);
                drawIcon(scale, mc, null);
                GlStateManager.popMatrix();
            }

            Feature[] texts = {Feature.DEFENCE_TEXT, Feature.DEFENCE_PERCENTAGE, Feature.MANA_TEXT, Feature.HEALTH_TEXT, Feature.HEALTH_UPDATES
                    , Feature.DARK_AUCTION_TIMER, Feature.MAGMA_BOSS_TIMER};
            for (Feature feature : texts) {
                if (main.getConfigValues().isEnabled(feature)) {
                    if (feature != Feature.HEALTH_UPDATES || main.getPlayerListener().getHealthUpdate() != null) {
                        float scale = main.getConfigValues().getGuiScale(feature);
                        GlStateManager.pushMatrix();
                        GlStateManager.scale(scale, scale, 1);
                        drawText(feature, scale, mc, null);
                        GlStateManager.popMatrix();
                    }
                }
            }

            if(main.getConfigValues().isEnabled(Feature.ITEM_PICKUP_LOG)) {
                float scale = main.getConfigValues().getGuiScale(Feature.ITEM_PICKUP_LOG);
                GlStateManager.pushMatrix();
                GlStateManager.scale(scale, scale, 1);
                drawItemPickupLog(mc, scale, null, null);
                GlStateManager.popMatrix();
            }
        }
    }

    private void drawBar(Feature feature, float scaleMultiplier, Minecraft mc) {
        drawBar(feature, scaleMultiplier, mc, null);
    }

    /**
     * This renders both the bars.
     */
    public void drawBar(Feature feature, float scale, Minecraft mc, ButtonLocation buttonLocation) {
        mc.getTextureManager().bindTexture(BARS);
        // The height and width of this element (box not included)
        int barHeightExpansion = 2*main.getConfigValues().getSizes(feature).getY();
        int height = 3+barHeightExpansion;

        int barWidthExpansion = 9*main.getConfigValues().getSizes(feature).getX();
        int width = 22+barWidthExpansion;

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
        ConfigColor color = main.getConfigValues().getColor(feature);

        if (feature == Feature.HEALTH_BAR && main.getConfigValues().isEnabled(Feature.CHANGE_BAR_COLOR_FOR_POTIONS)) {
            if (mc.thePlayer.isPotionActive(19/* Poison */)) {
                color = ConfigColor.DARK_GREEN;
            } else if (mc.thePlayer.isPotionActive(20/* Wither */)) {
                color = ConfigColor.DARK_GRAY;
            }
        }

        // Put the x & y to scale, remove half the width and height to center this element.
        x/=scale;
        y/=scale;
        x-=(float)width/2;
        y-=(float)height/2;
        int intX = Math.round(x);
        int intY = Math.round(y);
        if (buttonLocation == null) {
            drawModularBar(mc, color, false, intX, intY+barHeightExpansion/2, null,feature,filled, width);
            if (filled > 0) {
                drawModularBar(mc, color, true, intX, intY+barHeightExpansion/2, null,feature,filled, width);
            }
        } else {
            int boxXOne = intX-4;
            int boxXTwo = intX+width+5;
            int boxYOne = intY-3;
            int boxYTwo = intY+height+4;
            buttonLocation.checkHoveredAndDrawBox(boxXOne, boxXTwo, boxYOne, boxYTwo, scale);
            drawModularBar(mc, main.getConfigValues().getColor(feature), false, intX, intY+barHeightExpansion/2, buttonLocation,feature,filled, width);
            if (filled > 0) {
                drawModularBar(mc, main.getConfigValues().getColor(feature), true, intX, intY+barHeightExpansion/2, buttonLocation,feature,filled, width);
            }
        }
    }

    private void drawModularBar(Minecraft mc, ConfigColor color, boolean filled, int x, int y, ButtonLocation buttonLocation, Feature feature, int fillWidth, int maxWidth) {
        Gui gui = mc.ingameGUI;
        if (buttonLocation != null) {
            gui = buttonLocation;
        }
        if (color == ConfigColor.BLACK) {
            GlStateManager.color(0.25F,0.25F,0.25F); // too dark normally
        } else { // a little darker for contrast
            GlStateManager.color(((float)color.getR() / 255)*0.9F, ((float)color.getG() / 255)*0.9F, ((float)color.getB() / 255)*0.9F);
        }
        CoordsPair sizes = main.getConfigValues().getSizes(feature);
        drawBarStart(gui,x,y, filled, sizes.getX(), sizes.getY(), fillWidth, color, maxWidth);
    }

    private void drawBarStart(Gui gui, int x, int y, boolean filled, int barWidth, int barHeight, int fillWidth, ConfigColor color, int maxWidth) {
        int baseTextureY = filled ? 0 : 8;

        drawMiddleThreeRows(gui,x+10,y,barHeight,22,baseTextureY,2, fillWidth, 2); // these 2 just fill some gaps in the bar
        drawMiddleThreeRows(gui,x+11+(barWidth*9),y,barHeight,22,baseTextureY,2, fillWidth, 2);

        drawAllFiveRows(gui, x, y, barHeight, 0, baseTextureY, 11, fillWidth);

        drawBarSeparators(gui, x+11, y, baseTextureY, barWidth, barHeight, fillWidth);

        if (fillWidth < maxWidth && fillWidth > 0) {
            GlStateManager.color(((float) color.getR() / 255) * 0.8F, ((float) color.getG() / 255) * 0.8F, ((float) color.getB() / 255) * 0.8F);
            drawMiddleThreeRows(gui, x + fillWidth, y, barHeight, 22, 8, 2, fillWidth, 2);
        }
    }

    private void drawMiddleBarParts(Gui gui, int x, int y, int baseTextureY, int barWidth, int barHeight, int fillWidth) {
        int endBarX = 0;
        for (int i = 0; i < barWidth; i++) {
            endBarX = x+(i*9);
            drawAllFiveRows(gui, endBarX, y, barHeight, 13, baseTextureY, 9,fillWidth-11-1-(i*9));
        }
        drawBarEnd(gui, endBarX+9, y, baseTextureY, barWidth, barHeight,fillWidth);
    }

    private void drawBarSeparators(Gui gui, int x, int y, int baseTextureY, int barWidth, int barHeight, int fillWidth) {
        for (int i = 0; i <= barWidth; i++) {
            drawMiddleThreeRows(gui,x+(i*9),y,barHeight,22,baseTextureY,1, fillWidth-11-1-(i*9), 2);
        }
        drawMiddleBarParts(gui, x+1, y, baseTextureY, barWidth, barHeight,fillWidth);
    }

    private void drawBarEnd(Gui gui, int x, int y, int baseTextureY, int barWidth, int barHeight, int fillWidth) {
        drawAllFiveRows(gui, x, y, barHeight, 24, baseTextureY, 11,fillWidth-11-1-(barWidth*9));
    }

    private void drawAllFiveRows(Gui gui, int x, int y, int barHeight, int textureX, int baseTextureY, int width, int fillWidth) {
        if (fillWidth > width || baseTextureY >= 8) fillWidth = width;
        gui.drawTexturedModalRect(x, y+1-barHeight, textureX, baseTextureY, fillWidth, 1);

        drawMiddleThreeRows(gui,x,y,barHeight,textureX,baseTextureY,width,fillWidth, 1);

        gui.drawTexturedModalRect(x, y+3+barHeight, textureX, baseTextureY+6, fillWidth, 1);
    }

    private void drawMiddleThreeRows(Gui gui, int x, int y, int barHeight, int textureX, int baseTextureY,  int width, int fillWidth, int rowHeight) {
        if (fillWidth > width || baseTextureY >= 8) fillWidth = width;
        for (int i = 0; i < barHeight; i++) {
            if (rowHeight == 2) { //drawing bar separators is a little different
                gui.drawTexturedModalRect(x, y-i, textureX, baseTextureY, fillWidth, rowHeight);
            } else {
                gui.drawTexturedModalRect(x, y + 1 - i, textureX, baseTextureY + 1, fillWidth, rowHeight);
            }
        }

        gui.drawTexturedModalRect(x, y+2, textureX, baseTextureY+3, fillWidth, 1);

        for (int i = 0; i < barHeight; i++) {
            gui.drawTexturedModalRect(x, y+3+i, textureX, baseTextureY+5, fillWidth, rowHeight);
        }
    }

    private void drawUpdateMessage() {
        EnumUtils.UpdateMessageType messageType = downloadInfo.getMessageType();
        if (messageType != null) {
            Minecraft mc = Minecraft.getMinecraft();
            String[] textList;
            if (messageType == EnumUtils.UpdateMessageType.PATCH_AVAILABLE || messageType == EnumUtils.UpdateMessageType.MAJOR_AVAILABLE) {
                textList = downloadInfo.getMessageType().getMessages(downloadInfo.getNewestVersion());
            } else if (messageType == EnumUtils.UpdateMessageType.DOWNLOADING) {
                textList = downloadInfo.getMessageType().getMessages(String.valueOf(downloadInfo.getDownloadedBytes()), String.valueOf(downloadInfo.getTotalBytes()));
            } else if (messageType == EnumUtils.UpdateMessageType.DOWNLOAD_FINISHED) {
                textList = downloadInfo.getMessageType().getMessages(downloadInfo.getOutputFileName());
            } else {
                textList = downloadInfo.getMessageType().getMessages();
            }
            int halfWidth = new ScaledResolution(mc).getScaledWidth() / 2;
            Gui.drawRect(halfWidth - 110, 20, halfWidth + 110, 53+textList.length*10, ConfigColor.RED.getColor(127));
            String text = "SkyblockAddons";
            GlStateManager.pushMatrix();
            float scale = 1.5F;
            GlStateManager.scale(scale, scale, 1);
            mc.fontRendererObj.drawString(text, (int) (halfWidth / scale) - mc.fontRendererObj.getStringWidth(text) / 2, (int) (30 / scale), ConfigColor.WHITE.getColor());
            GlStateManager.popMatrix();
            int y = 45;
            for (String line : textList) {
                mc.fontRendererObj.drawString(line, halfWidth - mc.fontRendererObj.getStringWidth(line) / 2, y, ConfigColor.WHITE.getColor());
                y+=10;
            }
        }
    }

    /**
     * This renders the skeleton bar.
     */
    public void drawSkeletonBar(float scale, Minecraft mc, ButtonLocation buttonLocation) {
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
        float width = 3*15;
        x-=Math.round(width*scale/2);
        y-=Math.round(height*scale/2);
        x/=scale;
        y/=scale;
        if (buttonLocation != null) {
            int boxXOne = Math.round(x-4);
            int boxXTwo = Math.round(x+width+4);
            int boxYOne = Math.round(y-4);
            int boxYTwo = Math.round(y+height+4);
            buttonLocation.checkHoveredAndDrawBox(boxXOne, boxXTwo, boxYOne, boxYTwo, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        for (int boneCounter = 0; boneCounter < bones; boneCounter++) {
            mc.getRenderItem().renderItemIntoGUI(BONE_ITEM, Math.round((x+boneCounter*15)), Math.round(y));
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
            float newScale = scale*1.5F;
            GlStateManager.pushMatrix();
            GlStateManager.scale(newScale, newScale, 1);
            newScale*=scale;
            x-=Math.round((float)width*newScale/2);
            y-=Math.round((float)height*newScale/2);
            mc.ingameGUI.drawTexturedModalRect(x/newScale, y/newScale, 34, 9, width, height);
            GlStateManager.popMatrix();
        } else {
            scale *= (scale/1.5);
            x-=Math.round((float)width*scale/2);
            y-=Math.round((float)height*scale/2);
            x/=scale;
            y/=scale;
            int intX = Math.round(x);
            int intY = Math.round(y);
            int boxXOne = intX-2;
            int boxXTwo = intX+width+2;
            int boxYOne = intY-2;
            int boxYTwo = intY+height+2;
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
        int color = main.getConfigValues().getColor(feature).getColor();
        if (feature == Feature.MANA_TEXT) {
            text = getAttribute(Attribute.MANA) + "/" + getAttribute(Attribute.MAX_MANA);
        } else if (feature == Feature.HEALTH_TEXT) {
            text = getAttribute(Attribute.HEALTH) + "/" + getAttribute(Attribute.MAX_HEALTH);
        } else if (feature == Feature.DEFENCE_TEXT) {
            text = String.valueOf(getAttribute(Attribute.DEFENCE));
        } else if (feature == Feature.DEFENCE_PERCENTAGE) {
            double doubleDefence = (double)getAttribute(Attribute.DEFENCE);
            double percentage = ((doubleDefence/100)/((doubleDefence/100)+1))*100; //Formula taken from https://hypixel.net/threads/how-armor-works-and-the-diminishing-return-of-higher-defence.2178928/
            BigDecimal bigDecimal = new BigDecimal(percentage).setScale(1, BigDecimal.ROUND_HALF_UP);
            text = bigDecimal.toString()+"%";
        } else if (feature == Feature.HEALTH_UPDATES) {
            Integer healthUpdate = main.getPlayerListener().getHealthUpdate();
            if (buttonLocation == null) {
                if (healthUpdate != null) {
                    color = healthUpdate > 0 ? ConfigColor.GREEN.getColor() : ConfigColor.RED.getColor();
                    text = (healthUpdate > 0 ? "+" : "-") + Math.abs(healthUpdate);
                } else {
                    return;
                }
            } else {
                text = "+123";
                color = ConfigColor.GREEN.getColor();
            }
        } else if (feature == Feature.DARK_AUCTION_TIMER) { // The timezone of the server, to avoid problems with like timezones that are 30 minutes ahead or whatnot.
            Calendar nextDarkAuction = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
//            nextDarkAuction.setTimeInMillis(System.currentTimeMillis());
            if (nextDarkAuction.get(Calendar.MINUTE) >= 55) {
                nextDarkAuction.add(Calendar.HOUR_OF_DAY, 1);
            }
            nextDarkAuction.set(Calendar.MINUTE, 55);
            nextDarkAuction.set(Calendar.SECOND, 0);
            int difference = (int)(nextDarkAuction.getTimeInMillis()-System.currentTimeMillis());
            int minutes = difference/60000;
            int seconds = (int)Math.round((double)(difference%60000)/1000);
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
        } else {
            return;
        }
        float x = main.getConfigValues().getActualX(feature);
        float y = main.getConfigValues().getActualY(feature);

        int height = 7;
        int width = mc.fontRendererObj.getStringWidth(text);
        x-=Math.round(width*scale/2);
        y-=Math.round(height*scale/2);
        x/=scale;
        y/=scale;
        int intX = Math.round(x);
        int intY = Math.round(y);
        if (buttonLocation != null) {
            int boxXOne = intX-4;
            int boxXTwo = intX+width+4;
            int boxYOne = intY-4;
            int boxYTwo = intY+height+4;
            if (feature == Feature.MAGMA_BOSS_TIMER || feature == Feature.DARK_AUCTION_TIMER) {
                boxXOne-=18;
                boxYOne-=2;
            }
            buttonLocation.checkHoveredAndDrawBox(boxXOne, boxXTwo, boxYOne, boxYTwo, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
        if (main.getConfigValues().getTextStyle() == EnumUtils.TextStyle.BLACK_SHADOW) {
            mc.fontRendererObj.drawString(text, intX + 1, intY, 0);
            mc.fontRendererObj.drawString(text, intX - 1, intY, 0);
            mc.fontRendererObj.drawString(text, intX, intY + 1, 0);
            mc.fontRendererObj.drawString(text, intX, intY - 1, 0);
            mc.fontRendererObj.drawString(text, intX, intY, color);
        } else {
            mc.ingameGUI.drawString(mc.fontRendererObj, text, intX, intY, color);
        }
        mc.getTextureManager().bindTexture(TEXT_ICONS);
        GlStateManager.color(1,1,1,1);
        if (feature == Feature.DARK_AUCTION_TIMER) {
            Gui.drawModalRectWithCustomSizedTexture(intX-18, intY-5, 16, 0, 16,16,32,32);
        } else if (feature == Feature.MAGMA_BOSS_TIMER) {
            Gui.drawModalRectWithCustomSizedTexture(intX-18, intY-5, 0, 0, 16,16,32,32);
        }
    }

    public void drawItemPickupLog(Minecraft mc, float scale, Collection<ItemDiff> dummyLog, ButtonLocation buttonLocation) {
        float x = main.getConfigValues().getActualX(Feature.ITEM_PICKUP_LOG);
        float y = main.getConfigValues().getActualY(Feature.ITEM_PICKUP_LOG);

        EnumUtils.AnchorPoint anchorPoint = main.getConfigValues().getAnchorPoint(Feature.ITEM_PICKUP_LOG);
        boolean downwards = anchorPoint == EnumUtils.AnchorPoint.TOP_RIGHT || anchorPoint == EnumUtils.AnchorPoint.TOP_LEFT;

        int height = 8*3;
        int width = mc.fontRendererObj.getStringWidth("+ 1x Forceful Ember Chestplate");
        x-=Math.round(width*scale/2);
        y-=Math.round(height*scale/2);
        x/=scale;
        y/=scale;
        int intX = Math.round(x);
        int intY = Math.round(y);
        if (dummyLog != null) {
            int boxXOne = intX-4;
            int boxXTwo = intX+width+4;
            int boxYOne = intY-4;
            int boxYTwo = intY+height+4;
            buttonLocation.checkHoveredAndDrawBox(boxXOne, boxXTwo, boxYOne, boxYTwo, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }
        int i = 0;
        Collection<ItemDiff> log = main.getInventoryUtils().getItemPickupLog();
        if (dummyLog != null) {
            log = dummyLog;
        }
        for (ItemDiff itemDiff : log) {
            String text = String.format("%s %sx §r%s", itemDiff.getAmount() > 0 ? "§a+":"§c-",
                    Math.abs(itemDiff.getAmount()), itemDiff.getDisplayName());
            int stringY = intY+(i*mc.fontRendererObj.FONT_HEIGHT);
            if (!downwards) {
                stringY = intY-(i*mc.fontRendererObj.FONT_HEIGHT);
                stringY += 18;
            }
            drawString(mc, text, intX, stringY, ConfigColor.WHITE.getColor());
            i++;
        }
    }

    private void drawString(Minecraft mc, String text, int x, int y, int color) {
        if (main.getConfigValues().getTextStyle() == EnumUtils.TextStyle.BLACK_SHADOW) {
            String strippedText = main.getUtils().stripColor(text);
            mc.fontRendererObj.drawString(strippedText, x + 1, y, 0);
            mc.fontRendererObj.drawString(strippedText, x - 1, y, 0);
            mc.fontRendererObj.drawString(strippedText, x, y + 1, 0);
            mc.fontRendererObj.drawString(strippedText, x, y - 1, 0);
            mc.fontRendererObj.drawString(text, x, y, color);
        } else {
            mc.ingameGUI.drawString(mc.fontRendererObj, text, x, y, color);
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
        if (guiToOpen == PlayerListener.GUIType.MAIN) {
            Minecraft.getMinecraft().displayGuiScreen(new SkyblockAddonsGui(main, guiPageToOpen, guiTabToOpen));
        } else if (guiToOpen == PlayerListener.GUIType.EDIT_LOCATIONS) {
            Minecraft.getMinecraft().displayGuiScreen(new LocationEditGui(main, guiPageToOpen, guiTabToOpen));
        }
        guiToOpen = null;
    }


    void setPredictHealth(boolean predictHealth) {
        this.predictHealth = predictHealth;
    }

    void setPredictMana(boolean predictMana) {
        this.predictMana = predictMana;
    }

    boolean isPredictMana() {
        return predictMana;
    }

    boolean isPredictHealth() {
        return predictHealth;
    }

    void setCannotReachMobName(String cannotReachMobName) {
        this.cannotReachMobName = cannotReachMobName;
    }

    public void setTitleFeature(Feature titleFeature) {
        this.titleFeature = titleFeature;
    }

    public void setGuiToOpen(PlayerListener.GUIType guiToOpen, int page, EnumUtils.GuiTab tab) {
        this.guiToOpen = guiToOpen;
        this.guiPageToOpen = page;
        this.guiTabToOpen = tab;
    }

    public void setSubtitleFeature(Feature subtitleFeature) {
        this.subtitleFeature = subtitleFeature;
    }

    Feature getTitleFeature() {
        return titleFeature;
    }

    public DownloadInfo getDownloadInfo() {
        return downloadInfo;
    }
}
