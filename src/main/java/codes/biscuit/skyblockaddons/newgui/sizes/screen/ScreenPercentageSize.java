package codes.biscuit.skyblockaddons.newgui.sizes.screen;

import codes.biscuit.skyblockaddons.newgui.sizes.SizeBase;
import net.minecraft.client.Minecraft;

public class ScreenPercentageSize extends SizeBase {

    private float xPercentage;
    private float yPercentage;

    public ScreenPercentageSize(float xPercentage, float yPercentage) {
        this.xPercentage = xPercentage;
        this.yPercentage = yPercentage;
    }

    public ScreenPercentageSize(float percentage) {
        this(percentage, percentage);
    }

    @Override
    public void updatePositions() {
        y = Minecraft.getMinecraft().displayHeight * xPercentage;
        x = Minecraft.getMinecraft().displayWidth * yPercentage;
    }

    @Override
    public void updateSizes() {
        h = Minecraft.getMinecraft().displayHeight * xPercentage;
        w = Minecraft.getMinecraft().displayWidth * yPercentage;
    }
}
