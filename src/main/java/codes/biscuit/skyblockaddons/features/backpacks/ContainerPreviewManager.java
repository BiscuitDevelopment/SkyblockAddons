package codes.biscuit.skyblockaddons.features.backpacks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import codes.biscuit.skyblockaddons.utils.skyblockdata.ContainerData;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.input.Keyboard;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.List;

/**
 * This class contains utility methods for backpacks and stores the color of the backpack the player has open.
 */
public class ContainerPreviewManager {

    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private static final ResourceLocation UNKNOWN_ITEM = new ResourceLocation("skyblockaddons", "unknown.png");

    /**
     * The container preview to render
     */

    private static ContainerPreview currentContainerPreview;

    /**
     * Whether we are currently frozen in the container preview
     */
    @Getter private static boolean frozen;

    /**
     * The last (epoch) time we toggled the freeze button
     */
   private static long lastToggleFreezeTime;

    /**
     * True when we are drawing an itemstack's tooltip while {@link #isFrozen()} is true
     */
    private static boolean drawingFrozenItemTooltip;

    /**
     * Creates and returns a {@code ContainerPreview} object representing the given {@code ItemStack} if it is a backpack
     *
     * @param stack the {@code ItemStack} to create a {@code Backpack} instance from
     * @return a {@code ContainerPreview} object representing {@code stack} if it is a backpack, or {@code null} otherwise
     */
    public static ContainerPreview getFromItem(ItemStack stack) {
        if (stack == null) {
            return null;
        }

        NBTTagCompound extraAttributes = ItemUtils.getExtraAttributes(stack);
        String skyblockID = ItemUtils.getSkyblockItemID(extraAttributes);
        ContainerData containerData = ItemUtils.getContainerData(skyblockID);
        if (containerData != null) {
            int containerSize = containerData.getSize();

            // Parse out a list of items in the container
            ItemStack[] items = null;
            String compressedDataTag = containerData.getCompressedDataTag();
            List<String> itemStackDataTags = containerData.getItemStackDataTags();

            if (compressedDataTag != null) {
                if (extraAttributes.hasKey(compressedDataTag, Constants.NBT.TAG_BYTE_ARRAY)) {
                    byte[] bytes = extraAttributes.getByteArray(compressedDataTag);
                    items = decompressItems(bytes, containerSize);
                }
            } else if (itemStackDataTags != null) {
                items = new ItemStack[containerSize];
                Iterator<String> itemStackDataTagsIterator = containerData.getItemStackDataTags().iterator();
                for (int itemNumber = 0; itemNumber < containerSize && itemStackDataTagsIterator.hasNext(); itemNumber++) {
                    String key = itemStackDataTagsIterator.next();
                    if (!extraAttributes.hasKey(key)) {
                        continue;
                    }
                    items[itemNumber] = ItemUtils.getPersonalCompactorItemStack(extraAttributes.getString(key));
                }
            }
            if (items == null) {
                SkyblockAddons.getLogger().error("There was an error parsing container data.");
                return null;
            }

            // Get the container color
            BackpackColor color = ItemUtils.getBackpackColor(stack);
            String name = containerData.isPersonalCompactor() ? null : TextUtils.stripColor(stack.getDisplayName());

            return new ContainerPreview(items, name, color, containerData.getNumRows(), containerData.getNumCols());
        }
        return null;
    }

