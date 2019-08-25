package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.ConfigColor;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;


// Credits to MrGhetto#0236 for the original contribution/code (modified)
@Mixin(GuiChest.class)
public abstract class MixinGuiChest extends GuiContainer {

    private EnumUtils.InventoryType inventoryType = null;
    private GuiTextField textFieldMatch = null;
    private GuiTextField textFieldExclusions = null;

    @Shadow private IInventory lowerChestInventory;

    public MixinGuiChest(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (textFieldMatch != null) {
            SkyblockAddons main = SkyblockAddons.getInstance();
            String inventoryMessage = inventoryType.getMessage();
            int defaultBlue = main.getUtils().getDefaultBlue(255);
            mc.ingameGUI.drawString(mc.fontRendererObj, Message.MESSAGE_TYPE_ENCHANTMENTS.getMessage(inventoryMessage), guiLeft - 160, guiTop + 40, defaultBlue);
            mc.ingameGUI.drawString(mc.fontRendererObj, Message.MESSAGE_SEPARATE_ENCHANTMENTS.getMessage(), guiLeft - 160, guiTop + 50, defaultBlue);
            mc.ingameGUI.drawString(mc.fontRendererObj, Message.MESSAGE_ENCHANTS_TO_MATCH.getMessage(inventoryMessage), guiLeft - 160, guiTop + 70, defaultBlue);
            mc.ingameGUI.drawString(mc.fontRendererObj,Message.MESSAGE_ENCHANTS_TO_EXCLUDE.getMessage(inventoryMessage), guiLeft - 160, guiTop + 110, defaultBlue);
            textFieldMatch.drawTextBox();
            if (textFieldMatch.getText().equals("")) {
                mc.ingameGUI.drawString(mc.fontRendererObj, "ex. \"prot, feather\"", guiLeft - 156, guiTop + 86, ConfigColor.DARK_GRAY.getColor(255));
            }
            GlStateManager.color(1.0F, 0, 0);
            textFieldExclusions.drawTextBox();
            if (textFieldExclusions.getText().equals("")) {
                mc.ingameGUI.drawString(mc.fontRendererObj, "ex. \"proj, blast\"", guiLeft - 156, guiTop + 126, ConfigColor.DARK_GRAY.getColor(255));
            }
            GlStateManager.color(1F, 1F, 1F);
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        String guiName = lowerChestInventory.getDisplayName().getUnformattedText();
        if (guiName.equals("Enchant Item")) inventoryType = EnumUtils.InventoryType.ENCHANTMENT_TABLE;
        if (guiName.equals("Reforge Item")) inventoryType = EnumUtils.InventoryType.REFORGE_ANVIL;
        if (inventoryType != null) {
            int xPos = guiLeft - 160;
            int yPos = guiTop + 80;
            textFieldMatch = new GuiTextField(2, this.fontRendererObj, xPos, yPos, 120, 20);
            textFieldMatch.setMaxStringLength(500);
            List<String> lockedEnchantments = SkyblockAddons.getInstance().getUtils().getEnchantmentMatch();
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
            textFieldExclusions = new GuiTextField(2, this.fontRendererObj, xPos, yPos, 120, 20);
            textFieldExclusions.setMaxStringLength(500);
            lockedEnchantments = SkyblockAddons.getInstance().getUtils().getEnchantmentExclusion();
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
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (inventoryType != null) {
            if (keyCode != this.mc.gameSettings.keyBindInventory.getKeyCode() || (!textFieldMatch.isFocused() && !textFieldExclusions.isFocused())) {
                super.keyTyped(typedChar, keyCode);
            }
            if (textFieldMatch != null) {
                textFieldMatch.textboxKeyTyped(typedChar, keyCode);
                textFieldExclusions.textboxKeyTyped(typedChar, keyCode);
                List<String> enchantments = new LinkedList<>(Arrays.asList(textFieldMatch.getText().split(",")));
                SkyblockAddons.getInstance().getUtils().setEnchantmentMatch(enchantments);
                enchantments = new LinkedList<>(Arrays.asList(textFieldExclusions.getText().split(",")));
                SkyblockAddons.getInstance().getUtils().setEnchantmentExclusion(enchantments);
            }
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().getEnchantmentMatch().size() > 0) {
            if (slotIn != null && !slotIn.inventory.equals(mc.thePlayer.inventory) && slotIn.getHasStack()) {
                Container slots = inventorySlots;
                if (slotIn.getSlotIndex() == 13 && inventoryType == EnumUtils.InventoryType.ENCHANTMENT_TABLE) {
                    ItemStack[] enchantBottles = {slots.getSlot(29).getStack(), slots.getSlot(31).getStack(), slots.getSlot(33).getStack()};
                    for (ItemStack bottle : enchantBottles) {
                        if (bottle != null && bottle.hasDisplayName()) {
                            if (bottle.getDisplayName().startsWith(EnumChatFormatting.GREEN + "Enchant Item")) {
                                Minecraft mc = Minecraft.getMinecraft();
                                List<String> toolip = bottle.getTooltip(mc.thePlayer, false);
                                if (toolip.size() > 2) {
                                    String enchantLine = toolip.get(2).split(Pattern.quote("* "))[1];
                                    if (main.getUtils().enchantReforgeMatches(enchantLine)) {
                                        main.getUtils().playSound("random.orb", 0.1);
                                        return;
                                    }
                                }
                            } else if (bottle.getDisplayName().startsWith(EnumChatFormatting.RED + "Enchant Item")) {
                                // Stop player from removing item before the enchants have even loaded.
                                return;
                            }
                        }
                    }
                } else if (slotIn.getSlotIndex() == 22 && inventoryType == EnumUtils.InventoryType.REFORGE_ANVIL) {
                    Slot itemSlot = slots.getSlot(13);
                    if (itemSlot != null && itemSlot.getHasStack()) {
                        ItemStack item = itemSlot.getStack();
                        if (item.hasDisplayName()) {
                            String[] nameParts = item.getDisplayName().split(" ");
                            if (nameParts.length > 2) {
                                String reforge = main.getUtils().stripColor(nameParts[0]);
                                if (main.getUtils().enchantReforgeMatches(reforge)) {
                                    main.getUtils().playSound("random.orb", 0.1);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
        super.handleMouseClick(slotIn, slotId, clickedButton, clickType);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (textFieldMatch != null) {
            textFieldMatch.mouseClicked(mouseX, mouseY, mouseButton);
            textFieldExclusions.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }
}
