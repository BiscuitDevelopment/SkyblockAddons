package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiNewChat.class)
public class MixinGuiNewChat {

    @Redirect(method = "printChatMessageWithOptionalDeletion", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/IChatComponent;getUnformattedText()Ljava/lang/String;"))
    private String printChatMessageWithOptionalDeletion(IChatComponent iChatComponent) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        ICommandSender player = Minecraft.getMinecraft().thePlayer;
        if (main != null && main.getUtils().isDevEnviroment() || (player != null && player.getName().equals("Biscut"))) {
            return iChatComponent.getFormattedText(); // makes it easier for debugging
        }
        return iChatComponent.getUnformattedText();
    }
}
