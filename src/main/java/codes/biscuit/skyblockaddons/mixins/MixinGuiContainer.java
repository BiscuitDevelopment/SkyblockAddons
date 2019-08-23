package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.awt.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Mixin(GuiContainer.class)
public class MixinGuiContainer extends GuiScreen {

    private ResourceLocation CHEST_GUI_TEXTURE =  new ResourceLocation("textures/gui/container/generic_54.png");
    private EnchantPair reforgeToRender = null;
    private Set<EnchantPair> enchantsToRender = new HashSet<>();

    @Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderItem;renderItemOverlayIntoGUI(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void shouldRenderSaveSlots(Slot slotIn, CallbackInfo ci, int x, int y, ItemStack item, boolean flag, boolean flag1,
                                      ItemStack itemstack1, String s) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getConfigValues().isEnabled(Feature.SHOW_ENCHANTMENTS_REFORGES)) {
            Minecraft mc = Minecraft.getMinecraft();
            FontRenderer fr = mc.fontRendererObj;
            if (item != null && item.hasDisplayName()) {
                if (item.getDisplayName().startsWith(EnumChatFormatting.GREEN + "Enchant Item")) {
                    List<String> toolip = item.getTooltip(mc.thePlayer, false);
                    if (toolip.size() > 2) {
                        String enchantLine = toolip.get(2);
                        String toMatch = enchantLine.split(Pattern.quote("* "))[1];
//                        String enchant = EnumChatFormatting.YELLOW + enchantLine.split(Pattern.quote("* "))[1];
                        String enchant;
                        if (main.getUtils().getEnchantmentMatch().size() > 0 &&
                                main.getUtils().enchantReforgeMatches(toMatch)) {
                            enchant = EnumChatFormatting.RED + enchantLine.split(Pattern.quote("* "))[1];
                        } else {
                            enchant = EnumChatFormatting.YELLOW + enchantLine.split(Pattern.quote("* "))[1];
                        }
                        float yOff;
                        if (slotIn.slotNumber == 29 || slotIn.slotNumber == 33) {
                            yOff = 26;
                        } else {
                            yOff = 36;
                        }
                        float scaleMultiplier = 1 / 0.75F;
                        float halfStringWidth = fr.getStringWidth(enchant) / 2;
                        x += 8; // to center it
                        enchantsToRender.add(new EnchantPair(x * scaleMultiplier - halfStringWidth, y * scaleMultiplier + yOff, enchant));
                    }
                } else if (slotIn.inventory.getDisplayName().getUnformattedText().equals("Reforge Item") && slotIn.slotNumber == 13) {
                    String[] nameParts = item.getDisplayName().split(" ");
                    if (nameParts.length > 2) {
                        String reforge = main.getUtils().stripColor(nameParts[0]);
                        String enchant;
                        if (main.getUtils().getEnchantmentMatch().size() > 0 &&
                                main.getUtils().enchantReforgeMatches(reforge)) {
                            enchant = EnumChatFormatting.RED+reforge;
                        } else {
                            enchant = EnumChatFormatting.YELLOW+reforge;
                        }
                        x -= 28;
                        y += 22;
                        float halfStringWidth = fr.getStringWidth(enchant) / 2;
                        reforgeToRender = new EnchantPair(x-halfStringWidth, y, enchant);
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

    @Inject(method = "drawScreen", at = @At(value = "RETURN"))
    private void drawBackpacks(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        BackpackInfo backpackInfo = SkyblockAddons.getInstance().getUtils().getBackpackToRender();
        if (backpackInfo != null) {
            int x = backpackInfo.getX();
            int y = backpackInfo.getY();
            ItemStack[] items = backpackInfo.getItems();
            EnumUtils.Backpack backpack = backpackInfo.getBackpack();
            int length = items.length;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            if (SkyblockAddons.getInstance().getConfigValues().getBackpackStyle() == EnumUtils.BackpackStyle.GUI) {
                this.mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);
                int rows = length/9;
                GlStateManager.disableLighting();
                GlStateManager.pushMatrix();
                GlStateManager.translate(0,0,300);
                drawTexturedModalRect(x, y, 0, 0, 176, rows * 18 + 17);
                drawTexturedModalRect(x, y + rows * 18 + 17, 0, 215, 176, 7);
                fontRendererObj.drawString(backpack.getItemName(), x+8, y+6, 4210752);
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
                        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
                        zLevel = 200;
                        renderItem.zLevel = 200;
                        renderItem.renderItemAndEffectIntoGUI(item, itemX, itemY);
                        renderItem.renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRendererObj, item, itemX, itemY, null);
                        zLevel = 0;
                        renderItem.zLevel = 0;
                    }
                }
            } else {
                GlStateManager.disableLighting();
                GlStateManager.pushMatrix();
                GlStateManager.translate(0,0, 300);
                Gui.drawRect(x, y, x + (16 * 9) + 3, y + (16 * (length / 9)) + 3, ConfigColor.DARK_GRAY.getColor(250));
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
                        RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
                        zLevel = 200;
                        renderItem.zLevel = 200;
                        renderItem.renderItemAndEffectIntoGUI(item, itemX, itemY);
                        renderItem.renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRendererObj, item, itemX, itemY, null);
                        zLevel = 0;
                        renderItem.zLevel = 0;
                    }
                }
            }
            SkyblockAddons.getInstance().getUtils().setBackpackToRender(null);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            RenderHelper.enableStandardItemLighting();
        }
    }
}
