package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
    public void updateScreen() {
        if (this.textFieldMatch != null && this.textFieldExclusions != null) {
            this.textFieldMatch.updateCursorCounter();
            this.textFieldExclusions.updateCursorCounter();
        }
    }

    @Override
    public void onGuiClosed() {
        if (this.textFieldMatch != null && this.textFieldExclusions != null) {
            Keyboard.enableRepeatEvents(false);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (textFieldMatch != null) {
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
            mc.ingameGUI.drawString(mc.fontRendererObj, Message.MESSAGE_TYPE_ENCHANTMENTS.getMessage(inventoryMessage), Math.round(x/scale), Math.round((guiTop+40)/scale), defaultBlue);
            mc.ingameGUI.drawString(mc.fontRendererObj, Message.MESSAGE_SEPARATE_ENCHANTMENTS.getMessage(), Math.round(x/scale), Math.round((guiTop + 50)/scale), defaultBlue);
            mc.ingameGUI.drawString(mc.fontRendererObj, Message.MESSAGE_ENCHANTS_TO_MATCH.getMessage(inventoryMessage), Math.round(x/scale), Math.round((guiTop + 70)/scale), defaultBlue);
            mc.ingameGUI.drawString(mc.fontRendererObj,Message.MESSAGE_ENCHANTS_TO_EXCLUDE.getMessage(inventoryMessage), Math.round(x/scale), Math.round((guiTop + 110)/scale), defaultBlue);
            GlStateManager.popMatrix();
            textFieldMatch.drawTextBox();
            if (textFieldMatch.getText().equals("")) {
                mc.ingameGUI.drawString(mc.fontRendererObj, "ex. \"prot, feather\"", x+4, guiTop + 86, ConfigColor.DARK_GRAY.getColor(255));
            }
            textFieldExclusions.drawTextBox();
            if (textFieldExclusions.getText().equals("")) {
                mc.ingameGUI.drawString(mc.fontRendererObj, "ex. \"proj, blast\"", x+4, guiTop + 126, ConfigColor.DARK_GRAY.getColor(255));
            }
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
            if (xPos<0) {
                xPos = 20;
            }
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
            Keyboard.enableRepeatEvents(true);
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
                            String reforge = main.getUtils().getReforgeFromItem(item);
                            if (reforge != null) {
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
        if (main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) &&
                lowerChestInventory.hasCustomName() && EnumUtils.Merchant.isMerchant(lowerChestInventory.getDisplayName().getUnformattedText())
                && slotIn != null && slotIn.inventory instanceof InventoryPlayer) {
            if (main.getInventoryUtils().shouldCancelDrop(slotIn)) return;
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

    private Backpack backpack = null;

    @Redirect(method = "drawGuiContainerBackgroundLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V", ordinal = 0))
    private void color(float colorRed, float colorGreen, float colorBlue, float colorAlpha) { //Item item, ItemStack stack
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.SHOW_BACKPACK_PREVIEW) &&
                main.getConfigValues().isEnabled(Feature.MAKE_BACKPACK_INVENTORIES_COLORED)
        && lowerChestInventory.hasCustomName() && lowerChestInventory.getDisplayName().getUnformattedText().contains("Backpack")) {
            backpack = Backpack.getFromItem(mc.thePlayer.getHeldItem());
            if (backpack != null) {
                BackpackColor color = backpack.getBackpackColor();
                GlStateManager.color(color.getR(), color.getG(), color.getB(), 1);
                return;
            }
        }
        backpack = null;
        GlStateManager.color(colorRed,colorGreen,colorBlue,colorAlpha);
    }

    @Redirect(method = "drawGuiContainerForegroundLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawString(Ljava/lang/String;III)I", ordinal = 0))
    private int drawStringTop(FontRenderer fontRenderer, String text, int x, int y, int color) { //Item item, ItemStack stack
        return drawBackpackTest(fontRenderer, text,x,y,color);
    }

    @Redirect(method = "drawGuiContainerForegroundLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/FontRenderer;drawString(Ljava/lang/String;III)I", ordinal = 1))
    private int drawStringBottom(FontRenderer fontRenderer, String text, int x, int y, int color) { //Item item, ItemStack stack
        return drawBackpackTest(fontRenderer, text,x,y,color);
    }

    private int drawBackpackTest(FontRenderer fontRenderer, String text, int x, int y, int color) {
        if (backpack != null) {
            return fontRenderer.drawString(text, x,y, backpack.getBackpackColor().getTextColor());
        }
        return fontRenderer.drawString(text,x,y,color);
    }
}
