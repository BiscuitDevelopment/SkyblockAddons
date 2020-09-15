package codes.biscuit.skyblockaddons.commands;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Message;
import codes.biscuit.skyblockaddons.misc.SkyblockKeyBinding;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.DevUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Utils;
import com.google.common.base.CaseFormat;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.command.*;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * This is the main command of SkyblockAddons. It is used to open the menu, change settings, and for developer mode functions.
 */
public class SkyblockAddonsCommand extends CommandBase {

    private static final String HEADER = "§7§m------------§7[§b§l SkyblockAddons §7]§7§m------------";
    private static final String FOOTER = "§7§m------------------------------------------";
    private static final String[] SUBCOMMANDS = {"help", "set", "edit", "folder", "dev", "brand", "copyEntity", "copySidebar",
            "copyTabList", "toggleActionBarLogging"};

    private final SkyblockAddons main;
    private final Logger logger;

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
                "§b● " + CommandSyntax.BASE + " §7- " + Message.COMMAND_USAGE_SBA.getMessage() + "\n" +
                "§b● " + CommandSyntax.HELP + " §7- " + Message.COMMAND_USAGE_SBA_HELP.getMessage() + "\n" +
                "§b● " + CommandSyntax.EDIT + " §7- " + Message.COMMAND_USAGE_SBA_EDIT.getMessage() + "\n" +
                "§b● " + CommandSyntax.SET + " §7- " + Message.COMMAND_USAGE_SBA_SET_ZEALOT_COUNTER.getMessage() + "\n" +
                "§b● " + CommandSyntax.FOLDER + " §7- " + Message.COMMAND_USAGE_SBA_FOLDER.getMessage();

        if (main.isDevMode()) {
            usage = usage + "\n" +
                    "§b● " + CommandSyntax.DEV + " §7- " + Message.COMMAND_USAGE_SBA_DEV.getMessage() + "\n" +
                    "§b● " + CommandSyntax.BRAND + " §7- " + Message.COMMAND_USAGE_SBA_BRAND.getMessage() + "\n" +
                    "§b● " + CommandSyntax.COPY_ENTITY + " §7- " + Message.COMMAND_USAGE_SBA_COPY_ENTITY.getMessage() + "\n" +
                    "§b● " + CommandSyntax.COPY_SIDEBAR + " §7- " + Message.COMMAND_USAGE_SBA_COPY_SIDEBAR.getMessage() + "\n" +
                    "§b● " + CommandSyntax.COPY_TAB_LIST + " §7- " + Message.COMMAND_USAGE_SBA_COPY_TAB_LIST.getMessage() + "\n" +
                    "§b● " + CommandSyntax.TOGGLE_ACTION_BAR_LOGGING + " §7- " + Message.COMMAND_USAGE_TOGGLE_ACTION_BAR_LOGGING.getMessage();
        }

        usage = usage + "\n" + FOOTER;

