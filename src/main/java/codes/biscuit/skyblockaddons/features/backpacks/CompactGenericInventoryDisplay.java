package codes.biscuit.skyblockaddons.features.backpacks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class CompactGenericInventoryDisplay extends GenericInventoryDisplay {

    protected final Rectangle rightCorner = new Rectangle(54, 8, 72, 26);
    @Getter
    private int width, height;

    public CompactGenericInventoryDisplay(ItemStack[] items, int width, int height) {
        super("", items);
        this.width = width;
        this.height = height;
    }

    @Override
    public void draw(GuiContainer guiContainer, int mouseX, int mouseY, FontRenderer fontRendererObj) {
        Minecraft mc = Minecraft.getMinecraft();
        int x = mouseX, y = mouseY;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(TEXTURE);
        GlStateManager.disableLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 300);


        guiContainer.drawTexturedModalRect(x, y, topLeft.getX1(), topLeft.getY1(), topLeft.getWidth(), topLeft.getHeight());

        int rightX = items.length % width, rightY = items.length / width;
        for (int yPos = 0; yPos < height; yPos++)
            for (int xPos = 0; xPos < width; xPos++) {
                if (xPos == rightX && yPos == rightY) {
                    if (yPos == 0) {
                        guiContainer.drawTexturedModalRect(x + topLeft.getWidth() + xPos * topMiddle.getWidth(), y + topLeft.getHeight() + yPos * sideRight.getHeight(), sideRight.getX1(), sideRight.getY1(), sideRight.getWidth(), sideRight.getHeight());
                    }
                    else
                        guiContainer.drawTexturedModalRect(x + topLeft.getWidth() + xPos * slot.getWidth(),
                                y + topLeft.getHeight() + yPos * slot.getHeight(),
                                rightCorner.getX1(), rightCorner.getY1(), rightCorner.getWidth(), rightCorner.getHeight());

                    guiContainer.drawTexturedModalRect(x + bottomLeft.getWidth() + xPos * bottomMiddle.getWidth(), y + topLeft.getHeight() + (yPos + 1) * sideLeft.getHeight(), bottomRight.getX1(), bottomRight.getY1(), bottomRight.getWidth(), bottomRight.getHeight());
                    continue;
                }

                if ((rightY > 0 || xPos + 1 == rightX) && yPos == 0)
                    guiContainer.drawTexturedModalRect(x + topLeft.getWidth() + (xPos + 1) * topMiddle.getWidth(), y, topRight.getX1(), topRight.getY1(), topRight.getWidth(), topRight.getHeight());

                if (yPos == 0 && (rightY != 0 || xPos < rightX))
                    guiContainer.drawTexturedModalRect(x + topLeft.getWidth() + xPos * topMiddle.getWidth(), y, topMiddle.getX1(), topMiddle.getY1(), topMiddle.getWidth(), topMiddle.getHeight());

                if (xPos < rightX && yPos == rightY)
                    guiContainer.drawTexturedModalRect(x + bottomLeft.getWidth() + xPos * bottomMiddle.getWidth(), y + topLeft.getHeight() + (yPos + 1) * sideLeft.getHeight(), bottomMiddle.getX1(), bottomMiddle.getY1(), bottomMiddle.getWidth(), bottomMiddle.getHeight());

                if (xPos == width - 1 && yPos < rightY)
                    guiContainer.drawTexturedModalRect(x + topLeft.getWidth() + (xPos + 1) * topMiddle.getWidth(), y + topLeft.getHeight() + yPos * sideRight.getHeight(), sideRight.getX1(), sideRight.getY1(), sideRight.getWidth(), sideRight.getHeight());

                if (xPos == 0 && yPos <= rightY)
                    guiContainer.drawTexturedModalRect(x, y + topLeft.getHeight() + yPos * sideLeft.getHeight(), sideLeft.getX1(), sideLeft.getY1(), sideLeft.getWidth(), sideLeft.getHeight());

                if (xPos == 0 && yPos == rightY)
                    guiContainer.drawTexturedModalRect(x, y + topLeft.getHeight() + (yPos + 1) * sideLeft.getHeight(), bottomLeft.getX1(), bottomLeft.getY1(), bottomLeft.getWidth(), bottomLeft.getHeight());

                if (xPos == rightX && yPos + 1 == rightY)
                    guiContainer.drawTexturedModalRect(x + bottomLeft.getWidth() + (xPos + 1) * bottomMiddle.getWidth(), y + topLeft.getHeight() + (yPos + 1) * sideLeft.getHeight(), bottomRight.getX1(), bottomRight.getY1(), bottomRight.getWidth(), bottomRight.getHeight());

                if ((rightX > xPos && rightY == yPos) || rightY > yPos)
                    guiContainer.drawTexturedModalRect(x + topLeft.getWidth() + xPos * slot.getWidth(),
                            y + topLeft.getHeight() + yPos * slot.getHeight(),
                            slot.getX1(), slot.getY1(), slot.getWidth(), slot.getHeight());
            }

        GlStateManager.popMatrix();
        GlStateManager.enableLighting();

        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableRescaleNormal();
        ItemStack toRenderOverlay = null;
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item != null) {
                int itemX = x + topLeft.getWidth() + 1 + (i % width) * slot.getWidth();
                int itemY = y + topLeft.getHeight() + 1 + (i / width) * slot.getHeight();
                RenderItem renderItem = mc.getRenderItem();
                setZLevel(guiContainer, 200);
                renderItem.zLevel = 200;
                renderItem.renderItemAndEffectIntoGUI(item, itemX, itemY);
                renderItem.renderItemOverlayIntoGUI(mc.fontRendererObj, item, itemX, itemY, null);
                setZLevel(guiContainer, 0);
                renderItem.zLevel = 0;
            }
        }
        if (toRenderOverlay != null) {
            drawHoveringText(guiContainer, toRenderOverlay.getTooltip(null, mc.gameSettings.advancedItemTooltips),
                    mouseX, mouseY);
        }

        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
    }
}
