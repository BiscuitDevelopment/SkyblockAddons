package codes.biscuit.skyblockaddons.asm.hooks;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.asm.utils.ReturnValue;
import codes.biscuit.skyblockaddons.utils.Feature;
import net.minecraft.client.Minecraft;

public class GuiInventoryHook {

    public static void handleMouseClick(int guiLeft, int guiTop, float oldMouseX, float oldMouseY, int xSize, int ySize, ReturnValue returnValue) {
        SkyblockAddons main = SkyblockAddons.getInstance();
        if (main.getUtils().isOnSkyblock()) {
            boolean isOutsideGui = oldMouseX < guiLeft || oldMouseY < guiTop || oldMouseX >= guiLeft + xSize || oldMouseY >= guiTop + ySize;
            Minecraft mc = Minecraft.getMinecraft();
            if (main.getConfigValues().isEnabled(Feature.STOP_DROPPING_SELLING_RARE_ITEMS) &&
                    mc.thePlayer.inventory.getItemStack() != null && isOutsideGui &&
                    main.getInventoryUtils().shouldCancelDrop(mc.thePlayer.inventory.getItemStack())) returnValue.cancel();
        }
    }
}
