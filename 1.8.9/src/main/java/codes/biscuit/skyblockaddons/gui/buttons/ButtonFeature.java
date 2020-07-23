package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.core.Feature;
import net.minecraft.client.gui.GuiButton;

public class ButtonFeature extends GuiButton {

    // The feature that this button moves.
    public Feature feature;

    /**
     * Create a button that is assigned a feature (to toggle/change color etc.).
     */
    ButtonFeature(int buttonId, int x, int y, String buttonText, Feature feature) {
        super(buttonId, x, y, buttonText);
        this.feature = feature;
    }

    public Feature getFeature() {
        return feature;
    }
}
