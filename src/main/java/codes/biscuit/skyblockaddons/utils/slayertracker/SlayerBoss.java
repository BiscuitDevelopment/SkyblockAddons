package codes.biscuit.skyblockaddons.utils.slayertracker;

import codes.biscuit.skyblockaddons.core.Feature;
import codes.biscuit.skyblockaddons.core.ItemRarity;
import codes.biscuit.skyblockaddons.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public abstract class SlayerBoss {
    /**
     * The "feature" setting that determines if this boss' stats should be rendered
     */
    @Getter
    private Feature feature;
    /**
     * The name used in storing/loading the boss stats, MUST BE THE NAME SHOWN WHEN "Talk to Maddox to claim your <boss> xp"
     */
    @Getter
    private String bossName;
    @Getter @Setter
    private int kills = 0;

    public SlayerBoss(Feature feature, String bossName) {
        this.feature = feature;
        this.bossName = bossName;
    }

    /**
     * Get all of the tracked Drops for this boss
     *
     * @return An ArrayList of {@link SlayerDrop}s
     */
    public abstract ArrayList<SlayerDrop> getDrops();

    /**
     * Get the I18N name
     *
     * @return The Translated Name
     */
    public String getDisplayName() {
        String text = Utils.getTranslatedString("settings.slayerBosses." + bossName, "name");
        /*try {
            SkyblockAddons main = SkyblockAddons.getInstance();
            List<String> path = new LinkedList<String>(Arrays.asList(("settings.slayerBosses." + bossName).split(Pattern.quote("."))));
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
            text = bossName; // In case of fire...
        }*/
        return text;
    }

    /**
     * Get the plural name "<boss>s killed"
     *
     * @return The plural name
     */
    public String getKilledName() {
        String text = Utils.getTranslatedString("settings.slayerBosses." + bossName, "killedName");
        /*try {
            SkyblockAddons main = SkyblockAddons.getInstance();
            List<String> path = new LinkedList<String>(Arrays.asList(("settings.slayerBosses." + bossName).split(Pattern.quote("."))));
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
            text = bossName; // In case of fire...
        }*/
        return text + ": ";
    }

    public class SlayerDrop {

        @Getter
        private SlayerBoss boss;
        @Getter
        private String langName, skyblockID;
        @Getter
        private ItemRarity rarity;
        @Getter
        private ResourceLocation resourceLocation;
        @Getter
        private int count = 0;

        public SlayerDrop(SlayerBoss boss, String skyblockID, String langName, ItemRarity rarity) {
            this.boss = boss;
            this.skyblockID = skyblockID;
            this.langName = langName;
            this.rarity = rarity;
            //this.resourceLocation = new ResourceLocation("slayer/<bossname>/<dropname>"); TODO
        }

        /**
         * Get the I18N name
         *
         * @return The Translated Name
         */
        public String getDisplayName() {
            String text = Utils.getTranslatedString("settings.slayerBosses." + boss.getBossName() + ".drops", langName);;
            /*try {
                SkyblockAddons main = SkyblockAddons.getInstance();
                List<String> path = new LinkedList<String>(Arrays.asList(("settings.slayerBosses." + boss.getBossName() + ".drops").split(Pattern.quote("."))));
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
            }*/
            return text + ": ";
        }

        public void setCount(int count) {
            this.count = count;
        }

    }
}
