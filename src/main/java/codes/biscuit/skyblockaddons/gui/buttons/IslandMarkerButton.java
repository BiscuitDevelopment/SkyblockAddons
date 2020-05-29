package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.IslandWarpGui;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.geom.Point2D;

public class IslandMarkerButton extends GuiButton {

    public static final int MAX_SELECT_RADIUS = 90;

    private static final ResourceLocation PORTAL_ICON = new ResourceLocation("skyblockaddons", "portal.png");

    @Getter private IslandWarpGui.Marker marker;

    private float centerX;
    private float centerY;
    private boolean unlocked;

    public IslandMarkerButton(IslandWarpGui.Marker marker) {
        super(0, 0, 0, marker.getLabel());

        this.marker = marker;
    }

    public void drawButton(int islandX, int islandY, float expansion, boolean hovered, boolean islandUnlocked, IslandWarpGui.UnlockedStatus status) {
        Minecraft mc = Minecraft.getMinecraft();
        SkyblockAddons main =  SkyblockAddons.getInstance();

        int iconSize = (int) (50*expansion);

        float centerX = islandX+(marker.getX())*expansion;
        float centerY = islandY+(marker.getY())*expansion;

        this.centerX = centerX;
        this.centerY = centerY;
        this.unlocked = status == IslandWarpGui.UnlockedStatus.UNLOCKED || status == IslandWarpGui.UnlockedStatus.IN_COMBAT;

        int x = (int) (centerX-(iconSize/2));
        int y = (int) (centerY-(iconSize/2));

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

//        drawRect(x+(iconSize/2)-70, y+(iconSize/2)-70,
//                x+(iconSize/2)+70, y+(iconSize/2)+70, 0xFFFF0000);

        mc.getTextureManager().bindTexture(PORTAL_ICON);
        main.getUtils().drawModalRectWithCustomSizedTexture(x, y, 0, 0, iconSize, Math.round(iconSize*1.23F), iconSize, Math.round(iconSize*1.23F));

        if (hovered) {
            GlStateManager.pushMatrix();
            float textScale = 2.5F;
            textScale *= expansion;
            GlStateManager.scale(textScale, textScale, 1);

            int alpha = Math.max((int)(((expansion-1)/0.1)*255), 4);
            int color;
            if (this.unlocked) {
                color = main.getUtils().getColorWithAlpha(0xFFFFFF, alpha);
            } else {
                color = main.getUtils().getColorWithAlpha(0x999999, alpha);
            }

            mc.fontRendererObj.drawStringWithShadow(displayString, (x+(iconSize/2))/textScale - mc.fontRendererObj.getStringWidth(displayString)/2F, (y-20)/textScale, color);
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
