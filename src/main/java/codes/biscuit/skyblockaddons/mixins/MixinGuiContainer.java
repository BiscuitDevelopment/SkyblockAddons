package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.listeners.RenderListener;
import codes.biscuit.skyblockaddons.utils.Backpack;
import codes.biscuit.skyblockaddons.utils.BackpackColor;
import codes.biscuit.skyblockaddons.utils.ConfigColor;
import codes.biscuit.skyblockaddons.utils.EnchantPair;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerDispenser;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Mixin(GuiContainer.class)
public class MixinGuiContainer extends GuiScreen {

    @Shadow
    private Slot hoveredSlot;
    private ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
    private EnchantPair reforgeToRender = null;
    private Set<EnchantPair> enchantsToRender = new HashSet<>();

    @Inject(
            method = "drawSlot",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/RenderItem;renderItemOverlayIntoGUI(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
                    ordinal = 0
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void shouldRenderSaveSlots(Slot slotIn, CallbackInfo ci, int x, int y, ItemStack item, boolean flag, boolean flag1,
                                       ItemStack itemstack1, String s) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getConfigValues().isEnabled(Feature.SHOW_ENCHANTMENTS_REFORGES)) {
            Minecraft mc = Minecraft.getMinecraft();
            FontRenderer fr = mc.fontRenderer;
            if (item != null && item.hasDisplayName()) {
                if (item.getDisplayName().startsWith(ChatFormatting.GREEN + "Enchant Item")) {
                    List<String> toolip = item.getTooltip(mc.player, ITooltipFlag.TooltipFlags.NORMAL);
                    if (toolip.size() > 2) {
                        String enchantLine = toolip.get(2);
                        String toMatch = enchantLine.split(Pattern.quote("* "))[1];
//                        String enchant = ChatFormatting.YELLOW + enchantLine.split(Pattern.quote("* "))[1];
                        String enchant;
                        if (!main.getUtils().getEnchantmentMatch().isEmpty() &&
                                main.getUtils().enchantReforgeMatches(toMatch)) {
                            enchant = ChatFormatting.RED + enchantLine.split(Pattern.quote("* "))[1];
                        } else {
                            enchant = ChatFormatting.YELLOW + enchantLine.split(Pattern.quote("* "))[1];
                        }
                        float yOff;
                        if (slotIn.slotNumber == 29 || slotIn.slotNumber == 33) {
                            yOff = 26;
                        } else {
                            yOff = 36;
                        }
                        float scaleMultiplier = 1 / 0.75F;
                        float halfStringWidth = fr.getStringWidth(enchant) / 2f;
                        x += 8; // to center it
                        enchantsToRender.add(new EnchantPair(x * scaleMultiplier - halfStringWidth, y * scaleMultiplier + yOff, enchant));
                    }
                } else if ("Reforge Item".equals(slotIn.inventory.getDisplayName().getUnformattedText()) && slotIn.slotNumber == 13) {
                    String reforge = main.getUtils().getReforgeFromItem(item);
                    if (reforge != null) {
                        if (!main.getUtils().getEnchantmentMatch().isEmpty() &&
                                main.getUtils().enchantReforgeMatches(reforge)) {
                            reforge = ChatFormatting.RED + reforge;
                        } else {
                            reforge = ChatFormatting.YELLOW + reforge;
                        }
                        x -= 28;
                        y += 22;
                        float halfStringWidth = fr.getStringWidth(reforge) / 2f;
                        reforgeToRender = new EnchantPair(x - halfStringWidth, y, reforge);
                    }
                }
            }
            if (slotIn.slotNumber == 53) {
                GlStateManager.pushMatrix();

                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableBlend();
                if (reforgeToRender != null) {
                    fr.drawStringWithShadow(reforgeToRender.getEnchant(), reforgeToRender.getX(), reforgeToRender.getY(), new Color(255, 255, 255, 255).getRGB());
                    reforgeToRender = null;
                }
                GlStateManager.scale(0.75, 0.75, 1);
                Iterator<EnchantPair> enchantPairIterator = enchantsToRender.iterator();
                while (enchantPairIterator.hasNext()) {
                    EnchantPair enchant = enchantPairIterator.next();
                    fr.drawStringWithShadow(enchant.getEnchant(), enchant.getX(), enchant.getY(), new Color(255, 255, 255, 255).getRGB());
                    enchantPairIterator.remove();
                }
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
                GlStateManager.popMatrix();
            }
        }
    }

