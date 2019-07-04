package codes.biscuit.skyblockaddons.utils;

public class SkyblockDate {

    private SkyblockMonth month;
    private int day;
    private int hour;
    private int minute;

    public SkyblockDate(SkyblockMonth month, int day, int hour, int minute) {
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
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

    @SuppressWarnings("unused")
    enum SkyblockMonth {
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
