package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ConfigColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;

import java.util.List;

/**
 * Button that lets the user select one item in a given set of items.
 *
 * TODO: callback on item selection, provide selected index
 */
public class ButtonSelect extends GuiButton {

    private static ResourceLocation ARROW_LEFT = new ResourceLocation("skyblockaddons", "flat_arrow_left.png");
    private static ResourceLocation ARROW_RIGHT = new ResourceLocation("skyblockaddons", "flat_arrow_right.png");

    public interface SelectItem {
        String getName();
        String getDescription();
    }

    private List<SelectItem> itemList;
    private final int textWidth;
    private int index = 0;

    /**
     * Note: effective width is about {@code width - 2 * height} as the arrow buttons are squares with
     * a side length of {@code height}.
     * Text will be trimmed and marked with ellipses {@code …} if it is too long to fit in the text area.
     *
     * @param x x position
     * @param y y position
     * @param width total width
     * @param height height
     * @param items List of items to choose from
     */
    /*
     * Rough sketch of the button
     *  __ __________ __
     * |< |          |> |
     *  -- ---------- --
     */
    public ButtonSelect(int x, int y, int width, int height, List<SelectItem> items) {
        super(0, x, y, "");
        textWidth = width - (2 * height) - 6; // 2 * 3 text padding on both sides
        this.width = width;
        this.height = height;
        itemList = items;
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        final int endX = xPosition + width;

        int color = SkyblockAddons.getInstance().getUtils().getDefaultColor(100);
        int leftColor = SkyblockAddons.getInstance().getUtils().getDefaultColor(isOverLeftButton(mouseX, mouseY) ? 200 : 90);
        int rightColor = SkyblockAddons.getInstance().getUtils().getDefaultColor(isOverRightButton(mouseX, mouseY) ? 200 : 90);

        String name = itemList.get(index).getName();
        String text = minecraft.fontRendererObj.trimStringToWidth(name, textWidth);
        if(!name.equals(text)) {
            text = ellipsize(text);
        }
        // background / text area
        drawRect(xPosition, yPosition, endX, yPosition+height, color);
        // left button
        drawRect(xPosition, yPosition, xPosition+height, yPosition+height, leftColor);
        //right button
        drawRect(endX - height, yPosition, endX, yPosition+height, rightColor);
        drawCenteredString(minecraft.fontRendererObj, text, xPosition + width/2, yPosition+height/4, ConfigColor.WHITE.getColor(255));

        minecraft.getTextureManager().bindTexture(ARROW_LEFT);
        drawModalRectWithCustomSizedTexture(xPosition, yPosition,0,0,height,height,height,height);

        minecraft.getTextureManager().bindTexture(ARROW_RIGHT);
        drawModalRectWithCustomSizedTexture(endX - height, yPosition,0,0,height,height,height,height);
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
        if(isOverLeftButton(mouseX, mouseY)) {
            index = index == itemList.size() - 1 ? 0 : index + 1;
        }
        if(isOverRightButton(mouseX, mouseY)) {
            index = index == 0 ? itemList.size() - 1 : index - 1;
        }
        return true;
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
