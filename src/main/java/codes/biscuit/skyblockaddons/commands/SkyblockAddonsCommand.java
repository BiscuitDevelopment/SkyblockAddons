package codes.biscuit.skyblockaddons.commands;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerBoss;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerDrop;
import codes.biscuit.skyblockaddons.features.slayertracker.SlayerTracker;
import codes.biscuit.skyblockaddons.misc.SkyblockKeyBinding;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.DevUtils;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Utils;
import com.google.common.base.CaseFormat;
import lombok.Getter;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.command.*;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.*;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

//TODO: Clean this up a bit, make it less complex to add stuff

/**
 * This is the main command of SkyblockAddons. It is used to open the menu, change settings, and for developer mode functions.
 */
public class SkyblockAddonsCommand extends CommandBase {

    private static final String HEADER = "§7§m----------------§7[ §b§lSkyblockAddons §7]§7§m----------------";
    private static final String FOOTER = "§7§m-----------------------------------------------------";
    private static final String[] SUBCOMMANDS = {"help", "edit", "folder", "resetZealotCounter", "set", "slayer", "version", "dev", "brand", "copyBlock",
            "copyEntity", "copySidebar", "copyTabList", "pd", "reload", "reloadConfig", "reloadRes", "toggleActionBarLogging",
            "toggleMagmaTimerLogging"};

    private final SkyblockAddons main = SkyblockAddons.getInstance();

    /**
     * Gets the name of the command
     */
    public String getCommandName() {
        return "skyblockaddons";
    }

    /**
     * Returns the required permission level for this command.
     */
    public int getRequiredPermissionLevel() {
        return 0;
    }

    /**
     * Returns the aliases of this command
     */
    public List<String> getCommandAliases() {
        return Collections.singletonList("sba");
    }

