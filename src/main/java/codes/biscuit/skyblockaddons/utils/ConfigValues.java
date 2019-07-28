package codes.biscuit.skyblockaddons.utils;

import com.google.gson.*;

import java.io.*;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class ConfigValues {

    private File settingsConfigFile;
    private JsonObject settingsConfig = new JsonObject();

    private JsonObject languageConfig = new JsonObject();

    private Set<Feature> disabledFeatures = EnumSet.noneOf(Feature.class);
    private Map<Feature, ConfigColor> featureColors = new EnumMap<>(Feature.class);
    private Feature.ManaBarType manaBarType = Feature.ManaBarType.BAR_TEXT;
    private int warningSeconds = 4;
    private float manaBarX = 0.45F;
    private float manaBarY = 0.83F;
    private float skeletonBarX = 0.68F;
    private float skeletonBarY = 0.93F;
    private float guiScale = 0;
    private float manaTextX = 0.45F;
    private float manaTextY = 0.83F;
    private String enchantment = "Enchantment";
    private Feature.Language language = Feature.Language.ENGLISH;

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
                    addDefaultsAndSave();
                    return;
                }
                settingsConfig = fileElement.getAsJsonObject();
            } catch (JsonParseException | IllegalStateException | IOException ex) {
                ex.printStackTrace();
                System.out.println("SkyblockAddons: There was an error loading the config. Resetting to defaults.");
                addDefaultsAndSave();
                return;
            }
            for (JsonElement element : settingsConfig.getAsJsonArray("disabledFeatures")) {
                disabledFeatures.add(Feature.fromId(element.getAsInt()));
            }
            warningSeconds = settingsConfig.get("warningSeconds").getAsInt();
            if (settingsConfig.has("manaBarType")) {
                manaBarType = Feature.ManaBarType.values()[settingsConfig.get("manaBarType").getAsInt()];
            }
            if (settingsConfig.has("manaBarX")) {
                manaBarX = settingsConfig.get("manaBarX").getAsFloat();
            }
            if (settingsConfig.has("manaBarY")) {
                manaBarY = settingsConfig.get("manaBarY").getAsFloat();
            }
            if (settingsConfig.has("skeletonBarX")) {
                skeletonBarX = settingsConfig.get("skeletonBarX").getAsFloat();
            }
            if (settingsConfig.has("skeletonBarY")) {
                skeletonBarY = settingsConfig.get("skeletonBarY").getAsFloat();
            }
            if (settingsConfig.has("warningColor")) { // migrate from old config
                featureColors.put(Feature.WARNING_COLOR, ConfigColor.values()[settingsConfig.get("warningColor").getAsInt()]);
            } else {
                featureColors.put(Feature.WARNING_COLOR, ConfigColor.RED);
            }
            if (settingsConfig.has("confirmationColor")) { // migrate from old config
                featureColors.put(Feature.CONFIRMATION_COLOR, ConfigColor.values()[settingsConfig.get("confirmationColor").getAsInt()]);
            } else {
                featureColors.put(Feature.CONFIRMATION_COLOR, ConfigColor.RED);
            }
            if (settingsConfig.has("manaBarColor")) { // migrate from old config
                featureColors.put(Feature.MANA_BAR_COLOR, ConfigColor.values()[settingsConfig.get("manaBarColor").getAsInt()]);
            } else {
                featureColors.put(Feature.MANA_BAR_COLOR, ConfigColor.BLUE);
            }
            if (settingsConfig.has("manaBarTextColor")) { // migrate from old config
                featureColors.put(Feature.MANA_TEXT_COLOR, ConfigColor.values()[settingsConfig.get("manaBarTextColor").getAsInt()]);
            } else {
                featureColors.put(Feature.MANA_TEXT_COLOR, ConfigColor.BLUE);
            }
            if (settingsConfig.has("guiScale")) {
                guiScale = settingsConfig.get("guiScale").getAsFloat();
            }
            if (settingsConfig.has("manaTextX")) {
                manaTextX = settingsConfig.get("manaTextX").getAsFloat();
            }
            if (settingsConfig.has("manaTextY")) {
                manaTextY = settingsConfig.get("manaTextY").getAsFloat();
            }
            if (settingsConfig.has("enchantment")) {
            	enchantment = settingsConfig.get("enchantment").getAsString();
            }
            if (settingsConfig.has("language")) {
                language = Feature.Language.getFromPath(settingsConfig.get("language").getAsString());
            }
        } else {
            addDefaultsAndSave();
        }
        loadLanguageFile();
    }

    private void addDefaultsAndSave() {
        featureColors.put(Feature.CONFIRMATION_COLOR, ConfigColor.RED);
        featureColors.put(Feature.WARNING_COLOR, ConfigColor.RED);
        featureColors.put(Feature.MANA_TEXT_COLOR, ConfigColor.BLUE);
        featureColors.put(Feature.MANA_BAR_COLOR, ConfigColor.BLUE);
        disabledFeatures.add(Feature.DROP_CONFIRMATION);
        disabledFeatures.add(Feature.MINION_STOP_WARNING);
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
//            settingsConfig.addProperty("inventoryWarningSeconds", inventoryWarningSeconds);
            settingsConfig.addProperty("manaBarX", manaBarX);
            settingsConfig.addProperty("manaBarY", manaBarY);
            settingsConfig.addProperty("manaTextX", manaTextX);
            settingsConfig.addProperty("manaTextY", manaTextY);
            settingsConfig.addProperty("skeletonBarX", skeletonBarX);
            settingsConfig.addProperty("skeletonBarY", skeletonBarY);
            settingsConfig.addProperty("guiScale", guiScale);
            settingsConfig.addProperty("language", language.getPath());
            settingsConfig.addProperty("enchantment", enchantment);

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
        } else if (message.getMessageObject() == MessageObject.MANA_BAR_TYPE) {
            text = languageConfig.getAsJsonObject("settings").getAsJsonObject("manaBarTypes").get(message.getMemberName()).getAsString();
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
            } else if (message == Message.SETTING_GUI_SCALE) {
                text = text.replace("%scale%", variables[0]);
            } else if (message == Message.MESSAGE_NEW_VERSION) {
                text = text.replace("%newestVersion%", variables[0]);
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
        SETTING_ENCHANTMENT(MessageObject.SETTING, "enchantment"),

        MANA_BAR_TYPE_BAR_TEXT(MessageObject.MANA_BAR_TYPE, "barAndText"),
        MANA_BAR_TYPE_BAR(MessageObject.MANA_BAR_TYPE, "bar"),
        MANA_BAR_TYPE_TEXT(MessageObject.MANA_BAR_TYPE, "text"),
        MANA_BAR_TYPE_OFF(MessageObject.MANA_BAR_TYPE, "off"),

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
        MANA_BAR_TYPE,
        MESSAGES
    }

    public Feature.ManaBarType getManaBarType() {
        return manaBarType;
    }

    public Set<Feature> getDisabledFeatures() {
        return disabledFeatures;
    }

    public void setManaBarType(Feature.ManaBarType manaBarType) {
        this.manaBarType = manaBarType;
    }

    public void setLanguage(Feature.Language language) {
        this.language = language;
    }

    public Feature.Language getLanguage() {
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

    public float getManaBarX() {
        return manaBarX;
    }

    public void setManaBarX(int x, int maxX) {
        this.manaBarX = (float) x / maxX;
    }

    public float getManaBarY() {
        return manaBarY;
    }

    public void setManaBarY(int y, int maxY) {
        this.manaBarY = (float) y / maxY;
    }

    public float getManaTextX() {
        return manaTextX;
    }

    public void setManaTextX(int x, int maxX) {
        this.manaTextX = (float) x / maxX;
    }

    public float getManaTextY() {
        return manaTextY;
    }

    public void setManaTextY(int y, int maxY) {
        this.manaTextY = (float) y / maxY;
    }

    public float getSkeletonBarX() {
        return skeletonBarX;
    }

    public void setSkeletonBarX(int x, int maxX) {
        this.skeletonBarX = (float) x / maxX;
    }

    public float getSkeletonBarY() {
        return skeletonBarY;
    }

    public void setSkeletonBarY(int y, int maxY) {
        this.skeletonBarY = (float) y / maxY;
    }

    public float getGuiScale() {
        return guiScale;
    }

    public void setGuiScale(float guiScale) {
        this.guiScale = guiScale;
    }
    
    public String getEnchantment() {
    	return enchantment;
    }
    
    public void setEnchantment(String enchantment) {
    	this.enchantment = enchantment;
    }
}