        return usage;
    }

    /**
     * Returns the detailed usage for the sub-command provided with the header and footer included.
     *
     * @param subCommand the sub-command to fetch the usage of
     * @return the usage of the given sub-command
     * @throws IllegalArgumentException if there is no sub-command with the given name or the sub-command doesn't have a
     *                                  corresponding {@code SubCommandUsage}
     * @throws NullPointerException if {@code subCommand} is {@code null}
     */
    public String getSubCommandUsage(String subCommand) {
        for (String validSubCommand : SUBCOMMANDS) {
            if (subCommand.equalsIgnoreCase(validSubCommand)) {
                subCommand = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, validSubCommand);
            }
        }

        return HEADER + "\n" + SubCommandUsage.valueOf(subCommand) + "\n" + FOOTER;
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getSubCommandTabCompletionOptions(args);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("help")) {
                return getSubCommandTabCompletionOptions(args);
            } else if (args[0].equalsIgnoreCase("set")) {
                return getListOfStringsMatchingLastWord(args, "total", "zealots", "eyes");
            } else if (main.isDevMode()) {
                if (args[0].equalsIgnoreCase("copyEntity")) {
                    return getListOfStringsMatchingLastWord(args, DevUtils.ENTITY_NAMES);
                } else if (args[0].equalsIgnoreCase("copySidebar")) {
                    return getListOfStringsMatchingLastWord(args, "formatted");
                }
            }
        }

        return null;
    }

    /**
     * Callback when the command is invoked
     */
    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        try {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("help")) {
                    if (args.length == 2) {
                        try {
                            main.getUtils().sendMessage(getSubCommandUsage(args[1]), false);
                        } catch (IllegalArgumentException e) {
                            throw new CommandException(Message.COMMAND_USAGE_WRONG_USAGE_SUBCOMMAND_NOT_FOUND.getMessage(args[1]));
                        }
                    } else {
                        main.getUtils().sendMessage(getCommandUsage(sender), false);
                    }
                } else if (args[0].equalsIgnoreCase("edit")) {
                    main.getUtils().setFadingIn(false);
                    main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.EDIT_LOCATIONS, 0, null);

                } else if (args[0].equalsIgnoreCase("dev") || args[0].equalsIgnoreCase("nbt")) {
                    SkyblockKeyBinding devKeyBinding = main.getDeveloperCopyNBTKey();
                    main.setDevMode(!main.isDevMode());

                    if (main.isDevMode()) {
                        devKeyBinding.register();
                        main.getUtils().sendMessage(ColorCode.GREEN + Message.COMMAND_USAGE_SBA_DEV_ENABLED.getMessage(
                                Keyboard.getKeyName(devKeyBinding.getKeyCode())));
                    } else {
                        devKeyBinding.deRegister();
                        main.getUtils().sendMessage(ColorCode.RED + Message.COMMAND_USAGE_SBA_DEV_DISABLED.getMessage());
                    }
                } else if (args[0].equalsIgnoreCase("set")) {
                    int number;

                    if (args.length >= 3) {
                        number = parseInt(args[2]);
                    } else {
                        throw new WrongUsageException(Message.COMMAND_USAGE_WRONG_USAGE_GENERIC.getMessage());
                    }

                    if (args[1].equalsIgnoreCase("totalZealots") || args[1].equalsIgnoreCase("total")) {
                        main.getPersistentValues().setTotalKills(number);
                        main.getUtils().sendMessage(Message.COMMAND_USAGE_SBA_SET_ZEALOT_COUNTER_TOTAL_ZEALOTS.getMessage(
                                Integer.toString(number)));
                    } else if (args[1].equalsIgnoreCase("zealots")) {
                        main.getPersistentValues().setKills(number);
                        main.getUtils().sendMessage(Message.COMMAND_USAGE_SBA_SET_ZEALOT_COUNTER_ZEALOTS.getMessage(
                                Integer.toString(number)));
                    } else if (args[1].equalsIgnoreCase("eyes")) {
                        main.getPersistentValues().setSummoningEyeCount(number);
                        main.getUtils().sendMessage(Message.COMMAND_USAGE_SBA_SET_ZEALOT_COUNTER_EYES.getMessage(
                                Integer.toString(number)));
                    } else {
                        throw new CommandException(Message.COMMAND_USAGE_SBA_SET_ZEALOT_COUNTER_WRONG_USAGE.getMessage(
                                "'zealots', 'totalZealots/total', 'eyes'"));
                    }
                } else if (args[0].equalsIgnoreCase("folder")) {
                    try {
                        Desktop.getDesktop().open(main.getUtils().getSBAFolder());
                    } catch (IOException e) {
                        logger.catching(e);
                        throw new CommandException(Message.COMMAND_USAGE_SBA_FOLDER_ERROR.getMessage(), e.getMessage());
                    }
                } else if (args[0].equalsIgnoreCase("warp")) {
                    main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.WARP);
                } else if (main.isDevMode()) {
                    if (args[0].equalsIgnoreCase("brand")) {
                        String serverBrand = DevUtils.getServerBrand(Minecraft.getMinecraft());

                        if (serverBrand != null) {
                            main.getUtils().sendMessage(Message.COMMAND_USAGE_SBA_BRAND_BRAND_OUTPUT.getMessage(serverBrand));
                        } else {
                            throw new CommandException(Message.COMMAND_USAGE_SBA_BRAND_NOT_FOUND.getMessage());
                        }
                    } else if (args[0].equalsIgnoreCase("copyEntity")) {
                        try {
                            // Use default options if no options are provided and use defaults for any options that are missing.
                            if (args.length == 1) {
                                DevUtils.copyEntityData();
                            } else if (args.length == 2) {
                                DevUtils.copyEntityData(args[1], DevUtils.ENTITY_COPY_RADIUS);
                            } else if (args.length == 3) {
                                DevUtils.copyEntityData(args[1], parseInt(args[2]));
                            } else {
                                throw new SyntaxErrorException();
                            }
                        } catch (IllegalArgumentException e) {
                            throw new WrongUsageException(e.getMessage());
                        }
                    } else if (args[0].equalsIgnoreCase("copySidebar")) {
                        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();

                        try {
                            if (args.length < 2) {
                                DevUtils.copyScoreboardSideBar(scoreboard);

                            } else if (args.length == 2 && parseBoolean(args[1])) {
                                DevUtils.copyScoreboardSidebar(scoreboard, false);
                            } else {
                                throw new WrongUsageException(Message.COMMAND_USAGE_WRONG_USAGE_GENERIC.getMessage());
                            }
                        } catch (NullPointerException e) {
                            throw new CommandException(e.getMessage());
                        }
                    } else if (args[0].equalsIgnoreCase("copyTabList")) {
                        DevUtils.copyTabListHeaderAndFooter();
                    } else if (args[0].equalsIgnoreCase("toggleActionBarLogging")) {
                        DevUtils.setLoggingActionBarMessages(!DevUtils.isLoggingActionBarMessages());

                        if (DevUtils.isLoggingActionBarMessages()) {
                            main.getUtils().sendMessage(ColorCode.GREEN + Message.COMMAND_USAGE_TOGGLE_ACTION_BAR_LOGGING_ENABLED.getMessage());
                        } else {
                            main.getUtils().sendMessage(ColorCode.RED + Message.COMMAND_USAGE_TOGGLE_ACTION_BAR_LOGGING_DISABLED.getMessage());
                        }
                    } else {
                        throw new WrongUsageException(Message.COMMAND_USAGE_WRONG_USAGE_SUBCOMMAND_NOT_FOUND.getMessage(args[0]));
                    }
                } else {
                    throw new WrongUsageException(Message.COMMAND_USAGE_WRONG_USAGE_SUBCOMMAND_NOT_FOUND.getMessage(args[0]));
                }
            } else {
                // If there's no arguments given, open the main GUI
                main.getUtils().setFadingIn(true);
                main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, 1, EnumUtils.GuiTab.MAIN);
            }
        } catch (CommandException e) {
            ChatComponentTranslation errorMessage = new ChatComponentTranslation(e.getMessage(), e.getErrorObjects());
            errorMessage.getChatStyle().setColor(EnumChatFormatting.RED);

            // Intercept error handling to add our own prefix to error messages.
            throw new CommandException(Utils.MESSAGE_PREFIX + errorMessage.getFormattedText());
        }
    }

    /*
     Gets tab completion options listing all sub-commands.
     Developer mode commands are not included if developer mode is disabled.
     */
    private List<String> getSubCommandTabCompletionOptions(String[] args) {
        if (main.isDevMode()) {
            return getListOfStringsMatchingLastWord(args, SUBCOMMANDS);
        } else {
            return getListOfStringsMatchingLastWord(args, Arrays.copyOf(SUBCOMMANDS, 5));
        }
    }

    // This is an Enum representing options used by the sub-commands of this command.
    private enum CommandOption {
        COMMAND("Command", Message.SUBCOMMAND_HELP_HELP_COMMAND.getMessage()),
        ZEALOTS("Zealots", Message.SUBCOMMAND_HELP_SET_ZEALOT_COUNTER_ZEALOTS.getMessage()),
        EYES("Eyes", Message.SUBCOMMAND_HELP_SET_ZEALOT_COUNTER_EYES.getMessage()),
        TOTAL_ZEALOTS("TotalZealots|Total", Message.SUBCOMMAND_HELP_SET_ZEALOT_COUNTER_TOTAL_ZEALOTS.getMessage()),
        FORMATTED("Formatted", Message.SUBCOMMAND_HELP_COPY_SIDEBAR_FORMATTED.getMessage()),
        ENTITY_NAMES("EntityNames", Message.SUBCOMMAND_HELP_COPY_ENTITY_ENTITY_NAMES.getMessage()),
        RADIUS("Radius", Message.SUBCOMMAND_HELP_COPY_ENTITY_RADIUS.getMessage());

        @Getter
        private final String name;
        private final String description;

        CommandOption(String name, String description) {
            this.name = name;
            this.description = description;
        }

        @Override
        public String toString() {
            return "§b● " + name + " §7- " + description;
        }
    }

    // Syntax definitions for this command and its sub-commands
    private enum CommandSyntax {
        BASE("/sba"),
        HELP("/sba help [command]"),
        EDIT("/sba edit"),
        SET("/sba set <zealots|eyes|totalZealots §eor§b total> <number>"),
        FOLDER("/sba folder"),
        DEV("/sba dev"),
        BRAND("/sba brand"),
        COPY_ENTITY("/sba copyEntity [EntityNames] [radius]"),
        COPY_SIDEBAR("/sba sidebar [formatted: boolean]"),
        COPY_TAB_LIST("/sba copyTabList"),
        TOGGLE_ACTION_BAR_LOGGING("/sba toggleActionBarLogging");

        @Getter
        private final String syntax;

        CommandSyntax(String syntax) {
            this.syntax = syntax;
        }

        @Override
        public String toString() {
            return syntax;
        }
    }

    // Usage strings for all the sub-commands of this command
    private enum SubCommandUsage {
        HELP(CommandSyntax.HELP, Message.COMMAND_USAGE_SBA_HELP.getMessage(), Collections.singletonList(CommandOption.COMMAND)),
        EDIT(CommandSyntax.EDIT, Message.COMMAND_USAGE_SBA_EDIT.getMessage(), null),
        SET(CommandSyntax.SET, Message.SUBCOMMAND_HELP_SET_ZEALOT_COUNTER.getMessage(), Arrays.asList(CommandOption.ZEALOTS, CommandOption.EYES, CommandOption.TOTAL_ZEALOTS)),
        FOLDER(CommandSyntax.FOLDER, Message.COMMAND_USAGE_SBA_FOLDER.getMessage(), null),
        DEV(CommandSyntax.DEV, Message.SUBCOMMAND_HELP_DEV.getMessage(), null),
        BRAND(CommandSyntax.BRAND, Message.COMMAND_USAGE_SBA_BRAND.getMessage(), null),
        COPY_ENTITY(CommandSyntax.COPY_ENTITY, Message.SUBCOMMAND_HELP_COPY_ENTITY.getMessage(Integer.toString(DevUtils.ENTITY_COPY_RADIUS)), Arrays.asList(CommandOption.ENTITY_NAMES, CommandOption.RADIUS)),
        COPY_SIDEBAR(CommandSyntax.COPY_SIDEBAR, Message.COMMAND_USAGE_SBA_COPY_SIDEBAR.getMessage(), Collections.singletonList(CommandOption.FORMATTED)),
        COPY_TAB_LIST(CommandSyntax.COPY_TAB_LIST, Message.SUBCOMMAND_HELP_COPY_TAB_LIST.getMessage(), null),
        TOGGLE_ACTION_BAR_LOGGING(CommandSyntax.TOGGLE_ACTION_BAR_LOGGING, Message.COMMAND_USAGE_TOGGLE_ACTION_BAR_LOGGING.getMessage(), null)
        ;
        private final CommandSyntax syntax;
        private final String description;
        private final List<CommandOption> options;

        SubCommandUsage(CommandSyntax syntax, String description, List<CommandOption> options) {
            this.syntax = syntax;
            this.description = description;
            this.options = options;
        }

        /**
         * <p>Returns a formatted usage string for the sub-command with the name of this Enum constant.</p>
         * <p>Example:</p>
         * <p>Usage: §b/sba help [command]§r</p>
         * <br>
         * <p>§lDescription:</p>
         * <p>§7Show this help message. If a command is provided, detailed help about that command is shown.</p>
         * <br>
         * <p>§lOptions:</p>
         * <p>§b● Command §7- the sub-command to get detailed usage for</p>
         *
         * @return a formatted String representing this {@code SubCommandUsage}
         */
        @Override
        public String toString() {
            StringBuilder usageBuilder = new StringBuilder(
                    "Usage: §b" + syntax + "§r" +
                    "\n" +
                    "\n§lDescription:" +
                    "\n" + EnumChatFormatting.GRAY + description);

            if (options != null) {
                ListIterator<CommandOption> optionListIterator = options.listIterator();

                usageBuilder.append("\n").append("\n§lOptions:");

                while (optionListIterator.hasNext()) {
                    usageBuilder.append("\n");
                    usageBuilder.append(optionListIterator.next());
                }
            }

            return usageBuilder.toString();
        }
    }
}
