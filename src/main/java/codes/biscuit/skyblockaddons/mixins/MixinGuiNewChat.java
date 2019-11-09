package codes.biscuit.skyblockaddons.mixins;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Mixin(GuiNewChat.class)
public class MixinGuiNewChat {
    private static final Set<String> randomMessages = new HashSet<>(Arrays.asList("I feel like I can fly!", "What was in that soup?", "Hmm… tasty!", "You can now fly for 2 minutes.", "Your Magical Mushroom Soup flight has been extended for 2 extra minutes."));

    @Shadow
    public void printChatMessageWithOptionalDeletion(IChatComponent chatComponent, int chatLineId) {}

    @Redirect(method = "printChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiNewChat;printChatMessageWithOptionalDeletion(Lnet/minecraft/util/IChatComponent;I)V"))
    public void printChatMessage(GuiNewChat chat, IChatComponent chatComponent, int chatLineId) {
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (main.getConfigValues().isDisabled(Feature.DISABLE_MAGICAL_SOUP_MESSAGES) || !randomMessages.contains(chatComponent.getUnformattedText())) {
            this.printChatMessageWithOptionalDeletion(chatComponent, chatLineId);
        }
    }

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
