package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.utils.ReturnValue;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.InventoryType;
import codes.biscuit.skyblockaddons.features.backpacks.BackpackColor;
import codes.biscuit.skyblockaddons.features.backpacks.ContainerPreview;
import codes.biscuit.skyblockaddons.features.backpacks.ContainerPreviewManager;
import codes.biscuit.skyblockaddons.features.craftingpatterns.CraftingPattern;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GuiContainerHook {

    private static final ResourceLocation LOCK = new ResourceLocation("skyblockaddons", "lock.png");
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private static final int OVERLAY_RED = ColorCode.RED.getColor(127);
    private static final int OVERLAY_GREEN = ColorCode.GREEN.getColor(127);

    public static void keyTyped(int keyCode) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (keyCode == 1 || keyCode == Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode()) {
            ContainerPreviewManager.setFrozen(false);
            ContainerPreviewManager.setContainerPreview(null);
        }
        if (keyCode == main.getFreezeBackpackKey().getKeyCode() && ContainerPreviewManager.isFrozen() &&
                System.currentTimeMillis() - ContainerPreviewManager.getLastToggleFreezeTime() > 500) {
            ContainerPreviewManager.setLastToggleFreezeTime(System.currentTimeMillis());
            ContainerPreviewManager.setFrozen(false);
        }
    }

    public static void drawBackpacks(GuiContainer guiContainer, int mouseX, int mouseY, FontRenderer fontRendererObj) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        ContainerPreview containerPreview = ContainerPreviewManager.getContainerPreview();
        Minecraft mc = Minecraft.getMinecraft();
        if (containerPreview != null) {
            int x = containerPreview.getX();
            int y = containerPreview.getY();

            ItemStack[] items = containerPreview.getItems();
            int length = items.length;
            int rows = containerPreview.getNumRows();
            int cols = containerPreview.getNumCols();

            int screenHeight = guiContainer.height;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            ItemStack tooltipItem = null;

            if (main.getConfigValues().getBackpackStyle() == EnumUtils.BackpackStyle.GUI) {
                mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
                GlStateManager.disableLighting();
                GlStateManager.pushMatrix();
                GlStateManager.translate(0,0,300);
                int textColor = 4210752;
                if (main.getConfigValues().isEnabled(Feature.MAKE_BACKPACK_INVENTORIES_COLORED)) {
                    BackpackColor color = containerPreview.getBackpackColor();
                    if (color != null) {
                        GlStateManager.color(color.getR(), color.getG(), color.getB(), 1);
                        textColor = color.getInventoryTextColor();
                    }
                }

                final int textureBorder = 7;
                final int textureTopBorder = 17;
                final int textureItemSquare = 18;

                // Our chest has these properties
                final int topBorder = containerPreview.getName() == null ? textureBorder : textureTopBorder;
                int totalWidth = cols * textureItemSquare + 2 * textureBorder;
                int totalHeight = rows * textureItemSquare + topBorder + textureBorder;
                int squaresEndWidth = totalWidth - textureBorder;
                int squaresEndHeight = totalHeight - textureBorder;

                if (x + totalWidth > guiContainer.width) {
                    x -= totalWidth;
                }

                if (y + totalHeight > screenHeight) {
                    y = screenHeight - totalHeight;
                }

                // If there is no name, don't render the full top of the chest to make things look cleaner
                if (containerPreview.getName() == null) {
                    // Draw top border
                    guiContainer.drawTexturedModalRect(x, y, 0, 0, squaresEndWidth, topBorder);
                    // Draw left-side and all GUI display rows ("squares")
                    guiContainer.drawTexturedModalRect(x, y + topBorder, 0, textureTopBorder, squaresEndWidth, squaresEndHeight - topBorder);
                }
                else {
                    // Draw the top-left of the container
                    guiContainer.drawTexturedModalRect(x, y, 0, 0, squaresEndWidth, squaresEndHeight);
                }
                // Draw the bottom-left of the container
                guiContainer.drawTexturedModalRect(x, y + squaresEndHeight, 0, 215, squaresEndWidth, textureBorder);
                // Draw the top-right of the container
                guiContainer.drawTexturedModalRect(x + squaresEndWidth, y, 169, 0, textureBorder, squaresEndHeight);
                // Draw the bottom-right of the container
                guiContainer.drawTexturedModalRect(x + squaresEndWidth, y + squaresEndHeight, 169, 215, textureBorder, textureBorder);

                if (containerPreview.getName() != null) {
                    mc.fontRendererObj.drawString(containerPreview.getName(), x + 8, y + 6, textColor);
                }

                GlStateManager.popMatrix();
                GlStateManager.enableLighting();

                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();
                int itemStartX = x + textureBorder + 1;
                int itemStartY = y + topBorder + 1;
                for (int i = 0; i < length; i++) {
                    ItemStack item = items[i];
                    if (item != null) {
                        int itemX = itemStartX + ((i % cols) * textureItemSquare);
                        int itemY = itemStartY + ((i / cols) * textureItemSquare);
                        RenderItem renderItem = mc.getRenderItem();
                        guiContainer.zLevel = 200;
                        renderItem.zLevel = 200;
                        renderItem.renderItemAndEffectIntoGUI(item, itemX, itemY);
                        renderItem.renderItemOverlayIntoGUI(mc.fontRendererObj, item, itemX, itemY, null);
                        guiContainer.zLevel = 0;
                        renderItem.zLevel = 0;

                        if (ContainerPreviewManager.isFrozen() && mouseX > itemX && mouseX < itemX+16 && mouseY > itemY && mouseY < itemY+16) {
                            tooltipItem = item;
                        }
                    }
                }
            } else {
                int totalWidth = (16 * cols) + 3;
                if (x + totalWidth > guiContainer.width) {
                    x -= totalWidth;
                }
                int totalHeight = (16 * rows) + 3;
                if (y + totalHeight > screenHeight) {
                    y = screenHeight - totalHeight;
                }

                GlStateManager.disableLighting();
                GlStateManager.pushMatrix();
                GlStateManager.translate(0,0, 300);
                Gui.drawRect(x, y, x + totalWidth, y + totalHeight, ColorCode.DARK_GRAY.getColor(250));
                GlStateManager.popMatrix();
                GlStateManager.enableLighting();

                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();
                for (int i = 0; i < length; i++) {
                    ItemStack item = items[i];
                    if (item != null) {
                        int itemX = x + ((i % cols) * 16);
                        int itemY = y + ((i / cols) * 16);
                        RenderItem renderItem = mc.getRenderItem();
                        guiContainer.zLevel = 200;
                        renderItem.zLevel = 200;
                        renderItem.renderItemAndEffectIntoGUI(item, itemX, itemY);
                        renderItem.renderItemOverlayIntoGUI(mc.fontRendererObj, item, itemX, itemY, null);
                        guiContainer.zLevel = 0;
                        renderItem.zLevel = 0;

                        if (ContainerPreviewManager.isFrozen() && mouseX > itemX && mouseX < itemX+16 && mouseY > itemY && mouseY < itemY+16) {
                            tooltipItem = item;
                        }
                    }
                }
            }
            if (tooltipItem != null) {
                // Translate up to fix patcher glitch
                GlStateManager.pushMatrix();
                GlStateManager.translate(0,0, 302);
                ContainerPreviewManager.setRenderingBackpackTooltip(true);
                guiContainer.drawHoveringText(tooltipItem.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips), mouseX, mouseY);
                ContainerPreviewManager.setRenderingBackpackTooltip(false);
                GlStateManager.popMatrix();
            }
            if (!ContainerPreviewManager.isFrozen()) {
                ContainerPreviewManager.setContainerPreview(null);
            }
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
        }
    }

    public static void setLastSlot() {
        SkyblockAddons.getInstance().getUtils().setLastHoveredSlot(-1);
    }

    public static void drawGradientRect(GuiContainer guiContainer, int left, int top, int right, int bottom, int startColor, int endColor, Slot theSlot) {
        if (ContainerPreviewManager.isFrozen()) return;

        SkyblockAddons main = SkyblockAddons.getInstance();
        if (theSlot != null && theSlot.getHasStack() && main.getConfigValues().isEnabled(Feature.DISABLE_EMPTY_GLASS_PANES) && main.getUtils().isEmptyGlassPane(theSlot.getStack())) {
            return;
        }
        Container container = Minecraft.getMinecraft().thePlayer.openContainer;
        if (theSlot != null) {
            int slotNum = theSlot.slotNumber + main.getInventoryUtils().getSlotDifference(container);
            main.getUtils().setLastHoveredSlot(slotNum);
            if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) &&
                    main.getUtils().isOnSkyblock() && main.getConfigValues().getLockedSlots().contains(slotNum)
                    && (slotNum >= 9 || container instanceof ContainerPlayer && slotNum >= 5)) {
                guiContainer.drawGradientRect(left, top, right, bottom, OVERLAY_RED, OVERLAY_RED);
                return;
            }
        }
        guiContainer.drawGradientRect(left, top, right, bottom, startColor, endColor);
    }

    public static void drawSlot(GuiContainer guiContainer, Slot slot) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Minecraft mc = Minecraft.getMinecraft();
        Container container = mc.thePlayer.openContainer;

        if (slot != null) {
            // Draw crafting pattern overlays inside the crafting grid.
            if (main.getConfigValues().isEnabled(Feature.CRAFTING_PATTERNS) && main.getUtils().isOnSkyblock()
                    && slot.inventory.getDisplayName().getUnformattedText().equals(CraftingPattern.CRAFTING_TABLE_DISPLAYNAME)
                    && main.getPersistentValuesManager().getPersistentValues().getSelectedCraftingPattern() != CraftingPattern.FREE) {

                int craftingGridIndex = CraftingPattern.slotToCraftingGridIndex(slot.getSlotIndex());
                if (craftingGridIndex >= 0) {
                    int slotLeft = slot.xDisplayPosition;
                    int slotTop = slot.yDisplayPosition;
                    int slotRight = slotLeft + 16;
                    int slotBottom = slotTop + 16;
                    if (main.getPersistentValuesManager().getPersistentValues().getSelectedCraftingPattern().isSlotInPattern(craftingGridIndex)) {
                        if (!slot.getHasStack()) {
                            guiContainer.drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, OVERLAY_GREEN, OVERLAY_GREEN);
                        }
                    } else {
                        if (slot.getHasStack()) {
                            guiContainer.drawGradientRect(slotLeft, slotTop, slotRight, slotBottom, OVERLAY_RED, OVERLAY_RED);
                        }
                    }
                }
            }

            if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) &&
                    main.getUtils().isOnSkyblock()) {
                int slotNum = slot.slotNumber + main.getInventoryUtils().getSlotDifference(container);
                if (main.getConfigValues().getLockedSlots().contains(slotNum)
                        && (slotNum >= 9 || container instanceof ContainerPlayer && slotNum >= 5)) {
                    GlStateManager.disableLighting();
                    GlStateManager.disableDepth();
                    GlStateManager.color(1,1,1,0.4F);
                    GlStateManager.enableBlend();
                    mc.getTextureManager().bindTexture(LOCK);
                    mc.ingameGUI.drawTexturedModalRect(slot.xDisplayPosition, slot.yDisplayPosition, 0, 0, 16, 16);
                    GlStateManager.enableLighting();
                    GlStateManager.enableDepth();
                }
            }
        }
    }

    public static void keyTyped(GuiContainer guiContainer, int keyCode, Slot theSlot, ReturnValue<?> returnValue) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Minecraft mc = Minecraft.getMinecraft();
        if (main.getUtils().isOnSkyblock()) {
            if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && (keyCode != 1 && keyCode != mc.gameSettings.keyBindInventory.getKeyCode())) {
                int slot = main.getUtils().getLastHoveredSlot();
                if (mc.thePlayer.inventory.getItemStack() == null && theSlot != null) {
                    for (int i = 0; i < 9; ++i) {
                        if (keyCode == mc.gameSettings.keyBindsHotbar[i].getKeyCode()) {
                            slot = i + 36; // They are hotkeying, the actual slot is the targeted one, +36 because
                        }
                    }
                }
                if (slot >= 9 || mc.thePlayer.openContainer instanceof ContainerPlayer && slot >= 5) {
                    if (main.getConfigValues().getLockedSlots().contains(slot)) {
                        if (main.getLockSlotKey().getKeyCode() == keyCode) {
                            main.getUtils().playLoudSound("random.orb", 1);
                            main.getConfigValues().getLockedSlots().remove(slot);
                            main.getConfigValues().saveConfig();
                        } else {
                            main.getUtils().playLoudSound("note.bass", 0.5);
                            returnValue.cancel(); // slot is locked
                            return;
                        }
                    } else {
                        if (main.getLockSlotKey().getKeyCode() == keyCode) {
                            main.getUtils().playLoudSound("random.orb", 0.1);
                            main.getConfigValues().getLockedSlots().add(slot);
                            main.getConfigValues().saveConfig();
                        }
                    }
                }
            }
            if (mc.gameSettings.keyBindDrop.getKeyCode() == keyCode && main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) && !main.getUtils().isInDungeon()) {
                if (!main.getUtils().getItemDropChecker().canDropItem(theSlot)) returnValue.cancel();
            }
        }
    }

    /**
     * This method returns true to CANCEL the click in a GUI (lol I get confused)
     */
    public static boolean onHandleMouseClick(Slot slot, int slotId, int clickedButton, int clickType) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        return slot != null && slot.getHasStack() && main.getConfigValues().isEnabled(Feature.DISABLE_EMPTY_GLASS_PANES) &&
                main.getUtils().isEmptyGlassPane(slot.getStack()) && main.getUtils().isOnSkyblock() && !main.getUtils().isInDungeon() &&
                (main.getInventoryUtils().getInventoryType() != InventoryType.ULTRASEQUENCER || main.getUtils().isGlassPaneColor(slot.getStack(), EnumDyeColor.BLACK));
    }
}
