package codes.biscuit.skyblockaddons.gui;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.gui.buttons.ButtonColorBox;
import codes.biscuit.skyblockaddons.gui.elements.CheckBox;
import codes.biscuit.skyblockaddons.utils.ChromaManager;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.Feature;
import codes.biscuit.skyblockaddons.utils.Message;
import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import codes.biscuit.skyblockaddons.utils.nifty.reflection.MinecraftReflection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ColorSelectionGui extends GuiScreen {

    private static final ResourceLocation COLOR_PICKER = new ResourceLocation("skyblockaddons", "colorpicker.png");
    private BufferedImage COLOR_PICKER_IMAGE;

    // The feature that this color is for.
    private Feature feature;

    // Previous pages for when they return.
    private EnumUtils.GuiTab lastTab;
    private int lastPage;

    private int imageX;
    private int imageY;

    private GuiTextField hexColorField;

    private CheckBox chromaCheckbox;

    /**
     * Creates a gui to allow you to select a color for a specific feature.
     *
     * @param feature The feature that this color is for.
     * @param lastTab The previous tab that you came from.
     * @param lastPage The previous page.
     */
    ColorSelectionGui(Feature feature, EnumUtils.GuiTab lastTab, int lastPage) {
        this.feature = feature;
        this.lastTab = lastTab;
        this.lastPage = lastPage;

        try {
            COLOR_PICKER_IMAGE = TextureUtil.readBufferedImage(Minecraft.getMinecraft().getResourceManager().getResource(COLOR_PICKER).getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initGui() {
        chromaCheckbox = new CheckBox(mc, width / 2 + 88, 170, 12, "Chroma", false);

        chromaCheckbox.setOnToggleListener(value -> {
            ChromaManager.setFeature(feature, value);
        });

        hexColorField = new GuiTextField(0, (FontRenderer)MinecraftReflection.FontRenderer.getFontRenderer(),
                width/2+110-50, 220, 100, 15);
        hexColorField.setMaxStringLength(7);
        hexColorField.setFocused(true);

        // Set the current color in the text box after creating it.
        setTextBoxHex(SkyblockAddons.getInstance().getConfigValues().getColor(feature));

        if (feature.getGuiFeatureData().isColorsRestricted()) {

            // This creates the 16 buttons for all the color codes.

            int collumn = 1;
            int x = width / 2 - 160;
            int y = 120;

            for (ChatFormatting chatFormatting : ChatFormatting.values()) {
                if (chatFormatting.isFormat() || chatFormatting == ChatFormatting.RESET) continue;

                buttonList.add(new ButtonColorBox(x, y, chatFormatting));

                if (collumn < 6) { // 6 buttons per row.
                    collumn++; // Go to the next collumn once the 6 are over.
                    x += ButtonColorBox.WIDTH + 15; // 15 spacing.
                } else {
                    y += ButtonColorBox.HEIGHT + 20; // Go to next row.
                    collumn = 1; // Reset the collumn.
                    x = width / 2 - 160; // Reset the x vlue.
                }
            }
        }

        Keyboard.enableRepeatEvents(true);

        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        // Draw background and default text.
        int startColor = new Color(0,0, 0, 128).getRGB();
        int endColor = new Color(0,0, 0, 192).getRGB();
        drawGradientRect(0, 0, width, height, startColor, endColor);
        SkyblockAddonsGui.drawDefaultTitleText(this, 255);

        if (feature.getGuiFeatureData() != null) {
            if (feature.getGuiFeatureData().isColorsRestricted()) {
                SkyblockAddonsGui.drawScaledString(this, Message.MESSAGE_CHOOSE_A_COLOR.getMessage(), 90,
                        SkyblockAddons.getInstance().getUtils().getDefaultBlue(255), 1.5, 0);

            } else {
                int pickerWidth = COLOR_PICKER_IMAGE.getWidth();
                int pickerHeight = COLOR_PICKER_IMAGE.getHeight();

                imageX = width / 2 - 200;
                imageY = 90;

                if (SkyblockAddons.getInstance().getConfigValues().getChromaFeatures().contains(feature)) { // Fade out color picker if chroma enabled
                    GlStateManager.color(0.5F, 0.5F, 0.5F, 0.7F);
                    GlStateManager.enableBlend();
                }

                // Draw the color picker with no scaling so the size is the exact same.
                mc.getTextureManager().bindTexture(COLOR_PICKER);
                Gui.drawModalRectWithCustomSizedTexture(imageX, imageY, 0, 0, pickerWidth, pickerHeight, pickerWidth, pickerHeight);

                SkyblockAddonsGui.drawScaledString(this, Message.MESSAGE_SELECTED_COLOR.getMessage(), 120,
                        SkyblockAddons.getInstance().getUtils().getDefaultBlue(255), 1.5, 75);
                drawRect(width / 2 + 90, 140, width / 2 + 130, 160, SkyblockAddons.getInstance().getConfigValues().getColor(feature).getRGB());

                if (chromaCheckbox != null) chromaCheckbox.draw();

                if (!SkyblockAddons.getInstance().getConfigValues().getChromaFeatures().contains(feature)) { // Disabled cause chroma is enabled
                    SkyblockAddonsGui.drawScaledString(this, Message.MESSAGE_SET_HEX_COLOR.getMessage(), 200,
                            SkyblockAddons.getInstance().getUtils().getDefaultBlue(255), 1.5, 75);
                    hexColorField.drawTextBox();
                }
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (!feature.getGuiFeatureData().isColorsRestricted() && !SkyblockAddons.getInstance().getConfigValues().getChromaFeatures().contains(feature)) {
            int xPixel = mouseX - imageX;
            int yPixel = mouseY - imageY;

            // If the mouse is over the color picker.
            if (xPixel > 0 && xPixel < COLOR_PICKER_IMAGE.getWidth() &&
                    yPixel > 0 && yPixel < COLOR_PICKER_IMAGE.getHeight()) {

                // Get the color of the clicked pixel.
                Color selectedColor = new Color(COLOR_PICKER_IMAGE.getRGB(xPixel, yPixel), true);

                // Choose this color.
                if (selectedColor.getAlpha() == 255) {
                    SkyblockAddons.getInstance().getConfigValues().setColor(feature, selectedColor.getRGB());
                    setTextBoxHex(selectedColor);

                    SkyblockAddons.getInstance().getUtils().playSound("gui.button.press", 0.25, 1);
                }
            }

            hexColorField.mouseClicked(mouseX, mouseY, mouseButton);
        }

        if (chromaCheckbox != null) chromaCheckbox.onMouseClick(mouseX, mouseY, mouseButton);

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void setTextBoxHex(Color color) {
        hexColorField.setText(String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()));
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        if (hexColorField.isFocused()) {
            hexColorField.textboxKeyTyped(typedChar, keyCode);

            String text = hexColorField.getText();
            if (text.startsWith("#")) { // Get rid of the #.
                text = text.substring(1);
            }

            if (text.length() == 6) {
                int typedColor;
                try {
                    typedColor = Integer.parseInt(text, 16); // Try to read the hex value and put it in an integer.
                } catch (NumberFormatException ex) {
                    ex.printStackTrace(); // This just means it wasn't in the format of a hex number- that's fine!
                    return;
                }

                SkyblockAddons.getInstance().getConfigValues().setColor(feature, typedColor);
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button instanceof ButtonColorBox) {
            ButtonColorBox colorBox = (ButtonColorBox)button;
            SkyblockAddons.getInstance().getConfigValues().setColor(feature, colorBox.getColor().getRGB());
            this.mc.displayGuiScreen(null);
        }

        super.actionPerformed(button);
    }

    @Override
    public void updateScreen() {
        hexColorField.updateCursorCounter();

        super.updateScreen();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);

        SkyblockAddons.getInstance().getRenderListener().setGuiToOpen(EnumUtils.GUIType.SETTINGS, lastPage, lastTab, feature);
    }
}
