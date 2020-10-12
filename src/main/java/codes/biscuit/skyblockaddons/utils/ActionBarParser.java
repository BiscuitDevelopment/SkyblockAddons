package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Attribute;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.misc.scheduler.Scheduler;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to parse action bar messages and get stats and other info out of them.
 * Parses things like health, defense, mana, skill xp, item ability tickers and
 * if they are displayed else where by SBA, removes them from the action bar.
 * <p>
 * Action bars can take many shapes, but they're always divided into sections separated by 3 or more spaces
 * (usually 5, zombie tickers by 4, race timer by 12, trials of fire by 3).
 * Here are some examples:
 * <p>
 * Normal:                     §c1390/1390❤     §a720§a❈ Defense     §b183/171✎ Mana§r
 * Normal with Skill XP:       §c1390/1390❤     §3+10.9 Combat (313,937.1/600,000)     §b183/171✎ Mana§r
 * Zombie Sword:               §c1390/1390❤     §a725§a❈ Defense     §b175/233✎ Mana    §a§lⓩⓩⓩⓩ§2§l§r
 * Zombie Sword with Skill XP: §c1390/1390❤     §3+10.9 Combat (313,948/600,000)     §b187/233✎ Mana    §a§lⓩⓩⓩⓩ§2§l§r
 * Normal with Wand:           §c1390/1390❤+§c30▅     §a724§a❈ Defense     §b97/171✎ Mana§r
 * Normal with Absorption:     §61181/1161❤     §a593§a❈ Defense     §b550/550✎ Mana§r
 * Normal with Absorp + Wand:  §61181/1161❤+§c20▆     §a593§a❈ Defense     §b501/550✎ Mana§r
 * End Race:                   §d§lTHE END RACE §e00:52.370            §b147/147✎ Mana§r
 * Woods Race:                 §A§LWOODS RACING §e00:31.520            §b147/147✎ Mana§r
 * Trials of Fire:             §c1078/1078❤   §610 DPS   §c1 second     §b421/421✎ Mana§r
 * <p>
 * To add something new to parse, add an else-if case in {@link #parseActionBar(String)} to call a method that
 * parses information from that section.
 */

@Getter
public class ActionBarParser {

    private Pattern COLLECTIONS_CHAT_PATTERN = Pattern.compile("\\+(?<gained>[0-9,.]+) (?<skillName>[A-Za-z]+) (?<progress>\\((?<current>[0-9.,]+)/(?<total>[0-9.,]+)\\))");

    private final SkyblockAddons main;

    /** The amount of usable tickers or -1 if none are in the action bar. */
    private int tickers = -1;

    /** The total amount of possible tickers or 0 if none are in the action bar. */
    private int maxTickers = 0;
    @Setter private int lastSecondHealth = -1;
    @Setter private Integer healthUpdate;
    @Setter private long lastHealthUpdate;

    private float currentSkillXP;
    private int totalSkillXP;

    public ActionBarParser() {
        this.main = SkyblockAddons.getInstance();
    }

    /**
     * Parses the stats out of an action bar message and returns a new action bar message without the parsed stats
     * to display instead.
     * Looks for Health, Defense, Mana, Skill XP and parses and uses the stats accordingly.
     * Only removes the stats from the new action bar when their separate display features are enabled.
     *
     * @param actionBar Formatted action bar message
     * @return New action bar without parsed stats.
     */
    public String parseActionBar(String actionBar) {
        // First split the action bar into sections
        String[] splitMessage = actionBar.split(" {3,}");
        // This list holds the text of unused sections that aren't displayed anywhere else in SBA
        // so they can keep being displayed in the action bar
        List<String> unusedSections = new LinkedList<>();

        // health and mana section methods determine if prediction can be disabled, so enable both at first
        main.getRenderListener().setPredictMana(true);
        main.getRenderListener().setPredictHealth(true);
        // set ticker to -1 so the GUI element doesn't get displayed while they're not displayed in the action bar
        tickers = -1;

        // If the action bar is displaying player stats and the defense section is absent, the player's defense is zero.
        if (actionBar.contains("❤") && !actionBar.contains("❈") && splitMessage.length == 2) {
            setAttribute(Attribute.DEFENCE, 0);
        }

        for (String section : splitMessage) {
            try {
                String sectionReturn = parseSection(section);
                if (sectionReturn != null) {
                    // can either return a string to keep displaying in the action bar
                    // or null to not display them anymore
                    unusedSections.add(sectionReturn);
                }
            } catch(Exception ex) {
                unusedSections.add(section);
            }
        }

        // Finally display all unused sections separated by 5 spaces again
        return String.join(StringUtils.repeat(" ", 5), unusedSections);
    }

    /**
     * Parses a single section of the action bar.
     *
     * @param section Section to parse
     * @return Text to keep displaying or null
     */
    private String parseSection(String section) {
        String numbersOnly = TextUtils.getNumbersOnly(section).trim(); // keeps numbers and slashes
        String[] splitStats = numbersOnly.split("/");

        if (section.contains("❤")) {
            // ❤ indicates a health section
            return parseHealth(section, splitStats);
        } else if (section.contains("❈")) {
            // ❈ indicates a defense section
            return parseDefense(section, numbersOnly);
        } else if (section.contains("✎")) {
            return parseMana(section, splitStats);
        } else if (section.contains("(")) {
            return parseSkill(section);
        } else if (section.contains("Ⓞ") || section.contains("ⓩ")) {
            return parseTickers(section);
        }

        return section;
    }

    /**
     * Parses the health section and sets the read values as attributes in {@link Utils}.
     * Returns the healing indicator if a healing Wand is active.
     *
     * @param healthSection Health section of the action bar
     * @param splitStats Pre-split stat strings
     * @return null or Wand healing indicator or {@code healthSection} if neither health bar nor health text are enabled
     */
    private String parseHealth(String healthSection, String[] splitStats) {
        // Normal:      §c1390/1390❤
        // With Wand:   §c1390/1390❤+§c30▅
        final boolean separateDisplay = main.getConfigValues().isEnabled(Feature.HEALTH_BAR) || main.getConfigValues().isEnabled(Feature.HEALTH_TEXT);
        String returnString = healthSection;
        int newHealth;
        int maxHealth;
        if (healthSection.startsWith("§6")) { // Absorption chances §c to §6. Remove §6 to make sure it isn't detected as a number of health.
            healthSection = healthSection.substring(2);
            splitStats[0] = splitStats[0].substring(1); // One less because the '§' was already removed.
        }
        if (healthSection.contains("+")) {
            // Contains the Wand indicator so it has to be split differently
            String[] splitHealthAndWand = healthSection.split("\\+");
            String[] healthSplit = TextUtils.getNumbersOnly(splitHealthAndWand[0]).split("/");
            newHealth = Integer.parseInt(healthSplit[0]);
            maxHealth = Integer.parseInt(healthSplit[1]);
            if (separateDisplay) {
                // Return +30▅ for example
                returnString = "§c+" + splitHealthAndWand[1];
            }
        } else {
            newHealth = Integer.parseInt(splitStats[0]);
            maxHealth = Integer.parseInt(splitStats[1]);
            if (separateDisplay) {
                returnString = null;
            }
        }
        main.getScheduler().schedule(Scheduler.CommandType.SET_LAST_SECOND_HEALTH, 1, newHealth);
        if (lastSecondHealth != -1 && lastSecondHealth != newHealth) {
            healthUpdate = newHealth - lastSecondHealth;
            lastHealthUpdate = System.currentTimeMillis();
        }
        setAttribute(Attribute.HEALTH, newHealth);
        setAttribute(Attribute.MAX_HEALTH, maxHealth);
        main.getRenderListener().setPredictHealth(false);
        return returnString;
    }

    /**
     * Parses the mana section and sets the read values as attributes in {@link Utils}.
     *
     * @param manaSection Mana section of the action bar
     * @param splitStats Pre-split stat strings
     * @return null or {@code manaSection} if neither mana bar nor mana text are enabled
     */
    private String parseMana(String manaSection, String[] splitStats) {
        // §b183/171✎ Mana§r
        int mana = Integer.parseInt(splitStats[0]);
        int maxMana = Integer.parseInt(splitStats[1]);
        setAttribute(Attribute.MANA, mana);
        setAttribute(Attribute.MAX_MANA, maxMana);
        main.getRenderListener().setPredictMana(false);
        if (main.getConfigValues().isEnabled(Feature.MANA_BAR) || main.getConfigValues().isEnabled(Feature.MANA_TEXT)) {
            return null;
        } else {
            return manaSection;
        }
    }

    /**
     * Parses the defense section and sets the read values as attributes in {@link Utils}.
     *
     * @param defenseSection Defense section of the action bar
     * @param numbersOnly Pre-split stat string
     * @return null or {@code defenseSection} if neither defense text nor defense percentage are enabled
     */
    private String parseDefense(String defenseSection, String numbersOnly) {
        // §a720§a❈ Defense
        int defense = Integer.parseInt(numbersOnly);
        setAttribute(Attribute.DEFENCE, defense);
        if (main.getConfigValues().isEnabled(Feature.DEFENCE_TEXT) || main.getConfigValues().isEnabled(Feature.DEFENCE_PERCENTAGE)) {
            return null;
        } else {
            return defenseSection;
        }
    }

    /**
     * Parses the skill section and display the skill progress gui element
     *
     * @param skillSection Skill XP section of the action bar
     * @return null or {@code skillSection} if wrong format or skill display is disabled
     */
    private String parseSkill(String skillSection) {
        COLLECTIONS_CHAT_PATTERN = Pattern.compile("\\+(?<gained>[0-9,.]+) (?<skillName>[A-Za-z]+) (?<progress>\\((?<current>[0-9.,]+)/(?<total>[0-9.,]+)\\))");

        // §3+10.9 Combat (313,937.1/600,000)
        // Another Example: §5+§d30 §5Runecrafting (969/1000)
        Matcher matcher = COLLECTIONS_CHAT_PATTERN.matcher(TextUtils.stripColor(skillSection));
        if (matcher.matches() && main.getConfigValues().isEnabled(Feature.SKILL_DISPLAY)) {
            StringBuilder skillTextBuilder = new StringBuilder();

            if (main.getConfigValues().isEnabled(Feature.SHOW_SKILL_XP_GAINED)) {
                skillTextBuilder.append("+").append(matcher.group("gained"));
            }

            float gained = Float.parseFloat(matcher.group("gained").replaceAll(",", ""));
            currentSkillXP = Float.parseFloat(matcher.group("current").replaceAll(",", ""));
            totalSkillXP = Integer.parseInt(matcher.group("total").replaceAll(",", ""));

            if (main.getConfigValues().isEnabled(Feature.SHOW_SKILL_PERCENTAGE_INSTEAD_OF_XP)) {
                skillTextBuilder.append(" ").append(String.format("%.2f", currentSkillXP / (float) totalSkillXP * 100F)).append("%");
            } else {
                skillTextBuilder.append(" ").append(matcher.group("progress"));
            }

            if (main.getConfigValues().isEnabled(Feature.SKILL_ACTIONS_LEFT_UNTIL_NEXT_LEVEL)) {
                if (totalSkillXP != 0) { // 0 means it's maxed...
                    skillTextBuilder.append(" - ").append(Translations.getMessage("messages.actionsLeft", (int) Math.ceil((totalSkillXP - currentSkillXP) / gained)));
                }
            }

            main.getRenderListener().setSkillText(skillTextBuilder.toString());
            main.getRenderListener().setSkill(EnumUtils.SkillType.getFromString(matcher.group("skillName")));
            main.getRenderListener().setSkillFadeOutTime(System.currentTimeMillis() + 4000);
            return null;
        }
        return skillSection;
    }

    /**
     * Parses the ticker section and updates {@link #tickers} and {@link #maxTickers} accordingly.
     * {@link #tickers} being usable tickers and {@link #maxTickers} being the total amount of possible tickers.
     *
     * @param tickerSection Ticker section of the action bar
     * @return null or {@code tickerSection} if the ticker display is disabled
     */
    private String parseTickers(String tickerSection) {
        // Zombie with full charges: §a§lⓩⓩⓩⓩ§2§l§r
        // Zombie with one used charges: §a§lⓩⓩⓩ§2§lⓄ§r
        // Scorpion tickers: §e§lⓄⓄⓄⓄ§7§l§r
        // Ornate: §e§lⓩⓩⓩ§6§lⓄⓄ§r

        // Zombie uses ⓩ with color code a for usable charges, Ⓞ with color code 2 for unusable
        // Scorpion uses Ⓞ with color code e for usable tickers, Ⓞ with color code 7 for unusable
        // Ornate uses ⓩ with color code e for usable charges, Ⓞ with color code 6 for unusable
        tickers = 0;
        maxTickers = 0;
        boolean hitUnusables = false;
        for (char character : tickerSection.toCharArray()) {
            if (!hitUnusables && (character == '7' || character == '2' || character == '6')) {
                // While the unusable tickers weren't hit before and if it reaches a grey(scorpion) or dark green(zombie)
                // or gold (ornate) color code, it means those tickers are used, so stop counting them.
                hitUnusables = true;
            } else if (character == 'Ⓞ' || character == 'ⓩ') { // Increase the ticker counts
                if (!hitUnusables) {
                    tickers++;
                }
                maxTickers++;
            }
        }
        if(main.getConfigValues().isEnabled(Feature.TICKER_CHARGES_DISPLAY)) {
            return null;
        } else {
            return tickerSection;
        }
    }

    /**
     * Sets an attribute in {@link Utils}
     *
     * @param attribute Attribute
     * @param value Attribute value
     */
    private void setAttribute(Attribute attribute, int value) {
        main.getUtils().getAttributes().get(attribute).setValue(value);
    }
}
