package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.utils.ReturnValue;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.InventoryType;
import codes.biscuit.skyblockaddons.core.Message;
import codes.biscuit.skyblockaddons.core.npc.NPCUtils;
import codes.biscuit.skyblockaddons.features.backpacks.BackpackColor;
import codes.biscuit.skyblockaddons.features.backpacks.BackpackInventoryManager;
import codes.biscuit.skyblockaddons.features.backpacks.ContainerPreviewManager;
import codes.biscuit.skyblockaddons.gui.IslandWarpGui;
import codes.biscuit.skyblockaddons.gui.elements.CraftingPatternSelection;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import codes.biscuit.skyblockaddons.utils.ItemUtils;
import codes.biscuit.skyblockaddons.features.CityProjectsPin;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.player.inventory.ContainerLocalMenu;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO Fix for Hypixel localization
public class GuiChestHook {

    private static GuiTextField textFieldMatch = null;
    private static GuiTextField textFieldExclusions = null;
    @Setter
    @Getter
    private static String lastAccessoryBagReforge = null;
    private static CraftingPatternSelection craftingPatternSelection = null;

    private static final Pattern warpPattern = Pattern.compile("(?:§5§o)?§8/warp ([a-z_]*)");
    private static final Pattern unlockedPattern = Pattern.compile("(?:§5§o)?§eClick to warp!");
    private static final Pattern notUnlockedPattern = Pattern.compile("(?:§5§o)?§cWarp not unlocked!");
    private static final Pattern inCombatPattern = Pattern.compile("(?:§5§o)?§cYou're in combat!");
    private static final Pattern youAreHerePattern = Pattern.compile("(?:§5§o)?§aYou are here!");
    private static IslandWarpGui islandWarpGui = null;

    public static void updateScreen() {
        if (textFieldMatch != null && textFieldExclusions != null) {
            textFieldMatch.updateCursorCounter();
            textFieldExclusions.updateCursorCounter();
        }
    }

    /**
     * Resets variables when the chest is closed
     */
    public static void onGuiClosed() {
        SkyblockAddons.getInstance().getInventoryUtils().updateInventoryType();
        Keyboard.enableRepeatEvents(false);

        islandWarpGui = null;
        BackpackInventoryManager.setBackpackColor(null);
    }

