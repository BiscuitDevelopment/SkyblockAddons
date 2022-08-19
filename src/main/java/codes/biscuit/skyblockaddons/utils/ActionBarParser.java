package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Attribute;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.SkillType;
import codes.biscuit.skyblockaddons.core.Translations;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
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
 * Soulflow:                   §b421/421✎ §3100ʬ
 * Tethered + Alignment:      §a1039§a❈ Defense§a |||§a§l  T3!
 * Five stages of healing wand:     §62151/1851❤+§c120▆
 *                                  §62151/1851❤+§c120▅
 *                                  §62151/1851❤+§c120▄
 *                                  §62151/1851❤+§c120▃
 *                                  §62151/1851❤+§c120▂
 *                                  §62151/1851❤+§c120▁
 * <p>
 * To add something new to parse, add an else-if case in {@link #parseActionBar(String)} to call a method that
 * parses information from that section.
 */

@Getter
public class ActionBarParser {

    private static final Pattern COLLECTIONS_CHAT_PATTERN = Pattern.compile("\\+(?<gained>[0-9,.]+) (?<skillName>[A-Za-z]+) (?<progress>\\((((?<current>[0-9.,kM]+)/(?<total>[0-9.,kM]+))|((?<percent>[0-9.,]+)%))\\))");
    private static final Pattern SKILL_GAIN_PATTERN_S = Pattern.compile("\\+(?<gained>[0-9,.]+) (?<skillName>[A-Za-z]+) (?<progress>\\((((?<current>[0-9.,]+)/(?<total>[0-9.,]+))|((?<percent>[0-9.]+)%))\\))");
    private static final Pattern MANA_PATTERN_S = Pattern.compile("(?<num>[0-9,.]+)/(?<den>[0-9,.]+)✎(| Mana| (?<overflow>-?[0-9,.]+)ʬ)");
    private static final Pattern DEFENSE_PATTERN_S = Pattern.compile("(?<defense>[0-9,.]+)❈ Defense(?<other>( (?<align>\\|\\|\\|))?( {2}(?<tether>T[0-9,.]+!?))?.*)?");
    private static final Pattern HEALTH_PATTERN_S =Pattern.compile("(?<health>[0-9,.]+)/(?<maxHealth>[0-9,.]+)❤(?<wand>\\+(?<wandHeal>[0-9,.]+)[▆▅▄▃▂▁])?");


    private static final SkyblockAddons main = SkyblockAddons.getInstance();
    private static final Logger logger = SkyblockAddons.getLogger();

    /**
     * The amount of usable tickers or -1 if none are in the action bar.
     */
    private int tickers = -1;

    /**
     * The total amount of possible tickers or 0 if none are in the action bar.
     */
    private int maxTickers = 0;
    @Setter
    private float lastSecondHealth = -1;
    @Setter
    private Float healthUpdate;
    @Setter
    private long lastHealthUpdate;

    private float currentSkillXP;
    private int totalSkillXP;
    private float percent;
    private boolean healthLock;
    private String otherDefense;

    /** The skill section that was parsed from the last action bar message */
    private String lastParsedSkillSection = "";
    /** The string that was displayed on the skill progress display for the last action bar message */
    private String lastSkillProgressString;
    /** The skill type parsed from the last action bar message */
    private SkillType lastSkillType;

    private final LinkedList<String> stringsToRemove = new LinkedList<>();

    public ActionBarParser() {
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
        stringsToRemove.clear();

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
                } else {
                    // Remove via callback
                    stringsToRemove.add(section);
                }
            } catch(Exception ex) {
                unusedSections.add(section);
            }
        }

        // Finally, display all unused sections separated by 5 spaces again
        return String.join(StringUtils.repeat(" ", 5), unusedSections);
    }

    /**
     * Parses a single section of the action bar.
     *
     * @param section Section to parse
     * @return Text to keep displaying or null
     */
    private String parseSection(String section) {
        String stripColoring = TextUtils.stripColor(section);
        String convertMag;

        try {
            convertMag = TextUtils.convertMagnitudes(stripColoring);

            // Format for overflow mana is a bit different. Splitstats must parse out overflow first before getting numbers
            if (section.contains("ʬ")) {
                convertMag = convertMag.split(" ")[0];
            }
            String numbersOnly = TextUtils.getNumbersOnly(convertMag).trim(); // keeps numbers and slashes
            String[] splitStats = numbersOnly.split("/");

            if (section.contains("❤")) {
                // ❤ indicates a health section
                return parseHealth(section);
            } else if (section.contains("❈")) {
                // ❈ indicates a defense section
                return parseDefense(section);
            } else if (section.contains("✎")) {
                return parseMana(section);
            } else if (section.contains("(")) {
                return parseSkill(convertMag);
            } else if (section.contains("Ⓞ") || section.contains("ⓩ")) {
                return parseTickers(section);
            } else if (section.contains("Drill")) {
                return parseDrill(section, splitStats);
            }
        } catch (ParseException e) {
            logger.error("The section \"" + section + "\" will be skipped due to an error during number parsing.");
            logger.error("Failed to parse number at offset " + e.getErrorOffset() + " in string \"" + e.getMessage() + "\".", e);
            return section;
        }

        return section;
    }

    /**
     * Parses the health section and sets the read values as attributes in {@link Utils}.
     * Returns the healing indicator if a healing Wand is active.
     *
     * @param healthSection Health section of the action bar
     * @return null or Wand healing indicator or {@code healthSection} if neither health bar nor health text are enabled
     */
    private String parseHealth(String healthSection) {
        // Normal:      §c1390/1390❤
        // With Wand:   §c1390/1390❤+§c30▅
        final boolean separateDisplay = main.getConfigValues().isEnabled(Feature.HEALTH_BAR) || main.getConfigValues().isEnabled(Feature.HEALTH_TEXT);
        String returnString = healthSection;
        float newHealth;
        float maxHealth;
        String stripped = TextUtils.stripColor(healthSection);
        Matcher m = HEALTH_PATTERN_S.matcher(stripped);
        if (separateDisplay && m.matches()) {
            newHealth = parseFloat(m.group("health"));
            maxHealth = parseFloat(m.group("maxHealth"));
            if (m.group("wand") != null) {
                // Jank way of doing this for now
                returnString = "";// "§c"+ m.group("wand");
                stringsToRemove.add(stripped.substring(0, m.start("wand")));
            } else {
                returnString = null;
            }
            healthLock = false;
            boolean postSetLock = main.getUtils().getAttributes().get(Attribute.MAX_HEALTH).getValue() != maxHealth ||
                    (Math.abs(main.getUtils().getAttributes().get(Attribute.HEALTH).getValue() - newHealth) / maxHealth) > .05;
            setAttribute(Attribute.HEALTH, newHealth);
            setAttribute(Attribute.MAX_HEALTH, maxHealth);
            healthLock = postSetLock;
        }
        return returnString;
    }

    /**
     * Parses the mana section and sets the read values as attributes in {@link Utils}.
     *
     * @param manaSection Mana section of the action bar
     * @return null or {@code manaSection} if neither mana bar nor mana text are enabled
     */
    private String parseMana(String manaSection) {
        // 183/171✎ Mana
        // 421/421✎ 10ʬ
        // 421/421✎ -10ʬ
        Matcher m = MANA_PATTERN_S.matcher(TextUtils.stripColor(manaSection).trim());
        if (m.matches()) {
            setAttribute(Attribute.MANA, parseFloat(m.group("num")));
            setAttribute(Attribute.MAX_MANA, parseFloat(m.group("den")));
            float overflowMana = 0;
            if (m.group("overflow") != null) {
                overflowMana = parseFloat(m.group("overflow"));
            }
            setAttribute(Attribute.OVERFLOW_MANA, overflowMana);
            main.getRenderListener().setPredictMana(false);
            if (main.getConfigValues().isEnabled(Feature.MANA_BAR) || main.getConfigValues().isEnabled(Feature.MANA_TEXT)) {
                return null;
            }
        }
        return manaSection;
    }

    /**
     * Parses the defense section and sets the read values as attributes in {@link Utils}.
     *
     * @param defenseSection Defense section of the action bar
     * @return null or {@code defenseSection} if neither defense text nor defense percentage are enabled
     */
    private String parseDefense(String defenseSection) {
        // §a720§a❈ Defense
        // Tethered T1 (Dungeon Healer)--means tethered to 1 person I think: §a1024§a? Defense§6  T1
        // Tethered T3! (Dungeon Healer)--not sure why exclamation mark: §a1039§a? Defense§a§l  T3!
        // Tethered T3! (Dungeon Healer) + Aligned ||| (Gyrokinetic Wand): §a1039§a? Defense§a |||§a§l  T3!
        String stripped = TextUtils.stripColor(defenseSection);
        Matcher m = DEFENSE_PATTERN_S.matcher(stripped);
        if (m.matches()) {
            float defense = parseFloat(m.group("defense"));
            setAttribute(Attribute.DEFENCE, defense);
            otherDefense = TextUtils.getFormattedString(defenseSection, m.group("other").trim());
            if (main.getConfigValues().isEnabled(Feature.DEFENCE_TEXT) || main.getConfigValues().isEnabled(Feature.DEFENCE_PERCENTAGE)) {
                return null;
            }
        }
        return defenseSection;
    }

    /**
     * Parses the skill section and displays the skill progress gui element.
     * If the skill section provided is the same as the one from the last action bar message, then the last output is
     * displayed.
     *
     * <p>
     * <b>Example Skill Section Messages</b>
     * <p>
     * §3+10.9 Combat (313,937.1/600,000)
     * <p>
     * Another Example: §5+§d30 §5Runecrafting (969/1000)
     * <p>
     * Percent: §3+2 Farming (1.01%)
     * <p>
     * Percent without decimal: §3+2 Farming (1%)
     * <p>
     * Maxed out skill: §5+§d60 §5Runecrafting (118,084/0)
     *
     * @param skillSection Skill XP section of the action bar
     * @return {@code null} or {@code skillSection} if wrong format or skill display is disabled
     */
    private String parseSkill(String skillSection) throws ParseException {
        if (main.getConfigValues().isEnabled(Feature.SKILL_DISPLAY) || main.getConfigValues().isEnabled(Feature.SKILL_PROGRESS_BAR)) {
            Matcher matcher = SKILL_GAIN_PATTERN_S.matcher(TextUtils.stripColor(skillSection));
            NumberFormat nf = NumberFormat.getInstance(Locale.US);
            StringBuilder skillTextBuilder = new StringBuilder();
            SkillType skillType = null;

            nf.setMaximumFractionDigits(2);

            if (lastParsedSkillSection.equals(skillSection)) {
                skillTextBuilder.append(lastSkillProgressString);
                skillType = lastSkillType;
            } else if (matcher.matches()) {

                if (main.getConfigValues().isEnabled(Feature.SHOW_SKILL_XP_GAINED)) {
                    skillTextBuilder.append("+").append(matcher.group("gained"));
                }

                skillType = SkillType.getFromString(matcher.group("skillName"));

                boolean skillPercent = matcher.group("percent") != null;
                boolean parseCurrAndTotal = true;
                if (skillPercent) {
                    percent = nf.parse(matcher.group("percent")).floatValue();
                    int skillLevel = main.getSkillXpManager().getSkillLevel(skillType);
                    // Try to re-create xxx/xxx display
                    if (skillLevel != -1) {
                        totalSkillXP = main.getSkillXpManager().getSkillXpForNextLevel(skillType, skillLevel);
                        currentSkillXP = totalSkillXP * percent / 100;
                    } else {
                        parseCurrAndTotal = false;
                    }
                } else {
                    currentSkillXP = nf.parse(matcher.group("current")).floatValue();
                    totalSkillXP = nf.parse(matcher.group("total")).intValue();
                    percent = totalSkillXP == 0 ? 100F : 100F * currentSkillXP / totalSkillXP;
                }
                percent = Math.min(100, percent);


                if (!parseCurrAndTotal || main.getConfigValues().isEnabled(Feature.SHOW_SKILL_PERCENTAGE_INSTEAD_OF_XP)) {
                    // We may only have the percent at this point
                    skillTextBuilder.append(" (").append(String.format("%.2f", percent)).append("%)");
                } else {
                    // Append "(currentXp/totalXp)"
                    skillTextBuilder.append(" (").append(nf.format(currentSkillXP));
                    // Only print the total when it doesn't = 0
                    if (totalSkillXP != 0) {
                        skillTextBuilder.append("/");
                        if (main.getConfigValues().isEnabled(Feature.ABBREVIATE_SKILL_XP_DENOMINATOR)) {
                            skillTextBuilder.append(TextUtils.abbreviate(totalSkillXP));
                        } else {
                            skillTextBuilder.append(nf.format(totalSkillXP));
                        }
                    }
                    skillTextBuilder.append(")");
                }

                // This feature is only accessible when we have parsed the current and total skill xp
                if (parseCurrAndTotal && main.getConfigValues().isEnabled(Feature.SKILL_ACTIONS_LEFT_UNTIL_NEXT_LEVEL)) {
                    float gained = nf.parse(matcher.group("gained")).floatValue();

                    skillTextBuilder.append(" - ");

                    if (percent != 100) {
                        if (gained != 0) {
                            skillTextBuilder.append(Translations.getMessage("messages.actionsLeft", (int) Math.ceil((totalSkillXP - currentSkillXP) / gained)));
                        } else {
                            skillTextBuilder.append(Translations.getMessage("messages.actionsLeft", "∞"));
                        }
                    }
                }

                lastParsedSkillSection = skillSection;
                lastSkillProgressString = skillTextBuilder.toString();
                lastSkillType = skillType;
            }

            if (skillTextBuilder.length() != 0) {
                main.getRenderListener().setSkillText(skillTextBuilder.toString());
                main.getRenderListener().setSkill(skillType);
                main.getRenderListener().setSkillFadeOutTime(System.currentTimeMillis() + 4000);
                if (main.getConfigValues().isEnabled(Feature.SKILL_DISPLAY)) {
                    return null;
                }
            }
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
     * Parses the drill section
     *
     * @param drillSection Drill fuel section of the action bar
     * @return null or {@code drillSection} if wrong format or drill display is disabled
     */
    private String parseDrill(String drillSection, String[] splitStats) {
        // §21,798/3k Drill Fuel§r
        // splitStats should convert into [1798, 3000]
        int fuel = Math.max(0, Integer.parseInt(splitStats[0]));
        int maxFuel = Math.max(1, Integer.parseInt(splitStats[1]));
        setAttribute(Attribute.FUEL, fuel);
        setAttribute(Attribute.MAX_FUEL, maxFuel);
        if (main.getConfigValues().isEnabled(Feature.DRILL_FUEL_BAR) || main.getConfigValues().isEnabled(Feature.DRILL_FUEL_TEXT)) {
            return null;
        } else {
            return drillSection;
        }
    }

    /**
     * Parses a float from a given string.
     *
     * @param string the string to parse
     * @return the parsed float or `-1` if parsing was unsuccessful
     */
    private float parseFloat(String string) {
        try {
            return TextUtils.NUMBER_FORMAT.parse(string).floatValue();
        } catch (ParseException e) {
            return -1;
        }
    }

    /**
     * Sets an attribute in {@link Utils}
     * Ignores health if it's locked
     *
     * @param attribute Attribute
     * @param value     Attribute value
     */
    private void setAttribute(Attribute attribute, float value) {
        if (attribute == Attribute.HEALTH && healthLock) return;
        main.getUtils().getAttributes().get(attribute).setValue(value);
    }
}
