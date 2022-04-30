package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.features.discordrpc.DiscordRPCManager;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngameMenu.class)
public class MixinIngameMenu extends GuiScreen {

    @Inject(method = "actionPerformed", at = @At("HEAD"))
    protected void onActionPerformed(GuiButton button, CallbackInfo ci) {
        if (button.id == 1) {
            DiscordRPCManager discordRPCManager = SkyblockAddons.getInstance().getDiscordRPCManager();
            if (discordRPCManager.isActive()) {
                discordRPCManager.stop();
            }
        }

        if (button.id == 53) {
            onButtonClick();
        }
    }

    @Inject(method = "initGui", at = @At("RETURN"))
    public void addMenuButtons(CallbackInfo ci) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getUtils().isOnSkyblock() && main.getConfigValues().isEnabled(Feature.SKYBLOCK_ADDONS_BUTTON_IN_PAUSE_MENU)) {
            buttonList.add(new GuiButton(53, width - 120 - 5, height - 20 - 5, 120, 20, "SkyblockAddons Menu"));
        }
    }
    public void onButtonClick() {
        SkyblockAddons skyblockAddons = SkyblockAddons.getInstance();
        skyblockAddons.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, 1, EnumUtils.GuiTab.MAIN);
    }
}
