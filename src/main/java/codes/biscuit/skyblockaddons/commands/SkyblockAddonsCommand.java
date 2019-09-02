package codes.biscuit.skyblockaddons.commands;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.listeners.PlayerListener;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import java.util.Collections;
import java.util.List;

public class SkyblockAddonsCommand extends CommandBase {

    private SkyblockAddons main;

    public SkyblockAddonsCommand(SkyblockAddons main) {
        this.main = main;
    }

    @Override
    public String getName() {
        return "skyblockaddons";
    }

    @Override
    public List<String> getAliases()
    {
        return Collections.singletonList("sba");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return null;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }


    /**
     * Opens the main gui, or locations gui if they type /sba edit
     */
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("edit")) {
                main.getUtils().setFadingIn(false);
                main.getRenderListener().setGuiToOpen(PlayerListener.GUIType.EDIT_LOCATIONS);
                return;
            } else if (args[0].equalsIgnoreCase("nbt")) {
                boolean copyingNBT = !main.getUtils().isCopyNBT();
                main.getUtils().setCopyNBT(copyingNBT);
                if (copyingNBT)
                    main.getUtils().sendMessage(ChatFormatting.GREEN + "You are now able to copy the nbt of items. Hover over any item and press CTRL to copy.");
                else main.getUtils().sendMessage(ChatFormatting.RED + "You have disabled the ability to copy nbt.");
                return;
            }
        }
        main.getUtils().setFadingIn(true);
        main.getRenderListener().setGuiToOpen(PlayerListener.GUIType.MAIN);
    }
}
