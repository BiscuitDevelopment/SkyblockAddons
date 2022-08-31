package codes.biscuit.skyblockaddons.exceptions;

import com.google.common.base.Throwables;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraftforge.fml.client.CustomModLoadingErrorDisplayException;

import java.util.List;

import static net.minecraft.util.EnumChatFormatting.*;

public class LoadingException extends CustomModLoadingErrorDisplayException {
    private final boolean DRAW_ROOT_CAUSE;

    private String rootCauseString;
    private int maxWidth;
    private int xCenter;
    private int yStart;

    public LoadingException(String message, Throwable cause, boolean drawRootCause) {
        super(message, cause);
        DRAW_ROOT_CAUSE = drawRootCause;

        if (DRAW_ROOT_CAUSE) {
            rootCauseString = Throwables.getRootCause(this).toString();
        }
    }

    public LoadingException(String message, Throwable cause) {
        this(message, cause, false);
    }

    @Override
    public void initGui(GuiErrorScreen errorScreen, FontRenderer fontRenderer) {
        maxWidth = errorScreen.width - 80;
        yStart = 50;
        xCenter = errorScreen.width / 2;
    }

    @Override
    public void drawScreen(GuiErrorScreen errorScreen, FontRenderer fontRenderer, int mouseRelX, int mouseRelY, float tickTime) {
        int yPos = yStart;
        errorScreen.drawCenteredString(fontRenderer, String.format(
                        "%sSkyblockAddons%s has encountered an error while loading.", AQUA, RESET), xCenter,
                yPos, 0xFFFFFF);
        yPos += 20;
        if (getMessage() != null) {
            for (String errorLine : fontRenderer.listFormattedStringToWidth(getMessage(), maxWidth)) {
                errorScreen.drawCenteredString(fontRenderer, errorLine, xCenter, yPos, 0xFFFFFF);
                yPos += 10;
            }
        }
        if (DRAW_ROOT_CAUSE) {
            yPos += 10;
            errorScreen.drawCenteredString(fontRenderer, "Cause:", xCenter, yPos, 0xFFFFFF);
            yPos += 10;
            errorScreen.drawCenteredString(fontRenderer, rootCauseString, xCenter, yPos, 0xFFFFFF);
        }
        yPos += 30;
        errorScreen.drawCenteredString(fontRenderer, "Please restart your game.", xCenter, yPos, 0xFFFFFF);
        yPos += 10;
        List<String> errorPersistString = fontRenderer.listFormattedStringToWidth(String.format("If error persists after restarting, please report it at " +
                "%sdiscord.gg/biscuit.%s", BOLD, RESET), maxWidth);
        for (String line : errorPersistString) {
            errorScreen.drawCenteredString(fontRenderer, line, xCenter, yPos, 0xFFFFFF);
            yPos += 10;
        }
    }
}
