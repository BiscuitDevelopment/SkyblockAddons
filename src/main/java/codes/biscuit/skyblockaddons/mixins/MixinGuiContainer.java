package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.utils.EnchantPair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
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
public class MixinGuiContainer {

    private Set<EnchantPair> enchantsToRender = new HashSet<>();

        @Inject(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderItem;renderItemOverlayIntoGUI(Lnet/minecraft/client/gui/FontRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
            ordinal = 0), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void shouldRenderSaveSlots(Slot slotIn, CallbackInfo ci, int i, int j, ItemStack item, boolean flag, boolean flag1,
                                      ItemStack itemstack1, String s) {
        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRendererObj;
        if (item != null && item.hasDisplayName() && item.getDisplayName().startsWith(EnumChatFormatting.GREEN+"Enchant Item")) {
            List<String> toolip = item.getTooltip(mc.thePlayer, false);
            if (toolip.size() > 2) {
                String enchantLine = toolip.get(2);
                String enchant = EnumChatFormatting.YELLOW + enchantLine.split(Pattern.quote("* "))[1];
                float y;
                if (slotIn.slotNumber == 29 || slotIn.slotNumber == 33) {
                    y = 26;
                } else {
                    y = 36;
                }
                float scaleMultiplier = 1 / 0.75F;
                float halfStringWidth = fr.getStringWidth(enchant) / 2;
                i += 8; // to center it
                enchantsToRender.add(new EnchantPair(i * scaleMultiplier - halfStringWidth, j * scaleMultiplier + y, enchant));
            }
        }
        if (slotIn.slotNumber == 53) {
            Iterator<EnchantPair> enchantPairIterator = enchantsToRender.iterator();
            while (enchantPairIterator.hasNext()) {
                EnchantPair enchant = enchantPairIterator.next();
                GlStateManager.pushMatrix();
                GlStateManager.scale(0.75, 0.75, 1);

                GlStateManager.disableLighting();
                GlStateManager.disableDepth();
                GlStateManager.disableBlend();
                fr.drawStringWithShadow(enchant.getEnchant(), enchant.getX(), enchant.getY(), new Color(255,255,255,255).getRGB());
                GlStateManager.enableLighting();
                GlStateManager.enableDepth();

                GlStateManager.popMatrix();
                enchantPairIterator.remove();
            }
        }
    }

}
