package codes.biscuit.skyblockaddons.utils;

import org.apache.commons.lang3.mutable.MutableInt;

@SuppressWarnings("unused")
public class SkyblockDate {

    private SkyblockMonth month;
    private MutableInt day = new MutableInt();
    private String hour;
    private String minute;

    SkyblockDate(SkyblockMonth month, int day, String hour, String minute) {
        this.month = month;
        this.day.setValue(day);
        this.hour = hour;
        this.minute = minute;
    }

    public SkyblockMonth getMonth() {
        return month;
    }

    public int getDay() {
        return day.getValue();
    }

    public String getHour() {
        return hour;
    }

    public String getMinute() {
        return minute;
    }

    void setDay(int day) {
        this.day.setValue(day);
    }

    void setHour(String hour) {
        this.hour = hour;
    }

    void setMinute(String minute) {
        this.minute = minute;
    }

    void setMonth(SkyblockMonth month) {
        this.month = month;
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
    }
}
