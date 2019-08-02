package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;


// Credits to MrGhetto#0236 for the original contribution/code (modified)
@Mixin(GuiChest.class)
public abstract class MixinGuiChest extends GuiContainer {

    private Feature.InventoryType inventoryType = null;
    private GuiTextField textField = null;

    @Shadow private IInventory lowerChestInventory;

    public MixinGuiChest(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (textField != null) {
            textField.drawTextBox();
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        String guiName = lowerChestInventory.getDisplayName().getUnformattedText();
        if (guiName.equals("Enchant Item")) inventoryType = Feature.InventoryType.ENCHANTMENT_TABLE;
        if (guiName.equals("Reforge Item")) inventoryType = Feature.InventoryType.REFORGE_ANVIL;
        if (inventoryType != null) {
            int xPos = guiLeft - 140;
            int yPos = guiTop + 80;
            textField = new GuiTextField(2, this.fontRendererObj, xPos, yPos, 120, 20);
            textField.setMaxStringLength(50);
            textField.setFocused(true);
            String lockedEnchantment = SkyblockAddons.instance.getUtils().getLockedEnchantment();
            if (lockedEnchantment != null) {
                textField.setText(lockedEnchantment);
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (inventoryType != null) {
            if (keyCode != this.mc.gameSettings.keyBindInventory.getKeyCode()) {
                super.keyTyped(typedChar, keyCode);
            }
            if (textField != null) {
                textField.textboxKeyTyped(typedChar, keyCode);
                SkyblockAddons.instance.getUtils().setLockedEnchantment(textField.getText());
            }
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int clickedButton, int clickType) {
        if (SkyblockAddons.instance.getUtils().getLockedEnchantment().length() > 0) {
            if (slotIn != null && slotIn.getHasStack()) {
                Container slots = inventorySlots;
                if (slotIn.getSlotIndex() == 13 && inventoryType == Feature.InventoryType.ENCHANTMENT_TABLE) {
                    ItemStack[] enchantBottles = {slots.getSlot(29).getStack(), slots.getSlot(31).getStack(), slots.getSlot(33).getStack()};
                    for (ItemStack bottle : enchantBottles) {
                        if (bottle != null && bottle.hasDisplayName() && bottle.getDisplayName().startsWith(EnumChatFormatting.GREEN + "Enchant Item")) {
                            Minecraft mc = Minecraft.getMinecraft();
                            List<String> toolip = bottle.getTooltip(mc.thePlayer, false);
                            if (toolip.size() > 2) {
                                String enchantLine = toolip.get(2);
                                if (enchantLine.split(Pattern.quote("* "))[1].toLowerCase().contains(SkyblockAddons.instance.getUtils().getLockedEnchantment().toLowerCase())) {
                                    mc.thePlayer.playSound("random.orb", 1, 0.1F);
                                    return;
                                }
                            }
                        }
                    }
                } else if (slotIn.getSlotIndex() == 22 && inventoryType == Feature.InventoryType.REFORGE_ANVIL) {
                    Slot itemSlot = slots.getSlot(13);
                    if (itemSlot != null && itemSlot.getHasStack()) {
                        ItemStack item = itemSlot.getStack();
                        if (item.hasDisplayName()) {
                            String[] nameParts = item.getDisplayName().split(" ");
                            if (nameParts.length > 2) {
                                String reforge = SkyblockAddons.instance.getUtils().stripColor(nameParts[0]);
                                if (reforge.toLowerCase().contains(SkyblockAddons.instance.getUtils().getLockedEnchantment().toLowerCase())) {
                                    mc.thePlayer.playSound("random.orb", 1, 0.1F);
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
}
