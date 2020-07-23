package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsSetup;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.IChatComponent;

public class GuiNewChatHook {

    public static String getUnformattedText(IChatComponent iChatComponent) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        ICommandSender player = Minecraft.getMinecraft().thePlayer;
        if (main != null && SkyblockAddonsSetup.isDeobfuscatedEnvironment() || (player != null && player.getName().equals("Biscut"))) {
            return iChatComponent.getFormattedText(); // makes it easier for debugging
        }
        return iChatComponent.getUnformattedText();
    }
}
