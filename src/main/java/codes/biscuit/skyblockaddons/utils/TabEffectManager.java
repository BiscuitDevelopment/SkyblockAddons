package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonLocation;
import codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsTransformer;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.Field;
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
    private static final Pattern EFFECT_PATTERN = Pattern.compile("(?:(?<potion>§r§[a-f0-9][a-zA-Z ]+ (?:I[XV]|V?I{0,3}) §r§f\\d{0,2}:?\\d{1,2}:\\d{2}§r)|(?<powerup>§r§[a-f0-9][a-zA-Z ]+ §r§f\\d{0,2}:?\\d{1,2}:\\d{2}§r))");

    /**
     * The following two fields are accessed by
     * {@link codes.biscuit.skyblockaddons.listeners.RenderListener#drawPotionEffectTimers(float, ButtonLocation)} to retrieve lists for drawing.
     *
     * Both return a list of current Potion or Powerup timers. They can be empty, but are never null.
     */
    @Getter private List<String> potionTimers = new ArrayList<>();
    @Getter private List<String> powerupTimers = new ArrayList<>();

    /**
     * The following two fields are accessed by
     * {@link codes.biscuit.skyblockaddons.listeners.RenderListener#drawPotionEffectTimers(float, ButtonLocation)}
     * to retrieve dummy lists for drawing when editing GUI locations while no Effects are active.
     *
     * Both return a list of dummy Potion or Powerup timers.
     */
    @Getter private static final List<String> dummyPotionTimers = Arrays.asList(
            "§r§ePotion Effect II §r§f12:34§r",
            "§r§aEnchanting XP Boost III §r§f1:23:45§r");
    @Getter private static final List<String> dummyPowerupTimers = Collections.singletonList(
            "§r§bHoming Snowballs §r§f1:39§r");

    /**
     * Adds a potion effect to the ones currently being displayed.
     *
     * @param potionEffect The potion effect text to be added.
     */
    public void putPotionEffect(String potionEffect) {
        if(SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.HIDE_NIGHT_VISION_EFFECT_TIMER) && potionEffect.startsWith("§r§5Night Vision")) return;
        putEffect(potionEffect, potionTimers);
    }

    /**
     * Adds a powerup to the ones currently being displayed.
     *
     * @param powerup The powerup text to be added.
     */
    public void putPowerup(String powerup) {
        putEffect(powerup, powerupTimers);
    }

    /**
     * Adds the effect to the specified list, after replacing the roman numerals on it- if applicable.
     *
     * @param effect The potion effect/powerup text to be added.
     * @param list The list to add it to (either potionTimers or powerupTimers).
     */
    private void putEffect(String effect, List<String> list) {
        if (SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS)) {
            effect = RomanNumeralParser.replaceNumeralsWithIntegers(effect);
        }
        list.add(effect);
    }

    /**
     * Called by {@link codes.biscuit.skyblockaddons.listeners.PlayerListener#onTick(TickEvent.ClientTickEvent)} every 5 ticks
     * to update the list of current effect timers.
     */
    public void updatePotionEffects() {
        potionTimers.clear();
        powerupTimers.clear();
        IChatComponent tabFooterChatComponent = getFooter();

        // Convert tab footer to a String
        StringBuilder tabFooterString = new StringBuilder();
        if (tabFooterChatComponent != null) {
            for (IChatComponent line : tabFooterChatComponent.getSiblings()) {
                tabFooterString.append(line.getFormattedText());
            }
        }

        // Match the TabFooterString for Effects
        Matcher m = EFFECT_PATTERN.matcher(tabFooterString.toString());
        String effectString;
        while (m.find()) {
            if ((effectString = m.group("potion")) != null) {
                putPotionEffect(effectString);
            } else if ((effectString = m.group("powerup")) != null) {
                putPowerup(effectString);
            }
        }
    }

    /**
     * Holds the footer field in the case of reflection- so the field doesn't have to be looked up every time.
     */
    private static Field footer = null;

    /**
     * @return Grab the tab list's footer text, using reflection if an access transformer wasn't available to be used.
     */
    private static IChatComponent getFooter() {
        GuiPlayerTabOverlay guiTab = Minecraft.getMinecraft().ingameGUI.getTabList();

        if (SkyblockAddonsTransformer.isLabymodClient()) { // There are no access transformers in labymod.
            try {
                if (footer == null) {
                    footer = guiTab.getClass().getDeclaredField("h");
                    footer.setAccessible(true);
                }
                if (footer != null) {
                    footer.get(guiTab);
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        } else {
            return guiTab.footer;
        }
        return null;
    }
}