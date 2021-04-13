package codes.biscuit.skyblockaddons.features;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.config.ConfigValues;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.InventoryType;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.RomanNumeralParser;
import codes.biscuit.skyblockaddons.utils.TextUtils;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;
import org.lwjgl.input.Mouse;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnchantManager {

    // Catches successive [ENCHANT] [ROMAN NUMERALS OR DIGITS], as well as stacking enchants listing total stacked number
    private static final Pattern ENCHANTMENT_PATTERN = Pattern.compile("(?<enchant>[A-Za-z -]+) ((?<levelNumeral>[IVXLCDM]+)|(?<levelDigit>[\\d]+))(, |$| [\\d,]+$)");
    private static final String COMMA = ", ";
    @Setter
    private static Enchants enchants = new Enchants();

    public static class Enchants {
        HashMap<String, Enchant.Normal> NORMAL = new HashMap<>();
        HashMap<String, Enchant.Ultimate> ULTIMATE = new HashMap<>();
        HashMap<String, Enchant.Stacking> STACKING = new HashMap<>();

        public Enchant getFromLore(String loreName) {
            loreName = loreName.toLowerCase(Locale.US);
            Enchant enchant = NORMAL.get(loreName);
            if (enchant == null) {
                enchant = ULTIMATE.get(loreName);
            }
            if (enchant == null) {
                enchant = STACKING.get(loreName);
            }
            if (enchant == null) {
                enchant = new Enchant.Dummy(loreName);
            }
            return enchant;
        }

        public String toString() {
            return "NORMAL:\n" + NORMAL.toString() +"\nULTIMATE:\n" + ULTIMATE.toString() + "\nSTACKING:\n" + STACKING.toString();
        }
    }

    static class Enchant implements Comparable<Enchant> {
        String nbtName;
        String loreName;
        int goodLevel;
        int maxLevel;

        public boolean isNormal() {
            return this instanceof Normal;
        }

        public boolean isUltimate() {
            return this instanceof Ultimate;
        }

        public boolean isStacking() {
            return this instanceof Stacking;
        }

        public String getFormattedName(int level) {
            return getFormat(level) + loreName;
        }

        public String getFormat(int level) {
            ConfigValues config = SkyblockAddons.getInstance().getConfigValues();
            if (level >= maxLevel) {
                return config.getRestrictedColor(Feature.ENCHANTMENT_PERFECT_COLOR).toString();
            }
            if (level > goodLevel) {
                return config.getRestrictedColor(Feature.ENCHANTMENT_GREAT_COLOR).toString();
            }
            if (level == goodLevel) {
                return config.getRestrictedColor(Feature.ENCHANTMENT_GOOD_COLOR).toString();
            }
            return config.getRestrictedColor(Feature.ENCHANTMENT_POOR_COLOR).toString();
        }

        public String toString() {
            return nbtName + " " + goodLevel + " " + maxLevel + "\n";
        }


        /**
         * Orders enchants by type in the following way:
         *  1) Ultimates (alphabetically)
         *  2) Stacking (alphabetically)
         *  3) Normal (alphabetically)
         */
        @Override
        public int compareTo(Enchant o) {
            if (this.isUltimate() == o.isUltimate()) {
                if (this.isStacking() == o.isStacking()) {
                    return this.loreName.compareTo(o.loreName);
                }
                return this.isStacking() ? -1 : 1;
            }
            return this.isUltimate() ? -1 : 1;
        }


        static class Normal extends Enchant {
        }

        static class Ultimate extends Enchant {
            @Override
            public String getFormat(int level) {
                return "§d§l";
            }
        }

        static class Stacking extends Enchant {
            String nbtNum;
            String statLabel;
            TreeSet<Integer> stackLevel;

            public String toString() {
                return nbtNum + " " + stackLevel.toString() + " " + super.toString();
            }
        }

        static class Dummy extends Enchant {

            public Dummy (String name) {
                loreName = name;
                nbtName = name.toLowerCase().replaceAll(" ", "_");
            }

            @Override
            public String getFormat(int level) {
                return ColorCode.DARK_RED.toString();
            }
        }
    }

    /**
     * Helper method to determine whether we should skip this line in parsing the lore.
     * E.g. we want to skip "Breaking Power X" seen on pickaxes.
     * @param enchantNBT the enchantments extraAttributes NBT of the item
     * @param s the line of lore we are parsing
     * @return {@code true} if no enchants on the line are in the enchants table, {@code false} otherwise.
     */
    public static boolean containsEnchantment(NBTTagCompound enchantNBT, String s) {
        Matcher m = ENCHANTMENT_PATTERN.matcher(s);
        while (m.find()) {
            Enchant enchant = enchants.getFromLore(m.group("enchant"));
            if (enchantNBT == null || enchantNBT.hasKey(enchant.nbtName)) {
                return true;
            }
        }
        return false;
    }

    public static void parseEnchants(List<String> loreList, NBTTagCompound extraAttributes) {
        NBTTagCompound enchantNBT = extraAttributes == null ? null : extraAttributes.getCompoundTag("enchantments");
        if (enchantNBT == null && SkyblockAddons.getInstance().getInventoryUtils().getInventoryType() != InventoryType.SUPERPAIRS) {
            return;
        }
        if (SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.ENCHANTMENTS_HIGHLIGHT)) {

            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
            int startEnchant = -1, endEnchant = -1, maxTooltipWidth = 0;
            for (int i = 0; i < loreList.size(); i++) {
                String u = loreList.get(i);
                String s = TextUtils.stripColor(u);
                if (startEnchant == -1) {
                    if (containsEnchantment(enchantNBT, s)) {
                        startEnchant = i;
                    }
                }
                // Assume enchants end with an empty line "break"
                else if (s.trim().length() == 0 && endEnchant == -1) {
                    endEnchant = i - 1;
                }
                // Get max tooltip size, disregarding the enchants section
                if (startEnchant == -1 || endEnchant != -1) {
                    maxTooltipWidth = Math.max(fontRenderer.getStringWidth(loreList.get(i)), maxTooltipWidth);
                }
            }
            if (enchantNBT == null && endEnchant == -1) {
                endEnchant = startEnchant;
            }
            if (endEnchant == -1) {
                return;
            }
            // Figure out whether the item tooltip is gonna wrap, and if so, try to make our enchantments wrap
            maxTooltipWidth = correctTooltipWidth(maxTooltipWidth);

            TreeMap<Enchant, Integer> orderedEnchants = new TreeMap<>();
            // Order all enchants
            for (int i = startEnchant; i <= endEnchant; i++) {
                String currLine = TextUtils.stripColor(loreList.get(i));
                Matcher m = ENCHANTMENT_PATTERN.matcher(currLine);
                while (m.find()) {
                    Enchant enchant = enchants.getFromLore(m.group("enchant"));
                    int level = m.group("levelDigit") == null ? RomanNumeralParser.parseNumeral(m.group("levelNumeral")) : Integer.parseInt(m.group("levelDigit"));
                    if (enchant != null) {
                        orderedEnchants.put(enchant, level);
                    }
                }
            }
            int numEnchants = orderedEnchants.size();
            List<String> enchantList = new ArrayList<>(numEnchants);
            List<Integer> enchantSizes = new ArrayList<>(numEnchants);
            for (Map.Entry<Enchant, Integer> currEnchant : orderedEnchants.entrySet()) {
                int level = currEnchant.getValue();
                String formattedName = currEnchant.getKey().getFormattedName(level);
                String formattedLevel = SkyblockAddons.getInstance().getConfigValues().isEnabled(Feature.REPLACE_ROMAN_NUMERALS_WITH_NUMBERS) ?
                        String.valueOf(level) : RomanNumeralParser.integerToRoman(level);
                String fullEnchantName = formattedName + " " + formattedLevel;
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

            // Get format for comma
            String comma = SkyblockAddons.getInstance().getConfigValues().getRestrictedColor(Feature.ENCHANTMENT_COMMA_COLOR) + COMMA;
            int commaLength = fontRenderer.getStringWidth(comma);

            int i, e;
            for (i = 0, e = 0; e < numEnchants; i++) {
                StringBuilder builder = new StringBuilder(maxTooltipWidth);
                for (int sum = 0; e < numEnchants && sum + enchantSizes.get(e) <= maxTooltipWidth; e++) {
                    builder.append(enchantList.get(e)).append(comma);
                    sum += enchantSizes.get(e) + commaLength;
                }
                builder.delete(builder.length() - comma.length(), builder.length());
                loreList.add(startEnchant + i, builder.toString());
            }
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

    public static int insertStackingEnchantProgress(List<String> loreList, NBTTagCompound extraAttributes, int insertAt) {
        if (extraAttributes == null || SkyblockAddons.getInstance().getConfigValues().isDisabled(Feature.SHOW_STACKING_ENCHANT_PROGRESS)) {
            return insertAt;
        }
        for (Enchant.Stacking enchant : enchants.STACKING.values()) {
            if (extraAttributes.hasKey(enchant.nbtNum, Constants.NBT.TAG_ANY_NUMERIC)) {
                int stackedEnchantNum = extraAttributes.getInteger(enchant.nbtNum);
                Integer nextLevel = enchant.stackLevel.higher(stackedEnchantNum);
                String statLabel = Translations.getMessage("enchants." + enchant.statLabel);
                ColorCode colorCode = SkyblockAddons.getInstance().getConfigValues().getRestrictedColor(Feature.SHOW_STACKING_ENCHANT_PROGRESS);
                StringBuilder b = new StringBuilder();
                b.append("§7").append(statLabel).append(": ").append(colorCode);
                if (nextLevel == null) {
                    // §7Expertise Kills: §a5000000000 §7(Maxed)
                    b.append(TextUtils.abbreviate(stackedEnchantNum)).append(" §7(").append(Translations.getMessage("enchants.maxed")).append(")");
                } else {
                    // §7Expertise Kills: §a500 §7/ 1k
                    b.append(stackedEnchantNum).append(" §7/ ").append(TextUtils.abbreviate(nextLevel));
                }
                loreList.add(insertAt++, b.toString());
            }
        }
        return insertAt;
    }
}
