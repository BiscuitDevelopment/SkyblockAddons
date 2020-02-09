package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.utils.ReturnValue;
import codes.biscuit.skyblockaddons.gui.elements.CraftingPatternSelection;
import codes.biscuit.skyblockaddons.utils.Backpack;
import codes.biscuit.skyblockaddons.utils.BackpackColor;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.Message;
import codes.biscuit.skyblockaddons.utils.nifty.StringUtil;
import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import codes.biscuit.skyblockaddons.utils.nifty.reflection.MinecraftReflection;
import codes.biscuit.skyblockaddons.utils.npc.NPCUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class GuiChestHook {

    private static GuiTextField textFieldMatch = null;
    private static GuiTextField textFieldExclusions = null;
    private static CraftingPatternSelection craftingPatternSelection = null;
    private static Backpack backpack = null;

    public static void updateScreen() {
        if (textFieldMatch != null && textFieldExclusions != null) {
            textFieldMatch.updateCursorCounter();
            textFieldExclusions.updateCursorCounter();
        }
    }

    public static void onGuiClosed() {
        EnumUtils.InventoryType.resetCurrentInventoryType();
        if (craftingPatternSelection != null) {
            craftingPatternSelection.onGuiClosed();
        }
        if (textFieldMatch != null && textFieldExclusions != null) {
            Keyboard.enableRepeatEvents(false);
        }
    }

    public static void drawScreen(int guiLeft, int guiTop) {
        EnumUtils.InventoryType inventoryType = EnumUtils.InventoryType.getCurrentInventoryType();
        if (textFieldMatch != null && (inventoryType == EnumUtils.InventoryType.ENCHANTMENT_TABLE ||
                inventoryType== EnumUtils.InventoryType.REFORGE_ANVIL)) {
            GlStateManager.color(1F, 1F, 1F);
            SkyblockAddons main = SkyblockAddons.getInstance();
            String inventoryMessage = inventoryType.getMessage();
            int defaultBlue = main.getUtils().getDefaultBlue(255);
            GlStateManager.pushMatrix();
            float scale = 0.75F;
            GlStateManager.scale(scale, scale, 1);
            int x = guiLeft - 160;
            if (x<0) {
                x = 20;
            }
            MinecraftReflection.FontRenderer.drawString(Message.MESSAGE_TYPE_ENCHANTMENTS.getMessage(inventoryMessage), Math.round(x/scale), Math.round((guiTop+40)/scale), defaultBlue);
            MinecraftReflection.FontRenderer.drawString(Message.MESSAGE_SEPARATE_ENCHANTMENTS.getMessage(), Math.round(x/scale), Math.round((guiTop + 50)/scale), defaultBlue);
            MinecraftReflection.FontRenderer.drawString(Message.MESSAGE_ENCHANTS_TO_MATCH.getMessage(inventoryMessage), Math.round(x/scale), Math.round((guiTop + 70)/scale), defaultBlue);
            MinecraftReflection.FontRenderer.drawString(Message.MESSAGE_ENCHANTS_TO_EXCLUDE.getMessage(inventoryMessage), Math.round(x/scale), Math.round((guiTop + 110)/scale), defaultBlue);
            GlStateManager.popMatrix();
            textFieldMatch.drawTextBox();
            if (StringUtil.isEmpty(textFieldMatch.getText())) {
                MinecraftReflection.FontRenderer.drawString("ex. \"prot, feather\"", x+4, guiTop + 86, ChatFormatting.DARK_GRAY);
            }
            textFieldExclusions.drawTextBox();
            if (StringUtil.isEmpty(textFieldExclusions.getText())) {
                MinecraftReflection.FontRenderer.drawString("ex. \"proj, blast\"", x+4, guiTop + 126, ChatFormatting.DARK_GRAY);
            }
        }
    }

    public static void initGui(IInventory lowerChestInventory, int guiLeft, int guiTop, FontRenderer fontRendererObj) {
        if (!SkyblockAddons.getInstance().getUtils().isOnSkyblock()) {
            return; // don't draw any overlays outside SkyBlock
        }

        String guiName = lowerChestInventory.getDisplayName().getUnformattedText();
        EnumUtils.InventoryType inventoryType = EnumUtils.InventoryType.getCurrentInventoryType(guiName);

        if (inventoryType != null) {

            if (inventoryType == EnumUtils.InventoryType.CRAFTING_TABLE) {
                if (SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.CRAFTING_PATTERNS)) {
                    craftingPatternSelection = new CraftingPatternSelection(Minecraft.getMinecraft(), Math.max(guiLeft - CraftingPatternSelection.ICON_SIZE - 2, 10), guiTop + 1);
                }
                return;
            }

            int xPos = guiLeft - 160;
            if (xPos<0) {
                xPos = 20;
            }
            int yPos = guiTop + 80;
            textFieldMatch = new GuiTextField(2, fontRendererObj, xPos, yPos, 120, 20);
            textFieldMatch.setMaxStringLength(500);
            List<String> lockedEnchantments = SkyblockAddons.getInstance().getUtils().getEnchantmentMatches();
            StringBuilder enchantmentBuilder = new StringBuilder();
            int i = 1;
            for (String enchantment : lockedEnchantments) {
                enchantmentBuilder.append(enchantment);
                if (i < lockedEnchantments.size()) {
                    enchantmentBuilder.append(",");
                }
                i++;
            }
            String text = enchantmentBuilder.toString();
            if (text.length() > 0) {
                textFieldMatch.setText(text);
            }
            yPos += 40;
            textFieldExclusions = new GuiTextField(2, fontRendererObj, xPos, yPos, 120, 20);
            textFieldExclusions.setMaxStringLength(500);
            lockedEnchantments = SkyblockAddons.getInstance().getUtils().getEnchantmentExclusions();
            enchantmentBuilder = new StringBuilder();
            i = 1;
            for (String enchantment : lockedEnchantments) {
                enchantmentBuilder.append(enchantment);
                if (i < lockedEnchantments.size()) {
                    enchantmentBuilder.append(",");
                }
                i++;
            }
            text = enchantmentBuilder.toString();
            if (text.length() > 0) {
                textFieldExclusions.setText(text);
            }
            Keyboard.enableRepeatEvents(true);
        }
    }

    public static boolean keyTyped(char typedChar, int keyCode) { // return whether to continue (super.keyTyped(typedChar, keyCode);)
        if ((EnumUtils.InventoryType.getCurrentInventoryType() == EnumUtils.InventoryType.ENCHANTMENT_TABLE ||
                EnumUtils.InventoryType.getCurrentInventoryType() == EnumUtils.InventoryType.REFORGE_ANVIL)) {
            if (keyCode != Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode() || (!textFieldMatch.isFocused() && !textFieldExclusions.isFocused())) {
                processTextFields(typedChar, keyCode);
                return true;
            }
            processTextFields(typedChar, keyCode);
        } else {
            return true;
        }
        return false;
    }

    private static void processTextFields(char typedChar, int keyCode) {
        if (textFieldMatch != null) {
            textFieldMatch.textboxKeyTyped(typedChar, keyCode);
            textFieldExclusions.textboxKeyTyped(typedChar, keyCode);
            List<String> enchantments = new LinkedList<>(Arrays.asList(textFieldMatch.getText().split(",")));
            SkyblockAddons.getInstance().getUtils().setEnchantmentMatches(enchantments);
            enchantments = new LinkedList<>(Arrays.asList(textFieldExclusions.getText().split(",")));
            SkyblockAddons.getInstance().getUtils().setEnchantmentExclusions(enchantments);
        }
    }

    public static void handleMouseClick(Slot slotIn, Container slots, IInventory lowerChestInventory, ReturnValue returnValue) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().getEnchantmentMatches().size() > 0) {
            if (slotIn != null && !slotIn.inventory.equals(Minecraft.getMinecraft().thePlayer.inventory) && slotIn.getHasStack()) {
                if (slotIn.getSlotIndex() == 13 && EnumUtils.InventoryType.getCurrentInventoryType() == EnumUtils.InventoryType.ENCHANTMENT_TABLE) {
                    ItemStack[] enchantBottles = {slots.getSlot(29).getStack(), slots.getSlot(31).getStack(), slots.getSlot(33).getStack()};
                    for (ItemStack bottle : enchantBottles) {
                        if (bottle != null && bottle.hasDisplayName()) {
                            if (bottle.getDisplayName().startsWith(ChatFormatting.GREEN + "Enchant Item")) {
                                Minecraft mc = Minecraft.getMinecraft();
                                List<String> toolip = bottle.getTooltip(mc.thePlayer, false);
                                if (toolip.size() > 2) {
                                    String[] lines = toolip.get(2).split(Pattern.quote("* "));

                                    if (lines.length > 1) {
                                        String enchantLine = lines[1];
                                        if (main.getUtils().enchantReforgeMatches(enchantLine)) {
                                            main.getUtils().playLoudSound("random.orb", 0.1);
                                            returnValue.cancel();
                                        }
                                    }
                                }
                            } else if (bottle.getDisplayName().startsWith(ChatFormatting.RED + "Enchant Item")) {
                                // Stop player from removing item before the enchants have even loaded.
                                returnValue.cancel();
                            }
                        }
                    }
                } else if (slotIn.getSlotIndex() == 22 && EnumUtils.InventoryType.getCurrentInventoryType() == EnumUtils.InventoryType.REFORGE_ANVIL) {
                    Slot itemSlot = slots.getSlot(13);
                    if (itemSlot != null && itemSlot.getHasStack()) {
                        ItemStack item = itemSlot.getStack();
                        if (item.hasDisplayName()) {
                            String reforge = main.getUtils().getReforgeFromItem(item);
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

        if (main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) &&
                lowerChestInventory.hasCustomName() && NPCUtils.isFullMerchant(lowerChestInventory.getDisplayName().getUnformattedText())
                && slotIn != null && slotIn.inventory instanceof InventoryPlayer) {
            if (main.getInventoryUtils().shouldCancelDrop(slotIn)) returnValue.cancel();
        }
    }

    public static void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (textFieldMatch != null) {
            textFieldMatch.mouseClicked(mouseX, mouseY, mouseButton);
            textFieldExclusions.mouseClicked(mouseX, mouseY, mouseButton);
        }

        if (craftingPatternSelection != null) {
            craftingPatternSelection.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    public static void color(float colorRed, float colorGreen, float colorBlue, float colorAlpha, IInventory lowerChestInventory) { //Item item, ItemStack stack
        SkyblockAddons main = SkyblockAddons.getInstance();

        // Draw here to make sure it's in the background of the GUI and items overlay it.
        if (EnumUtils.InventoryType.getCurrentInventoryType() == EnumUtils.InventoryType.CRAFTING_TABLE && craftingPatternSelection != null) {
            craftingPatternSelection.draw();
        }

        Minecraft mc = Minecraft.getMinecraft();

        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.SHOW_BACKPACK_PREVIEW) &&
                main.getConfigValues().isEnabled(Feature.MAKE_BACKPACK_INVENTORIES_COLORED)
                && lowerChestInventory.hasCustomName()) {
            if (lowerChestInventory.getDisplayName().getUnformattedText().contains("Backpack")) {
                backpack = Backpack.getFromItem(mc.thePlayer.getHeldItem());
                if (backpack != null) {
                    BackpackColor color = backpack.getBackpackColor();
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
        backpack = null;
        GlStateManager.color(colorRed,colorGreen,colorBlue,colorAlpha);
    }

    public static int drawString(FontRenderer fontRenderer, String text, int x, int y, int color) {
        if (backpack != null) {
            return fontRenderer.drawString(text, x,y, backpack.getBackpackColor().getInventoryTextColor());
        }
        return fontRenderer.drawString(text,x,y,color);
    }
}
