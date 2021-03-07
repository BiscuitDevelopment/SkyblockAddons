package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.utils.RomanNumeralParser;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Mouse;

import java.util.*;
import java.util.regex.Pattern;

public class EnchantManager {

    private static final Pattern ENCHANTMENT_PATTERN = Pattern.compile("^((?:[\\w ]+) (?:[\\dIVXLCDM]+)(, |$))+");
    private static final Pattern ENCHANTMENT_FORMAT_START = Pattern.compile("^(§[a-f0-9k-or])*§9(§[k-o])?.*");
    private static final String COMMA = "§r, ";
    private static final int COMMA_LENGTH = COMMA.length();
    private static final int COMMA_SIZE = Minecraft.getMinecraft().fontRendererObj.getStringWidth(COMMA);
    @Setter private static Map<String, ItemEnchants> enchants = new HashMap<>();


    /**
     * Place ultimate enchantments at the beginning, and alphabetize
     */
    private static final Comparator<String> ENCHANT_COMPARATOR = (s1, s2) -> {
        if (s1 == null) {
            return -1;
        }
        if (s2 == null) {
            return 1;
        }
        ItemEnchants i1 = enchants.get(s1);
        ItemEnchants i2 = enchants.get(s2);
        // If both are ultimates (shouldn't happen) or both are not ultimates, compare lore names, otherwise order ultimates first
        return i1 == null || i2 == null ? 0 : i1.isUltimate == i2.isUltimate ? i1.loreName.compareTo(i2.loreName) : i1.isUltimate ? -1 : 1;
    };


     public static class ItemEnchants {
        String loreName;
        int maxCraft;
        int maxLevel;
        boolean isUltimate;
    }

    /**
     * Imitates EnumChatFormatting, but allows for chroma color code w/in enchants
     */
    public enum EnchantQuality {
        PERFECT("CHROMA", 'z'),
        GREAT(EnumChatFormatting.GOLD),
        GOOD(EnumChatFormatting.BLUE),
        POOR(EnumChatFormatting.GRAY);

        String controlString;
        String name;
        EnchantQuality(EnumChatFormatting formatCode) {
            this.name = formatCode.name();
            this.controlString = formatCode.toString();
        }
        EnchantQuality(String name, char code) {
            this.name = name;
            this.controlString = "\u00a7" + code;
        }
    }

    public static EnchantQuality getEnchantQuality(String enchantName, int level) {
        ItemEnchants enchant = enchants.get(enchantName);
        if (enchant == null) {
            return EnchantQuality.POOR;
        }
        if (level == enchant.maxLevel) {
            return EnchantQuality.PERFECT;
        }
        if (level > enchant.maxCraft) {
            return EnchantQuality.GREAT;
        }
        if (level == enchant.maxCraft) {
            return EnchantQuality.GOOD;
        }
        return EnchantQuality.POOR;
    }

    public static String getEnchantLore(String enchantName) {
        return enchants.get(enchantName) != null ? enchants.get(enchantName).loreName : TextUtils.toProperCase(enchantName.replaceAll("_", " "));
    }

    public static boolean isUltimate(String enchantName) {
        return enchants.get(enchantName) != null ? enchants.get(enchantName).isUltimate : enchantName.startsWith("ultimate_");
    }

