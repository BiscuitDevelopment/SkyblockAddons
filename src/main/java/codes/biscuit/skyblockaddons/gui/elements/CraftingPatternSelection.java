package codes.biscuit.skyblockaddons.gui.elements;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.CraftingPattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

/**
 * GUI Element that lets the user select a {@link CraftingPattern}
 *
 * @author DidiSkywalker
 */
public class CraftingPatternSelection {

    /**
     * Icon size in pixel
     */
    public static final int ICON_SIZE = 32;

    /**
     * Margin value to use between the icons
     */
    private static final int MARGIN = 2;

    /**
     * Currently selected crafting pattern
     */
    public static CraftingPattern selectedPattern = CraftingPattern.FREE;
    private final Minecraft mc;
    private final int x;
    private final int y;

    public CraftingPatternSelection(Minecraft mc, int x, int y) {
        this.mc = mc;
        this.x = x;
        this.y = y;
    }

    public void draw() {
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        Minecraft.getMinecraft().getTextureManager().bindTexture(CraftingPattern.ICONS);
        for(CraftingPattern craftingPattern : CraftingPattern.values()) {
            int offset = getYOffsetByIndex(craftingPattern.index);
            GlStateManager.color(1,1,1, 1F);
            mc.ingameGUI.drawTexturedModalRect(x, y+ offset, 0, offset, ICON_SIZE, ICON_SIZE);
            if(craftingPattern != selectedPattern) {
                GlStateManager.color(1,1,1, .5F);
                mc.ingameGUI.drawTexturedModalRect(x, y+ offset, 33, 0, ICON_SIZE, ICON_SIZE);
            }
        }
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if(mouseButton != 0
                || mouseX < this.x || mouseX > this.x + ICON_SIZE
                || mouseY < this.y || mouseY > this.y + CraftingPattern.values().length * (ICON_SIZE + MARGIN)) {
            return; // cannot hit
        }

        for (CraftingPattern craftingPattern : CraftingPattern.values()) {
            int offset = getYOffsetByIndex(craftingPattern.index);
            if(mouseY > this.y + offset && mouseY < this.y + offset + ICON_SIZE) {
                if(selectedPattern != craftingPattern) {
                    SkyblockAddons.getInstance().getUtils().playSound("gui.button.press", 1F);
                    selectedPattern = craftingPattern;
                }
            }
        }
    }

    private int getYOffsetByIndex(int index) {
        return index * (ICON_SIZE + MARGIN);
    }
}
