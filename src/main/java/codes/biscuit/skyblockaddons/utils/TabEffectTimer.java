package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for accessing Potion Effect and Power Up timers to draw on screen.
 */
public class TabEffectTimer {

    /* STATIC FIELDS*/
    private static List<TabEffectTimer> potionTimers = new ArrayList<>();
    private static List<TabEffectTimer> powerupTimers = new ArrayList<>();

    private static final Pattern effectPattern = Pattern.compile("(?:(?<potion>§r§[a-f0-9][a-zA-Z ]+ (?:I[XV]|V?I{0,3}) §r§f\\d{0,2}:?\\d{1,2}:\\d{2}§r)|(?<powerup>§r§[a-f0-9][a-zA-Z ]+ §r§f\\d{0,2}:?\\d{1,2}:\\d{2}§r))");

    private static TabEffectTimer[] dummyPotionArray = new TabEffectTimer[]{
            new TabEffectTimer("§r§ePotion Effect II §r§f12:34§r"),
            new TabEffectTimer("§r§aEnchanting XP Boost III §r§f1:23:45§r")
    };
    private static final List dummyPotionTimers = java.util.Arrays.asList(dummyPotionArray);

    private static TabEffectTimer[] dummyPowerupArray = new TabEffectTimer[]{
            new TabEffectTimer("§r§bHoming Snowballs §r§f1:39§r")
    };
    private static final List dummyPowerupTimers = java.util.Arrays.asList(dummyPowerupArray);

    /* INSTANCE FIELD */
    private String effect;

    /* INSTANCE METHODS */
    public TabEffectTimer(String effectString) {
        if (SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS)) {
            effectString = RomanNumeralParser.replaceNumeralsWithIntegers(effectString);
        }
        this.effect = effectString;
    }

    public String getEffect() {
        return effect;
    }

    /* STATIC METHODS */
    /**
     * Called by {@link codes.biscuit.skyblockaddons.listeners.PlayerListener#onTick(TickEvent.ClientTickEvent)} every 5 ticks
     * to update the list of current effect timers.
     */
    public static void updatePotionEffects() {
        potionTimers.clear();
        powerupTimers.clear();
        IChatComponent tabFooterChatComponent = Minecraft.getMinecraft().ingameGUI.getTabList().footer;

        //Convert tab footer to a String
        String tabFooterString = "";
        if (tabFooterChatComponent != null) {
            for (IChatComponent line : tabFooterChatComponent.getSiblings()) {
                tabFooterString += line.getFormattedText();
            }
        }

        //Match the TabFooterString for Effects
        Matcher m = effectPattern.matcher(tabFooterString);
        String effectString;
        while (m.find()) {
            if ((effectString = m.group("potion")) != null) {
                potionTimers.add(new TabEffectTimer(effectString));
            } else if ((effectString = m.group("powerup")) != null) {
                powerupTimers.add(new TabEffectTimer(effectString));
            }
        }
    }

    /*
     * The following two methods are called by by RenderListener#drawPotionEffectTimers(float, Minecraft, ButtonLocation) to retrieve lists for drawing.
     *
     * Both return a list of current Potion or Powerup timers. They can be empty, but are never null.
     */

    public static List<TabEffectTimer> getPotionTimers() {
        return potionTimers;
    }

    public static List<TabEffectTimer> getPowerupTimers() {
        return powerupTimers;
    }

    /*
     * The following two methods are called by by RenderListener#drawPotionEffectTimers(float, Minecraft, ButtonLocation) to retrieve dummy lists for drawing
     * when editing GUI locations while no Effects are active.
     *
     * Both return a list of dummy Potion or Powerup timers.
     */
    public static List<TabEffectTimer> getDummyPotions() {
        return dummyPotionTimers;
    }

    public static List<TabEffectTimer> getDummyPowerups() {
        return dummyPowerupTimers;
    }
}