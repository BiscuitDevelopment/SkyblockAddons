package codes.biscuit.skyblockaddons.utils;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class SkyblockDate {

    private static final Pattern DATE_PATTERN = Pattern.compile("(?<month>[\\w ]+) (?<day>\\d{1,2})(th|st|nd|rd)");
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

    private SkyblockMonth month;
    private int day;
    private int hour;
    private int minute;
    private String period;

    SkyblockDate(SkyblockMonth month, int day, int hour, int minute, String period) {
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.period = period;
    }

    public SkyblockMonth getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    void setDay(int day) {
        this.day = day;
    }

    void setHour(int hour) {
        this.hour = hour;
    }

    void setMinute(int minute) {
        this.minute = minute;
    }

    void setMonth(SkyblockMonth month) {
        this.month = month;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    @SuppressWarnings("unused")
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
        EARLY_FALL("Early Fall"),
        FALL("Fall"),
        LATE_FALL("Late Fall");

        private String scoreboardString;

        SkyblockMonth(String scoreboardString) {
            this.scoreboardString = scoreboardString;
        }

        public String getScoreboardString() {
            return scoreboardString;
        }

        public static SkyblockMonth fromName(String scoreboardName) {
            for (SkyblockMonth skyblockMonth : values()) {
                if(skyblockMonth.scoreboardString.equals(scoreboardName)) {
                    return skyblockMonth;
                }
            }
            return null;
        }
    }

    @Override
    public String toString() {
        // Month Day, hh:mm
        DecimalFormat decimalFormat = new DecimalFormat("00");
        return String.format("%s %s, %d:%s%s",
                month.scoreboardString,
                day + TextUtils.getOrdinalSuffix(day),
                hour,
                decimalFormat.format(minute),
                period);
    }
}
