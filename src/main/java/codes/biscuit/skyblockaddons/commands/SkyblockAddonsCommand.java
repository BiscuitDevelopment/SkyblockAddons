package codes.biscuit.skyblockaddons.commands;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.listeners.PlayerListener;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class SkyblockAddonsCommand extends CommandBase {

    private SkyblockAddons main;

    public SkyblockAddonsCommand(SkyblockAddons main) {
        this.main = main;
    }

    @Override
    public String getCommandName() {
        return "skyblockaddons";
    }

    @Override
    public List<String> getCommandAliases()
    {
        return Collections.singletonList("sba");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return null;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender)
    {
        return true;
    }


    /**
     * Opens the main gui, or locations gui if they type /sba edit
     */
    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("edit")) {
                main.getUtils().setFadingIn(false);
                main.getRenderListener().setGuiToOpen(PlayerListener.GUIType.EDIT_LOCATIONS, 0, null);
                return;
            } else if (args[0].equalsIgnoreCase("nbt")) {
                boolean copyingNBT = !main.getUtils().isCopyNBT();
                main.getUtils().setCopyNBT(copyingNBT);
                if (copyingNBT) main.getUtils().sendMessage(EnumChatFormatting.GREEN+"You are now able to copy the nbt of items. Hover over any item and press CTRL to copy.");
                else main.getUtils().sendMessage(EnumChatFormatting.RED+"You have disabled the ability to copy nbt.");
                return;
            } else if (args[0].equalsIgnoreCase("update")) {
                if (main.getRenderListener().getDownloadInfo().isPatch()) main.getUtils().downloadPatch(main.getRenderListener().getDownloadInfo().getNewestVersion());
                return;
            } else if (args[0].equalsIgnoreCase("folder")) {
                try {
                    Desktop.getDesktop().open(main.getUtils().getSBAFolder(false));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }
        }
        main.getUtils().setFadingIn(true);
        main.getRenderListener().setGuiToOpen(PlayerListener.GUIType.MAIN, 1, EnumUtils.SkyblockAddonsGuiTab.FEATURES);
    }
}