    public static void organizeEnchants(List<String> loreList, NBTTagCompound enchantNbt) {

        int numEnchants = enchantNbt.getKeySet().size();
        if (numEnchants == 0) {
            return;
        }

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        int startEnchant = -1, endEnchant = -1, maxTooltipWidth = 0;
        for (int i = 0; i < loreList.size(); i++) {
            String u = loreList.get(i);
            String s = TextUtils.stripColor(u);

            int width = fontRenderer.getStringWidth(loreList.get(i));
            // Get max tooltip size, disregarding the enchants section
            if (startEnchant == -1 || endEnchant != -1) {
                maxTooltipWidth = Math.max(width, maxTooltipWidth);
            }
            if (startEnchant == -1) {
                if (ENCHANTMENT_FORMAT_START.matcher(u).matches() && ENCHANTMENT_PATTERN.matcher(s).matches()) {
                    startEnchant = i;
                }
            }
            // Assume enchants end with an empty line "break"
            else if (s.trim().length() == 0 && endEnchant == -1) {
                endEnchant = i - 1;
            }
        }
        if (endEnchant == -1) {
            //SkyblockAddons.getLogger().info("Failed parse");
            return;
        }

        // Order all enchants
        SortedSet<String> orderedEnchants = new TreeSet<>(ENCHANT_COMPARATOR);
        orderedEnchants.addAll(enchantNbt.getKeySet());

        // Figure out whether the item tooltip is gonna wrap, and if so, try to make our enchantments wrap
        maxTooltipWidth = correctTooltipWidth(maxTooltipWidth);

        numEnchants = orderedEnchants.size();
        List<String> enchantList = new ArrayList<>(numEnchants);
        List<Integer> enchantSizes = new ArrayList<>(numEnchants);
        for (String currEnchant : orderedEnchants) {
            // Get the name, formatting code, and the formatted level (decimal or roman numeral) of the enchantment
            String name = getEnchantLore(currEnchant);
            boolean isUltimate = isUltimate(currEnchant);
            int level = enchantNbt.getInteger(currEnchant);
            String formattedLevel = SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS) ?
                    String.valueOf(level) : RomanNumeralParser.integerToRoman(level);
            String formatCode = isUltimate ? "§d§l" : SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.ENCHANTMENTS_HIGHLIGHT) ?
                    getEnchantQuality(currEnchant, level).controlString : String.valueOf(EnumChatFormatting.BLUE);

            String fullEnchantName = formatCode + name + " " + formattedLevel;
            int enchantRenderLength = fontRenderer.getStringWidth(fullEnchantName);
            enchantList.add(fullEnchantName);
            enchantSizes.add(enchantRenderLength);
            maxTooltipWidth = Math.max(enchantRenderLength, maxTooltipWidth);
        }
        if (enchantList.size() == 0) {
            return;
        }
        // Remove enchantment lines
        loreList.subList(startEnchant, endEnchant + 1).clear();

        for (int i = 0, ench = 0; ench < numEnchants; i++) {
            StringBuilder builder = new StringBuilder(maxTooltipWidth);
            for (int sum = 0; ench < numEnchants && sum + enchantSizes.get(ench) <= maxTooltipWidth; ench++) {
                builder.append(enchantList.get(ench)).append(COMMA);
                sum += enchantSizes.get(ench) + COMMA_SIZE;
            }
            builder.delete(builder.length()-COMMA_LENGTH, builder.length());
            loreList.add(startEnchant + i, builder.toString());
        }

    }



    private static int correctTooltipWidth(int maxTooltipWidth) {
        // Figure out whether the item tooltip is gonna wrap, and if so, try to make our enchantments wrap
        final ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft().currentScreen.mc);
        final int mouseX = Mouse.getX() * scaledresolution.getScaledWidth() / Minecraft.getMinecraft().currentScreen.mc.displayWidth;
        int tooltipX = mouseX + 12;
        if (tooltipX + maxTooltipWidth + 4 > scaledresolution.getScaledWidth()) {
            tooltipX = mouseX - 16 - maxTooltipWidth;
            if (tooltipX < 4) {
                if (mouseX > scaledresolution.getScaledWidth() / 2) {
                    maxTooltipWidth = mouseX - 12 - 8;
                }
                else {
                    maxTooltipWidth = scaledresolution.getScaledWidth() - 16 - mouseX;
                }
            }
        }

        if (scaledresolution.getScaledWidth() > 0 && maxTooltipWidth > scaledresolution.getScaledWidth()) {
            maxTooltipWidth = scaledresolution.getScaledWidth();
        }
        return maxTooltipWidth;
    }
}
