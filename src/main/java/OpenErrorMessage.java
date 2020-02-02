import javax.swing.JOptionPane;

/**
 * Shows an error message if the mod jar is opened directly instead of in forge.
 */
public class OpenErrorMessage {

    public static void main(String[] args) {
        JOptionPane.showMessageDialog(null,
                "Opening this file directly does nothing, please install forge and put this file in your .minecraft/mods folder. " +
                        System.lineSeparator() + "If you are unsure how, you can search for \"Forge Installation Tutorials\" online, or get help by joining my discord: biscuit.codes/discord.",
                "You are installing this incorrectly!",
                JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }
}
