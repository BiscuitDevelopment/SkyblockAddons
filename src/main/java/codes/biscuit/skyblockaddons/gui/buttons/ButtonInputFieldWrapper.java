package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.utils.ColorCode;
import codes.biscuit.skyblockaddons.utils.objects.UpdateCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ButtonInputFieldWrapper extends GuiButton {

    private GuiTextField textField;
    private String placeholderText;
    private UpdateCallback<String> textUpdated;

    public ButtonInputFieldWrapper(int x, int y, int w, int h, String buttonText, int maxLength, boolean focused, UpdateCallback<String> textUpdated) {
        this(x, y, w, h, buttonText, null, maxLength, focused, textUpdated);
    }

    public ButtonInputFieldWrapper(int x, int y, int w, int h, String buttonText, String placeholderText, int maxLength, boolean focused, UpdateCallback<String> textUpdated) {
        super(-1, x, y, buttonText);
        this.placeholderText = placeholderText;
        this.textUpdated = textUpdated;

        textField = new GuiTextField(-1, Minecraft.getMinecraft().fontRendererObj, x, y, w, h);
        textField.setMaxStringLength(maxLength);
        textField.setFocused(focused);
        textField.setText(buttonText);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        textField.drawTextBox();
        if (placeholderText != null && StringUtils.isEmpty(textField.getText())) {
            mc.fontRendererObj.drawString(placeholderText, xPosition+4, yPosition+3, ColorCode.DARK_GRAY.getColor());
        }
    }

    protected void keyTyped(char typedChar, int keyCode) {
        if (textField.isFocused()) {
            textField.textboxKeyTyped(typedChar, keyCode);
        }
        textUpdated.onUpdate(textField.getText());
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        textField.mouseClicked(mouseX, mouseY, 0);

        return textField.isFocused();
    }

    public void updateScreen() {
        textField.updateCursorCounter();
    }

    public static void callKeyTyped(List<GuiButton> buttonList, char typedChar, int keyCode) {
        for (GuiButton button : buttonList) {
            if (button instanceof ButtonInputFieldWrapper) {
                ((ButtonInputFieldWrapper)button).keyTyped(typedChar, keyCode);
            }
        }
    }

    public static void callUpdateScreen(List<GuiButton> buttonList) {
        for (GuiButton button : buttonList) {
            if (button instanceof ButtonInputFieldWrapper) {
                ((ButtonInputFieldWrapper)button).updateScreen();
            }
        }
    }
}
