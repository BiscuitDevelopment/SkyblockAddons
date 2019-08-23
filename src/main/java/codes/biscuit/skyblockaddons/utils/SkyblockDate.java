package codes.biscuit.skyblockaddons.utils;

import org.apache.commons.lang3.mutable.MutableInt;

public class SkyblockDate {

    private SkyblockMonth month;
    private MutableInt day = new MutableInt();
    private MutableInt hour = new MutableInt();
    private MutableInt minute = new MutableInt();

    SkyblockDate(SkyblockMonth month, int day, int hour, int minute) {
        this.month = month;
        this.day.setValue(day);
        this.hour.setValue(hour);
        this.minute.setValue(minute);
    }

    public SkyblockMonth getMonth() {
        return month;
    }

    public int getDay() {
        return day.getValue();
    }

    public int getHour() {
        return hour.getValue();
    }

    public int getMinute() {
        return minute.getValue();
    }

    void setDay(int day) {
        this.day.setValue(day);
    }

    void setHour(int hour) {
        this.hour.setValue(hour);
    }

    void setMinute(int minute) {
        this.minute.setValue(minute);
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
        EARLY_FALLL("Early Fall"),
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
