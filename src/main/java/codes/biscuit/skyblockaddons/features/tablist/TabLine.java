package codes.biscuit.skyblockaddons.features.tablist;

import lombok.Getter;
import net.minecraft.client.Minecraft;

public class TabLine {

    @Getter private TabStringType type;
    @Getter private String text;

    public TabLine(String text, TabStringType type) {
        this.type = type;
        this.text = text;
    }

    public int getWidth() {
        Minecraft mc = Minecraft.getMinecraft();

        int width = mc.fontRendererObj.getStringWidth(text);

        if (type == TabStringType.PLAYER) {
            width += 8 + 2; // Player head
        }

        if (type == TabStringType.TEXT) {
            width += 4; // Space is 4
        }

        return width;
    }
}
