package codes.biscuit.skyblockaddons.gui.elements;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.CraftingPattern;
import codes.biscuit.skyblockaddons.utils.Utils;
import codes.biscuit.skyblockaddons.utils.nifty.color.ChatFormatting;
import codes.biscuit.skyblockaddons.utils.nifty.reflection.MinecraftReflection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

/**
 * CheckBox GUI element to use in other GUI elements.
 *
 * @author DidiSkywalker
 */
public class CheckBox {

    @FunctionalInterface
    public interface OnToggleListener {
        void onToggle(boolean value);
    }

    /**
     * Size of the CheckBox icon
     */
    private static final int ICON_SIZE = 16;

    private final float scale;

    private final Minecraft mc;
    private final int x;
    private final int y;
    private final String text;
    private final int textWidth;
    private final int size;

    private boolean value;
    private OnToggleListener onToggleListener;

    /**
     * @param mc Minecraft instance
     * @param x x position
     * @param y y position
     * @param size Desired size (height) to scale to
     * @param text Displayed text
     * @param value Default value
     */
    CheckBox(Minecraft mc, int x, int y, int size, String text, boolean value) {
        this(mc, x, y, size, text);
        this.value = value;
    }

    /**
     * @param mc Minecraft instance
     * @param x x position
     * @param y y position
     * @param size Desired size (height) to scale to
     * @param text Displayed text
     */
    CheckBox(Minecraft mc, int x, int y, int size, String text) {
        this.mc = mc;
        this.x = x;
        this.y = y;
        this.scale = (float) size / (float) ICON_SIZE;
        this.text = text;
        this.textWidth = MinecraftReflection.FontRenderer.getStringWidth(text);
        this.size = size;
    }

    public void draw() {
        int scaledX = Math.round(x / scale);
        int scaledY = Math.round(y / scale);

        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1);

        int color = value ? ChatFormatting.WHITE.getColor().asRGB() : ChatFormatting.GRAY.getColor().asRGB();
        SkyblockAddons.getInstance().getUtils().drawString(mc, text, scaledX + Math.round(size * 1.5f / scale), scaledY + (size / 2), color);

        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        Minecraft.getMinecraft().getTextureManager().bindTexture(CraftingPattern.ICONS);
        GlStateManager.color(1, 1, 1, 1F);

        if (value) {
            mc.ingameGUI.drawTexturedModalRect(scaledX, scaledY, 49, 34, 16, 16);
        } else {
            mc.ingameGUI.drawTexturedModalRect(scaledX, scaledY, 33, 34, 16, 16);
        }

        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    void onMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0
                && mouseX > this.x && mouseX < this.x + this.size + this.textWidth
                && mouseY > this.y && mouseY < this.y + this.size) {
            value = !value;
            SkyblockAddons.getInstance().getUtils().playSound("gui.button.press", 1F);
            if (onToggleListener != null) {
                onToggleListener.onToggle(value);
            }

            Utils.blockNextClick = true;
        }
    }

    void setValue(boolean value) {
        this.value = value;
    }

    boolean getValue() {
        return value;
    }

    /**
     * Attaches a listener that gets notified whenever the CheckBox is toggled
     *
     * @param listener Listener to attach
     */
    void setOnToggleListener(OnToggleListener listener) {
        onToggleListener = listener;
    }
}
