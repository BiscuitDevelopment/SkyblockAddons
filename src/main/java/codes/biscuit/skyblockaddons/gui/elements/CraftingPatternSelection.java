package codes.biscuit.skyblockaddons.gui.elements;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.features.craftingpatterns.CraftingPattern;
import codes.biscuit.skyblockaddons.core.Message;
import codes.biscuit.skyblockaddons.config.PersistentValuesManager;
import codes.biscuit.skyblockaddons.utils.Utils;
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

    private final Minecraft mc;
    private final int x;
    private final int y;
    private final CheckBox blockIncompleteCheckBox;

    public CraftingPatternSelection(Minecraft mc, int x, int y) {
        this.mc = mc;
        this.x = x;
        this.y = y;
        int checkBoxY = (y - MARGIN - 8);
        String checkBoxText = Message.MESSAGE_BLOCK_INCOMPLETE_PATTERNS.getMessage();

        PersistentValuesManager persistentValuesManager = SkyblockAddons.getInstance().getPersistentValuesManager();
        blockIncompleteCheckBox = new CheckBox(mc, x, checkBoxY, 8, checkBoxText, persistentValuesManager.getPersistentValues().isBlockCraftingIncompletePatterns());
        blockIncompleteCheckBox.setOnToggleListener(value -> {
            persistentValuesManager.getPersistentValues().setBlockCraftingIncompletePatterns(value);
            persistentValuesManager.saveValues();
        });
    }

    public void draw() {
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        Minecraft.getMinecraft().getTextureManager().bindTexture(CraftingPattern.ICONS);
        GlStateManager.color(1,1,1, 1F);
        for(CraftingPattern craftingPattern : CraftingPattern.values()) {
            int offset = getYOffsetByIndex(craftingPattern.index);
            GlStateManager.color(1,1,1, 1F);
            mc.ingameGUI.drawTexturedModalRect(x, y+ offset, 0, offset, ICON_SIZE, ICON_SIZE);
            if(craftingPattern != SkyblockAddons.getInstance().getPersistentValuesManager().getPersistentValues().getSelectedCraftingPattern()) {
                GlStateManager.color(1,1,1, .5F);
                mc.ingameGUI.drawTexturedModalRect(x, y+ offset, 33, 0, ICON_SIZE, ICON_SIZE);
            }
        }
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();

        blockIncompleteCheckBox.draw();
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        blockIncompleteCheckBox.onMouseClick(mouseX, mouseY, mouseButton);
        if(mouseButton != 0
                || mouseX < this.x || mouseX > this.x + ICON_SIZE
                || mouseY < this.y || mouseY > this.y + CraftingPattern.values().length * (ICON_SIZE + MARGIN)) {
            return; // cannot hit
        }

        PersistentValuesManager persistentValuesManager =  SkyblockAddons.getInstance().getPersistentValuesManager();

        for (CraftingPattern craftingPattern : CraftingPattern.values()) {
            int offset = getYOffsetByIndex(craftingPattern.index);
            if(mouseY > this.y + offset && mouseY < this.y + offset + ICON_SIZE) {
                if(persistentValuesManager.getPersistentValues().getSelectedCraftingPattern() != craftingPattern) {
                    SkyblockAddons.getInstance().getUtils().playLoudSound("gui.button.press", 1F);
                    persistentValuesManager.getPersistentValues().setSelectedCraftingPattern(craftingPattern);
                    persistentValuesManager.saveValues();
                }
            }
        }

        Utils.blockNextClick = true;
    }

    private int getYOffsetByIndex(int index) {
        return index * (ICON_SIZE + MARGIN);
    }
}
