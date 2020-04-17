package codes.biscuit.skyblockaddons.commands;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Utils;
import codes.biscuit.skyblockaddons.utils.dev.DevUtils;
import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.command.*;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class SkyblockAddonsCommand extends CommandBase {

    private SkyblockAddons main;
    private Logger logger;
    public SkyblockAddonsCommand(SkyblockAddons main) {
        this.main = main;
        logger = main.getLogger();
    }

    /**
     * Gets the name of the command
     */
    public String getCommandName() {
        return "skyblockaddons";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel()
    {
        return 0;
    }

    /**
     * Returns the aliases of this command
     */
    public List<String> getCommandAliases()
    {
        return Collections.singletonList("sba");
    }

    /**
     * Gets the usage string for the command.
     */
    public String getCommandUsage(ICommandSender sender) { return
        Utils.color("&7&m------------&7[&b&l SkyblockAddons &7]&7&m------------") + "\n" +
        Utils.color("&b● /sba &7- Open the main menu") + "\n" +
        Utils.color("&b● /sba edit &7- Edit GUI locations") + "\n" +
        Utils.color("&b● /sba folder &7- Open your mods folder") + "\n" +
        Utils.color("&7&m----------------------------------------------");
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return true;
    }

    /**
     * Opens the main gui, or locations gui if they type /sba edit
     */
    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 0) {
            switch (args[0]) {
                case "edit":
                    main.getUtils().setFadingIn(false);
                    main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.EDIT_LOCATIONS, 0, null);
                    break;
                case "devmode":
                    main.setDevMode(!main.isDevMode());

                    if (main.isDevMode()) {
                        main.getUtils().sendMessage(ChatFormatting.GREEN + "Developer mode enabled!");
                    } else {
                        main.getUtils().sendMessage(ChatFormatting.RED + "Developer mode disabled!");
                    }
                    break;
                case "folder":
                    try {
                        Desktop.getDesktop().open(main.getUtils().getSBAFolder(false));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
/*                case "update": // Not actually a command.
                    if (main.getRenderListener().getDownloadInfo().isPatch())
                        main.getUtils().downloadPatch(main.getRenderListener().getDownloadInfo().getNewestVersion());
                    break;*/
                default:
                    main.getUtils().sendMessage(getCommandUsage(sender), false);
            }
        } else {
            // If there's no arguments given, open the main GUI
            main.getUtils().setFadingIn(true);
            main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, 1, EnumUtils.GuiTab.MAIN);
        }
    }
}
