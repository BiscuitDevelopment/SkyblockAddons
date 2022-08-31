package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.gui.IslandWarpGui;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.geom.Point2D;

public class IslandMarkerButton extends GuiButton {

    public static final int MAX_SELECT_RADIUS = 90;

    private static final ResourceLocation PORTAL_ICON = new ResourceLocation("skyblockaddons", "portal.png");

    @Getter private final IslandWarpGui.Marker marker;

    private float centerX;
    private float centerY;
    private boolean unlocked;

    public IslandMarkerButton(IslandWarpGui.Marker marker) {
        super(0, 0, 0, marker.getLabel());

        this.marker = marker;
    }

    public void drawButton(float islandX, float islandY, float expansion, boolean hovered, boolean islandUnlocked, IslandWarpGui.UnlockedStatus status) {
        Minecraft mc = Minecraft.getMinecraft();
        status = IslandWarpGui.UnlockedStatus.UNLOCKED;

        float width = 50*expansion;
        float height = width*(100/81F); // Ratio is 81w : 100h

        float centerX = islandX+(marker.getX())*expansion;
        float centerY = islandY+(marker.getY())*expansion;

        this.centerX = centerX;
        this.centerY = centerY;

        this.unlocked = status == IslandWarpGui.UnlockedStatus.UNLOCKED ||
                status == IslandWarpGui.UnlockedStatus.IN_COMBAT;

        float x = centerX-(width/2);
        float y = centerY-(height/2);

        if (this.unlocked) {
            if (hovered) {
                GlStateManager.color(1, 1, 1, 1F);
            } else {
                GlStateManager.color(1, 1, 1, 0.6F);
            }
        } else {
            if (islandUnlocked) {
                GlStateManager.color(0.3F, 0.3F, 0.3F, 1F);
            } else {
                GlStateManager.color(0.3F, 0.3F, 0.3F, 0.6F);
            }
        }

        mc.getTextureManager().bindTexture(PORTAL_ICON);
        DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, width, height, width, height, true);

        if (hovered) {
            GlStateManager.pushMatrix();
            float textScale = 2.5F;
            textScale *= expansion;
            GlStateManager.scale(textScale, textScale, 1);

            int alpha = Math.max((int)(((expansion-1)/0.1)*255), 4);
            int color;
            if (this.unlocked) {
                color = ColorCode.WHITE.getColor();
            } else {
                color = ColorUtils.setColorAlpha(0x999999, alpha);
            }

            mc.fontRendererObj.drawStringWithShadow(displayString, (x+(width/2))/textScale - mc.fontRendererObj.getStringWidth(displayString)/2F, (y-20)/textScale, color);
            GlStateManager.color(1,1,1,1);

            GlStateManager.popMatrix();
        }
    }

    public double getDistance(int mouseX, int mouseY) {
        double distance = new Point2D.Double(mouseX, mouseY).distance(new Point2D.Double(this.centerX, this.centerY));

        if (distance > MAX_SELECT_RADIUS || !unlocked) distance = -1;

        return distance;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return false;
    }
}
