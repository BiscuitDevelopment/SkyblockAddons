package codes.biscuit.skyblockaddons.utils;

import com.google.gson.*;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.util.*;

public class ConfigValues {

    private File settingsConfigFile;
    private JsonObject settingsConfig = new JsonObject();

    private JsonObject languageConfig = new JsonObject();

    private Set<Feature> disabledFeatures = EnumSet.noneOf(Feature.class);
    private Map<Feature, ConfigColor> featureColors = new EnumMap<>(Feature.class);
    private Feature.BarType manaBarType = Feature.BarType.BAR_TEXT;
    private Feature.BarType healthBarType = Feature.BarType.BAR_TEXT;
    private Feature.IconType defenceIconType = Feature.IconType.ICON_DEFENCE;
    private int warningSeconds = 4;
    private Map<Feature, CoordsPair> coordinates = new EnumMap<>(Feature.class);
    private float guiScale = 0;
    private Language language = Language.ENGLISH;
    private Feature.BackpackStyle backpackStyle = Feature.BackpackStyle.GUI;

    public ConfigValues(File settingsConfigFile) {
        this.settingsConfigFile = settingsConfigFile;
    }

    public void loadConfig() {
        if (settingsConfigFile.exists()) {
            try {
                FileReader reader = new FileReader(settingsConfigFile);
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuilder builder = new StringBuilder();
                String nextLine;
                while ((nextLine = bufferedReader.readLine()) != null) {
                    builder.append(nextLine);
                }
                String complete = builder.toString();
                JsonElement fileElement = new JsonParser().parse(complete);
                if (fileElement == null || fileElement.isJsonNull()) {
                    throw new JsonParseException("File is null!");
                }
                settingsConfig = fileElement.getAsJsonObject();
            } catch (JsonParseException | IllegalStateException | IOException ex) {
                ex.printStackTrace();
                System.out.println("SkyblockAddons: There was an error loading the config. Resetting all settings to default.");
                addDefaultsAndSave();
                return;
            }
            for (JsonElement element : settingsConfig.getAsJsonArray("disabledFeatures")) {
                Feature feature = Feature.fromId(element.getAsInt());
                if (feature != null) {
                    disabledFeatures.add(feature);
                }
            }
            warningSeconds = settingsConfig.get("warningSeconds").getAsInt();
            if (settingsConfig.has("manaBarType")) {
                int ordinal = settingsConfig.get("manaBarType").getAsInt();
                if (Feature.BarType.values().length > ordinal) {
                    manaBarType = Feature.BarType.values()[ordinal];
                }
            }
            if (settingsConfig.has("manaBarX")) {
                coordinates.put(Feature.MANA_BAR, new CoordsPair(settingsConfig.get("manaBarX").getAsFloat(), settingsConfig.get("manaBarY").getAsFloat()));
            }
            if (settingsConfig.has("skeletonBarX")) {
                coordinates.put(Feature.SKELETON_BAR, new CoordsPair(settingsConfig.get("skeletonBarX").getAsFloat(), settingsConfig.get("skeletonBarY").getAsFloat()));
            }
            if (settingsConfig.has("warningColor")) { // migrate from old config
                int ordinal = settingsConfig.get("warningColor").getAsInt();
                if (ConfigColor.values().length > ordinal) {
                    featureColors.put(Feature.WARNING_COLOR, ConfigColor.values()[ordinal]);
                }
            } else {
                featureColors.put(Feature.WARNING_COLOR, ConfigColor.RED);
            }
            if (settingsConfig.has("confirmationColor")) { // migrate from old config
                int ordinal = settingsConfig.get("confirmationColor").getAsInt();
                if (ConfigColor.values().length > ordinal) {
                    featureColors.put(Feature.CONFIRMATION_COLOR, ConfigColor.values()[ordinal]);
                }
            } else {
                featureColors.put(Feature.CONFIRMATION_COLOR, ConfigColor.RED);
            }
            if (settingsConfig.has("manaBarColor")) { // migrate from old config
                int ordinal = settingsConfig.get("manaBarColor").getAsInt();
                if (ConfigColor.values().length > ordinal) {
                    featureColors.put(Feature.MANA_BAR_COLOR, ConfigColor.values()[ordinal]);
                }
            } else {
                featureColors.put(Feature.MANA_BAR_COLOR, ConfigColor.BLUE);
            }
            if (settingsConfig.has("manaBarTextColor")) { // migrate from old config
                int ordinal = settingsConfig.get("manaBarTextColor").getAsInt();
                if (ConfigColor.values().length > ordinal) {
                    featureColors.put(Feature.MANA_TEXT_COLOR, ConfigColor.values()[ordinal]);
                }
            } else {
                featureColors.put(Feature.MANA_TEXT_COLOR, ConfigColor.BLUE);
            }
            if (settingsConfig.has("guiScale")) {
                guiScale = settingsConfig.get("guiScale").getAsFloat();
            }
            if (settingsConfig.has("manaTextX")) {
                coordinates.put(Feature.MANA_TEXT, new CoordsPair(settingsConfig.get("manaTextX").getAsFloat(), settingsConfig.get("manaTextY").getAsFloat()));
            }
            if (settingsConfig.has("language")) {
                Language configLanguage = Language.getFromPath(settingsConfig.get("language").getAsString().toLowerCase());
                if (configLanguage != null) {
                    language = configLanguage;
                }
            }
            if (settingsConfig.has("backpackStyle")) {
                int ordinal = settingsConfig.get("backpackStyle").getAsInt();
                if (Feature.BackpackStyle.values().length > ordinal) {
                    backpackStyle = Feature.BackpackStyle.values()[ordinal];
                }
            }
        } else {
            addDefaultsAndSave();
        }
        loadLanguageFile();
    }