    @Inject(
            method = "drawScreen",
            at = @At(
                    value = "RETURN"
            )
    )
    private void drawBackpacks(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        Backpack backpack = main.getUtils().getBackpackToRender();

        if (backpack != null) {
            int x = backpack.getX();
            int y = backpack.getY();
            ItemStack[] items = backpack.getItems();
            int length = items.length;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            if (main.getConfigValues().getBackpackStyle() == EnumUtils.BackpackStyle.GUI) {
                this.mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
                int rows = length/9;
                GlStateManager.disableLighting();
                GlStateManager.pushMatrix();
                GlStateManager.translate(0,0,300);
                int textColor = 4210752;

                if (main.getConfigValues().isEnabled(Feature.MAKE_BACKPACK_INVENTORIES_COLORED)) {
                    BackpackColor color = backpack.getBackpackColor();
                    GlStateManager.color(color.getR(), color.getG(), color.getB(), 1);
                    textColor = color.getTextColor();
                }

                drawTexturedModalRect(x, y, 0, 0, 176, rows * 18 + 17);
                drawTexturedModalRect(x, y + rows * 18 + 17, 0, 215, 176, 7);
                fontRenderer.drawString(backpack.getBackpackName(), x+8, y+6, textColor);
                GlStateManager.popMatrix();
                GlStateManager.enableLighting();

                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();

                for (int i = 0; i < length; i++) {
                    ItemStack item = items[i];

                    if (item != null) {
                        int itemX = x+8 + ((i % 9) * 18);
                        int itemY = y+18 + ((i / 9) * 18);
                        RenderItem renderItem = mc.getRenderItem();
                        zLevel = 200;
                        renderItem.zLevel = 200;
                        renderItem.renderItemAndEffectIntoGUI(item, itemX, itemY);
                        renderItem.renderItemOverlayIntoGUI(mc.fontRenderer, item, itemX, itemY, null);
                        zLevel = 0;
                        renderItem.zLevel = 0;
                    }
                }
            } else {
                GlStateManager.disableLighting();
                GlStateManager.pushMatrix();
                GlStateManager.translate(0,0, 300);
                Gui.drawRect(x, y, x + (16 * 9) + 3, y + (16 * (length / 9)) + 3, ConfigColor.DARK_GRAY.getRGB(250));
                GlStateManager.popMatrix();
                GlStateManager.enableLighting();

                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                GlStateManager.enableRescaleNormal();

                for (int i = 0; i < length; i++) {
                    ItemStack item = items[i];

                    if (item != null) {
                        int itemX = x + ((i % 9) * 16);
                        int itemY = y + ((i / 9) * 16);
                        RenderItem renderItem = mc.getRenderItem();
                        zLevel = 200;
                        renderItem.zLevel = 200;
                        renderItem.renderItemAndEffectIntoGUI(item, itemX, itemY);
                        renderItem.renderItemOverlayIntoGUI(mc.fontRenderer, item, itemX, itemY, null);
                        zLevel = 0;
                        renderItem.zLevel = 0;
                    }
                }
            }

            main.getUtils().setBackpackToRender(null);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
        }
    }


