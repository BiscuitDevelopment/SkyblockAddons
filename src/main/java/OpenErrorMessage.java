import javax.swing.JOptionPane;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Shows an error message if the mod jar is opened directly instead of in forge.
 */
public class OpenErrorMessage {

    public static void main(String[] args) {
        String[] options = {"Ok", "Help"};
        int response = JOptionPane.showOptionDialog(null, "Opening this file directly does nothing, please install Forge and put this file in your .minecraft/mods folder. " + System.lineSeparator() +
                "If you are unsure how, please click \"Help.\"", "You are installing this incorrectly!", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, null);

        if (response == 1) {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/BiscuitDevelopment/SkyblockAddons/wiki/FAQ#how-do-i-install-skyblockaddons"));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }

        System.exit(0);
    }
}
