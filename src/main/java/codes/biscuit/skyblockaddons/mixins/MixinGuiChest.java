package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Backpack;
import codes.biscuit.skyblockaddons.utils.BackpackColor;
import codes.biscuit.skyblockaddons.utils.ConfigColor;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.Message;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.spongepowered.asm.mixin.Final;
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

    @Final
    @Shadow
    private IInventory upperChestInventory;

    @Final
    @Shadow
    private IInventory lowerChestInventory;

    public MixinGuiChest(Container inventorySlotsIn) {
        super(inventorySlotsIn);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);

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
            mc.ingameGUI.drawString(mc.fontRenderer, Message.MESSAGE_TYPE_ENCHANTMENTS.getMessage(inventoryMessage), Math.round(x/scale), Math.round((guiTop+40)/scale), defaultBlue);
            mc.ingameGUI.drawString(mc.fontRenderer, Message.MESSAGE_SEPARATE_ENCHANTMENTS.getMessage(), Math.round(x/scale), Math.round((guiTop + 50)/scale), defaultBlue);
            mc.ingameGUI.drawString(mc.fontRenderer, Message.MESSAGE_ENCHANTS_TO_MATCH.getMessage(inventoryMessage), Math.round(x/scale), Math.round((guiTop + 70)/scale), defaultBlue);
            mc.ingameGUI.drawString(mc.fontRenderer,Message.MESSAGE_ENCHANTS_TO_EXCLUDE.getMessage(inventoryMessage), Math.round(x/scale), Math.round((guiTop + 110)/scale), defaultBlue);
            GlStateManager.popMatrix();
            textFieldMatch.drawTextBox();
            if ("".equals(textFieldMatch.getText())) {
                mc.ingameGUI.drawString(mc.fontRenderer, "ex. \"prot, feather\"", x+4, guiTop + 86, ConfigColor.DARK_GRAY.getColor(255));
            }
            textFieldExclusions.drawTextBox();
            if ("".equals(textFieldExclusions.getText())) {
                mc.ingameGUI.drawString(mc.fontRenderer, "ex. \"proj, blast\"", x+4, guiTop + 126, ConfigColor.DARK_GRAY.getColor(255));
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        String guiName = lowerChestInventory.getDisplayName().getUnformattedText();
        if ("Enchant Item".equals(guiName)) inventoryType = EnumUtils.InventoryType.ENCHANTMENT_TABLE;
        if ("Reforge Item".equals(guiName)) inventoryType = EnumUtils.InventoryType.REFORGE_ANVIL;
        if (inventoryType != null) {
            int xPos = guiLeft - 160;
            if (xPos<0) {
                xPos = 20;
            }
            int yPos = guiTop + 80;
            textFieldMatch = new GuiTextField(2, this.fontRenderer, xPos, yPos, 120, 20);
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
            if (!text.isEmpty()) {
                textFieldMatch.setText(text);
            }
            yPos += 40;
            textFieldExclusions = new GuiTextField(2, this.fontRenderer, xPos, yPos, 120, 20);
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
            if (!text.isEmpty()) {
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
    protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (!main.getUtils().getEnchantmentMatch().isEmpty()) {
            if (slotIn != null && !slotIn.inventory.equals(mc.player.inventory) && slotIn.getHasStack()) {
                Container slots = inventorySlots;
                if (slotIn.getSlotIndex() == 13 && inventoryType == EnumUtils.InventoryType.ENCHANTMENT_TABLE) {
                    ItemStack[] enchantBottles = {slots.getSlot(29).getStack(), slots.getSlot(31).getStack(), slots.getSlot(33).getStack()};
                    for (ItemStack bottle : enchantBottles) {
                        if (bottle != null && bottle.hasDisplayName()) {
                            if (bottle.getDisplayName().startsWith(ChatFormatting.GREEN + "Enchant Item")) {
                                Minecraft mc = Minecraft.getMinecraft();
                                List<String> toolip = bottle.getTooltip(mc.player, ITooltipFlag.TooltipFlags.NORMAL);
                                if (toolip.size() > 2) {
                                    String enchantLine = toolip.get(2).split(Pattern.quote("* "))[1];
                                    if (main.getUtils().enchantReforgeMatches(enchantLine)) {
                                        main.getUtils().playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1);
                                        return;
                                    }
                                }
                            } else if (bottle.getDisplayName().startsWith(ChatFormatting.RED + "Enchant Item")) {
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
                                    main.getUtils().playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1);
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
        out:
        if (slotIn != null && main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) &&
                main.getUtils().isOnSkyblock()) {
            int slotNum = slotIn.slotNumber;
            Container container = mc.player.openContainer;
            slotNum -= ((ContainerChest)container).getLowerChestInventory().getSizeInventory()-9;
            if (slotNum < 9) break out; // for chests
            if (main.getConfigValues().getLockedSlots().contains(slotNum)) {
                main.getUtils().playSound(SoundEvents.BLOCK_NOTE_BASS, 0.5);
                return;
            }
        }

        if (main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) &&
                lowerChestInventory.hasCustomName() && EnumUtils.Merchant.isMerchant(lowerChestInventory.getDisplayName().getUnformattedText()) &&
                slotIn != null && slotIn.inventory instanceof InventoryPlayer) {
            if (main.getInventoryUtils().shouldCancelDrop(slotIn))
                return;
        }

        super.handleMouseClick(slotIn, slotId, mouseButton, type);
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

    @Redirect(
            method = "drawGuiContainerBackgroundLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V",
                    ordinal = 0
            )
    )
    private void color(float colorRed, float colorGreen, float colorBlue, float colorAlpha) { //Item item, ItemStack stack
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.SHOW_BACKPACK_PREVIEW) &&
                main.getConfigValues().isEnabled(Feature.MAKE_BACKPACK_INVENTORIES_COLORED)
                && lowerChestInventory.hasCustomName() && lowerChestInventory.getDisplayName().getUnformattedText().contains("Backpack")) {
            backpack = Backpack.getFromItem(mc.player.getHeldItem(EnumHand.MAIN_HAND));
            if (backpack != null) {
                BackpackColor color = backpack.getBackpackColor();
                GlStateManager.color(color.getR(), color.getG(), color.getB(), 1);
                return;
            }
        }
        backpack = null;
        GlStateManager.color(colorRed, colorGreen, colorBlue, colorAlpha);
    }

    @Redirect(
            method = "drawGuiContainerForegroundLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/FontRenderer;drawString(Ljava/lang/String;III)I",
                    ordinal = 0
            )
    )
    private int drawStringTop(int mouseX, int mouseY) { //Item item, ItemStack stack
        return drawBackpackTest(this.lowerChestInventory.getDisplayName().getUnformattedText(), mouseX, mouseY);
    }

    @Redirect(
            method = "drawGuiContainerForegroundLayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/FontRenderer;drawString(Ljava/lang/String;III)I",
                    ordinal = 1
            )
    )
    private int drawStringBottom(int mouseX, int mouseY) { //Item item, ItemStack stack
        return drawBackpackTest(this.upperChestInventory.getDisplayName().getUnformattedText(), mouseX, mouseY);
    }

    private int drawBackpackTest(String text, int x, int y) {
        if (backpack != null)
            return this.fontRenderer.drawString(text, x, y, backpack.getBackpackColor().getTextColor());

        return this.fontRenderer.drawString(text, x, y, 4210752);
    }

}