    @Inject(
            method = "drawScreen",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/OpenGlHelper;setLightmapTextureCoords(IFF)V",
                    ordinal = 0
            )
    )
    private void setLightmapTextureCoords(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        SkyblockAddons.getInstance().getUtils().setLastHoveredSlot(-1);
    }

    @Redirect(
            method = "drawScreen",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGradientRect(IIIIII)V",
                    ordinal = 0
            )
    )
    private void drawGradientRect(GuiContainer guiContainer, int left, int top, int right, int bottom, int startColor, int endColor) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        int slotNum = hoveredSlot.slotNumber;
        boolean skipSlot = false;
        Container container = mc.player.openContainer;

        if (container instanceof ContainerChest) {
            slotNum -= ((ContainerChest)container).getLowerChestInventory().getSizeInventory()-9;
            if (slotNum < 9) skipSlot = true;
        } else if (container instanceof ContainerHopper) {
            slotNum += 4;
            if (slotNum < 9) skipSlot = true;
        } else if (container instanceof ContainerDispenser) {
            if (slotNum < 9) skipSlot = true;
        } else if (container instanceof ContainerFurnace) {
            slotNum += 6;
            if (slotNum < 9) skipSlot = true;
        }
        main.getUtils().setLastHoveredSlot(slotNum);
        if (!skipSlot && hoveredSlot != null && main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) &&
                main.getUtils().isOnSkyblock() && main.getConfigValues().getLockedSlots().contains(slotNum)) {
            int red = ConfigColor.RED.getRGB(127);
            drawGradientRect(left,top,right,bottom,red,red);
        } else {
            drawGradientRect(left,top,right,bottom,startColor,endColor);
        }
    }

    @Inject(
            method = "drawScreen",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawSlot(Lnet/minecraft/inventory/Slot;)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT
    )
    private void drawSlot(int mouseX, int mouseY, float partialTicks, CallbackInfo ci, int i, int j, int k, int l, int i1, Slot slot) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (slot != null && main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) &&
                main.getUtils().isOnSkyblock()) {
            int slotNum = slot.slotNumber;
            Container container = mc.player.openContainer;
            if (container instanceof ContainerChest) {
                slotNum -= ((ContainerChest)container).getLowerChestInventory().getSizeInventory()-9;
                if (slotNum < 9) return;
            } else if (container instanceof ContainerHopper) {
                slotNum += 4;
                if (slotNum < 9) return;
            } else if (container instanceof ContainerDispenser) {
                if (slotNum < 9) return;
            } else if (container instanceof ContainerFurnace) {
                slotNum += 6;
                if (slotNum < 9) return;
            }
            if (main.getConfigValues().getLockedSlots().contains(slotNum)) {
                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.color(1,1,1,0.4F);
                GlStateManager.enableBlend();
                Minecraft.getMinecraft().getTextureManager().bindTexture(RenderListener.LOCK);
                mc.ingameGUI.drawTexturedModalRect(slot.xPos, slot.yPos, 0, 0, 16, 16);
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();
            }
        }
    }

    @Inject(
            method = "keyTyped",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/inventory/GuiContainer;checkHotbarKeys(I)Z",
                    ordinal = 0,
                    shift = At.Shift.BEFORE
            ),
            locals = LocalCapture.CAPTURE_FAILSOFT,
            cancellable = true
    )
    private void keyTyped(char typedChar, int keyCode, CallbackInfo ci) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getUtils().isOnSkyblock()) {
            if (main.getConfigValues().isEnabled(Feature.LOCK_SLOTS) && (keyCode != 1 && keyCode != this.mc.gameSettings.keyBindInventory.getKeyCode())) {
                int slot = main.getUtils().getLastHoveredSlot();
                if (mc.player.inventory.getItemStack() == null && this.hoveredSlot != null) {
                    for (int i = 0; i < 9; ++i) {
                        if (keyCode == this.mc.gameSettings.keyBindsHotbar[i].getKeyCode()) {
                            slot = i + 36; // They are hotkeying, the actual slot is the targeted one, +36 because
                        }
                    }
                }
                if (slot >= 9 || (slot >= 5 && mc.currentScreen instanceof GuiInventory)) {
                    if (main.getConfigValues().getLockedSlots().contains(slot)) {
                        if (main.getLockSlot().getKeyCode() == keyCode) {
                            main.getUtils().playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1);
                            main.getConfigValues().getLockedSlots().remove(slot);
                            main.getConfigValues().saveConfig();
                        } else {
                            main.getUtils().playSound(SoundEvents.BLOCK_NOTE_BASS, 0.5);
                            ci.cancel(); // slot is locked
                            return;
                        }
                    } else {
                        if (main.getLockSlot().getKeyCode() == keyCode) {
                            main.getUtils().playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1);
                            main.getConfigValues().getLockedSlots().add(slot);
                            main.getConfigValues().saveConfig();
                        }
                    }
                }
            }
            if (mc.gameSettings.keyBindDrop.getKeyCode() == keyCode && main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS)) {
                if (main.getInventoryUtils().shouldCancelDrop(this.hoveredSlot))
                    ci.cancel();
            }
        }
    }

}
