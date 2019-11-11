package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Backpack;
import codes.biscuit.skyblockaddons.utils.CooldownManager;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.InventoryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiScreen.class)
public abstract class MixinGuiScreen {

    private static final long MADDOX_BATPHONE_COOLDOWN = 3 * 60 * 1000;

    @Inject(method = "renderToolTip", at = @At(value = "HEAD"), cancellable = true)
    private void shouldRenderRedirect(ItemStack stack, int x, int y, CallbackInfo ci) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (stack.getItem().equals(Items.skull) && main.getConfigValues().isEnabled(Feature.SHOW_BACKPACK_PREVIEW)) {
            if (main.getConfigValues().isEnabled(Feature.SHOW_BACKPACK_HOLDING_SHIFT) && !GuiScreen.isShiftKeyDown()) {
                return;
            }
            Container playerContainer = Minecraft.getMinecraft().thePlayer.openContainer;
            if (playerContainer instanceof ContainerChest) { // Avoid showing backpack preview in auction stuff.
                IInventory chest = ((ContainerChest) playerContainer).getLowerChestInventory();
                if (chest.hasCustomName()) {
                    String chestName = chest.getDisplayName().getUnformattedText();
                    if (chestName.contains("Auction") || chestName.equals("Your Bids")) {
                        return;
                    }
                }
            }
            Backpack backpack = Backpack.getFromItem(stack);
            if (backpack != null) {
                backpack.setX(x);
                backpack.setY(y);
                main.getUtils().setBackpackToRender(backpack);
                main.getPlayerListener().onItemTooltip(new ItemTooltipEvent(stack, null, null, false));
                ci.cancel();
            }
        }
    }

    @Inject(method = "handleComponentClick", at = @At(value = "INVOKE"))
    private void handleComponentClick(IChatComponent component, CallbackInfoReturnable cir) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock()) {
            if (component != null) {
                if (component.getUnformattedText().equals("§2§l[OPEN MENU]")) { // Prompt when Maddox picks up the phone
                    if (!CooldownManager.isOnCooldown(InventoryUtils.MADDOX_BATPHONE_DISPLAYNAME)) {
                        CooldownManager.put(InventoryUtils.MADDOX_BATPHONE_DISPLAYNAME, MADDOX_BATPHONE_COOLDOWN);
                    }
                }
            }
        }
    }
}
