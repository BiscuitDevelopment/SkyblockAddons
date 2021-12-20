package codes.biscuit.skyblockaddons.core;

import codes.biscuit.skyblockaddons.utils.TextUtils;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a date (excluding the year) and time in Skyblock.
 * <p>
 * <p>Examples:</p>
 * <p>Spring 28th</p>
 * <p>5:20pm ☀</p>
 * <p>
 * <p>Spring 28th</p>
 * <p>9:10pm ☽</p>
 */
public class SkyblockDate {

    private static final Pattern DATE_PATTERN = Pattern.compile("(?<month>[\\w ]+) (?<day>\\d{1,2})(?:th|st|nd|rd)");
    private static final Pattern TIME_PATTERN = Pattern.compile("(?<hour>\\d{1,2}):(?<minute>\\d\\d)(?<period>am|pm)");

    public static SkyblockDate parse(String dateString, String timeString) {
        if(dateString == null || timeString == null) {
            return null;
        }

        Matcher dateMatcher = DATE_PATTERN.matcher(dateString.trim());
        Matcher timeMatcher = TIME_PATTERN.matcher(timeString.trim());
        int day = 1;
        int hour = 0;
        int minute = 0;
        String month = SkyblockMonth.EARLY_SPRING.scoreboardString;
        String period = "am";
        if(dateMatcher.find()) {
            month = dateMatcher.group("month");
            day = Integer.parseInt(dateMatcher.group("day"));
        }
        if(timeMatcher.find()) {
            hour = Integer.parseInt(timeMatcher.group("hour"));
            minute = Integer.parseInt(timeMatcher.group("minute"));
            period = timeMatcher.group("period");
        }
        return new SkyblockDate(SkyblockMonth.fromName(month), day, hour, minute, period);
    }

    private final SkyblockMonth MONTH;
    private final int DAY;
    private final int HOUR;
    private final int MINUTE;
    private final String PERIOD;

    public SkyblockDate(SkyblockMonth month, int day, int hour, int minute, String period) {
        MONTH = month;
        DAY = day;
        HOUR = hour;
        MINUTE = minute;
        PERIOD = period;
    }

    /**
     * All the months of the Skyblock calendar
     */
    public enum SkyblockMonth {
        EARLY_WINTER("Early Winter"),
        WINTER("Winter"),
        LATE_WINTER("Late Winter"),
        EARLY_SPRING("Early Spring"),
        SPRING("Spring"),
        LATE_SPRING("Late Spring"),
        EARLY_SUMMER("Early Summer"),
        SUMMER("Summer"),
        LATE_SUMMER("Late Summer"),
        EARLY_AUTUMN("Early Autumn"),
        AUTUMN("Autumn"),
        LATE_AUTUMN("Late Autumn");

        final String scoreboardString;

        SkyblockMonth(String scoreboardString) {
            this.scoreboardString = scoreboardString;
        }

        /**
         * Returns the {@code SkyblockMonth} value with the given name.
         *
         * @param scoreboardName the name of the month as it appears on the scoreboard
         * @return the {@code SkyblockMonth} value with the given name or {@code null} if a value with the given name
         * isn't found
         */
        public static SkyblockMonth fromName(String scoreboardName) {
            for (SkyblockMonth skyblockMonth : values()) {
                if(skyblockMonth.scoreboardString.equals(scoreboardName)) {
                    return skyblockMonth;
                }
            }
            return null;
        }
    }

    /**
     * Returns this Skyblock date as a String in the format:
     * Month Day, hh:mm
     *
     * @return this Skyblock date as a formatted String
     */
    @Override
    public String toString() {
        String monthName;

        if (MONTH != null) {
            monthName = MONTH.scoreboardString;
        }
        else {
            monthName = null;
        }

        return String.format("%s %s, %d:%s%s",
                monthName,
                DAY + TextUtils.getOrdinalSuffix(DAY),
                HOUR,
                TextUtils.NUMBER_FORMAT.format(MINUTE),
                PERIOD);
    }
}
