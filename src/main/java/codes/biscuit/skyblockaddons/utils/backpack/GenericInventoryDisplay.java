package codes.biscuit.skyblockaddons.utils.backpack;

import codes.biscuit.skyblockaddons.asm.hooks.GuiContainerHook;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsTransformer;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import codes.biscuit.skyblockaddons.utils.nifty.reflection.MinecraftReflection;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * A generic inventory class to allow for easy rendering of any new inventories that get added
 * TODO: Add support for names, thus needing a check for adding 6 to the topMiddle Height or what ever it is
 *
 * @author Charzard4261
 */
public class GenericInventoryDisplay {

    private final static ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private static Field zLevel = null;
    private static Method drawHoveringText = null;
    private final Rectangle topLeft = new Rectangle(0, 0, 7, 7);
    private final Rectangle topMiddle = new Rectangle(7, 0, 25, 7);
    private final Rectangle topRight = new Rectangle(169, 0, 176, 7);
    private final Rectangle bottomLeft = new Rectangle(0, 215, 7, 222);
    private final Rectangle bottomMiddle = new Rectangle(7, 215, 25, 222);
    private final Rectangle bottomRight = new Rectangle(169, 215, 176, 222);
    private final Rectangle sideLeft = new Rectangle(0, 17, 7, 35);
    private final Rectangle sideRight = new Rectangle(169, 17, 176, 35);
    private final Rectangle slot = new Rectangle(7, 17, 25, 35);
    @Getter
    private int width, height;
    private ItemStack[] items;
    @Getter
    private String name = "";


    public GenericInventoryDisplay(int width, int height, ItemStack[] items) {
        this.width = width;
        this.height = height;
        this.items = items;
    }

    public GenericInventoryDisplay(String containerName, int width, int height, ItemStack[] items) {
        this(width, height, items);
        this.name = containerName;
    }

    private static void setZLevel(Gui gui, int zLevelToSet) {
        if (SkyblockAddonsTransformer.isLabymodClient()) { // There are no access transformers in labymod.
            try {
                if (zLevel == null) {
                    zLevel = gui.getClass().getDeclaredField("e");
                    zLevel.setAccessible(true);
                }
                if (zLevel != null) {
                    zLevel.set(gui, zLevelToSet);
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        } else {
            gui.zLevel = zLevelToSet;
        }
    }

    private static void drawHoveringText(GuiContainer guiContainer, java.util.List<String> text, int x, int y) {
        if (SkyblockAddonsTransformer.isLabymodClient()) { // There are no access transformers in labymod.
            try {
                if (drawHoveringText == null) {
                    drawHoveringText = guiContainer.getClass().getSuperclass().getDeclaredMethod("a",
                            List.class, int.class, int.class);
                    drawHoveringText.setAccessible(true);
                }
                if (drawHoveringText != null) {
                    drawHoveringText.invoke(guiContainer, text, x, y);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {
            guiContainer.drawHoveringText(text, x, y);
        }
    }

    public void draw(GuiContainer guiContainer, int mouseX, int mouseY, FontRenderer fontRendererObj) {
        Minecraft mc = Minecraft.getMinecraft();
        int x = mouseX, y = mouseY;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
        GlStateManager.disableLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 300);

        int extend = (name.equals("") ? 0 : 10);

        System.out.println(name + " " + extend);

        int bottomY = y + topLeft.getHeight() + height * sideLeft.getHeight() + extend;
        int rightX = x + topLeft.getWidth() + width * topMiddle.getWidth();


        guiContainer.drawTexturedModalRect(x, y, topLeft.x1, topLeft.y1, topLeft.getWidth(), topLeft.getHeight() + extend);
        guiContainer.drawTexturedModalRect(rightX, y, topRight.x1, topRight.y1, topRight.getWidth(), topRight.getHeight() + extend);
        for (int i = 0; i < width; i++) {
            guiContainer.drawTexturedModalRect(x + topLeft.getWidth() + i * topMiddle.getWidth(), y, topMiddle.x1, topMiddle.y1, topMiddle.getWidth(), topMiddle.getHeight() + extend);
            guiContainer.drawTexturedModalRect(x + bottomLeft.getWidth() + i * bottomMiddle.getWidth(), bottomY, bottomMiddle.x1, bottomMiddle.y1, bottomMiddle.getWidth(), bottomMiddle.getHeight());
        }
        guiContainer.drawTexturedModalRect(x, bottomY, bottomLeft.x1, bottomLeft.y1, bottomLeft.getWidth(), bottomLeft.getHeight());
        guiContainer.drawTexturedModalRect(rightX, bottomY, bottomRight.x1, bottomRight.y1, bottomRight.getWidth(), bottomRight.getHeight());

        for (int i = 0; i < height; i++) {
            guiContainer.drawTexturedModalRect(x, y + topLeft.getHeight() + i * sideLeft.getHeight() + extend, sideLeft.x1, sideLeft.y1, sideLeft.getWidth(), sideLeft.getHeight());
            guiContainer.drawTexturedModalRect(rightX, y + topLeft.getHeight() + i * sideRight.getHeight() + extend, sideRight.x1, sideRight.y1, sideRight.getWidth(), sideRight.getHeight());
        }

        for (int xPos = 0; xPos < width; xPos++)
            for (int yPos = 0; yPos < height; yPos++)
                guiContainer.drawTexturedModalRect(x + topLeft.getWidth() + xPos * slot.getWidth(),
                        y + topLeft.getHeight() + yPos * slot.getHeight() + extend,
                        slot.x1, slot.y1, slot.getWidth(), slot.getHeight());

        MinecraftReflection.FontRenderer.drawString(name, x+8, y+6, 4210752);

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
                int itemY = y + topLeft.getHeight() + 1 + (i / width) * slot.getHeight() + extend;
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

    private class Rectangle {
        private int x1, x2, y1, y2;

        public Rectangle(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        public int getWidth() {
            return x2 - x1;
        }

        public int getHeight() {
            return y2 - y1;
        }
    }
}
