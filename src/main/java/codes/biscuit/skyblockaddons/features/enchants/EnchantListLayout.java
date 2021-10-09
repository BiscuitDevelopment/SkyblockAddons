package codes.biscuit.skyblockaddons.features.enchants;

import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonSelect;

/**
 * Statuses that are shown on the Discord RPC feature
 * <p>
 * This file has LF line endings because ForgeGradle is weird and will throw a NullPointerException if it's CRLF.
 */
public enum EnchantListLayout implements ButtonSelect.SelectItem {

    NORMAL("enchantLayout.titleNormal", "enchantLayout.descriptionNormal"),
    COMPRESS("enchantLayout.titleCompress", "enchantLayout.descriptionCompress"),
    EXPAND("enchantLayout.titleExpand", "enchantLayout.descriptionExpand");

    private final String title;
    private final String description;

    EnchantListLayout(String title, String description) {
        this.title = title;
        this.description = description;
    }

    @Override
    public String getName() {
        return Translations.getMessage(title);
    }

    @Override
    public String getDescription() {
        return Translations.getMessage(description);
    }
}