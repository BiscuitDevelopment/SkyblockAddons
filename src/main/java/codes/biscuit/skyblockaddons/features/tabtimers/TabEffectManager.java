package codes.biscuit.skyblockaddons.features.tabtimers;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonLocation;
import codes.biscuit.skyblockaddons.utils.RomanNumeralParser;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for accessing Potion Effect and Power Up timers to draw on screen.
 */
public class TabEffectManager {

    /** The main TabEffectManager instance. */
    @Getter private static final TabEffectManager instance = new TabEffectManager();

    /** Used to match potion effects from the footer. */
    private static final Pattern EFFECT_PATTERN = Pattern.compile("(?:(?<potion>§r§[a-f0-9][a-zA-Z ]+ (?:I[XV]|V?I{0,3})§r )|(?<powerup>§r§[a-f0-9][a-zA-Z ]+ ))§r§f(?<timer>\\d{0,2}:?\\d{1,2}:\\d{2})");
    private static final Pattern EFFECT_COUNT_PATTERN = Pattern.compile("You have (?<effectCount>[0-9]+) active effects\\. Use \"/effects\" to see them!");

    /**
     * The following two fields are accessed by
     * {@link codes.biscuit.skyblockaddons.listeners.RenderListener#drawPotionEffectTimers(float, ButtonLocation)} to retrieve lists for drawing.
     *
     * Both return a list of current Potion or Powerup timers. They can be empty, but are never null.
     */
    @Getter private List<TabEffect> potionTimers = new ArrayList<>();
    @Getter private List<TabEffect> powerupTimers = new ArrayList<>();

    @Getter private int effectCount;

    /**
     * The following two fields are accessed by
     * {@link codes.biscuit.skyblockaddons.listeners.RenderListener#drawPotionEffectTimers(float, ButtonLocation)}
     * to retrieve dummy lists for drawing when editing GUI locations while no Effects are active.
     *
     * Both return a list of dummy Potion or Powerup timers.
     */
    @Getter private static final List<TabEffect> dummyPotionTimers = Arrays.asList(
            new TabEffect("§r§ePotion Effect II ", "12:34"),
            new TabEffect("§r§aEnchanting XP Boost III ", "1:23:45"));
    @Getter private static final List<TabEffect> dummyPowerupTimers = Collections.singletonList(
            new TabEffect("§r§bHoming Snowballs ", "1:39"));

    /**
     * Adds a potion effect to the ones currently being displayed.
     *
     * @param potionEffect The potion effect text to be added.
     */
    public void putPotionEffect(String potionEffect, String timer) {
        if (SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.HIDE_NIGHT_VISION_EFFECT_TIMER) && potionEffect.startsWith("§r§5Night Vision")) {
            return;
        }
        putEffect(new TabEffect(potionEffect, timer), potionTimers);
    }

    /**
     * Adds a powerup to the ones currently being displayed.
     *
     * @param powerup The powerup text to be added.
     */
    public void putPowerup(String powerup, String timer) {
        putEffect(new TabEffect(powerup, timer), powerupTimers);
    }

    /**
     * Adds the effect to the specified list, after replacing the roman numerals on it- if applicable.
     *
     * @param effect The potion effect/powerup text to be added.
     * @param list The list to add it to (either potionTimers or powerupTimers).
     */
    private void putEffect(TabEffect effect, List<TabEffect> list) {
        if (SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS)) {
            effect.setEffect(RomanNumeralParser.replaceNumeralsWithIntegers(effect.getEffect()));
        }
        list.add(effect);
    }

    /**
     * Called by {@link codes.biscuit.skyblockaddons.listeners.PlayerListener#onTick(TickEvent.ClientTickEvent)} every second
     * to update the list of current effect timers.
     */
    public void updatePotionEffects() {
        potionTimers.clear();
        powerupTimers.clear();
        IChatComponent tabFooterChatComponent = Minecraft.getMinecraft().ingameGUI.getTabList().footer;

        // Convert tab footer to a String
        StringBuilder tabFooterStringBuilder = new StringBuilder();
        if (tabFooterChatComponent != null) {
            for (IChatComponent line : tabFooterChatComponent.getSiblings()) {
                tabFooterStringBuilder.append(line.getFormattedText());
            }
        }

        String tabFooterString = tabFooterStringBuilder.toString();
        String strippedTabFooterString = TextUtils.stripColor(tabFooterString);

        // Match the TabFooterString for Effects
        Matcher matcher = EFFECT_PATTERN.matcher(tabFooterString);
        String effectString;
        while (matcher.find()) {
            if ((effectString = matcher.group("potion")) != null) {
                putPotionEffect(effectString, matcher.group("timer"));
            } else if ((effectString = matcher.group("powerup")) != null) {
                putPowerup(effectString, matcher.group("timer"));
            }
        }

        matcher = EFFECT_COUNT_PATTERN.matcher(strippedTabFooterString);
        if (matcher.find()) {
            effectCount = Integer.parseInt(matcher.group("effectCount"));
        } else {
            effectCount = 0;
        }

        if(SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.SORT_TAB_EFFECT_TIMERS)) {
            Collections.sort(potionTimers);
            Collections.sort(powerupTimers);
        }
    }
}