    /**
     * Gets the usage string for the command. If developer mode is enabled, the developer mode usage string is added to
     * the main usage string.
     */
    public String getCommandUsage(ICommandSender sender) {
        String usage =
                HEADER + "\n" +
                "§b● " + CommandSyntax.BASE + " §7-§r " + Translations.getMessage("commands.usage.sba.base.help") + "\n" +
                "§b● " + CommandSyntax.HELP + " §7-§r " + Translations.getMessage("commands.usage.sba.help.help") + "\n" +
                "§b● " + CommandSyntax.EDIT + " §7-§r " + Translations.getMessage("commands.usage.sba.edit.help") + "\n" +
                "§b● " + CommandSyntax.SET + " §7-§r " + Translations.getMessage("commands.usage.sba.set.zealotCounter.help") + "\n" +
                "§b● " + CommandSyntax.RESET_ZEALOT_COUNTER + " §7-§r " + Translations.getMessage("commands.usage.sba.resetZealotCounter.help") + "\n" +
                "§b● " + CommandSyntax.FOLDER + " §7-§r " + Translations.getMessage("commands.usage.sba.folder.help") + "\n" +
                "§b● " + CommandSyntax.SLAYER + " §7-§r " + Translations.getMessage("commands.usage.sba.slayer.help") + "\n" +
                "§b● " + CommandSyntax.VERSION + " §7-§r " + Translations.getMessage("commands.usage.sba.version.help") + "\n" +
                "§b● " + CommandSyntax.DEV + " §7-§r " + Translations.getMessage("commands.usage.sba.dev.help");

        if (main.getConfigValues().isEnabled(Feature.DEVELOPER_MODE)) {
            usage = usage + "\n" +
                    "§b● " + CommandSyntax.BRAND + " §7- " + getDevPrefixFormatted() + Translations.getMessage("commands.usage.sba.brand.help") + "\n" +
                    "§b● " + CommandSyntax.COPY_BLOCK + " §7- " + getDevPrefixFormatted() +  Translations.getMessage("commands.usage.sba.copyBlock.help") + "\n" +
                    "§b● " + CommandSyntax.COPY_ENTITY + " §7- " + getDevPrefixFormatted() + Translations.getMessage("commands.usage.sba.copyEntity.help") + "\n" +
                    "§b● " + CommandSyntax.COPY_SIDEBAR + " §7- " + getDevPrefixFormatted() + Translations.getMessage("commands.usage.sba.copySidebar.help") + "\n" +
                    "§b● " + CommandSyntax.COPY_TAB_LIST + " §7- " + getDevPrefixFormatted() + Translations.getMessage("commands.usage.sba.copyTabList.help") + "\n" +
                    "§b● " + CommandSyntax.PD + " §7- " + getDevPrefixFormatted() + Translations.getMessage("commands.usage.sba.printDeaths.help") + "\n" +
                    "§b● " + CommandSyntax.RELOAD + " §7- " + getDevPrefixFormatted() + Translations.getMessage("commands.usage.sba.reload.help") + "\n" +
                    "§b● " + CommandSyntax.RELOAD_CONFIG + " §7- " + getDevPrefixFormatted() + Translations.getMessage("commands.usage.sba.reloadConfig.help") + "\n" +
                    "§b● " + CommandSyntax.RELOAD_RES + " §7- " + getDevPrefixFormatted() + Translations.getMessage("commands.usage.sba.reloadRes.help") + "\n" +
                    "§b● " + CommandSyntax.TOGGLE_ACTION_BAR_LOGGING + " §7- " + getDevPrefixFormatted() + Translations.getMessage("commands.usage.sba.toggleActionBarLogging.help") + "\n" +
                    "§b● " + CommandSyntax.TOGGLE_MAGMA_TIMER_LOGGING + " §7- " + getDevPrefixFormatted() + Translations.getMessage("commands.usage.sba.toggleMagmaTimerLogging.help")
            ;
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

            } else if (args[0].equalsIgnoreCase("slayer")) {
                String[] slayers = new String[SlayerBoss.values().length];
                for (int i = 0; i < SlayerBoss.values().length; i++) {
                    slayers[i] = SlayerBoss.values()[i].getMobType().toLowerCase(Locale.US);
                }
                return getListOfStringsMatchingLastWord(args, slayers);

            } else if (main.getConfigValues().isEnabled(Feature.DEVELOPER_MODE)) {
                if (args[0].equalsIgnoreCase("copyEntity")) {
                    return getListOfStringsMatchingLastWord(args, DevUtils.ALL_ENTITY_NAMES);
                } else if (args[0].equalsIgnoreCase("copySidebar")) {
                    return getListOfStringsMatchingLastWord(args, "formatted");
                }
            }
        }  else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("slayer")) {
                SlayerBoss slayerBoss = SlayerBoss.getFromMobType(args[1]);

                if (slayerBoss != null) {
                    String[] drops = new String[slayerBoss.getDrops().size() + 1];
                    drops[0] = "kills";
                    int i = 1;
                    for (SlayerDrop slayerDrop : slayerBoss.getDrops()) {
                        drops[i] = slayerDrop.name().toLowerCase(Locale.US);
                        i++;
                    }
                    return getListOfStringsMatchingLastWord(args, drops);
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
                            throw new CommandException(Translations.getMessage("commands.errors.wrongUsage.subCommandNotFound", args[1]));
                        }
                    } else {
                        main.getUtils().sendMessage(getCommandUsage(sender), false);
                    }
                } else if (args[0].equalsIgnoreCase("edit")) {
                    main.getUtils().setFadingIn(false);
                    main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.EDIT_LOCATIONS, 0, null);

                } else if (args[0].equalsIgnoreCase("dev") || args[0].equalsIgnoreCase("nbt")) {
                    SkyblockKeyBinding devModeKeyBinding = main.getDeveloperCopyNBTKey();
                    Feature.DEVELOPER_MODE.setEnabled(!main.getConfigValues().isEnabled(Feature.DEVELOPER_MODE));

                    if (main.getConfigValues().isEnabled(Feature.DEVELOPER_MODE)) {
                        main.getUtils().sendMessage(ColorCode.GREEN + Translations.getMessage("commands.responses.sba.dev.enabled",
                                GameSettings.getKeyDisplayString(devModeKeyBinding.getKeyCode())));
                    } else {
                        main.getUtils().sendMessage(ColorCode.RED + Translations.getMessage("commands.responses.sba.dev.disabled"));
                    }
                } else if (args[0].equalsIgnoreCase("resetZealotCounter")) {
                    main.getPersistentValuesManager().resetZealotCounter();
                    main.getUtils().sendMessage(ColorCode.GREEN + Translations.getMessage("commands.responses.sba.resetZealotCounter.resetSuccess"));
                } else if (args[0].equalsIgnoreCase("set")) {
                    int number;

                    if (args.length >= 3) {
                        number = parseInt(args[2]);
                    } else {
                        throw new WrongUsageException(Translations.getMessage("commands.errors.wrongUsage.generic"));
                    }

                    if (args[1].equalsIgnoreCase("totalZealots") || args[1].equalsIgnoreCase("total")) {
                        main.getPersistentValuesManager().getPersistentValues().setTotalKills(number);
                        main.getPersistentValuesManager().saveValues();
                        main.getUtils().sendMessage(Translations.getMessage("commands.responses.sba.set.zealotCounter.totalZealotsSet",
                                Integer.toString(number)));
                    } else if (args[1].equalsIgnoreCase("zealots")) {
                        main.getPersistentValuesManager().getPersistentValues().setKills(number);
                        main.getPersistentValuesManager().saveValues();
                        main.getUtils().sendMessage(Translations.getMessage("commands.responses.sba.set.zealotCounter.zealotsSet",
                                Integer.toString(number)));
                    } else if (args[1].equalsIgnoreCase("eyes")) {
                        main.getPersistentValuesManager().getPersistentValues().setSummoningEyeCount(number);
                        main.getPersistentValuesManager().saveValues();
                        main.getUtils().sendMessage(Translations.getMessage("commands.responses.sba.set.zealotCounter.eyesSet",
                                Integer.toString(number)));
                    } else {
                        throw new WrongUsageException(Translations.getMessage("sba.set.zealotCounter.wrongUsage",
                                "'zealots', 'totalZealots/total', 'eyes'"));
                    }
                } else if (args[0].equalsIgnoreCase("folder")) {
                    try {
                        Desktop.getDesktop().open(main.getUtils().getSBAFolder());
                    } catch (IOException e) {
                        throw new CommandException(Translations.getMessage("commands.responses.sba.folder.error"), e.getMessage());
                    }
                } else if (args[0].equalsIgnoreCase("warp")) {
                    main.getRenderListener().setGuiToOpen(EnumUtils.GUIType.WARP);
                } else if (args[0].equalsIgnoreCase("slayer")) {
                    if (args.length == 1) {
                        StringBuilder bosses = new StringBuilder();
                        for (int i = 0; i < SlayerBoss.values().length; i++) {
                            SlayerBoss slayerBoss = SlayerBoss.values()[i];
                            bosses.append("'").append(slayerBoss.getMobType().toLowerCase(Locale.US)).append("'");
                            if (i + 1 < SlayerBoss.values().length) {
                                bosses.append(", ");
                            }
                        }

                        throw new WrongUsageException(Translations.getMessage("commands.responses.sba.slayer.bossRequired", bosses.toString()));
                    } else if (args.length == 2) {
                        throw new WrongUsageException(Translations.getMessage("commands.responses.sba.slayer.statRequired"));
                    } else if (args.length == 3) {
                        throw new WrongUsageException(Translations.getMessage("commands.responses.sba.slayer.numberRequired"));
                    } else if (args.length == 4) {
                        try {
                            SlayerTracker.getInstance().setStatManually(args);
                        } catch (NumberFormatException e) {
                            throw new NumberInvalidException("commands.generic.num.invalid", args[3]);
                        } catch (IllegalArgumentException e) {
                            throw new WrongUsageException(e.getMessage());
                        }
                    }
                } else if (args[0].equalsIgnoreCase("version")) {
                    String versionString = Translations.getMessage("messages.version") + " v" + SkyblockAddons.VERSION;
                    ChatComponentText versionChatComponent = new ChatComponentText(versionString);
                    ChatStyle versionChatStyle = new ChatStyle().setColor(EnumChatFormatting.AQUA)
                            .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    new ChatComponentText(
                                            Translations.getMessage("commands.responses.sba.version.hoverText"))
                                            .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.WHITE))))
                            .setChatClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, SkyblockAddons.getVersionFull()));
                    versionChatComponent.setChatStyle(versionChatStyle);

                    /*
                     Include MAJOR.MINOR.PATCH-pre-release in the chat message and add build number if it's defined when
                     the user chooses to copy for diagnostic purposes.
                     */
                    main.getUtils().sendMessage(versionChatComponent, true);
                } else if (args[0].equalsIgnoreCase("internal")) {
                    if (args.length > 2) {
                        if (args[1].equalsIgnoreCase("copy")) {
                            DevUtils.copyStringToClipboard(
                                    Arrays.stream(args).skip(2).collect(Collectors.joining(" ")),
                                    Translations.getMessage("messages.copied"));
                        }
                    }
                } else if (main.getConfigValues().isEnabled(Feature.DEVELOPER_MODE)) {
                    if (args[0].equalsIgnoreCase("brand")) {
                        String serverBrand = DevUtils.getServerBrand();

                        if (serverBrand != null) {
                            main.getUtils().sendMessage(Translations.getMessage("commands.responses.sba.brand.brandOutput", serverBrand));
                        } else {
                            throw new CommandException(Translations.getMessage("commands.responses.sba.brand.notFound"));
                        }
                    } else if (args[0].equalsIgnoreCase("copyBlock")) {
                        DevUtils.setCopyMode(DevUtils.CopyMode.BLOCK);
                        DevUtils.copyData();

                    } else if (args[0].equalsIgnoreCase("copyEntity")) {
                        try {
                            // Use default options if no options are provided and use defaults for any options that are missing.
                            if (args.length >= 3) {
                                DevUtils.setEntityNamesFromString(args[1]);
                                DevUtils.setEntityCopyRadius(parseInt(args[2]));
                            } else if (args.length >= 2) {
                                DevUtils.setEntityNamesFromString(args[1]);
                                DevUtils.resetEntityCopyRadiusToDefault();
                            } else {
                                DevUtils.resetEntityNamesToDefault();
                                DevUtils.resetEntityCopyRadiusToDefault();
                            }
                            DevUtils.setCopyMode(DevUtils.CopyMode.ENTITY);
                            DevUtils.copyData();

                        } catch (IllegalArgumentException e) {
                            throw new WrongUsageException(e.getMessage());
                        }
                    } else if (args[0].equalsIgnoreCase("copySidebar")) {
                        try {
                            if (args.length >= 2) {
                                DevUtils.setSidebarFormatted(parseBoolean(args[1]));
                            }
                            DevUtils.setCopyMode(DevUtils.CopyMode.SIDEBAR);
                            DevUtils.copyData();

                        } catch (NullPointerException e) {
                            throw new WrongUsageException(Translations.getMessage("commands.errors.wrongUsage.generic"));
                        }
                    } else if (args[0].equalsIgnoreCase("copyTabList")) {
                        DevUtils.setCopyMode(DevUtils.CopyMode.TAB_LIST);
                        DevUtils.copyData();

                    } else if (args[0].equalsIgnoreCase("pd")) {
                        main.getUtils().sendMessage(EnumChatFormatting.BOLD + "Death Counts: ");
                        main.getUtils().sendMessage(EnumChatFormatting.WHITE + "Deaths: " + EnumChatFormatting.GOLD +
                                main.getDungeonManager().getDeaths());
                        main.getUtils().sendMessage(EnumChatFormatting.WHITE + "Alt Deaths: " + EnumChatFormatting.GOLD +
                                main.getDungeonManager().getAlternateDeaths());
                        main.getUtils().sendMessage(EnumChatFormatting.WHITE + "Tab Deaths: " + EnumChatFormatting.GOLD +
                                main.getDungeonManager().getPlayerListInfoDeaths());
                    } else if (args[0].equalsIgnoreCase("reload")) {
                        DevUtils.reloadAll();
                    } else if (args[0].equalsIgnoreCase("reloadConfig")) {
                        DevUtils.reloadConfig();
                    } else if (args[0].equalsIgnoreCase("reloadRes")) {
                        DevUtils.reloadResources();
                    } else if (args[0].equalsIgnoreCase("toggleActionBarLogging")) {
                        DevUtils.setLoggingActionBarMessages(!DevUtils.isLoggingActionBarMessages());

                        if (DevUtils.isLoggingActionBarMessages()) {
                            main.getUtils().sendMessage(ColorCode.GREEN + Translations.getMessage(
                                    "commands.responses.sba.toggleActionBarLogging.enabled"));
                        } else {
                            main.getUtils().sendMessage(ColorCode.RED + Translations.getMessage(
                                    "commands.responses.sba.toggleActionBarLogging.disabled"));
                        }
                    } else if (args[0].equalsIgnoreCase("toggleMagmaTimerLogging")) {
                        DevUtils.setMagmaTimerDebugLoggingEnabled(!DevUtils.isMagmaTimerDebugLoggingEnabled());

                        if (DevUtils.isMagmaTimerDebugLoggingEnabled()) {
                            main.getUtils().sendMessage(ColorCode.GREEN + Translations.getMessage(
                                    "commands.responses.sba.toggleMagmaTimerLogging.enabled"));
                        } else {
                            main.getUtils().sendMessage(ColorCode.RED + Translations.getMessage(
                                    "commands.responses.sba.toggleMagmaTimerLogging.disabled"));
                        }
                    } else {
                        throw new WrongUsageException(Translations.getMessage(
                                "commandUsage.sba.errors.wrongUsage.subCommandNotFound", args[0]));
                    }
                } else {
                    throw new WrongUsageException(Translations.getMessage(
                            "commandUsage.sba.errors.wrongUsage.subCommandNotFound", args[0]));
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
    Returns the Dev prefix in brackets and with formatting codes.
    This simplifies the string for localization to just "Dev".
     */
    private String getDevPrefixFormatted() {
        return "§e(" + Translations.getMessage("commands.usage.sba.dev.prefix") + ")§r ";
    }

    /*
     Gets tab completion options listing all sub-commands.
     Developer mode commands are not included if developer mode is disabled.
     */
    private List<String> getSubCommandTabCompletionOptions(String[] args) {
        if (main.getConfigValues().isEnabled(Feature.DEVELOPER_MODE)) {
            return getListOfStringsMatchingLastWord(args, SUBCOMMANDS);
        } else {
            return getListOfStringsMatchingLastWord(args, Arrays.copyOf(SUBCOMMANDS, 7));
        }
    }

    // This is an Enum representing options used by the sub-commands of this command.
    private enum CommandOption {
        COMMAND("Command", "commands.usage.sba.help.detailedHelp.options.command"),
        ZEALOTS("Zealots", "commands.usage.sba.set.zealotCounter.detailedHelp.options.zealots"),
        EYES("Eyes", "commands.usage.sba.set.zealotCounter.detailedHelp.options.eyes"),
        TOTAL_ZEALOTS("TotalZealots|Total", "commands.usage.sba.set.zealotCounter.detailedHelp.options.totalZealots"),
        FORMATTED("Formatted", "commands.usage.sba.copySidebar.detailedHelp.options.formatted"),
        ENTITY_NAMES("EntityNames", "commands.usage.sba.copyEntity.detailedHelp.options.entityNames"),
        RADIUS("Radius", "commands.usage.sba.copyEntity.detailedHelp.options.radius"),
        SLAYER_BOSS("Boss", "commands.usage.sba.slayer.detailedHelp.options.boss"),
        SLAYER_NUMBER("Number", "commands.usage.sba.slayer.detailedHelp.options.number"),
        SLAYER_STAT("Stat", "commands.usage.sba.slayer.detailedHelp.options.stat"),
        ;

        @Getter
        private final String name;
        private final String descriptionTranslationKey;

        CommandOption(String name, String descriptionTranslationKey) {
            this.name = name;
            this.descriptionTranslationKey = descriptionTranslationKey;
        }

        /**
         * <p>This method returns a formatted string representation of this {@code CommandOption} object for display in a
         * sub-command help prompt. The format is as follows:</p>
         *
         * <i>§b● Option Name §7- Option Description</i>
         *
         * @return a formatted string representation of this {@code CommandOption} object
         */
        @Override
        public String toString() {
            return "§b● " + name + " §7- " + Translations.getMessage(descriptionTranslationKey);
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
        COPY_ENTITY("/sba copyEntity [entityNames] [radius: integer]"),
        COPY_SIDEBAR("/sba copySidebar [formatted: boolean]"),
        COPY_TAB_LIST("/sba copyTabList"),
        TOGGLE_ACTION_BAR_LOGGING("/sba toggleActionBarLogging"),
        SLAYER("/sba slayer <boss> <stat> <number>"),
        COPY_BLOCK("/sba copyBlock"),
        RELOAD("/sba reload"),
        RELOAD_CONFIG("/sba reloadConfig"),
        RELOAD_RES("/sba reloadRes"),
        RESET_ZEALOT_COUNTER("/sba resetZealotCounter"),
        PD("/sba pd"),
        TOGGLE_MAGMA_TIMER_LOGGING("/sba toggleMagmaTimerLogging"),
        VERSION("/sba version")
        ;

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
        HELP(CommandSyntax.HELP, "commands.usage.sba.help.help", Collections.singletonList(CommandOption.COMMAND)),
        EDIT(CommandSyntax.EDIT, "commands.usage.sba.edit.help", null),
        SET(CommandSyntax.SET, "commands.usage.sba.set.zealotCounter.detailedHelp.description", Arrays.asList(CommandOption.ZEALOTS, CommandOption.EYES, CommandOption.TOTAL_ZEALOTS)),
        RESET_ZEALOT_COUNTER(CommandSyntax.RESET_ZEALOT_COUNTER, "commands.usage.sba.resetZealotCounter.help", null),
        FOLDER(CommandSyntax.FOLDER, "commands.usage.sba.folder.help", null),
        DEV(CommandSyntax.DEV, "commands.usage.sba.dev.detailedHelp.description", null),
        BRAND(CommandSyntax.BRAND, "commands.usage.sba.brand.help", null),
        COPY_ENTITY(CommandSyntax.COPY_ENTITY, "commands.usage.sba.copyEntity.detailedHelp.description", Arrays.asList(CommandOption.ENTITY_NAMES, CommandOption.RADIUS)),
        COPY_SIDEBAR(CommandSyntax.COPY_SIDEBAR, "commands.usage.sba.copySidebar.detailedHelp.description", Collections.singletonList(CommandOption.FORMATTED)),
        COPY_TAB_LIST(CommandSyntax.COPY_TAB_LIST, "commands.usage.sba.copyTabList.detailedHelp.description", null),
        TOGGLE_ACTION_BAR_LOGGING(CommandSyntax.TOGGLE_ACTION_BAR_LOGGING, "commands.usage.sba.toggleActionBarLogging.help", null),
        SLAYER(CommandSyntax.SLAYER, "commands.usage.sba.slayer.detailedHelp.description", Arrays.asList(CommandOption.SLAYER_BOSS, CommandOption.SLAYER_STAT, CommandOption.SLAYER_NUMBER)),
        COPY_BLOCK(CommandSyntax.COPY_BLOCK, "commands.usage.sba.copyBlock.help", null),
        RELOAD(CommandSyntax.RELOAD, "commands.usage.sba.reload.help", null),
        RELOAD_CONFIG(CommandSyntax.RELOAD_CONFIG, "commands.usage.sba.reloadConfig.help", null),
        RELOAD_RES(CommandSyntax.RELOAD_RES, "commands.usage.sba.reloadRes.help", null),
        PD(CommandSyntax.PD, "commands.usage.sba.printDeaths.help", null),
        TOGGLE_MAGMA_TIMER_LOGGING(CommandSyntax.TOGGLE_MAGMA_TIMER_LOGGING, "commands.usage.sba.toggleMagmaTimerLogging.help", null),
        VERSION(CommandSyntax.VERSION, "commands.usage.sba.version.help", null)
        ;

        private final CommandSyntax syntax;
        private final String descriptionTranslationKey;
        private final List<CommandOption> options;

        SubCommandUsage(CommandSyntax syntax, String descriptionTranslationKey, List<CommandOption> options) {
            this.syntax = syntax;
            this.descriptionTranslationKey = descriptionTranslationKey;
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
                    "\n§7" + Translations.getMessage(descriptionTranslationKey));

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
