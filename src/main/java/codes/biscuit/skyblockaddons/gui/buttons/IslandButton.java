package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.gui.IslandWarpGui;
import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.ColorUtils;
import codes.biscuit.skyblockaddons.utils.DrawUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IslandButton extends GuiButton {

    @Getter private List<IslandMarkerButton> markerButtons = new ArrayList<>();

    private boolean disableHover = false;

    private long startedHover = -1;
    private long stoppedHover = -1;

    private IslandWarpGui.Island island;

    private static int ANIMATION_TIME = 200;

    private IslandWarpGui.UnlockedStatus unlockedStatus;
    private Map<IslandWarpGui.Marker, IslandWarpGui.UnlockedStatus> markers;

    public IslandButton(IslandWarpGui.Island island, IslandWarpGui.UnlockedStatus unlockedStatus, Map<IslandWarpGui.Marker, IslandWarpGui.UnlockedStatus> markers) {
        super(0, island.getX(), island.getY(), island.getLabel());

        this.island = island;
        this.unlockedStatus = IslandWarpGui.UnlockedStatus.UNLOCKED;
        this.markers = markers;

        for (IslandWarpGui.Marker marker : IslandWarpGui.Marker.values()) {
            if (marker.getIsland() == island) {
                this.markerButtons.add(new IslandMarkerButton(marker));
            }
        }
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        drawButton(mc, mouseX, mouseY, true);
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY, boolean actuallyDraw) {
        int minecraftScale = new ScaledResolution(mc).getScaleFactor();
        float islandGuiScale = IslandWarpGui.ISLAND_SCALE;

        mouseX *= minecraftScale;
        mouseY *= minecraftScale;

        mouseX /= islandGuiScale;
        mouseY /= islandGuiScale;

        mouseX -= IslandWarpGui.SHIFT_LEFT;
        mouseY -= IslandWarpGui.SHIFT_TOP;

        float x = island.getX();
        float y = island.getY();
        float h = island.getH();
        float w = island.getW();

        float centerX = x+(w/2F);
        float centerY = y+(h/2F);
        float expansion = 1;
        boolean hovered = false;

        if (isHovering()) {
            int hoverTime = (int)(System.currentTimeMillis() - startedHover);
            if (hoverTime > ANIMATION_TIME) {
                hoverTime = ANIMATION_TIME;
            }
            expansion = hoverTime/(float)ANIMATION_TIME;
            expansion *= 0.10;
            expansion += 1;

            h = h*expansion;
            w = w*expansion;
            x = centerX-(w/2F);
            y = centerY-(h/2F);
        } else if (isStoppingHovering()) {
            int hoverTime = (int)(System.currentTimeMillis() - stoppedHover);

            if (hoverTime < ANIMATION_TIME) {
                hoverTime = ANIMATION_TIME - hoverTime;
                expansion = hoverTime/(float)ANIMATION_TIME;
                expansion *= 0.10;
                expansion += 1;

                h = h*expansion;
                w = w*expansion;
                x = centerX-(w/2F);
                y = centerY-(h/2F);
            } else {
                stoppedHover = -1;
            }
        }

        boolean unlocked = unlockedStatus == IslandWarpGui.UnlockedStatus.UNLOCKED || unlockedStatus == IslandWarpGui.UnlockedStatus.IN_COMBAT;

        if (!unlocked) {
            expansion = 1;
            x = island.getX();
            y = island.getY();
            h = island.getH();
            w = island.getW();
        }

        if (mouseX > x && mouseY > y && mouseX < x+w && mouseY < y+h) {
            if (island.getBufferedImage() != null) {
                int xPixel = Math.round(((mouseX - x) * IslandWarpGui.IMAGE_SCALED_DOWN_FACTOR) / expansion);
                int yPixel = Math.round(((mouseY - y) * IslandWarpGui.IMAGE_SCALED_DOWN_FACTOR) / expansion);

                try {
                    int rgb = island.getBufferedImage().getRGB(xPixel, yPixel);
                    int alpha = (rgb & 0xff000000) >> 24;
                    if (alpha != 0) {
                        hovered = true;
                    }
                } catch (IndexOutOfBoundsException ex) {
                    // Can't find pixel, its okay just leave it grey.
                }
            } else {
                hovered = true;
            }
        }

        if (disableHover) {
            disableHover = false;

            hovered = false;
        }

        if (hovered) {
            if (!isHovering()) {
                startedHover = System.currentTimeMillis();

                if (isStoppingHovering()) {
                    int timeSoFar = (int)(System.currentTimeMillis()-stoppedHover);
                    if (timeSoFar > ANIMATION_TIME) {
                        timeSoFar = ANIMATION_TIME;
                    }

                    startedHover -= (ANIMATION_TIME-timeSoFar);
                    stoppedHover = -1;
                }
            }
        } else if (isHovering()) {
            stoppedHover = System.currentTimeMillis();

            int timeSoFar = (int)(System.currentTimeMillis()-startedHover);
            if (timeSoFar > ANIMATION_TIME) {
                timeSoFar = ANIMATION_TIME;
            }

            stoppedHover -= (ANIMATION_TIME-timeSoFar);
            startedHover = -1;
        }

        if (actuallyDraw) {
            if (unlocked) {
                if (unlockedStatus == IslandWarpGui.UnlockedStatus.IN_COMBAT) {
                    GlStateManager.color(1F, 0.6F, 0.6F, 1F);
                } else {
                    if (hovered) {
                        GlStateManager.color(1F, 1F, 1F, 1F);
                    } else {
                        GlStateManager.color(0.9F, 0.9F, 0.9F, 1F);
                    }
                }
            } else {
                GlStateManager.color(0.3F, 0.3F, 0.3F, 1F);
            }

            mc.getTextureManager().bindTexture(island.getResourceLocation());
            DrawUtils.drawModalRectWithCustomSizedTexture(x, y, 0, 0, w, h, w, h);

            for (IslandMarkerButton marker : markerButtons) {
                marker.drawButton(x, y, expansion, hovered, unlocked, this.markers.get(marker.getMarker()));
            }

            GlStateManager.pushMatrix();
            float textScale = 3F;
            textScale *= expansion;
            GlStateManager.scale(textScale, textScale, 1);

            int alpha = Math.max(255 - (int) (((expansion - 1) / 0.1) * 255), 4);
            int color;
            if (unlocked) {
                color = ColorCode.WHITE.getColor();
            } else {
                color = ColorUtils.setColorAlpha(0x999999, alpha);
            }

            mc.fontRendererObj.drawStringWithShadow(displayString, centerX / textScale - mc.fontRendererObj.getStringWidth(displayString) / 2F, centerY / textScale, color);

            if (unlockedStatus != IslandWarpGui.UnlockedStatus.UNLOCKED) {
                mc.fontRendererObj.drawStringWithShadow(unlockedStatus.getMessage(), centerX / textScale - mc.fontRendererObj.getStringWidth(unlockedStatus.getMessage()) / 2F, (centerY + 30) / textScale, color);
            }

            GlStateManager.color(1, 1, 1, 1);

            GlStateManager.popMatrix();
        }
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
//        int minecraftScale = Minecraft.getMinecraft().gameSettings.guiScale;
//        float islandGuiScale = ISLAND_SCALE;
//
//        mouseX *= minecraftScale;
//        mouseY *= minecraftScale;
//
//        mouseX /= islandGuiScale;
//        mouseY /= islandGuiScale;
//
//        mouseX -= IslandWarpGui.SHIFT_LEFT;
//        mouseY -= IslandWarpGui.SHIFT_TOP;

//        for (IslandWarpGui.Island island : IslandWarpGui.Island.values()) {
//            System.out.println(island.getLabel()+" "+(mouseX-island.getX()) + " " + (mouseY-island.getY()));
//        }

        return false;
    }

    public boolean isHovering() {
        return startedHover != -1;
    }

    private boolean isStoppingHovering() {
        return stoppedHover != -1;
    }

    public void setDisableHover(boolean disableHover) {
        this.disableHover = disableHover;
    }
}
