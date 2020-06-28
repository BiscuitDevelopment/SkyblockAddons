package codes.biscuit.skyblockaddons.utils.slayertracker;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.constants.game.Rarity;
import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.utils.Language;
import com.google.gson.JsonObject;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.regex.Pattern;

public abstract class SlayerBoss {
    /*
    SVEN(Feature.SLAYER_WOLF, "slayerWolf",
            new Pair<>("slayerDropWolfTeeth", Rarity.COMMON),
            new Pair<>("slayerDropHamsterWheels", Rarity.COMMON),
            new Pair<>("slayerDropSpiritRunes", Rarity.COMMON),
            new Pair<>("slayerDropCrit6Books", Rarity.COMMON),
            new Pair<>("slayerDropRedClawEggs", Rarity.COMMON),
            new Pair<>("slayerDropCoutureRunes", Rarity.COMMON),
            new Pair<>("slayerDropOverfluxes", Rarity.COMMON),
            new Pair<>("slayerDropGrizzlyBaits", Rarity.COMMON));*/

    @Getter
    private Feature feature;
    @Getter
    private String langName;
    @Getter
    private int kills = 0;

    public SlayerBoss(Feature feature, String langName) {
        this.feature = feature;
        this.langName = langName;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public abstract ArrayList<SlayerDrop> getDrops();

    public String getDisplayName() {
        String text;
        try {
            SkyblockAddons main = SkyblockAddons.getInstance();
            List<String> path = new LinkedList<String>(Arrays.asList(("settings.slayerBosses." + langName).split(Pattern.quote("."))));
            JsonObject jsonObject = main.getConfigValues().getLanguageConfig();
            for (String part : path) {
                if (!part.equals("")) {
                    jsonObject = jsonObject.getAsJsonObject(part);
                }
            }
            text = jsonObject.get("name").getAsString();
            if (text != null && (main.getConfigValues().getLanguage() == Language.HEBREW || main.getConfigValues().getLanguage() == Language.ARABIC) && !Minecraft.getMinecraft().fontRendererObj.getBidiFlag()) {
                text = bidiReorder(text);
            }
        } catch (NullPointerException ex) {
            text = langName; // In case of fire...
        }
        return text;
    }

    public String getKilledName() {
        String text;
        try {
            SkyblockAddons main = SkyblockAddons.getInstance();
            List<String> path = new LinkedList<String>(Arrays.asList(("settings.slayerBosses." + langName).split(Pattern.quote("."))));
            JsonObject jsonObject = main.getConfigValues().getLanguageConfig();
            for (String part : path) {
                if (!part.equals("")) {
                    jsonObject = jsonObject.getAsJsonObject(part);
                }
            }
            text = jsonObject.get("killedName").getAsString();
            if (text != null && (main.getConfigValues().getLanguage() == Language.HEBREW || main.getConfigValues().getLanguage() == Language.ARABIC) && !Minecraft.getMinecraft().fontRendererObj.getBidiFlag()) {
                text = bidiReorder(text);
            }
        } catch (NullPointerException ex) {
            text = langName; // In case of fire...
        }
        return text + ": ";
    }

    private String bidiReorder(String text) {
        try {
            Bidi bidi = new Bidi((new ArabicShaping(ArabicShaping.LETTERS_SHAPE)).shape(text), Bidi.DIRECTION_DEFAULT_RIGHT_TO_LEFT);
            bidi.setReorderingMode(Bidi.REORDER_DEFAULT);
            return bidi.writeReordered(Bidi.DO_MIRRORING);
        } catch (ArabicShapingException var3) {
            return text;
        }
    }

    public class SlayerDrop {

        @Getter
        private SlayerBoss boss;
        @Getter
        private String langName, actualName;
        @Getter
        private Rarity rarity;
        @Getter
        private ResourceLocation resourceLocation;
        @Getter
        private int count = 0;

        public SlayerDrop(SlayerBoss boss, String actualName, String langName, Rarity rarity) {
            this.boss = boss;
            this.actualName = actualName;
            this.langName = langName;
            this.rarity = rarity;
            //this.resourceLocation = new ResourceLocation("slayer/<bossname>/<dropname>");
        }

        public String getDisplayName() {
            String text;
            try {
                SkyblockAddons main = SkyblockAddons.getInstance();
                List<String> path = new LinkedList<String>(Arrays.asList(("settings.slayerBosses." + boss.getLangName() + ".drops").split(Pattern.quote("."))));
                JsonObject jsonObject = main.getConfigValues().getLanguageConfig();
                for (String part : path) {
                    if (!part.equals("")) {
                        jsonObject = jsonObject.getAsJsonObject(part);
                    }
                }
                text = jsonObject.get(langName).getAsString();
                if (text != null && (main.getConfigValues().getLanguage() == Language.HEBREW || main.getConfigValues().getLanguage() == Language.ARABIC) && !Minecraft.getMinecraft().fontRendererObj.getBidiFlag()) {
                    text = bidiReorder(text);
                }
            } catch (NullPointerException ex) {
                text = langName; // In case of fire...
            }
            return text + ": ";
        }

        public void setCount(int count) {
            this.count = count;
        }

    }
}
