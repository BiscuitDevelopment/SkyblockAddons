package codes.biscuit.skyblockaddons.utils;

public class TabEffect implements Comparable<TabEffect> {

    private String duration; //Duration String, eg. "01:20"

    private String effect; //Effect Name, eg. "Critical"

    private int durationSeconds; //Duration in seconds, eg. 80

    public TabEffect(String effect, String duration){
        this.effect = effect;
        this.duration = duration;
        String[] s = duration.split(":");
        durationSeconds = 0;
        for(int i=s.length; i>0; i--){
            durationSeconds += Integer.parseInt(s[i-1]) * (Math.pow(60, (s.length - i)));
        }
    }

    public void setEffect(String effect){
        this.effect = effect;
    }

    public String getEffect(){
        return effect;
    }

    public String getDurationForDisplay(){
        return "§r"+duration;
    }

    /**
     * @return the duration in seconds.
     */
    public int getDurationSeconds(){
        return durationSeconds;
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
