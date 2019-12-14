package codes.biscuit.skyblockaddons.commands;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.listeners.PlayerListener;
import codes.biscuit.skyblockaddons.utils.EnumUtils;
import codes.biscuit.skyblockaddons.utils.nifty.color.ChatFormatting;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

import java.awt.Desktop;
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
            switch (args[0]) {
                case "edit":
                    main.getUtils().setFadingIn(false);
                    main.getRenderListener().setGuiToOpen(PlayerListener.GUIType.EDIT_LOCATIONS, 0, null);
                    break;
                case "nbt":
                    boolean copyingNBT = !main.getUtils().isCopyNBT();
                    main.getUtils().setCopyNBT(copyingNBT);
                    if (copyingNBT) main.getUtils().sendMessage(ChatFormatting.GREEN+"You are now able to copy the nbt of items. Hover over any item and press CTRL to copy.");
                    else main.getUtils().sendMessage(ChatFormatting.RED+"You have disabled the ability to copy nbt.");
                    break;
                case "update":
                    if (main.getRenderListener().getDownloadInfo().isPatch())
                        main.getUtils().downloadPatch(main.getRenderListener().getDownloadInfo().getNewestVersion());
                    break;
                case "folder":
                    try {
                        Desktop.getDesktop().open(main.getUtils().getSBAFolder(false));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }

            return;
        }

        main.getUtils().setFadingIn(true);
        main.getRenderListener().setGuiToOpen(PlayerListener.GUIType.MAIN, 1, EnumUtils.GuiTab.FEATURES);
    }

}
