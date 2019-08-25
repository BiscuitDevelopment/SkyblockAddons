package codes.biscuit.skyblockaddons.listeners;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.LocationEditGui;
import codes.biscuit.skyblockaddons.gui.SkyblockAddonsGui;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonLocation;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonSlider;
import codes.biscuit.skyblockaddons.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
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

    private boolean predictHealth = false;
    private boolean predictMana = false;

    private Feature subtitleFeature = null;
    private Feature titleFeature = null;
    private String cannotReachMobName = null;

    private PlayerListener.GUIType guiToOpen = null;

    public RenderListener(SkyblockAddons main) {
        this.main = main;
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
        }
    }

    /**
     * I have an option so you can see the magma timer in other games so that's why.
     */
    private void renderTimersOnly() {
        Minecraft mc = Minecraft.getMinecraft();
        if (!(mc.currentScreen instanceof LocationEditGui) && !(mc.currentScreen instanceof GuiNotification)) {
            float scale = main.getUtils().denormalizeValue(main.getConfigValues().getGuiScale(), ButtonSlider.GUI_SCALE_MINIMUM, ButtonSlider.GUI_SCALE_MAXIMUM, ButtonSlider.GUI_SCALE_STEP);
            GlStateManager.disableBlend();
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
            if (main.getConfigValues().isEnabled(Feature.MAGMA_BOSS_TIMER) && main.getConfigValues().isEnabled(Feature.SHOW_MAGMA_TIMER_IN_OTHER_GAMES) &&
                    main.getPlayerListener().getMagmaAccuracy() != EnumUtils.MagmaTimerAccuracy.NO_DATA) {
                drawText(Feature.MAGMA_BOSS_TIMER, scale, mc, null);
            }
            if (main.getConfigValues().isEnabled(Feature.DARK_AUCTION_TIMER) && main.getConfigValues().isEnabled(Feature.SHOW_DARK_AUCTION_TIMER_IN_OTHER_GAMES)) {
                drawText(Feature.DARK_AUCTION_TIMER, scale, mc, null);
            }
            GlStateManager.popMatrix();
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
            }
            if (message != null) {
                String text = message.getMessage();
                mc.ingameGUI.getFontRenderer().drawString(text, (float) (-mc.ingameGUI.getFontRenderer().getStringWidth(text) / 2), -20.0F,
                        main.getConfigValues().getColor(titleFeature).getColor(255), true);
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
                        main.getConfigValues().getColor(subtitleFeature).getColor(255), true);
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
            float scale = main.getUtils().denormalizeValue(main.getConfigValues().getGuiScale(), ButtonSlider.GUI_SCALE_MINIMUM, ButtonSlider.GUI_SCALE_MAXIMUM, ButtonSlider.GUI_SCALE_STEP);
            GlStateManager.disableBlend();
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
            if ((main.getConfigValues().isEnabled(Feature.SKELETON_BAR)) && main.getInventoryUtils().isWearingSkeletonHelmet()) {
                drawSkeletonBar(scale, mc, null);
            }
            Feature[] bars = {Feature.MANA_BAR, Feature.HEALTH_BAR};
            for (Feature feature : bars) {
                if (main.getConfigValues().isEnabled(feature)) {
                    drawBar(feature, scale, mc);
                }
            }

            if (main.getConfigValues().isEnabled(Feature.DEFENCE_ICON)) {
                drawIcon(scale, mc, null);
            }

            Feature[] texts = {Feature.DEFENCE_TEXT, Feature.DEFENCE_PERCENTAGE, Feature.MANA_TEXT, Feature.HEALTH_TEXT, Feature.HEALTH_UPDATES
            , Feature.DARK_AUCTION_TIMER, Feature.MAGMA_BOSS_TIMER};
            for (Feature feature : texts) {
                if (main.getConfigValues().isEnabled(feature)) {
                    if (feature != Feature.HEALTH_UPDATES || main.getPlayerListener().getHealthUpdate() != null) {
                        drawText(feature, scale, mc, null);
                    }
                }
            }

            if(main.getConfigValues().isEnabled(Feature.ITEM_PICKUP_LOG)) {
                drawItemPickupLog(mc, scale, null, null);
            }
            GlStateManager.popMatrix();
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
        int height = 5;
        int width = 92;

        // The fill of the bar from 0 to 1
        float fill;
        if (feature == Feature.MANA_BAR) {
            fill = (float) getAttribute(Attribute.MANA) / getAttribute(Attribute.MAX_MANA);
        } else {
            fill = (float) getAttribute(Attribute.HEALTH) / getAttribute(Attribute.MAX_HEALTH);
        }
        if (fill > 1) fill = 1;
        int filled = (int) (fill * width);

        float x = main.getConfigValues().getActualX(feature);
        float y = main.getConfigValues().getActualY(feature);
        int textureY = main.getConfigValues().getColor(feature).ordinal()*10;

        // Put the x & y to scale, remove half the width and height to center this element.
        x/=scale;
        y/=scale;
        x-=(float)width/2;
        y-=(float)height/2;
        int intX = Math.round(x);
        int intY = Math.round(y);
        if (buttonLocation == null) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.ingameGUI.drawTexturedModalRect(intX, intY, 0, textureY, width, height);
            if (filled > 0) {
                mc.ingameGUI.drawTexturedModalRect(intX, intY, 0, textureY+5, filled, height);
            }
        } else {
            int boxXOne = intX-4;
            int boxXTwo = intX+width+4;
            int boxYOne = intY-4;
            int boxYTwo = intY+height+4;
            buttonLocation.checkHoveredAndDrawBox(boxXOne, boxXTwo, boxYOne, boxYTwo, scale);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            buttonLocation.drawTexturedModalRect(intX, intY, 0, textureY, width, height);
            if (filled > 0) {
                buttonLocation.drawTexturedModalRect(intX, intY, 0, textureY+5, filled, height);
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
                        listEntity.ridingEntity instanceof EntityZombie && listEntity.ridingEntity.isInvisible() && listEntity.getDistanceToEntity(mc.thePlayer) <= 8) {
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
        int color = main.getConfigValues().getColor(feature).getColor(255);
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
                    color = healthUpdate > 0 ? ConfigColor.GREEN.getColor(255) : ConfigColor.RED.getColor(255);
                    text = (healthUpdate > 0 ? "+" : "-") + Math.abs(healthUpdate);
                } else {
                    return;
                }
            } else {
                text = "+123";
                color = ConfigColor.GREEN.getColor(255);
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
            if (buttonLocation == null) {
                StringBuilder magmaBuilder = new StringBuilder();
                magmaBuilder.append(main.getPlayerListener().getMagmaAccuracy().getSymbol());
                EnumUtils.MagmaTimerAccuracy ma = main.getPlayerListener().getMagmaAccuracy();
                if (ma == EnumUtils.MagmaTimerAccuracy.ABOUT || ma == EnumUtils.MagmaTimerAccuracy.EXACTLY) {
                    int totalSeconds = main.getPlayerListener().getMagmaTime();
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
                }// else if (ma == EnumUtils.MagmaTimerAccuracy.SPAWNED) {
//                    magmaBuilder.append(main.getPlayerListener().getMagmaBossHealth()).append("\u2764");
                //}
                text = magmaBuilder.toString();
            } else {
                text = "~12:34";
            }
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
//                Gui.drawModalRectWithCustomSizedTexture(intX, intY, 0, 0, 16,16,32,32);
            Gui.drawModalRectWithCustomSizedTexture(intX-18, intY-5, 16, 0, 16,16,32,32);
        } else if (feature == Feature.MAGMA_BOSS_TIMER) {
                Gui.drawModalRectWithCustomSizedTexture(intX-18, intY-5, 0, 0, 16,16,32,32);
//            Gui.drawModalRectWithCustomSizedTexture(intX-16, intY-(16-10), 16, 0, 16,16,32,32);
        }
    }

    public void drawItemPickupLog(Minecraft mc, float scale, Collection<ItemDiff> dummyLog, ButtonLocation buttonLocation) {
        float x = main.getConfigValues().getActualX(Feature.ITEM_PICKUP_LOG);
        float y = main.getConfigValues().getActualY(Feature.ITEM_PICKUP_LOG);

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
            String text = String.format("%s %sx \u00A7r%s", itemDiff.getAmount() > 0 ? "\u00A7a+":"\u00A7c-",
                    Math.abs(itemDiff.getAmount()), itemDiff.getDisplayName());
            drawString(mc, text, intX, intY+(i*mc.fontRendererObj.FONT_HEIGHT), ConfigColor.WHITE.getColor(255));
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
            Minecraft.getMinecraft().displayGuiScreen(new SkyblockAddonsGui(main, 1, 1));
        } else if (guiToOpen == PlayerListener.GUIType.EDIT_LOCATIONS) {
            Minecraft.getMinecraft().displayGuiScreen(new LocationEditGui(main));
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

    public void setGuiToOpen(PlayerListener.GUIType guiToOpen) {
        this.guiToOpen = guiToOpen;
    }

    void setSubtitleFeature(Feature subtitleFeature) {
        this.subtitleFeature = subtitleFeature;
    }

    Feature getTitleFeature() {
        return titleFeature;
    }
}