    private void addDefaultsAndSave() {
        String minecraftLanguage = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode().toLowerCase();
        Language configLanguage = Language.getFromPath(minecraftLanguage);
        if (configLanguage != null) { // Check if we have the exact locale they are using for Minecraft
            language = configLanguage;
        } else { // Check if we at least have the same language (different locale)
            String languageCode = minecraftLanguage.split("_")[0];
            for (Language loopLanguage : Language.values()) {
                String loopLanguageCode = loopLanguage.getPath().split("_")[0];
                if (loopLanguageCode.equals(languageCode)) {
                    language = loopLanguage;
                    break;
                }
            }
        }
        featureColors.put(Feature.CONFIRMATION_COLOR, ConfigColor.RED);
        featureColors.put(Feature.WARNING_COLOR, ConfigColor.RED);
        featureColors.put(Feature.MANA_TEXT_COLOR, ConfigColor.BLUE);
        featureColors.put(Feature.MANA_BAR_COLOR, ConfigColor.BLUE);
        disabledFeatures.add(Feature.DROP_CONFIRMATION);
        disabledFeatures.add(Feature.MINION_STOP_WARNING);
        coordinates.put(Feature.SKELETON_BAR, new CoordsPair(0.68F, 0.93F));
        coordinates.put(Feature.DEFENCE_ICON, new CoordsPair(0.45F, 0.83F));
        coordinates.put(Feature.DEFENCE_TEXT, new CoordsPair(0.45F, 0.83F));
        coordinates.put(Feature.DEFENCE_PERCENTAGE, new CoordsPair(0.45F, 0.83F));
        coordinates.put(Feature.HEALTH_BAR, new CoordsPair(0.45F, 0.83F));
        coordinates.put(Feature.HEALTH_TEXT, new CoordsPair(0.45F, 0.83F));
        coordinates.put(Feature.MANA_BAR, new CoordsPair(0.45F, 0.83F));
        coordinates.put(Feature.MANA_TEXT, new CoordsPair(0.45F, 0.83F));
        saveConfig();
    }

