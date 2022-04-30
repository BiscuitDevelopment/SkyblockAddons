package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.features.discordrpc.DiscordRPCManager;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiDisconnected.class)
public class MixinGuiDisconnected {

    @Inject(method = "<init>", at = @At("RETURN"))
    void onDisconnect(GuiScreen p_i45020_1_, String p_i45020_2_, IChatComponent p_i45020_3_, CallbackInfo ci) {
        DiscordRPCManager discordRPCManager = SkyblockAddons.getInstance().getDiscordRPCManager();
        if (discordRPCManager.isActive()) {
            discordRPCManager.stop();
        }
    }
}
