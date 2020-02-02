package codes.biscuit.skyblockaddons.commands;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class SkyblockAddonsCommand extends CommandBase {
    private static String[] HELP_MESSAGE = {"Skyblock Addons Command Help", "Usage: /sba <option>", "help — Shows this help message",
            "edit — Edit the GUI", "dev — Toggles developer mode", "folder — Opens the Minecraft mods folder", "update — Checks for updates"};

    private SkyblockAddons main;

    public SkyblockAddonsCommand(SkyblockAddons main) {
        this.main = main;
    }

    @Override
    public String getCommandName() {
        return "skyblockaddons";
    }

    @Override
    public List<String> getCommandAliases()
    {
        return Collections.singletonList("sba");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return null;
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
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "help":
                    for (String line: HELP_MESSAGE) {
                        main.getUtils().sendMessage(line);
                    }
                    break;
                case "edit":
                    main.getUtils().setFadingIn(false);
                    main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.EDIT_LOCATIONS, 0, null);
                    break;
                case "dev":
                    main.setDevMode(!main.isDevMode());

                    if (main.isDevMode()) {
                        main.getUtils().sendMessage(ChatFormatting.GREEN + "Developer mode enabled!");
                    } else {
                        main.getUtils().sendMessage(ChatFormatting.RED + "Developer mode disabled!");
                    }
                    break;
                case "update":
                    if (main.getRenderListener().getDownloadInfo().isPatch())
                        main.getUtils().downloadPatch(main.getRenderListener().getDownloadInfo().getNewestVersion());
                    break;
                case "folder":
                    try {
                        Desktop.getDesktop().open(main.getUtils().getSBAFolder(false));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
        else {
            // If there's no arguments given, open the main GUI
            main.getUtils().setFadingIn(true);
            main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, 1, EnumUtils.GuiTab.MAIN);
        }
    }

}
