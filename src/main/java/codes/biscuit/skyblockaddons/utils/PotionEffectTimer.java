package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PotionEffectTimer {

    /* STATIC FIELDS*/
    private static List<PotionEffectTimer> potionEffectTimers = new ArrayList<>();
    private static PotionEffectTimer[] dummyArray = new PotionEffectTimer[]{
            new PotionEffectTimer("§r§ePotion Effect II §r§f12:34§r"),
            new PotionEffectTimer("§r§aFire Resistance I §r§f1:23:45§r"),
            new PotionEffectTimer("§r§bSpeed VIII §r§f3:32§r")
    };
    private static final List dummyEffectTimers = java.util.Arrays.asList(dummyArray);
    private static final Pattern potionEffectPattern = Pattern.compile("§r§[a-f0-9][a-zA-Z ]+ (?:I[XV]|V?I{0,3}) §r§f\\d{0,2}:?\\d{1,2}:\\d{2}§r");

    /* INSTANCE FIELD */
    private String effect;

    /* INSTANCE METHODS */

    public PotionEffectTimer(String effectString){
        this.effect = effectString;
    }

    public String getEffect(){
        return effect;
    }

    /* STATIC METHODS */

    /**
     * Called by {@link codes.biscuit.skyblockaddons.listeners.PlayerListener#onTick(TickEvent.ClientTickEvent)} every 20 ticks
     * to update the list of current PotionEffectTimers.
     */
    public static void updatePotionEffects(){
        List<PotionEffectTimer> effects = new ArrayList<PotionEffectTimer>();
        IChatComponent tabFooterChatComponent = Minecraft.getMinecraft().ingameGUI.getTabList().footer;
        
        //Convert tab footer to a String
        String tabFooterString = "";
        if(tabFooterChatComponent != null){
            for(IChatComponent line : tabFooterChatComponent.getSiblings()){
                tabFooterString += line.getFormattedText();
            }
        }

        //Match the TabFooterString for Potion Effects
        Matcher m = potionEffectPattern.matcher(tabFooterString);
        while(m.find()){
            String effectString = m.group();

            if(SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS)){
                effectString = RomanNumeralParser.replaceNumeralsWithIntegers(effectString);
            }

            effects.add(new PotionEffectTimer(effectString));
        }

        //Store the new List of Potion Effects
        potionEffectTimers = effects;
    }

    /**
     * Called by {@link codes.biscuit.skyblockaddons.listeners.RenderListener#drawPotionEffectTimers(float, Minecraft, ButtonLocation)} to retrieve list for drawing.
     * @return a list of current PotionEffectTimers. Can be empty, but is never null.
     */
    public static List<PotionEffectTimer> getPotionEffects(){
        return potionEffectTimers;
    }

    /**
     * Called by {@link codes.biscuit.skyblockaddons.listeners.RenderListener#drawPotionEffectTimers(float, Minecraft, ButtonLocation)} to retrieve a dummy list for drawing
     * when there are no Potion effects active while editing GUI locations.
     * @return a list of dummy PotionEffectTimers.
     */
    public static List<PotionEffectTimer> getDummyEffects(){
        return dummyEffectTimers;
    }
}
