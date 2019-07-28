package codes.biscuit.skyblockaddons.mixins;

import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.EnchantPair;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

@Mixin(GuiContainer.class)
public class MixinGuiContainer {

    private EnchantPair reforgeToRender = null;
    private Set<EnchantPair> enchantsToRender = new HashSet<>();
    private Boolean playedSound = false;

    @Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderItem;renderItemOverlayIntoGUI(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void shouldRenderSaveSlots(Slot slotIn, CallbackInfo ci, int x, int y, ItemStack item, boolean flag, boolean flag1,
                                      ItemStack itemstack1, String s) {
        if (!SkyblockAddons.INSTANCE.getConfigValues().getDisabledFeatures().contains(Feature.SHOW_ENCHANTMENTS_REFORGES)) {
            Minecraft mc = Minecraft.getMinecraft();
            FontRenderer fr = mc.fontRendererObj;
            if (item != null && item.hasDisplayName()) {
                if (item.getDisplayName().startsWith(EnumChatFormatting.GREEN + "Enchant Item")) {
                    List<String> toolip = item.getTooltip(mc.thePlayer, false);
                    if (toolip.size() > 2) {
                        String enchantLine = toolip.get(2);
                        String enchant = null;
                        if (enchantLine.split(Pattern.quote("* "))[1].toLowerCase().contains(SkyblockAddons.INSTANCE.getConfigValues().getEnchantment().toLowerCase())) {
                        	enchant = EnumChatFormatting.DARK_GREEN + enchantLine.split(Pattern.quote("* "))[1];
                        	if(!playedSound) {
                        		mc.thePlayer.playSound("random.orb", 10, 0.5F);
                        		playedSound = true;
                        	}
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
                        String enchant = EnumChatFormatting.YELLOW+SkyblockAddons.INSTANCE.getUtils().stripColor(nameParts[0]);
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
}
