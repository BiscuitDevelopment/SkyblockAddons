package codes.biscuit.skyblockaddons.gui.buttons;

import codes.biscuit.skyblockaddons.utils.UpdateCallback;
import codes.biscuit.skyblockaddons.utils.nifty.ChatFormatting;
import codes.biscuit.skyblockaddons.utils.nifty.StringUtil;
import codes.biscuit.skyblockaddons.utils.nifty.reflection.MinecraftReflection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;

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
        if (placeholderText != null && StringUtil.isEmpty(textField.getText())) {
            MinecraftReflection.FontRenderer.drawString(placeholderText, xPosition+4, yPosition+3, ChatFormatting.DARK_GRAY);
        }
    }

    protected void keyTyped(char typedChar, int keyCode) {
        if (textField.isFocused()) {
            textField.textboxKeyTyped(typedChar, keyCode);
        }
        textUpdated.onUpdate(textField.getText());
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton){
        textField.mouseClicked(mouseX, mouseY, mouseButton);
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

    public static void callMouseClicked(List<GuiButton> buttonList, int mouseX, int mouseY, int mouseButton) {
        for (GuiButton button : buttonList) {
            if (button instanceof ButtonInputFieldWrapper) {
                ((ButtonInputFieldWrapper)button).mouseClicked(mouseX, mouseY, mouseButton);
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
