package codes.biscuit.skyblockaddons.utils;

public class TabEffect implements Comparable {
    private String duration; //Duration String, eg. "01:20"
    private String effect; //Effect Name, eg. "Critical"
    private int durationI; //Duration in seconds, eg. 80

    public TabEffect(String effect, String duration){
        this.effect = effect;
        this.duration = duration;
        String[] s = duration.split(":");
        durationI = 0;
        for(int i=s.length; i>0; i--){
            durationI += Integer.parseInt(s[i-1]) * (Math.pow(60, (s.length - i)));
        }
    }

    public void setEffect(String effect){
        this.effect = effect;
    }

    public String getEffect(){
        return effect;
    }

    public String toString(){
        return effect+"§r§f"+duration;
    }

    /**
     * @return the duration in seconds.
     */
    public int getDurationI(){
        return durationI;
    }

    @Override
    public int compareTo(Object o) {
        return ((TabEffect) o).getDurationI() - getDurationI();
    }
}
