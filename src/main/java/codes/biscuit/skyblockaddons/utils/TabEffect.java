package codes.biscuit.skyblockaddons.utils;

import lombok.Getter;
import lombok.Setter;

public class TabEffect implements Comparable<TabEffect> {

    private String duration; //Duration String, eg. "01:20"

    @Getter @Setter private String effect; //Effect Name, eg. "Critical"

    @Getter private int durationSeconds; //Duration in seconds, eg. 80

    public TabEffect(String effect, String duration){
        this.effect = effect;
        this.duration = duration;
        String[] s = duration.split(":");
        durationSeconds = 0;
        for (int i = s.length; i > 0; i--){
            durationSeconds += Integer.parseInt(s[i-1]) * (Math.pow(60, (s.length-i)));
        }
    }

    public String getDurationForDisplay(){
        return "§r"+duration;
    }

    @Override
    public int compareTo(TabEffect o) {
        int difference = o.getDurationSeconds() - getDurationSeconds();

        if (Math.abs(difference) <= 1) {
            return TextUtils.stripColor(o.getEffect()).compareTo(TextUtils.stripColor(getEffect()));
        }

        return difference;
    }
}
