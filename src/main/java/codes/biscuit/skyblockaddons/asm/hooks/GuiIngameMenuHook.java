package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import net.minecraft.client.gui.GuiButton;

import java.util.List;

public class GuiIngameMenuHook {

    public static void addMenuButtons(List<GuiButton> buttonList, int width, int height) {
        if (SkyblockAddons.getInstance().getUtils().isOnSkyblock()) {
            buttonList.add(new GuiButton(53, width - 120 - 5, height - 20 - 5, 120, 20, "SkyblockAddons Menu"));
        }
    }

    public static void onButtonClick() {
        SkyblockAddons skyblockAddons = SkyblockAddons.getInstance();
        skyblockAddons.getRenderListener().setGuiToOpen(EnumUtils.GUIType.MAIN, 1, EnumUtils.GuiTab.MAIN);
    }
}
