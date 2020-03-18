package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonChromaSlider;

import java.awt.*;

public class ChromaManager {

    private static float currentHue = 0;
//    private static Color currentColor = new Color(Color.HSBtoRGB(0, 0.72F, 0.90F));

    public static void increment() {
        currentHue+= ButtonChromaSlider.denormalizeScale(SkyblockAddons.getInstance().getConfigValues().getChromaSpeed());
        if (currentHue > 360) {
            currentHue = 0;
        }

//        currentColor = new Color(Color.HSBtoRGB(currentHue/360F, 0.72F, 0.90F));
    }

    public static Color getCurrentColor(int x, int y) { // TODO check performance on this?
        return new Color(Color.HSBtoRGB((((x+y)/2F+currentHue)%360)/360, 0.72F, 0.90F));
    }

    public static void setFeature(Feature feature, boolean state) {
        if (state) {
            SkyblockAddons.getInstance().getConfigValues().getChromaFeatures().add(feature);
        } else {
            SkyblockAddons.getInstance().getConfigValues().getChromaFeatures().remove(feature);
        }
    }
}