    public static void drawScreenIslands(int mouseX, int mouseY, ReturnValue<?> returnValue) {
        Minecraft mc = Minecraft.getMinecraft();
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
                    markers.put(IslandWarpGui.Marker.DWARVEN_FORGE, markers.getOrDefault(IslandWarpGui.Marker.DEEP_CAVERNS, IslandWarpGui.UnlockedStatus.UNKNOWN));
                    markers.put(IslandWarpGui.Marker.DUNGEON_HUB_ISLAND, markers.getOrDefault(IslandWarpGui.Marker.DUNGEON_HUB, IslandWarpGui.UnlockedStatus.UNKNOWN));
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
        InventoryType inventoryType = SkyblockAddons.getInstance().getInventoryUtils().updateInventoryType();

        if (inventoryType == InventoryType.SALVAGING) {
            int ySize = 222 - 108 + 6 * 18;
            float x = guiLeft - 69 - 5;
            float y = guiTop + ySize / 2F - 72 / 2F;

            SkyblockAddons.getInstance().getRenderListener().drawCollectedEssences(x, y, false, false);
        } else if (inventoryType == InventoryType.STORAGE_BACKPACK) {
            ContainerPreviewManager.saveStorageContainerInventory(SkyblockAddons.getInstance().getInventoryUtils().getInventoryKey());
        } else if (inventoryType == InventoryType.ENDER_CHEST) {
            ContainerPreviewManager.saveStorageContainerInventory(SkyblockAddons.getInstance().getInventoryUtils().getInventoryKey());
        }

        if (SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.REFORGE_FILTER) && textFieldMatch != null &&
                (inventoryType== InventoryType.BASIC_REFORGING ||
                        inventoryType == InventoryType.BASIC_ACCESSORY_BAG_REFORGING)) {
            Minecraft mc = Minecraft.getMinecraft();
            SkyblockAddons main = SkyblockAddons.getInstance();
            String typeToMatch = Message.MESSAGE_REFORGES.getMessage();
            String inclusionExample;
            String exclusionExample;
            int defaultBlue = main.getUtils().getDefaultBlue(255);
            float scale = 0.75F;
            int x = guiLeft - 160;
            if (x<0) {
                x = 20;
            }

            inclusionExample = Message.MESSAGE_REFORGE_INCLUSION_EXAMPLE.getMessage();
            exclusionExample = Message.MESSAGE_REFORGE_EXCLUSION_EXAMPLE.getMessage();

            GlStateManager.color(1F, 1F, 1F);
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
            mc.fontRendererObj.drawString(Message.MESSAGE_TYPE_ENCHANTMENTS.getMessage(typeToMatch), Math.round(x/scale), Math.round((guiTop+40)/scale), defaultBlue);
            int width = mc.fontRendererObj.getStringWidth(Message.MESSAGE_SEPARATE_ENCHANTMENTS.getMessage());
            if (width > guiLeft - x) {
                mc.fontRendererObj.drawSplitString(Message.MESSAGE_SEPARATE_ENCHANTMENTS.getMessage(), Math.round(x/scale), Math.round((guiTop + 50)/scale), textFieldMatch.width - x, defaultBlue);
            } else {
                mc.fontRendererObj.drawString(Message.MESSAGE_SEPARATE_ENCHANTMENTS.getMessage(), Math.round(x/scale), Math.round((guiTop + 50)/scale), defaultBlue);
            }
            mc.fontRendererObj.drawString(Message.MESSAGE_ENCHANTS_TO_MATCH.getMessage(typeToMatch), Math.round(x/scale), Math.round((guiTop + 70)/scale), defaultBlue);
            mc.fontRendererObj.drawString(Message.MESSAGE_ENCHANTS_TO_EXCLUDE.getMessage(typeToMatch), Math.round(x/scale), Math.round((guiTop + 110)/scale), defaultBlue);
            GlStateManager.popMatrix();

            textFieldMatch.drawTextBox();
            if (StringUtils.isEmpty(textFieldMatch.getText())) {
                mc.fontRendererObj.drawString(inclusionExample, x+4, guiTop + 86, ColorCode.DARK_GRAY.getColor());
            }

            textFieldExclusions.drawTextBox();
            if (StringUtils.isEmpty(textFieldExclusions.getText())) {
                mc.fontRendererObj.drawString(exclusionExample, x+4, guiTop + 126, ColorCode.DARK_GRAY.getColor());
            }
        }
    }

    public static void initGui(IInventory lowerChestInventory, int guiLeft, int guiTop, FontRenderer fontRendererObj) {
        if (!SkyblockAddons.getInstance().getUtils().isOnSkyblock()) {
            return; // don't draw any overlays outside SkyBlock
        }

        InventoryType inventoryType = SkyblockAddons.getInstance().getInventoryUtils().updateInventoryType();

        if (inventoryType != null) {
            if (inventoryType == InventoryType.CRAFTING_TABLE) {
                if (false /*SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.CRAFTING_PATTERNS)*/) {
                    craftingPatternSelection = new CraftingPatternSelection(Minecraft.getMinecraft(), Math.max(guiLeft - CraftingPatternSelection.ICON_SIZE - 2, 10), guiTop + 1);
                }
                return;
            }

            if (SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.REFORGE_FILTER) && inventoryType ==
                    InventoryType.BASIC_REFORGING || inventoryType == InventoryType.BASIC_ACCESSORY_BAG_REFORGING) {
                int xPos = guiLeft - 160;
                if (xPos<0) {
                    xPos = 20;
                }
                int yPos = guiTop + 80;
                textFieldMatch = new GuiTextField(2, fontRendererObj, xPos, yPos, 120, 20);
                textFieldMatch.setMaxStringLength(500);
                List<String> lockedReforges = SkyblockAddons.getInstance().getUtils().getReforgeMatches();
                StringBuilder reforgeBuilder = new StringBuilder();
                int i = 1;
                for (String reforge : lockedReforges) {
                    reforgeBuilder.append(reforge);
                    if (i < lockedReforges.size()) {
                        reforgeBuilder.append(",");
                    }
                    i++;
                }
                String text = reforgeBuilder.toString();
                if (text.length() > 0) {
                    textFieldMatch.setText(text);
                }
                yPos += 40;
                textFieldExclusions = new GuiTextField(2, fontRendererObj, xPos, yPos, 120, 20);
                textFieldExclusions.setMaxStringLength(500);
                lockedReforges = SkyblockAddons.getInstance().getUtils().getReforgeExclusions();
                reforgeBuilder = new StringBuilder();
                i = 1;
                for (String enchantment : lockedReforges) {
                    reforgeBuilder.append(enchantment);
                    if (i < lockedReforges.size()) {
                        reforgeBuilder.append(",");
                    }
                    i++;
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
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.REFORGE_FILTER)) {
            InventoryType inventoryType = main.getInventoryUtils().getInventoryType();

            if (inventoryType== InventoryType.BASIC_REFORGING || inventoryType == InventoryType.BASIC_ACCESSORY_BAG_REFORGING) {
                if (keyCode != Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode() ||
                        (!textFieldMatch.isFocused() && !textFieldExclusions.isFocused())) {
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
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getConfigValues().isEnabled(Feature.REFORGE_FILTER) && textFieldMatch != null) {
            textFieldMatch.textboxKeyTyped(typedChar, keyCode);
            textFieldExclusions.textboxKeyTyped(typedChar, keyCode);
            List<String> reforges = new LinkedList<>(Arrays.asList(textFieldMatch.getText().split(",")));
            main.getUtils().setReforgeMatches(reforges);
            reforges = new LinkedList<>(Arrays.asList(textFieldExclusions.getText().split(",")));
            main.getUtils().setReforgeExclusions(reforges);
        }
    }

    public static void handleMouseClick(Slot slotIn, Container slots, IInventory lowerChestInventory, ReturnValue<?> returnValue) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getUtils().isOnSkyblock()) {
            if (main.getConfigValues().isEnabled(Feature.REFORGE_FILTER) && !main.getUtils().getReforgeMatches().isEmpty()) {
                if (slotIn != null && !slotIn.inventory.equals(Minecraft.getMinecraft().thePlayer.inventory) && slotIn.getHasStack()) {
                    InventoryType inventoryType = main.getInventoryUtils().getInventoryType();

                    if (slotIn.getSlotIndex() == 22 && (inventoryType == InventoryType.BASIC_REFORGING || inventoryType == InventoryType.BASIC_ACCESSORY_BAG_REFORGING)) {
                        Slot itemSlot = slots.getSlot(13);

                        if (itemSlot != null && itemSlot.getHasStack()) {
                            if (inventoryType == InventoryType.BASIC_REFORGING) {
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
                            } else {
                                if (lastAccessoryBagReforge != null && main.getUtils().enchantReforgeMatches(lastAccessoryBagReforge)) {
                                    main.getUtils().playLoudSound("random.orb", 0.1);
                                    returnValue.cancel();
                                }
                            }
                        }
                    }
                }
            }

            if (main.getConfigValues().isEnabled(Feature.CITY_PROJECTS_PIN)
                    && slotIn != null && slotIn.inventory instanceof ContainerLocalMenu && slotIn.getHasStack()
                    && lowerChestInventory.hasCustomName())
            {
                if (lowerChestInventory.getDisplayName().getUnformattedText().startsWith("Project - ")) {
                    ItemStack itemStack = slotIn.getStack();
                    if (itemStack.getTagCompound().getCompoundTag("display") == null || itemStack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8) == null)
                        return;
                    NBTTagList loreNbt = itemStack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
                    if (loreNbt.get(0).toString().equalsIgnoreCase("\"§8City Project\"")) {
                        CityProjectsPin.getInstance().pinProject(lowerChestInventory);
                        returnValue.cancel();
                    }
                } else if (CityProjectsPin.getInstance().pin != null && lowerChestInventory.getDisplayName().getUnformattedText().startsWith("Confirm Contribution"))
                {
                    ItemStack itemStack = slotIn.getStack();
                    if (itemStack.getTagCompound().getCompoundTag("display") == null || itemStack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8) == null)
                        return;
                    NBTTagList loreNbt = itemStack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
                    if (loreNbt.get(0).toString().equalsIgnoreCase("\"§7Project: " + CityProjectsPin.getInstance().pin.name + "\"")) {
                        for (CityProjectsPin.Contribute cont : CityProjectsPin.getInstance().pin.contribs)
                            if (TextUtils.stripColor(loreNbt.get(1).toString()).contains(TextUtils.stripColor(cont.name))){
                                cont.completed = true;
                                break;
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

    // TODO: Replace ASM?
    public static void mouseClicked(int mouseX, int mouseY, int mouseButton, ReturnValue<?> returnValue) throws IOException {
        if (islandWarpGui != null) {
            islandWarpGui.mouseClicked(mouseX, mouseY, mouseButton);
            returnValue.cancel();
            return;
        }

        if (textFieldMatch != null) {
            textFieldMatch.mouseClicked(mouseX, mouseY, mouseButton);
            textFieldExclusions.mouseClicked(mouseX, mouseY, mouseButton);
        }


        if (craftingPatternSelection != null && SkyblockAddons.getInstance().getInventoryUtils().getInventoryType() ==
                InventoryType.CRAFTING_TABLE) {
            craftingPatternSelection.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    public static void color(float colorRed, float colorGreen, float colorBlue, float colorAlpha, IInventory lowerChestInventory) { //Item item, ItemStack stack
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (!main.getUtils().isOnSkyblock()) {
            return;
        }

        // Draw here to make sure it's in the background of the GUI and items overlay it.
        if (false && /*main.getConfigValues().isEnabled(Feature.CRAFTING_PATTERNS) && */
                main.getInventoryUtils().getInventoryType() == InventoryType.CRAFTING_TABLE &&
                craftingPatternSelection != null) {
            craftingPatternSelection.draw();
        }

        Minecraft mc = Minecraft.getMinecraft();

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
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.SHOW_BACKPACK_PREVIEW) &&
                main.getConfigValues().isEnabled(Feature.MAKE_BACKPACK_INVENTORIES_COLORED) && BackpackInventoryManager.getBackpackColor() != null) {
            return fontRenderer.drawString(text, x,y, BackpackInventoryManager.getBackpackColor().getInventoryTextColor());
        }
        return fontRenderer.drawString(text,x,y,color);
    }

    public static void mouseReleased(ReturnValue<?> returnValue) {
        if (islandWarpGui != null) {
            returnValue.cancel();
        }
    }

    public static void mouseClickMove(ReturnValue<?> returnValue) {
        if (islandWarpGui != null) {
            returnValue.cancel();
        }
    }

    public static void onRenderChestForegroundLayer(GuiChest guiChest) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getConfigValues().isEnabled(Feature.SHOW_REFORGE_OVERLAY)) {
            Minecraft mc = Minecraft.getMinecraft();

            if (guiChest.inventorySlots.inventorySlots.size() > 13) {
                Slot slot = guiChest.inventorySlots.inventorySlots.get(13);
                if (slot != null) {

                    ItemStack item = slot.getStack();
                    if (item != null) {
                        String reforge = null;
                        if (main.getInventoryUtils().getInventoryType() == InventoryType.BASIC_REFORGING) {
                            reforge = ItemUtils.getReforge(item);
                        } else if (main.getInventoryUtils().getInventoryType() == InventoryType.BASIC_ACCESSORY_BAG_REFORGING) {
                            reforge = GuiChestHook.getLastAccessoryBagReforge();
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
                            drawTooltipBackground(renderX, renderY, stringWidth, 8);
                            mc.fontRendererObj.drawString(reforge, renderX, renderY, color, true);
                            GlStateManager.enableDepth();
                        }
                    }
                }
            }
        }
    }

    private static void drawTooltipBackground(float x, float y, float width, float height) {
        int l = -267386864;
        DrawUtils.drawRectAbsolute(x - 3, y - 4, x + width + 3, y - 3, l);
        DrawUtils.drawRectAbsolute(x - 3, y + height + 3, x + width + 3, y + height + 4, l);
        DrawUtils.drawRectAbsolute(x - 3, y - 3, x + width + 3, y + height + 3, l);
        DrawUtils.drawRectAbsolute(x - 4, y - 3, x - 3, y + height + 3, l);
        DrawUtils.drawRectAbsolute(x + width + 3, y - 3, x + width + 4, y + height + 3, l);

        int borderColor = 1347420415;
        DrawUtils.drawRectAbsolute(x - 3, y - 3 + 1, x - 3 + 1, y + height + 3 - 1, borderColor);
        DrawUtils.drawRectAbsolute(x + width + 2, y - 3 + 1, x + width + 3, y + height + 3 - 1, borderColor);
        DrawUtils.drawRectAbsolute(x - 3, y - 3, x + width + 3, y - 3 + 1, borderColor);
        DrawUtils.drawRectAbsolute(x - 3, y + height + 2, x + width + 3, y + height + 3, borderColor);
    }
}
