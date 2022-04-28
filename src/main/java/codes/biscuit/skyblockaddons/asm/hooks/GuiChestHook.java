package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.utils.ReturnValue;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.InventoryType;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.core.npc.NPCUtils;
import codes.biscuit.skyblockaddons.features.backpacks.BackpackColor;
import codes.biscuit.skyblockaddons.features.backpacks.BackpackInventoryManager;
import codes.biscuit.skyblockaddons.gui.IslandWarpGui;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;
import org.objectweb.asm.tree.ClassNode;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO Fix for Hypixel localization
public class GuiChestHook {
    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final FontRenderer fontRenderer = mc.fontRendererObj;

    /** Strings for reforge filter */
    private static final String TYPE_TO_MATCH = Translations.getMessage("messages.reforges");
    private static final String TYPE_ENCHANTMENTS = Translations.getMessage("messages.typeEnchantmentsHere", TYPE_TO_MATCH);
    private static final String SEPARATE_MULTIPLE = Translations.getMessage("messages.separateMultiple");
    private static final String ENCHANTS_TO_INCLUDE = Translations.getMessage("messages.enchantsToMatch", TYPE_TO_MATCH);
    private static final String INCLUSION_EXAMPLE = Translations.getMessage("messages.reforgeInclusionExample");
    private static final String ENCHANTS_TO_EXCLUDE = Translations.getMessage("messages.enchantsToExclude", TYPE_TO_MATCH);
    private static final String EXCLUSION_EXAMPLE = Translations.getMessage("messages.reforgeExclusionExample");

    private static final int REFORGE_MENU_HEIGHT = 222 - 108 + 5 * 18;

    @Getter
    private static IslandWarpGui islandWarpGui = null;

    /** Reforge filter text field for reforges to match */
    private static GuiTextField textFieldMatches = null;
    /** Reforge filter text field for reforges to exclude */
    private static GuiTextField textFieldExclusions = null;

    private static final Pattern warpPattern = Pattern.compile("(?:§5§o)?§8/warp ([a-z_]*)");
    private static final Pattern unlockedPattern = Pattern.compile("(?:§5§o)?§eClick to warp!");
    private static final Pattern notUnlockedPattern = Pattern.compile("(?:§5§o)?§cWarp not unlocked!");
    private static final Pattern inCombatPattern = Pattern.compile("(?:§5§o)?§cYou're in combat!");
    private static final Pattern youAreHerePattern = Pattern.compile("(?:§5§o)?§aYou are here!");

    private static int reforgeFilterHeight;

    /** String dimensions for reforge filter */
    private static int maxStringWidth;
    private static int typeEnchantmentsHeight;
    private static int enchantsToIncludeHeight;
    private static int enchantsToExcludeHeight;

    /**
     * @see codes.biscuit.skyblockaddons.asm.GuiChestTransformer#transform(ClassNode, String)
     */
    @SuppressWarnings("unused")
    public static void updateScreen() {
        if (textFieldMatches != null && textFieldExclusions != null) {
            textFieldMatches.updateCursorCounter();
            textFieldExclusions.updateCursorCounter();
        }
    }

