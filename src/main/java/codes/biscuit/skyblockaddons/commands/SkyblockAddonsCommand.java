package codes.biscuit.skyblockaddons.commands;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Utils;
import codes.biscuit.skyblockaddons.utils.dev.DevUtils;
import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.command.*;
import net.minecraft.scoreboard.Scoreboard;
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
        Utils.color("&7&m------------------------------------------");
    }

    /**
     * Gets the usage string for the developer mode sub-command.
     */
    public String getDevCommandUsage() { return
        Utils.color("&7&m----&7[&b&l SkyblockAddons Developer Mode &7]&7&m----") + "\n" +
        Utils.color("&b● /sba dev &7- Toggle developer mode") + "\n" +
        Utils.color("") + "\n" +
        Utils.color("&7Options (&b/sba dev [option])&7:") + "\n" +
        Utils.color("&b● copySidebar [keepControlCodes] &7- Copy the") + "\n" +
        Utils.color("    &7scoreboard sidebar. \"keepControlCodes\"") + "\n" +
        Utils.color("    &7keeps the formatting codes when copying.") + "\n" +
        Utils.color("&b● brand &7- Show the server brand") + "\n" +
        Utils.color("&7&m-------------------------------------------");
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "edit", "folder");
        }
        else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("dev")) {
                return getListOfStringsMatchingLastWord(args, "copySidebar", "serverBrand");
            }
        }
        else if (args.length == 3) {
            if (args[1].equalsIgnoreCase("copySidebar")) {
                return getListOfStringsMatchingLastWord(args, "keepControlCodes");
            }
        }

        return null;
    }

    /**
     * Opens the main gui, or locations gui if they type /sba edit
     */
    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("edit")) {
                main.getUtils().setFadingIn(false);
                main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.EDIT_LOCATIONS, 0, null);
            }
            else if (args[0].equalsIgnoreCase("dev")) {
                if (args.length == 1) {
                    main.setDevMode(!main.isDevMode());

                    if (main.isDevMode()) {
                        main.getUtils().sendMessage(ChatFormatting.GREEN + "Developer mode enabled!");
                    } else {
                        main.getUtils().sendMessage(ChatFormatting.RED + "Developer mode disabled!");
                    }
                }
                else {
                    if (main.isDevMode()) {
                        if (args[1].equalsIgnoreCase("help")) {
                            main.getUtils().sendMessage(getDevCommandUsage(), false);
                        }
                        else if (args[1].equalsIgnoreCase("copySidebar")) {
                            Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();

                            if (args.length < 3) {
                                DevUtils.copyScoreboardSideBar(scoreboard);
                            }
                            else if (args.length == 3 && args[2].equalsIgnoreCase("keepControlCodes")) {
                                DevUtils.copyScoreboardSidebar(scoreboard, false);
                            }
                            else {
                                main.getUtils().sendErrorMessage("Wrong usage!");
                            }
                        }
                        else if (args[1].equalsIgnoreCase("serverBrand")) {
                            main.getUtils().sendMessage(DevUtils.getServerBrand(Minecraft.getMinecraft()));
                        }
                        else {
                            main.getUtils().sendErrorMessage("Wrong usage!");
                        }
                    }
                }
            }
            else if (args[0].equalsIgnoreCase("folder")) {
                try {
                    Desktop.getDesktop().open(main.getUtils().getSBAFolder(false));
                } catch (IOException e) {
                    logger.catching(e);
                    main.getUtils().sendErrorMessage("Failed to open mods folder.");
                }
            }
/*            else if (args[0].equalsIgnoreCase("update")) {
                if (main.getRenderListener().getDownloadInfo().isPatch())
                    main.getUtils().downloadPatch(main.getRenderListener().getDownloadInfo().getNewestVersion());
            }*/
            else {
                main.getUtils().sendMessage(getCommandUsage(sender), false);
            }
        } else {
            // If there's no arguments given, open the main GUI
            main.getUtils().setFadingIn(true);
            main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, 1, EnumUtils.GuiTab.MAIN);
        }
    }
}