    private static ItemStack[] decompressItems(byte[] bytes, int maxItems) {
        ItemStack[] items = null;
        try {
            NBTTagCompound decompressedData = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
            NBTTagList list = decompressedData.getTagList("i", Constants.NBT.TAG_COMPOUND);
            if (list.hasNoTags()) {
                throw new Exception("Decompressed container list has no item tags");
            }
            int size = Math.min(list.tagCount(), maxItems);
            items = new ItemStack[size];

            for (int i = 0; i < size; i++) {
                NBTTagCompound item = list.getCompoundTagAt(i);
                // This fixes an issue in Hypixel where enchanted potatoes have the wrong id (potato block instead of item).
                short itemID = item.getShort("id");
                if (itemID == 142) { // Potato Block -> Potato Item
                    item.setShort("id", (short) 392);
                } else if (itemID == 141) { // Carrot Block -> Carrot Item
                    item.setShort("id", (short) 391);
                }
                ItemStack itemStack = ItemStack.loadItemStackFromNBT(item);
                items[i] = itemStack;
            }
        } catch (Exception ex) {
            SkyblockAddons.getLogger().error("There was an error decompressing container data.");
            SkyblockAddons.getLogger().catching(ex);
        }
        return items;
    }

    public static void drawContainerPreviews(GuiContainer guiContainer, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getMinecraft();
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (currentContainerPreview != null) {
            int x = currentContainerPreview.getX();
            int y = currentContainerPreview.getY();

            ItemStack[] items = currentContainerPreview.getItems();
            int length = items.length;
            int rows = currentContainerPreview.getNumRows();
            int cols = currentContainerPreview.getNumCols();

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
                    BackpackColor color = currentContainerPreview.getBackpackColor();
                    if (color != null) {
                        GlStateManager.color(color.getR(), color.getG(), color.getB(), 1);
                        textColor = color.getInventoryTextColor();
                    }
                }

                final int textureBorder = 7;
                final int textureTopBorder = 17;
                final int textureItemSquare = 18;

                // Our chest has these properties
                final int topBorder = currentContainerPreview.getName() == null ? textureBorder : textureTopBorder;
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
                if (currentContainerPreview.getName() == null) {
                    // Draw top border
                    guiContainer.drawTexturedModalRect(x, y, 0, 0, squaresEndWidth, topBorder);
                    // Draw left-side and all GUI display rows ("squares")
                    guiContainer.drawTexturedModalRect(x, y + topBorder, 0, textureTopBorder, squaresEndWidth, squaresEndHeight - topBorder);
                } else {
                    // Draw the top-left of the container
                    guiContainer.drawTexturedModalRect(x, y, 0, 0, squaresEndWidth, squaresEndHeight);
                }
                // Draw the bottom-left of the container
                guiContainer.drawTexturedModalRect(x, y + squaresEndHeight, 0, 215, squaresEndWidth, textureBorder);
                // Draw the top-right of the container
                guiContainer.drawTexturedModalRect(x + squaresEndWidth, y, 169, 0, textureBorder, squaresEndHeight);
                // Draw the bottom-right of the container
                guiContainer.drawTexturedModalRect(x + squaresEndWidth, y + squaresEndHeight, 169, 215, textureBorder, textureBorder);

                if (currentContainerPreview.getName() != null) {
                    mc.fontRendererObj.drawString(currentContainerPreview.getName(), x + 8, y + 6, textColor);
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

                        if (frozen && mouseX > itemX && mouseX < itemX + 16 && mouseY > itemY && mouseY < itemY + 16) {
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

                        if (frozen && mouseX > itemX && mouseX < itemX+16 && mouseY > itemY && mouseY < itemY+16) {
                            tooltipItem = item;
                        }
                    }
                }
            }
            if (tooltipItem != null) {
                // Translate up to fix patcher glitch
                GlStateManager.pushMatrix();
                GlStateManager.translate(0,0, 302);
                drawingFrozenItemTooltip = true;
                guiContainer.drawHoveringText(tooltipItem.getTooltip(mc.thePlayer, mc.gameSettings.advancedItemTooltips), mouseX, mouseY);
                drawingFrozenItemTooltip = false;
                GlStateManager.popMatrix();
            }
            if (!frozen) {
                currentContainerPreview = null;
            }
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
        }
    }

    //TODO Fix for Hypixel localization
    public static boolean onRenderTooltip(ItemStack itemStack, int x, int y) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        // Cancel tooltips while containers are frozen and we aren't trying to render a tooltip in the backpack
        if (frozen && !drawingFrozenItemTooltip) {
            return true;
        }

        if (main.getConfigValues().isEnabled(Feature.SHOW_BACKPACK_PREVIEW)) {
            // Don't show if we only want to show while holding shift, and the player isn't holding shift
            if (main.getConfigValues().isEnabled(Feature.SHOW_BACKPACK_HOLDING_SHIFT) && !GuiScreen.isShiftKeyDown()) {
                return false;
            }
            // Don't render the preview the item represents a crafting recipe or the result of one.
            if (ItemUtils.isMenuItem(itemStack)) {
                return false;
            }
            // Check the subfeature conditions
            NBTTagCompound extraAttributes = ItemUtils.getExtraAttributes(itemStack);
            ContainerData containerData = ItemUtils.getContainerData(ItemUtils.getSkyblockItemID(extraAttributes));

            // TODO: Does checking menu item handle the baker inventory thing?
            if (containerData == null || (containerData.isCakeBag() && main.getConfigValues().isDisabled(Feature.CAKE_BAG_PREVIEW)) ||
                    (containerData.isPersonalCompactor() && main.getConfigValues().isDisabled(Feature.SHOW_PERSONAL_COMPACTOR_PREVIEW))) {
                return false;
            }

            //TODO: Probably some optimizations here we can do. Can we check chest equivalence?
            // Avoid showing backpack preview in auction stuff.
            net.minecraft.inventory.Container playerContainer = Minecraft.getMinecraft().thePlayer.openContainer;
            if (playerContainer instanceof ContainerChest) {
                IInventory chestInventory = ((ContainerChest) playerContainer).getLowerChestInventory();
                if (chestInventory.hasCustomName()) {
                    String chestName = chestInventory.getDisplayName().getUnformattedText();
                    if (chestName.contains("Auction") || "Your Bids".equals(chestName)) {

                        // Make sure this backpack is in the auction house and not just in your inventory before cancelling.
                        for (int slotNumber = 0; slotNumber < chestInventory.getSizeInventory(); slotNumber++) {
                            if (chestInventory.getStackInSlot(slotNumber) == itemStack) {
                                return false;
                            }
                        }
                    }
                }
            }

            ContainerPreview containerPreview = ContainerPreviewManager.getFromItem(itemStack);
            if (containerPreview != null) {
                containerPreview.setX(x);
                containerPreview.setY(y);

                // Handle the freeze container toggle
                if (isFreezeKeyDown() && System.currentTimeMillis() - lastToggleFreezeTime > 500) {
                    lastToggleFreezeTime = System.currentTimeMillis();
                    frozen = !frozen;
                    currentContainerPreview = containerPreview;
                }

                if (!frozen) {
                    currentContainerPreview = containerPreview;
                }
                main.getPlayerListener().onItemTooltip(new ItemTooltipEvent(itemStack, null, null, false));
                return true;
            }
        }

        if (frozen) {
            return true;
        }

        return false;
    }


    /**
     * Returns whether the backpack freeze key is down
     *
     * @return {@code true} if the backpack freeze key is down, {@code false} otherwise
     */
    private static boolean isFreezeKeyDown() {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getFreezeBackpackKey().isKeyDown()) return true;
        try {
            if (Keyboard.isKeyDown(main.getFreezeBackpackKey().getKeyCode())) return true;
        } catch (Exception ignored) {}

        return false;
    }

    public static void onContainerKeyTyped(int keyCode) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getUtils().isOnSkyblock()) {
            if (keyCode == 1 || keyCode == Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode()) {
                frozen = false;
                currentContainerPreview = null;
            }
            if (keyCode == main.getFreezeBackpackKey().getKeyCode() && frozen && System.currentTimeMillis() - lastToggleFreezeTime > 500) {
                lastToggleFreezeTime = System.currentTimeMillis();
                frozen = false;
            }
        }
    }
}