    /**
     * Resets variables when the chest is closed
     *
     * @see codes.biscuit.skyblockaddons.asm.GuiChestTransformer#transform(ClassNode, String)
     */
    @SuppressWarnings("unused")
    public static void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);

        islandWarpGui = null;
        BackpackInventoryManager.setBackpackColor(null);

        if (main.getConfigValues().isEnabled(Feature.SHOW_SALVAGE_ESSENCES_COUNTER)) {
            InventoryType inventoryType = main.getInventoryUtils().getInventoryType();

            if (inventoryType == InventoryType.SALVAGING) {
                main.getDungeonManager().getSalvagedEssences().clear();
            }
        }
    }

    /**
     * @see codes.biscuit.skyblockaddons.asm.GuiChestTransformer#transform(ClassNode, String)
     */
    @SuppressWarnings("unused")
    public static void drawScreenIslands(int mouseX, int mouseY, ReturnValue<?> returnValue) {
        if (!SkyblockAddons.getInstance().getUtils().isOnSkyblock()) {
            return; // don't draw any overlays outside SkyBlock
        }

        Container playerContainer = mc.thePlayer.openContainer;
        if (playerContainer instanceof ContainerChest && SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.FANCY_WARP_MENU)) {
            IInventory chestInventory = ((ContainerChest) playerContainer).getLowerChestInventory();
            if (chestInventory.hasCustomName()) {
                String chestName = chestInventory.getDisplayName().getUnformattedText();
                if (chestName.equals("Fast Travel")) {
                    Map<IslandWarpGui.Marker, IslandWarpGui.UnlockedStatus> markers = new EnumMap<>(IslandWarpGui.Marker.class);

                    for (int slot = 0; slot < chestInventory.getSizeInventory(); slot++) {
                        ItemStack itemStack = chestInventory.getStackInSlot(slot);

                        if (itemStack != null && (Items.skull == itemStack.getItem() || Items.paper == itemStack.getItem())) {
                            List<String> lore = ItemUtils.getItemLore(itemStack);
                            IslandWarpGui.Marker marker = null;
                            IslandWarpGui.UnlockedStatus status = IslandWarpGui.UnlockedStatus.UNKNOWN;

                            for (String loreLine : lore) {
                                Matcher matcher = warpPattern.matcher(loreLine);
                                if (matcher.matches()) {
                                    marker = IslandWarpGui.Marker.fromWarpName(matcher.group(1));
                                }

                                matcher = unlockedPattern.matcher(loreLine);
                                if (matcher.matches() || youAreHerePattern.matcher(loreLine).matches()) {
                                    status = IslandWarpGui.UnlockedStatus.UNLOCKED;
                                    break;
                                }

                                matcher = notUnlockedPattern.matcher(loreLine);
                                if (matcher.matches()) {
                                    status = IslandWarpGui.UnlockedStatus.NOT_UNLOCKED;
                                    break;
                                }

                                matcher = inCombatPattern.matcher(loreLine);
                                if (matcher.matches()) {
                                    status = IslandWarpGui.UnlockedStatus.IN_COMBAT;
                                    break;
                                }
                            }

                            if (marker != null) {
                                markers.put(marker, status);
                            }
                        }
                    }

                    for (IslandWarpGui.Marker marker : IslandWarpGui.Marker.values()) {
                        if (!markers.containsKey(marker)) {
                            markers.put(marker, IslandWarpGui.UnlockedStatus.UNKNOWN);
                        }
                    }

                    /*
                    Special case: We have an extra dungeon hub warp as a separate island for convenience, so we have to
                    add it manually.
                     */
                    markers.put(IslandWarpGui.Marker.DUNGEON_HUB_ISLAND,
                            markers.getOrDefault(IslandWarpGui.Marker.DUNGEON_HUB, IslandWarpGui.UnlockedStatus.UNKNOWN));

                    if (islandWarpGui == null || !islandWarpGui.getMarkers().equals(markers)) {
                        islandWarpGui = new IslandWarpGui(markers);
                        ScaledResolution scaledresolution = new ScaledResolution(mc);
                        int i = scaledresolution.getScaledWidth();
                        int j = scaledresolution.getScaledHeight();
                        islandWarpGui.setWorldAndResolution(mc, i, j);
                    }

                    try {
                        islandWarpGui.drawScreen(mouseX, mouseY, 0);
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }

                    returnValue.cancel();
                } else {
                    islandWarpGui = null;
                }
            } else {
                islandWarpGui = null;
            }
        } else {
            islandWarpGui = null;
        }
    }

    public static void drawScreen(int guiLeft, int guiTop) {
        if (!SkyblockAddons.getInstance().getUtils().isOnSkyblock()) {
            return; // don't draw any overlays outside SkyBlock
        }

        InventoryType inventoryType = SkyblockAddons.getInstance().getInventoryUtils().getInventoryType();

        if (inventoryType == InventoryType.SALVAGING) {
            int ySize = 222 - 108 + 6 * 18;
            float x = guiLeft - 69 - 5;
            float y = guiTop + ySize / 2F - 72 / 2F;

            SkyblockAddons.getInstance().getRenderListener().drawCollectedEssences(x, y, false, false);
        }

        if (SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.REFORGE_FILTER)) {
            if ((inventoryType== InventoryType.BASIC_REFORGING) &&
                    textFieldMatches != null) {

                int defaultBlue = main.getUtils().getDefaultBlue(255);
                int x = guiLeft - 160;
                if (x<0) {
                    x = 20;
                }
                int y = guiTop + REFORGE_MENU_HEIGHT / 2 - reforgeFilterHeight / 2;

                GlStateManager.color(1F, 1F, 1F);
                fontRenderer.drawSplitString(TYPE_ENCHANTMENTS, x, y, maxStringWidth, defaultBlue);
                y = y + typeEnchantmentsHeight;
                fontRenderer.drawSplitString(SEPARATE_MULTIPLE, x, y, maxStringWidth, defaultBlue);

                int placeholderTextX = textFieldMatches.xPosition + 4;
                int placeholderTextY = textFieldMatches.yPosition + (textFieldMatches.height - 8) / 2;

                y = textFieldMatches.yPosition - enchantsToIncludeHeight - 1;
                fontRenderer.drawSplitString(ENCHANTS_TO_INCLUDE, x, y, maxStringWidth, defaultBlue);

                textFieldMatches.drawTextBox();
                if (StringUtils.isEmpty(textFieldMatches.getText())) {
                    fontRenderer.drawString(fontRenderer.trimStringToWidth(INCLUSION_EXAMPLE, textFieldMatches.width), placeholderTextX, placeholderTextY, ColorCode.DARK_GRAY.getColor());
                }

                y = textFieldExclusions.yPosition - enchantsToExcludeHeight - 1;
                fontRenderer.drawSplitString(ENCHANTS_TO_EXCLUDE, x, y, maxStringWidth, defaultBlue);

                placeholderTextY = textFieldExclusions.yPosition + (textFieldExclusions.height - 8) / 2;
                textFieldExclusions.drawTextBox();
                if (StringUtils.isEmpty(textFieldExclusions.getText())) {
                    fontRenderer.drawString(fontRenderer.trimStringToWidth(EXCLUSION_EXAMPLE, textFieldExclusions.width), placeholderTextX, placeholderTextY, ColorCode.DARK_GRAY.getColor());
                }
            }
        }
    }

    /**
     * @see codes.biscuit.skyblockaddons.asm.GuiChestTransformer#transform(ClassNode, String)
     */
    @SuppressWarnings("unused")
    public static void initGui(IInventory lowerChestInventory, int guiLeft, int guiTop, FontRenderer fontRendererObj) {
        if (!SkyblockAddons.getInstance().getUtils().isOnSkyblock()) {
            return; // don't draw any overlays outside SkyBlock
        }

        InventoryType inventoryType = SkyblockAddons.getInstance().getInventoryUtils().getInventoryType();

        if (inventoryType != null) {
            if (SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.REFORGE_FILTER) && inventoryType ==
                    InventoryType.BASIC_REFORGING) {
                int xPos = guiLeft - 160;
                if (xPos<0) {
                    xPos = 20;
                }
                int yPos;
                int textFieldWidth = guiLeft - 20 - xPos;
                int textFieldHeight = REFORGE_MENU_HEIGHT / 10;
                int textFieldSpacing = (int) (textFieldHeight * 1.5);

                // Calculate the height of the whole thing to center it vertically in relation to the chest UI.
                maxStringWidth = textFieldWidth + 5;
                typeEnchantmentsHeight = fontRenderer.splitStringWidth(TYPE_ENCHANTMENTS, maxStringWidth);
                int separateEnchantmentsHeight = fontRenderer.splitStringWidth(SEPARATE_MULTIPLE, maxStringWidth) + fontRendererObj.FONT_HEIGHT;
                enchantsToIncludeHeight = fontRenderer.splitStringWidth(ENCHANTS_TO_INCLUDE, maxStringWidth);
                enchantsToExcludeHeight = fontRenderer.splitStringWidth(ENCHANTS_TO_EXCLUDE, maxStringWidth);
                reforgeFilterHeight = typeEnchantmentsHeight + separateEnchantmentsHeight + enchantsToIncludeHeight +
                        2 * textFieldHeight + textFieldSpacing;

                yPos = guiTop + REFORGE_MENU_HEIGHT / 2 - reforgeFilterHeight / 2;

                // Matches text field
                yPos = yPos + typeEnchantmentsHeight + separateEnchantmentsHeight + enchantsToIncludeHeight;
                textFieldMatches = new GuiTextField(2, fontRendererObj, xPos, yPos, textFieldWidth, textFieldHeight);
                textFieldMatches.setMaxStringLength(500);
                List<String> reforgeMatches = SkyblockAddons.getInstance().getUtils().getReforgeMatches();
                StringBuilder reforgeBuilder = new StringBuilder();

                for (int i = 0; i < reforgeMatches.size(); i++) {
                    reforgeBuilder.append(reforgeMatches.get(i));
                    if (i < reforgeMatches.size() - 1) {
                        reforgeBuilder.append(',');
                    }
                }
                String text = reforgeBuilder.toString();
                if (text.length() > 0) {
                    textFieldMatches.setText(text);
                }

                // Exclusions text field
                yPos = yPos + textFieldHeight + textFieldSpacing;
                textFieldExclusions = new GuiTextField(2, fontRendererObj, xPos, yPos, textFieldWidth, textFieldHeight);
                textFieldExclusions.setMaxStringLength(500);
                List<String> reforgeExclusions = SkyblockAddons.getInstance().getUtils().getReforgeExclusions();
                reforgeBuilder = new StringBuilder();

                for (int i = 0; i < reforgeExclusions.size(); i++) {
                    reforgeBuilder.append(reforgeExclusions.get(i));
                    if (i < reforgeExclusions.size() - 1) {
                        reforgeBuilder.append(',');
                    }
                }
                text = reforgeBuilder.toString();
                if (text.length() > 0) {
                    textFieldExclusions.setText(text);
                }

                Keyboard.enableRepeatEvents(true);
            }
        }
    }

    public static boolean keyTyped(char typedChar, int keyCode) { // return whether to continue (super.keyTyped(typedChar, keyCode);)
        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.REFORGE_FILTER)) {
            InventoryType inventoryType = main.getInventoryUtils().getInventoryType();

            if (inventoryType== InventoryType.BASIC_REFORGING) {
                if (keyCode != mc.gameSettings.keyBindInventory.getKeyCode() ||
                        (!textFieldMatches.isFocused() && !textFieldExclusions.isFocused())) {
                    processTextFields(typedChar, keyCode);
                    return true;
                }
                processTextFields(typedChar, keyCode);
            } else {
                return true;
            }
            return false;
        } else {
            return true;
        }
    }

    private static void processTextFields(char typedChar, int keyCode) {
        if (main.getConfigValues().isEnabled(Feature.REFORGE_FILTER) && textFieldMatches != null) {
            textFieldMatches.textboxKeyTyped(typedChar, keyCode);
            textFieldExclusions.textboxKeyTyped(typedChar, keyCode);
            List<String> reforges = new LinkedList<>(Arrays.asList(textFieldMatches.getText().split(",")));
            main.getUtils().setReforgeMatches(reforges);
            reforges = new LinkedList<>(Arrays.asList(textFieldExclusions.getText().split(",")));
            main.getUtils().setReforgeExclusions(reforges);
        }
    }

    /**
     * @see codes.biscuit.skyblockaddons.asm.GuiChestTransformer#transform(ClassNode, String)
     */
    @SuppressWarnings("unused")
    public static void handleMouseClick(Slot slotIn, Container slots, IInventory lowerChestInventory, ReturnValue<?> returnValue) {
        if (main.getUtils().isOnSkyblock()) {
            if (main.getConfigValues().isEnabled(Feature.REFORGE_FILTER) && !main.getUtils().getReforgeMatches().isEmpty()) {
                if (slotIn != null && !slotIn.inventory.equals(mc.thePlayer.inventory) && slotIn.getHasStack()) {
                    InventoryType inventoryType = main.getInventoryUtils().getInventoryType();

                    if (slotIn.getSlotIndex() == 22 && (inventoryType == InventoryType.BASIC_REFORGING)) {
                        Slot itemSlot = slots.getSlot(13);

                        if (itemSlot != null && itemSlot.getHasStack()) {
                            ItemStack item = itemSlot.getStack();
                            if (item.hasDisplayName()) {
                                String reforge = ItemUtils.getReforge(item);
                                if (reforge != null) {
                                    if (main.getUtils().enchantReforgeMatches(reforge)) {
                                        main.getUtils().playLoudSound("random.orb", 0.1);
                                        returnValue.cancel();
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) && !main.getUtils().isInDungeon() &&
                    NPCUtils.isSellMerchant(lowerChestInventory) && slotIn != null && slotIn.inventory instanceof InventoryPlayer) {
                if (!main.getUtils().getItemDropChecker().canDropItem(slotIn)) {
                    returnValue.cancel();
                }
            }
        }
    }

    /**
     * Handles mouse clicks for the Fancy Warp GUI and the Reforge Filter text fields.
     *
     * @param mouseX x coordinate of the mouse pointer
     * @param mouseY y coordinate of the mouse pointer
     * @param mouseButton mouse button that was clicked
     */
    public static void mouseClicked(int mouseX, int mouseY, int mouseButton, ReturnValue<?> returnValue) throws IOException {
        if (islandWarpGui != null) {
            islandWarpGui.mouseClicked(mouseX, mouseY, mouseButton);
            returnValue.cancel();
            return;
        }

        if (textFieldMatches != null) {
            textFieldMatches.mouseClicked(mouseX, mouseY, mouseButton);
            textFieldExclusions.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    public static void color(float colorRed, float colorGreen, float colorBlue, float colorAlpha, IInventory lowerChestInventory) { //Item item, ItemStack stack
        if (!main.getUtils().isOnSkyblock()) {
            return;
        }

        if (main.getConfigValues().isEnabled(Feature.SHOW_BACKPACK_PREVIEW) &&
                main.getConfigValues().isEnabled(Feature.MAKE_BACKPACK_INVENTORIES_COLORED) && lowerChestInventory.hasCustomName()) {
            if (lowerChestInventory.getDisplayName().getUnformattedText().contains("Backpack")) {
                if (BackpackInventoryManager.getBackpackColor() != null) {
                    BackpackColor color = BackpackInventoryManager.getBackpackColor();
                    GlStateManager.color(color.getR(), color.getG(), color.getB(), 1);
                    return;
                }
            } else if (lowerChestInventory.getDisplayName().getUnformattedText().contains("Bank")) {
                ItemStack item = mc.thePlayer.getHeldItem(); // easter egg question mark
                if (item != null && item.hasDisplayName() && item.getDisplayName().contains("Piggy Bank")) {
                    BackpackColor color = BackpackColor.PINK;
                    GlStateManager.color(color.getR(), color.getG(), color.getB(), 1);
                }
                return;
            }
        }
        GlStateManager.color(colorRed,colorGreen,colorBlue,colorAlpha);
    }

    public static int drawString(FontRenderer fontRenderer, String text, int x, int y, int color) {
        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.SHOW_BACKPACK_PREVIEW) &&
                main.getConfigValues().isEnabled(Feature.MAKE_BACKPACK_INVENTORIES_COLORED) && BackpackInventoryManager.getBackpackColor() != null) {
            return fontRenderer.drawString(text, x,y, BackpackInventoryManager.getBackpackColor().getInventoryTextColor());
        }
        return fontRenderer.drawString(text,x,y,color);
    }

    /**
     * @see codes.biscuit.skyblockaddons.asm.GuiChestTransformer#transform(ClassNode, String)
     */
    @SuppressWarnings("unused")
    public static void mouseReleased(ReturnValue<?> returnValue) {
        if (islandWarpGui != null) {
            returnValue.cancel();
        }
    }

    /**
     * @see codes.biscuit.skyblockaddons.asm.GuiChestTransformer#transform(ClassNode, String)
     */
    @SuppressWarnings("unused")
    public static void mouseClickMove(ReturnValue<?> returnValue) {
        if (islandWarpGui != null) {
            returnValue.cancel();
        }
    }

    /**
     * @see codes.biscuit.skyblockaddons.asm.GuiChestTransformer#transform(ClassNode, String)
     */
    @SuppressWarnings("unused")
    public static void onRenderChestForegroundLayer(GuiChest guiChest) {
        if (!SkyblockAddons.getInstance().getUtils().isOnSkyblock()) {
            return; // don't draw any overlays outside SkyBlock
        }

        if (main.getConfigValues().isEnabled(Feature.SHOW_REFORGE_OVERLAY)) {
            if (guiChest.inventorySlots.inventorySlots.size() > 13) {
                Slot slot = guiChest.inventorySlots.inventorySlots.get(13);
                if (slot != null) {

                    ItemStack item = slot.getStack();
                    if (item != null) {
                        String reforge = null;
                        if (main.getInventoryUtils().getInventoryType() == InventoryType.BASIC_REFORGING) {
                            reforge = ItemUtils.getReforge(item);
                        }

                        if (reforge != null) {
                            int color = ColorCode.YELLOW.getColor();
                            if (main.getConfigValues().isEnabled(Feature.REFORGE_FILTER) &&
                                    !main.getUtils().getReforgeMatches().isEmpty() &&
                                    main.getUtils().enchantReforgeMatches(reforge)) {
                                color = ColorCode.RED.getColor();
                            }

                            int x = slot.xDisplayPosition;
                            int y = slot.yDisplayPosition;

                            int stringWidth = mc.fontRendererObj.getStringWidth(reforge);
                            float renderX = x - 28 - stringWidth / 2F;
                            int renderY = y + 22;

                            GlStateManager.disableDepth();
                            drawTooltipBackground(renderX, renderY, stringWidth);
                            mc.fontRendererObj.drawString(reforge, renderX, renderY, color, true);
                            GlStateManager.enableDepth();
                        }
                    }
                }
            }
        }
    }

    private static void drawTooltipBackground(float x, float y, float width) {
        int l = -267386864;
        DrawUtils.drawRectAbsolute(x - 3, y - 4, x + width + 3, y - 3, l);
        DrawUtils.drawRectAbsolute(x - 3, y + 8 + 3, x + width + 3, y + 8 + 4, l);
        DrawUtils.drawRectAbsolute(x - 3, y - 3, x + width + 3, y + 8 + 3, l);
        DrawUtils.drawRectAbsolute(x - 4, y - 3, x - 3, y + 8 + 3, l);
        DrawUtils.drawRectAbsolute(x + width + 3, y - 3, x + width + 4, y + 8 + 3, l);

        int borderColor = 1347420415;
        DrawUtils.drawRectAbsolute(x - 3, y - 3 + 1, x - 3 + 1, y + 8 + 3 - 1, borderColor);
        DrawUtils.drawRectAbsolute(x + width + 2, y - 3 + 1, x + width + 3, y + 8 + 3 - 1, borderColor);
        DrawUtils.drawRectAbsolute(x - 3, y - 3, x + width + 3, y - 3 + 1, borderColor);
        DrawUtils.drawRectAbsolute(x - 3, y + 8 + 2, x + width + 3, y + 8 + 3, borderColor);
    }
}
