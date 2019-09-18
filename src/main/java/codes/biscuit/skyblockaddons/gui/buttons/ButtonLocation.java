package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ConfigColor;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.ItemDiff;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;

public class ButtonLocation extends ButtonFeature {

    // So we know the latest hovered feature (used for arrow key movement).
    private static Feature lastHoveredFeature = null;

    private SkyblockAddons main;
    private int lastMouseX;
    private int lastMouseY;

    /**
     * Create a button that allows you to change the location of a GUI element.
     */
    public ButtonLocation(SkyblockAddons main, Feature feature) {
        super(-1, 0, 0, null, feature);
        this.main = main;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        // The scale of the GUI

        if (feature == Feature.MANA_BAR || feature == Feature.HEALTH_BAR) {
            float scale = main.getConfigValues().getGuiScale(feature);
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
            main.getRenderListener().drawBar(feature, scale, mc, this);
            GlStateManager.popMatrix();
        } else if (feature == Feature.SKELETON_BAR) {
            float scale = main.getConfigValues().getGuiScale(feature);
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
            main.getRenderListener().drawSkeletonBar(scale, mc, this);
            GlStateManager.popMatrix();
        } else if (feature == Feature.MANA_TEXT || feature == Feature.HEALTH_TEXT ||
                feature == Feature.DEFENCE_TEXT || feature == Feature.DEFENCE_PERCENTAGE ||
                feature == Feature.HEALTH_UPDATES || feature == Feature.DARK_AUCTION_TIMER ||
                feature == Feature.MAGMA_BOSS_TIMER) {
            float scale = main.getConfigValues().getGuiScale(feature);
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
            main.getRenderListener().drawText(feature, scale, mc, this);
            GlStateManager.popMatrix();
        } else if (feature == Feature.ITEM_PICKUP_LOG) {
            List<ItemDiff> collectionLog = new ArrayList<>();
            collectionLog.add(new ItemDiff(ChatFormatting.DARK_PURPLE+"Forceful Ember Chestplate", 1));
            collectionLog.add(new ItemDiff("Boat", -1));
            collectionLog.add(new ItemDiff(ChatFormatting.BLUE+"Aspect of the End", 1));
            float scale = main.getConfigValues().getGuiScale(feature);
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
            main.getRenderListener().drawItemPickupLog(mc, scale, collectionLog, this);
            GlStateManager.popMatrix();
        } else if (feature == Feature.DEFENCE_ICON) {
            float scale = main.getConfigValues().getGuiScale(feature);
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
            scale *= 1.5;
            GlStateManager.scale(scale,scale,1);
            main.getRenderListener().drawIcon(scale, mc, this);
            scale /= 1.5;
            GlStateManager.scale(scale,scale,1);
            GlStateManager.popMatrix();
        }
        if (hovered) {
            lastHoveredFeature = feature;
        }
    }

    /**
     * This just updates the hovered status and draws the box around each feature. To avoid repetitive code.
     */
    public void checkHoveredAndDrawBox(int boxXOne, int boxXTwo, int boxYOne, int boxYTwo, float scale) {
        hovered = lastMouseX >= boxXOne * scale && lastMouseY >= boxYOne * scale && lastMouseX < boxXTwo * scale && lastMouseY < boxYTwo * scale;
        int boxAlpha = 100;
        if (hovered) {
            boxAlpha = 170;
        }
        int boxColor = ConfigColor.GRAY.getColor(boxAlpha);
        drawRect(boxXOne, boxYOne,
                boxXTwo, boxYTwo, boxColor);
    }

    /**
     * Because the box changes with the scale, have to override this.
     */
    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        return this.enabled && this.visible && hovered;
    }

    public static Feature getLastHoveredFeature() {
        return lastHoveredFeature;
    }

    public int getLastMouseY() {
        return lastMouseY;
    }

    public int getLastMouseX() {
        return lastMouseX;
    }
}
