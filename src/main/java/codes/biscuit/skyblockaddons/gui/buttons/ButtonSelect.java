package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.util.List;

/**
 * Button that lets the user select one item in a given set of items.
 */
public class ButtonSelect extends GuiButton {

    private static ResourceLocation ARROW_LEFT = new ResourceLocation("skyblockaddons", "gui/flatarrowleft.png");
    private static ResourceLocation ARROW_RIGHT = new ResourceLocation("skyblockaddons", "gui/flatarrowright.png");

    /**
     * Item that can be used in this Select button
     */
    public interface SelectItem {

        /**
         * @return A name displayed inside the button
         */
        String getName();

        /**
         * @return A description displayed below the button
         */
        String getDescription();
    }

    @FunctionalInterface
    public interface OnItemSelectedCallback {
        /**
         * Called whenever the selected item changes by clicking the next or previous button.
         *
         * @param index The new selected index
         */
        void onItemSelected(int index);
    }

    private final List<SelectItem> itemList;
    private int index = 0;

    private final int textWidth;
    private OnItemSelectedCallback callback;

    /*
     * Rough sketch of the button
     *  __ __________ __
     * |< |          |> |
     *  -- ---------- --
     */
    /**
     * Create a new Select button at (x, y) with a given width and height and set of items to select from.
     * Initially selects the given {@code selectedIndex} or {@code 0} if that is out of bounds of the given list.
     * Optionally accept a callback that is called whenever a new item is selected.
     * Note: Effective width for text is about {@code width - 2 * height} as the arrow buttons are squares with
     * a side length of {@code height}.
     * Text will be trimmed and marked with ellipses {@code …} if it is too long to fit in the text area.
     *
     * @param x             x position
     * @param y             y position
     * @param width         total width
     * @param height        height
     * @param items         non-null and non-empty List of items to choose from
     * @param selectedIndex initially selected index in the given list of items
     * @param callback      Nullable callback when a new item is selected
     */
    public ButtonSelect(int x, int y, int width, int height, List<SelectItem> items, int selectedIndex, OnItemSelectedCallback callback) {
        super(0, x, y, "");
        if(items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Item list must have at least one element.");
        }

        textWidth = width - (2 * height) - 6; // 2 * 3 text padding on both sides
        this.width = width;
        this.height = height;
        itemList = items;
        this.index = selectedIndex > 0 && selectedIndex < itemList.size() ? selectedIndex : 0;
        this.callback = callback;
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        final int endX = xPosition + width;

        int color = SkyblockAddons.getInstance().getUtils().getDefaultColor(100);
        int leftColor = SkyblockAddons.getInstance().getUtils().getDefaultColor(isOverLeftButton(mouseX, mouseY) ? 200 : 90);
        int rightColor = SkyblockAddons.getInstance().getUtils().getDefaultColor(isOverRightButton(mouseX, mouseY) ? 200 : 90);

        String name = itemList.get(index).getName();
        String trimmedName = minecraft.fontRendererObj.trimStringToWidth(name, textWidth);
        if (!name.equals(trimmedName)) {
            trimmedName = ellipsize(trimmedName);
        }
        String description = itemList.get(index).getDescription();
        // background / text area
        drawRect(xPosition, yPosition, endX, yPosition + height, color);
        // left button
        drawRect(xPosition, yPosition, xPosition + height, yPosition + height, leftColor);
        //right button
        drawRect(endX - height, yPosition, endX, yPosition + height, rightColor);

        // inside text
        drawCenteredString(minecraft.fontRendererObj, trimmedName, xPosition + width / 2, yPosition + height / 4, ColorCode.WHITE.getColor());
        // description
        drawCenteredString(minecraft.fontRendererObj, description, xPosition + width / 2, yPosition + height + 2, ColorCode.GRAY.getColor());

        GlStateManager.color(1, 1, 1, 1);
        minecraft.getTextureManager().bindTexture(ARROW_LEFT);
        drawModalRectWithCustomSizedTexture(xPosition, yPosition, 0, 0, height, height, height, height);

        minecraft.getTextureManager().bindTexture(ARROW_RIGHT);
        drawModalRectWithCustomSizedTexture(endX - height, yPosition, 0, 0, height, height, height, height);

        if (!name.equals(trimmedName)) {
            if(isOverText(mouseX, mouseY)) {
                // draw tooltip next to the cursor showing the full title
                final int stringWidth = minecraft.fontRendererObj.getStringWidth(name);
                int rectLeft = mouseX + 3;
                int rectTop = mouseY + 3;
                int rectRight = rectLeft + stringWidth + 8;
                int rectBottom = rectTop + 12;
                drawRect(rectLeft, rectTop, rectRight, rectBottom, ColorCode.BLACK.getColor());
                minecraft.fontRendererObj.drawString(name, rectLeft + 4, rectTop+2, ColorCode.WHITE.getColor());
            }
        }
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
        if (isOverLeftButton(mouseX, mouseY)) {
            index = index == itemList.size() - 1 ? 0 : index + 1;
            notifyCallback(index);
        }
        if (isOverRightButton(mouseX, mouseY)) {
            index = index == 0 ? itemList.size() - 1 : index - 1;
            notifyCallback(index);
        }
        return true;
    }

    /**
     * Notifies the callback - if it's not null - that the given index was selected.
     *
     * @param index Selected index
     */
    private void notifyCallback(int index) {
        if(callback != null) {
            callback.onItemSelected(index);
        }
    }

    private boolean isOverText(int mouseX, int mouseY) {
        return mouseX > xPosition + height
                && mouseX < xPosition + width - height
                && mouseY > yPosition
                && mouseY < yPosition + height;
    }

    /**
     * @return Whether the the given mouse position is hovering over the left arrow button
     */
    private boolean isOverLeftButton(int mouseX, int mouseY) {
        return mouseX > xPosition
                && mouseX < xPosition + height
                && mouseY > yPosition
                && mouseY < yPosition + height;
    }

    /**
     * @return Whether the the given mouse position is hovering over the right arrow button
     */
    private boolean isOverRightButton(int mouseX, int mouseY) {
        return mouseX > xPosition + width - height
                && mouseX < xPosition + width
                && mouseY > yPosition
                && mouseY < yPosition + height;
    }

    /**
     * Replaces the last character in the given string with the ellipses character {@code …}
     *
     * @param text Text to ellipsize
     * @return Input text with … at the end
     */
    private String ellipsize(String text) {
        return new StringBuilder(text)
                .replace(text.length() - 1, text.length(), "…")
                .toString();
    }

}