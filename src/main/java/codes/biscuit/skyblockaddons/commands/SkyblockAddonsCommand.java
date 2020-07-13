package codes.biscuit.skyblockaddons.commands;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Message;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.DevUtils;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.BlockPos;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class SkyblockAddonsCommand extends CommandBase {

    private static final String HEADER = "§7§m------------§7[§b§l SkyblockAddons §7]§7§m------------";
    private static final String FOOTER = "§7§m------------------------------------------";

    private SkyblockAddons main;
    private Logger logger;

    public SkyblockAddonsCommand() {
        main = SkyblockAddons.getInstance();
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
     * Gets the usage string for the command. If developer mode is enabled, the developer mode usage string is added to
     * the main usage string.
     */
    public String getCommandUsage(ICommandSender sender) {
        String usage =
                HEADER + "\n" +
                "§b● /sba §7- " + Message.COMMAND_USAGE_SBA.getMessage() + "\n" +
                "§b● /sba edit §7- " + Message.COMMAND_USAGE_SBA_EDIT.getMessage() + "\n" +
                "§b● /sba set <zealots|eyes|totalzealots §eor§b total> <number> §7- " + Message.COMMAND_USAGE_SBA_SET_ZEALOT_COUNTER.getMessage() + "\n" +
                "§b● /sba folder §7- " + Message.COMMAND_USAGE_SBA_FOLDER.getMessage();

        if (main.isDevMode()) {
            usage = usage + "\n" +
                    "§b● /sba dev §7- " + Message.COMMAND_USAGE_SBA_DEV.getMessage() + "\n" +
                    "§b● /sba sidebar [formatted] §7- " + Message.COMMAND_USAGE_SBA_SIDEBAR.getMessage() + "\n" +
                    "§b● /sba brand §7- " + Message.COMMAND_USAGE_SBA_BRAND.getMessage();
        }

        usage = usage + "\n" + FOOTER;

        return usage;
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            if (main.isDevMode()) {
                return getListOfStringsMatchingLastWord(args, "set", "edit", "folder", "dev", "sidebar", "brand");
            } else {
                return getListOfStringsMatchingLastWord(args, "set", "edit", "folder", "dev");
            }

        } else if (args.length == 2) {
            if (main.isDevMode() && args[1].equalsIgnoreCase("sidebar")) {
                return getListOfStringsMatchingLastWord(args, "formatted");
            } else if (args[1].equalsIgnoreCase("set")) {
                return getListOfStringsMatchingLastWord(args, "total", "zealots", "eyes");
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

            } else if (args[0].equalsIgnoreCase("dev") || args[0].equalsIgnoreCase("nbt")) {
                main.setDevMode(!main.isDevMode());

                if (main.isDevMode()) {
                    main.getDeveloperCopyNBTKey().register();
                    main.getUtils().sendMessage(ColorCode.GREEN + "Developer mode enabled! TIP: Press right ctrl to copy nbt!");
                } else {
                    main.getDeveloperCopyNBTKey().deRegister();
                    main.getUtils().sendMessage(ColorCode.RED + "Developer mode disabled!");
                }
            } else if (args[0].equalsIgnoreCase("set")) {
                Integer number = null;
                if (args.length >= 3) {
                    try {
                        number = Integer.parseInt(args[2]);
                    } catch (NumberFormatException ex) {
                        main.getUtils().sendErrorMessage("Invalid number to set!");
                        return;
                    }
                }
                if (number == null) {
                    main.getUtils().sendErrorMessage("Invalid number to set!");
                    return;
                }

                if (args[1].equalsIgnoreCase("totalzealots") || args[1].equalsIgnoreCase("total")) {
                    main.getPersistentValues().setTotalKills(number);
                    main.getUtils().sendMessage("Set total zealot count to: "+number+"!");
                } else if (args[1].equalsIgnoreCase("zealots")) {
                    main.getPersistentValues().setKills(number);
                    main.getUtils().sendMessage("Set current zealot count to: "+number+"!");
                } else if (args[1].equalsIgnoreCase("eyes")) {
                    main.getPersistentValues().setSummoningEyeCount(number);
                    main.getUtils().sendMessage("Set total summoning eye count to: "+number+"!");
                } else {
                    main.getUtils().sendErrorMessage("Invalid selection! Please choose 'zealots', 'totalzealots/total', 'eyes'");
                }
            }  else if (args[0].equalsIgnoreCase("folder")) {
                try {
                    Desktop.getDesktop().open(main.getUtils().getSBAFolder());
                } catch (IOException e) {
                    logger.catching(e);
                    main.getUtils().sendErrorMessage("Failed to open mods folder.");
                }
            }  else if (args[0].equalsIgnoreCase("warp")) {
                main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.WARP);
            } else if (main.isDevMode()) {
                if (args[0].equalsIgnoreCase("sidebar")) {
                    Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();

                    if (args.length < 2) {
                        DevUtils.copyScoreboardSideBar(scoreboard);

                    } else if (args.length == 2 && args[1].equalsIgnoreCase("formatted")) {
                        DevUtils.copyScoreboardSidebar(scoreboard, false);

                    } else {
                        main.getUtils().sendMessage(getCommandUsage(sender), false);
                    }
                } else if (args[0].equalsIgnoreCase("brand")) {
                    main.getUtils().sendMessage(DevUtils.getServerBrand(Minecraft.getMinecraft()));

                } else {
                    main.getUtils().sendMessage(getCommandUsage(sender), false);
                }
            } else {
                main.getUtils().sendMessage(getCommandUsage(sender), false);
            }
        } else {
            // If there's no arguments given, open the main GUI
            main.getUtils().setFadingIn(true);
            main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, 1, EnumUtils.GuiTab.MAIN);
        }
    }
}
