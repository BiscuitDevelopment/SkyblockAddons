package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import net.minecraft.util.IChatComponent;

public class GuiNewChatHook {

    public static String getUnformattedText(IChatComponent iChatComponent) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main != null && main.getConfigValues().isEnabled(Feature.DEVELOPER_MODE)) {
            return iChatComponent.getFormattedText(); // For logging colored messages...
        }
        return iChatComponent.getUnformattedText();
    }
}