    public void loadLanguageFile() {
        try {
            InputStream fileStream = getClass().getClassLoader().getResourceAsStream("lang/" + language.getPath() + ".json");
            if (fileStream != null) {
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fileStream.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                String dataString = result.toString("UTF-8");
                languageConfig = new JsonParser().parse(dataString).getAsJsonObject();
                fileStream.close();
            }
        } catch (JsonParseException | IllegalStateException | IOException ex) {
            ex.printStackTrace();
            System.out.println("SkyblockAddons: There was an error loading the language file.");
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void saveConfig() {
        settingsConfig = new JsonObject();
        try {
            settingsConfigFile.createNewFile();
            FileWriter writer = new FileWriter(settingsConfigFile);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);

            JsonArray jsonArray = new JsonArray();
            for (Feature element : disabledFeatures) {
                jsonArray.add(new GsonBuilder().create().toJsonTree(element.getId()));
            }
            settingsConfig.add("disabledFeatures", jsonArray);
            settingsConfig.addProperty("warningColor", getColor(Feature.WARNING_COLOR).ordinal());
            settingsConfig.addProperty("confirmationColor", getColor(Feature.CONFIRMATION_COLOR).ordinal());
            settingsConfig.addProperty("manaBarColor", getColor(Feature.MANA_BAR_COLOR).ordinal());
            settingsConfig.addProperty("manaBarTextColor", getColor(Feature.MANA_TEXT_COLOR).ordinal());
            settingsConfig.addProperty("manaBarType", manaBarType.ordinal());
            settingsConfig.addProperty("warningSeconds", warningSeconds);
            settingsConfig.addProperty("manaBarX", coordinates.get(Feature.MANA_BAR).getX());
            settingsConfig.addProperty("manaBarY", coordinates.get(Feature.MANA_BAR).getY());
            settingsConfig.addProperty("manaTextX", coordinates.get(Feature.MANA_TEXT).getX());
            settingsConfig.addProperty("manaTextY", coordinates.get(Feature.MANA_TEXT).getY());
            settingsConfig.addProperty("skeletonBarX", coordinates.get(Feature.SKELETON_BAR).getX());
            settingsConfig.addProperty("skeletonBarY", coordinates.get(Feature.MANA_TEXT).getY());
            settingsConfig.addProperty("guiScale", guiScale);
            settingsConfig.addProperty("language", language.getPath());
            settingsConfig.addProperty("backpackStyle", backpackStyle.ordinal());

            bufferedWriter.write(settingsConfig.toString());
            bufferedWriter.close();
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("An error occurred while attempting to save the config!");
        }
    }

    public String getMessage(Message message, String... variables) {
        String text = null;
        if (message.getMessageObject() == MessageObject.SETTING) {
            text = languageConfig.getAsJsonObject("settings").get(message.getMemberName()).getAsString();
        } else if (message.getMessageObject() == MessageObject.BAR_TYPE) {
            text = languageConfig.getAsJsonObject("settings").getAsJsonObject("barTypes").get(message.getMemberName()).getAsString();
        } else if (message.getMessageObject() == MessageObject.ICON_TYPE) {
            text = languageConfig.getAsJsonObject("settings").getAsJsonObject("iconTypes").get(message.getMemberName()).getAsString();
        } else if (message.getMessageObject() == MessageObject.STYLE) {
            text = languageConfig.getAsJsonObject("settings").getAsJsonObject("styles").get(message.getMemberName()).getAsString();
        } else if (message.getMessageObject() == MessageObject.MESSAGES) {
            text = languageConfig.getAsJsonObject("messages").get(message.getMemberName()).getAsString();
        } else if (message.getMessageObject() == MessageObject.ROOT) {
            text = languageConfig.get(message.getMemberName()).getAsString();
        }
        if (text != null) {
            if (message == Message.SETTING_WARNING_TIME) {
                text = text.replace("%time%", String.valueOf(warningSeconds));
            } else if (message == Message.SETTING_MANA_BAR) {
                text = text.replace("%type%", manaBarType.getDisplayText());
            } else if (message == Message.SETTING_HEALTH_BAR) {
                text = text.replace("%type%", healthBarType.getDisplayText());
            } else if (message == Message.SETTING_DEFENCE_ICON) {
                text = text.replace("%type%", defenceIconType.getDisplayText());
            } else if (message == Message.SETTING_GUI_SCALE) {
                text = text.replace("%scale%", variables[0]);
            } else if (message == Message.MESSAGE_NEW_VERSION) {
                text = text.replace("%newestVersion%", variables[0]);
            } else if (message == Message.SETTING_BACKPACK_STYLE) {
                text = text.replace("%style%", backpackStyle.getDisplayText());
            } else if (message == Message.MESSAGE_DEVELOPMENT_VERSION) {
                text = text.replace("%version%", variables[0]).replace("%newestVersion%", variables[1]);
            } else if (message == Message.LANGUAGE) {
                text = "Language: "+text;
            }
        }
        return text;
    }

    public enum Message {
        LANGUAGE(MessageObject.ROOT, "language"),

        SETTING_MAGMA_BOSS_WARNING(MessageObject.SETTING, "magmaBossWarning"),
        SETTING_ITEM_DROP_CONFIRMATION(MessageObject.SETTING, "itemDropConfirmation"),
        SETTING_WARNING_TIME(MessageObject.SETTING, "warningTime"),
        SETTING_MANA_BAR(MessageObject.SETTING, "manaBar"),
        SETTING_HIDE_SKELETON_HAT_BONES(MessageObject.SETTING, "hideSkeletonHatBones"),
        SETTING_SKELETON_HAT_BONES_BAR(MessageObject.SETTING, "skeletonHatBonesBar"),
        SETTING_HIDE_FOOD_AND_ARMOR(MessageObject.SETTING, "hideFoodAndArmor"),
        SETTING_FULL_INVENTORY_WARNING(MessageObject.SETTING, "fullInventoryWarning"),
        SETTING_MAGMA_BOSS_HEALTH_BAR(MessageObject.SETTING, "magmaBossHealthBar"),
        SETTING_DISABLE_EMBER_ROD_ABILITY(MessageObject.SETTING, "disableEmberRodAbility"),
        SETTING_WARNING_COLOR(MessageObject.SETTING, "warningColor"),
        SETTING_CONFIRMATION_COLOR(MessageObject.SETTING, "confirmationColor"),
        SETTING_MANA_TEXT_COLOR(MessageObject.SETTING, "manaTextColor"),
        SETTING_MANA_BAR_COLOR(MessageObject.SETTING, "manaBarColor"),
        SETTING_EDIT_LOCATIONS(MessageObject.SETTING, "editLocations"),
        SETTING_GUI_SCALE(MessageObject.SETTING, "guiScale"),
        SETTING_RESET_LOCATIONS(MessageObject.SETTING, "resetLocations"),
        SETTING_SETTINGS(MessageObject.SETTING, "settings"),
        SETTING_EDIT_SETTINGS(MessageObject.SETTING, "openSettings"),
        SETTING_HIDE_DURABILITY(MessageObject.SETTING, "hideDurability"),
        SETTING_ENCHANTS_AND_REFORGES(MessageObject.SETTING, "showEnchantmentsReforges"),
        SETTING_MINION_STOP_WARNING(MessageObject.SETTING, "minionStopWarning"),
        SETTING_AUCTION_HOUSE_PLAYERS(MessageObject.SETTING, "hideActionHousePlayers"),
        SETTING_HEALTH_BAR(MessageObject.SETTING, "healthBar"),
        SETTING_DEFENCE_ICON(MessageObject.SETTING, "defenceIcon"),
        SETTING_BACKPACK_STYLE(MessageObject.SETTING, "backpackStyle"),
        SETTING_SHOW_BACKPACK_PREVIEW(MessageObject.SETTING, "showBackpackPreview"),

        STYLE_GUI(MessageObject.STYLE, "inventory"),
        STYLE_COMPACT(MessageObject.STYLE, "compact"),

        BAR_TYPE_BAR_TEXT(MessageObject.BAR_TYPE, "barAndText"),
        BAR_TYPE_BAR(MessageObject.BAR_TYPE, "bar"),
        BAR_TYPE_TEXT(MessageObject.BAR_TYPE, "text"),
        BAR_TYPE_OFF(MessageObject.BAR_TYPE, "off"),

        ICON_TYPE_ICON_DEFENCE_PERCENTAGE(MessageObject.ICON_TYPE, "iconDefenceAndPercentage"),
        ICON_TYPE_ICON_DEFENCE(MessageObject.ICON_TYPE, "iconAndDefence"),
        ICON_TYPE_ICON_PERCENTAGE(MessageObject.ICON_TYPE, "iconAndPercentage"),
        ICON_TYPE_DEFENCE_PERCENTAGE(MessageObject.ICON_TYPE, "defenceAndPercentage"),
        ICON_TYPE_DEFENCE(MessageObject.ICON_TYPE, "defence"),
        ICON_TYPE_PERCENTAGE(MessageObject.ICON_TYPE, "percentage"),
        ICON_TYPE_ICON(MessageObject.ICON_TYPE, "icon"),
        ICON_TYPE_OFF(MessageObject.ICON_TYPE, "off"),

        MESSAGE_DROP_CONFIRMATION(MessageObject.MESSAGES, "dropConfirmation"),
        MESSAGE_MAGMA_BOSS_WARNING(MessageObject.MESSAGES, "magmaBossWarning"),
        MESSAGE_FULL_INVENTORY(MessageObject.MESSAGES, "fullInventory"),
        MESSAGE_NEW_VERSION(MessageObject.MESSAGES, "newVersion"),
        MESSAGE_LABYMOD(MessageObject.MESSAGES, "labymod"),
        MESSAGE_DISCORD(MessageObject.MESSAGES, "discord"),
        MESSAGE_DEVELOPMENT_VERSION(MessageObject.MESSAGES, "developmentVersion");


        private MessageObject messageObject;
        private String memberName;

        Message(MessageObject messageObject, String memberName) {
            this.messageObject = messageObject;
            this.memberName = memberName;
        }

        public MessageObject getMessageObject() {
            return messageObject;
        }

        public String getMemberName() {
            return memberName;
        }
    }

    private enum MessageObject {
        ROOT,
        SETTING,
        BAR_TYPE,
        MESSAGES,
        STYLE,
        ICON_TYPE
    }

    public Feature.BarType getManaBarType() {
        return manaBarType;
    }

    public Feature.BarType getHealthBarType() {
        return healthBarType;
    }

    public Set<Feature> getDisabledFeatures() {
        return disabledFeatures;
    }

    public void setManaBarType(Feature.BarType barType) {
        this.manaBarType = barType;
    }

    public void setHealthBarType(Feature.BarType healthBarType) {
        this.healthBarType = healthBarType;
    }

    public Feature.IconType getDefenceIconType() {
        return defenceIconType;
    }

    public void setDefenceIconType(Feature.IconType defenceIconType) {
        this.defenceIconType = defenceIconType;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public Language getLanguage() {
        return language;
    }

    public void setColor(Feature feature, ConfigColor color) {
        featureColors.put(feature, color);
    }

    public ConfigColor getColor(Feature feature) {
        return featureColors.getOrDefault(feature, ConfigColor.RED);
    }

    public int getWarningSeconds() {
        return warningSeconds;
    }

    public void setWarningSeconds(int warningSeconds) {
        this.warningSeconds = warningSeconds;
    }

    public CoordsPair getCoords(Feature feature) {
        return coordinates.getOrDefault(feature, new CoordsPair(0,0));
    }

    public void setCoords(Feature feature, int x, int maxX, int y, int maxY) {
        if (coordinates.containsKey(feature)) {
            coordinates.get(feature).setX((float)x/maxX);
            coordinates.get(feature).setY((float)y/maxY);
        } else {
            coordinates.put(feature, new CoordsPair((float)x/maxX, (float)y/maxY));
        }
    }

    public float getGuiScale() {
        return guiScale;
    }

    public void setGuiScale(float guiScale) {
        this.guiScale = guiScale;
    }

    public Feature.BackpackStyle getBackpackStyle() {
        return backpackStyle;
    }

    public void setBackpackStyle(Feature.BackpackStyle backpackStyle) {
        this.backpackStyle = backpackStyle;
    }
}
