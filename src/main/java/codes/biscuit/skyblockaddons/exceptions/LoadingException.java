package codes.biscuit.skyblockaddons.exceptions;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;

import java.util.List;

import static net.minecraft.util.EnumChatFormatting.*;

public class LoadingException extends CustomModLoadingErrorDisplayException {
    public LoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {
    }

    @Override
    public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime) {
        int maxWidth = errorScreen.width - 80;
        int offset = 50;
        int xPos = errorScreen.width / 2;

        errorScreen.drawCenteredString(fontRenderer, String.format(
                        "%sSkyblockAddons%s has encountered an error while loading.", AQUA, RESET), xPos,
                offset, 0xFFFFFF);
        offset += 10;
        if (getMessage() != null) {
            for (String errorLine : getMessage().split("\n")) {
                errorScreen.drawCenteredString(fontRenderer, errorLine, xPos, offset, 0xFFFFFF);
                offset += 10;
            }
        }
        offset += 10;
        errorScreen.drawCenteredString(fontRenderer, "Please restart your game.", xPos, offset, 0xFFFFFF);
        offset += 10;
        List<String> errorPersistString = fontRenderer.listFormattedStringToWidth(String.format("If error persists after restarting, please report it at " +
                "%sdiscord.gg/biscuit.%s", BOLD, RESET), maxWidth);
        for (String line : errorPersistString) {
            errorScreen.drawCenteredString(fontRenderer, line, xPos, offset, 0xFFFFFF);
            offset += 10;
        }
    }
}
