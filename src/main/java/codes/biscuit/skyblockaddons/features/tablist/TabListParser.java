package codes.biscuit.skyblockaddons.features.tablist;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.features.tabtimers.TabEffectManager;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TabListParser {

    private static final SkyblockAddons main = SkyblockAddons.getInstance();

    public static String HYPIXEL_ADVERTISEMENT_CONTAINS = "HYPIXEL.NET";

    //private static final Pattern GOD_POTION_PATTERN = Pattern.compile("You have a God Potion active! (?<timer>\\d{0,2}:?\\d{1,2}:\\d{2})");
    private static final Pattern ACTIVE_EFFECTS_PATTERN = Pattern.compile("Active Effects(?:§.)*(?:\\n(?:§.)*§7.+)*");
    private static final Pattern COOKIE_BUFF_PATTERN = Pattern.compile("Cookie Buff(?:§.)*(?:\\n(§.)*§7.+)*");
    private static final Pattern UPGRADES_PATTERN = Pattern.compile("(?<firstPart>§e[A-Za-z ]+)(?<secondPart> §f[0-9dhms ]+)");

    @Getter
    private static List<RenderColumn> renderColumns;

    public static void parse() {
        Minecraft mc = Minecraft.getMinecraft();

        if (!main.getUtils().isOnSkyblock() || !main.getConfigValues().isEnabled(Feature.COMPACT_TAB_LIST)) {
            renderColumns = null;
            return;
        }

        if (mc.thePlayer == null || mc.thePlayer.sendQueue == null) {
            renderColumns = null;
            return;
        }

        NetHandlerPlayClient netHandler = mc.thePlayer.sendQueue;
        List<NetworkPlayerInfo> fullList = GuiPlayerTabOverlay.field_175252_a.sortedCopy(netHandler.getPlayerInfoMap());
        if (fullList.size() < 80) {
            renderColumns = null;
            return;
        }
        fullList = fullList.subList(0, 80);


        // Parse into columns, combining any duplicate columns
        List<ParsedTabColumn> columns = parseColumns(fullList);

        columns.add(parseFooterAsColumn());

        // Parse every column into sections
        parseSections(columns);

        // Combine columns into how they will be rendered
        renderColumns = new LinkedList<>();
        RenderColumn renderColumn = new RenderColumn();
        renderColumns.add(renderColumn);
        combineColumnsToRender(columns, renderColumn);
    }

    public static ParsedTabColumn getColumnFromName(List<ParsedTabColumn> columns, String name) {
        for (ParsedTabColumn parsedTabColumn : columns) {
            if (name.equals(parsedTabColumn.getTitle())) {
                return parsedTabColumn;
            }
        }

        return null;
    }

    private static List<ParsedTabColumn> parseColumns(List<NetworkPlayerInfo> fullList) {
        GuiPlayerTabOverlay tabList = Minecraft.getMinecraft().ingameGUI.getTabList();

        List<ParsedTabColumn> columns = new LinkedList<>();
        for (int entry = 0; entry < fullList.size(); entry += 20) {
            String title = TextUtils.trimWhitespaceAndResets(tabList.getPlayerName(fullList.get(entry)));
            ParsedTabColumn column = getColumnFromName(columns, title);
            if (column == null) {
                column = new ParsedTabColumn(title);
                columns.add(column);
            }

            for (int columnEntry = entry + 1; columnEntry < fullList.size() && columnEntry < entry + 20; columnEntry++) {
                column.addLine(tabList.getPlayerName(fullList.get(columnEntry)));
            }
        }

        return columns;
    }

    public static ParsedTabColumn parseFooterAsColumn() {
        GuiPlayerTabOverlay tabList = Minecraft.getMinecraft().ingameGUI.getTabList();

        ParsedTabColumn column = new ParsedTabColumn("§2§lOther");

        String footer = tabList.footer.getFormattedText();
        //System.out.println(footer);

        // Make active effects/booster cookie status compact...
        Matcher m = Pattern.compile("You have a God Potion active! (?<timer>\\d{0,2}:?\\d{1,2}:\\d{2})").matcher(tabList.footer.getUnformattedText());
        if (m.find()) {
            footer = ACTIVE_EFFECTS_PATTERN.matcher(footer).replaceAll("Active Effects: §r§e" + TabEffectManager.getInstance().getEffectCount() + "\n§cGod Potion§r: " + m.group("timer"));
        } else {
            footer = ACTIVE_EFFECTS_PATTERN.matcher(footer).replaceAll("Active Effects: §r§e" + TabEffectManager.getInstance().getEffectCount());
        }

        Matcher matcher = COOKIE_BUFF_PATTERN.matcher(footer);
        if (matcher.matches() && matcher.group().contains("Not active!")) {
            footer = matcher.replaceAll("Cookie Buff \n§r§7Not Active");
        }

        for (String line : new ArrayList<>(Arrays.asList(footer.split("\n")))) {
            // Lets not add the advertisements to the columns
            if (line.contains(HYPIXEL_ADVERTISEMENT_CONTAINS)) {
                continue;
            }

            // Split every upgrade into 2 lines so it's not too long...
            matcher = UPGRADES_PATTERN.matcher(TextUtils.stripResets(line));
            if (matcher.matches()) {
                // Adds a space in front of any text that is not a sub-title
                String firstPart = TextUtils.trimWhitespaceAndResets(matcher.group("firstPart"));
                if (!firstPart.contains("§l")) {
                    firstPart = " " + firstPart;
                }
                column.addLine(firstPart);

                line = matcher.group("secondPart");
            }
            // Adds a space in front of any text that is not a sub-title
            line = TextUtils.trimWhitespaceAndResets(line);
            if (!line.contains("§l")) {
                line = " " + line;
            }

            column.addLine(line);
        }

        return column;
    }

    public static void parseSections(List<ParsedTabColumn> columns) {
        for (ParsedTabColumn column : columns) {
            ParsedTabSection currentSection = null;
            for (String line : column.getLines()) {

                // Empty lines reset the current section
                if (TextUtils.trimWhitespaceAndResets(line).isEmpty()) {
                    currentSection = null;
                    continue;
                }

                if (currentSection == null) {
                    column.addSection(currentSection = new ParsedTabSection(column));
                }

                currentSection.addLine(line);
            }
        }
    }

    @SuppressWarnings("StringEquality")
    public static void combineColumnsToRender(List<ParsedTabColumn> columns, RenderColumn initialColumn) {
        String lastTitle = null;
        for (ParsedTabColumn column : columns) {
            for (ParsedTabSection section : column.getSections()) {
                int sectionSize = section.size();

                // Check if we need to add the column title before this section
                boolean needsTitle = false;
                if (lastTitle != section.getColumn().getTitle()) {
                    needsTitle = true;
                    sectionSize++;
                }

                int currentCount = initialColumn.size();

                // The section is larger than max lines, we need to overflow
                if (sectionSize >= TabListRenderer.MAX_LINES / 2) { // TODO Double check this?

                    // If we are already at the max, we must start a new
                    // column so the title isn't by itself
                    if (currentCount >= TabListRenderer.MAX_LINES) {
                        renderColumns.add(initialColumn = new RenderColumn());
                        currentCount = 1;
                    } else {
                        // Add separator between sections, because there will be text above
                        if (initialColumn.size() > 0) {
                            initialColumn.addLine(new TabLine("", TabStringType.TEXT));
                        }
                    }

                    // Add the title first
                    if (needsTitle) {
                        lastTitle = section.getColumn().getTitle();
                        initialColumn.addLine(new TabLine(lastTitle, TabStringType.TITLE));
                        currentCount++;
                    }

                    // Add lines 1 by 1, checking whether the count goes over the maximum.
                    // If it does go over the maximum add a new column
                    for (String line : section.getLines()) {
                        if (currentCount >= TabListRenderer.MAX_LINES) {
                            renderColumns.add(initialColumn = new RenderColumn());
                            currentCount = 1;
                        }

                        initialColumn.addLine(new TabLine(line, TabStringType.fromLine(line)));
                        currentCount++;
                    }
                } else {
                    // This section will cause this column to go over the max, so let's
                    // move on to the next column
                    if (currentCount + sectionSize > TabListRenderer.MAX_LINES) {
                        renderColumns.add(initialColumn = new RenderColumn());
                    } else {
                        // Add separator between sections, because there will be text above
                        if (initialColumn.size() > 0) {
                            initialColumn.addLine(new TabLine("", TabStringType.TEXT));
                        }
                    }

                    // Add the title first
                    if (needsTitle) {
                        lastTitle = section.getColumn().getTitle();
                        initialColumn.addLine(new TabLine(lastTitle, TabStringType.TITLE));
                    }

                    // And then add all the lines
                    for (String line : section.getLines()) {
                        initialColumn.addLine(new TabLine(line, TabStringType.fromLine(line)));
                    }
                }
            }
        }
    }
}
