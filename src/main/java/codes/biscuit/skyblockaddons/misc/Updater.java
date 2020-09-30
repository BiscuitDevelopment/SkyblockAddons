package codes.biscuit.skyblockaddons.misc;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Message;
import lombok.Getter;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.versioning.ComparableVersion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraftforge.common.ForgeVersion.Status.*;

/**
 * This class is the SkyblockAddons updater. It reads the Forge Update Checker results for SkyblockAddons.
 */
public class Updater {

    private static final Pattern VERSION_PATTERN = Pattern.compile("(?<major>[0-9])\\.(?<minor>[0-9])\\.(?<patch>[0-9]).*");

    private SkyblockAddons main = SkyblockAddons.getInstance();

    private boolean hasUpdate = false;
    @Getter
    private String messageToRender;

    private boolean isPatch = false;
    private boolean sentUpdateMessage = false;

    /**
     * Returns whether there is an update available
     *
     * @return {@code true} if there is an update available, false otherwise.
     */
    public boolean hasUpdate() {
        return hasUpdate;
    }

    /**
     * Processes the update checker result from the Forge Update Checker and sets the correct message to be displayed.
     */
    public void processUpdateCheckResult() {
        SkyblockAddons main = SkyblockAddons.getInstance();

        ComparableVersion current = new ComparableVersion(SkyblockAddons.VERSION);
        boolean isCurrentBeta = SkyblockAddons.VERSION.contains("b");
        ComparableVersion latest = new ComparableVersion(isCurrentBeta ? main.getOnlineData().getLatestBeta() : main.getOnlineData().getLatestVersion());
        SkyblockAddons.getLogger().info("Checking to see if an update is available. Current version is " + current.toString() + ". Latest version is " + latest.toString() + ".");

        ForgeVersion.Status status;
        int versionDifference = latest.compareTo(current);
        if (versionDifference == 0) {
            status = UP_TO_DATE;
        } else if (versionDifference < 0) {
            status = AHEAD;
        } else {
            status = OUTDATED;
        }

        if (status == ForgeVersion.Status.OUTDATED) {
            hasUpdate = true;

            String currentVersion = current.toString();
            String latestVersion = latest.toString();

            SkyblockAddons.getLogger().info("Found an update: " + latestVersion + ".");

            try {
                Matcher currentMatcher = VERSION_PATTERN.matcher(currentVersion);
                Matcher latestMatcher = VERSION_PATTERN.matcher(latestVersion);

                // Its a patch if the major & minor numbers are the same & the player isn't upgrading out of a beta.
                if (currentMatcher.matches() && latestMatcher.matches() &&
                        currentMatcher.group("major").equals(latestMatcher.group("major")) &&
                        currentMatcher.group("minor").equals(latestMatcher.group("minor")) &&
                        !(currentVersion.contains("beta") && !latestVersion.contains("beta"))) {
                    isPatch = true;
                }
            } catch (Exception ex) {
                SkyblockAddons.getLogger().warn("Couldn't parse update version numbers... This shouldn't affect too much.");
                SkyblockAddons.getLogger().catching(ex);
            }

            if (isPatch) {
                messageToRender = Message.UPDATE_MESSAGE_PATCH.getMessage(latestVersion);
            } else {
                messageToRender = Message.UPDATE_MESSAGE_MAJOR.getMessage(latestVersion);
            }
        }
    }

    public void sendUpdateMessage() {
        if (sentUpdateMessage) return;
        if (main.getOnlineData().getVideoLink() == null) return;
        String newestVersion = main.getOnlineData().getLatestVersion();
        if (newestVersion == null) return;

        sentUpdateMessage = true;

        main.getUtils().sendMessage("§7§m------------§7[§b§l SkyblockAddons §7]§7§m------------", false);

        ChatComponentText newUpdate = new ChatComponentText("§b" + Message.MESSAGE_NEW_UPDATE.getMessage(newestVersion) + "\n");
        main.getUtils().sendMessage(newUpdate, false);

        ChatComponentText buttonsMessage = new ChatComponentText("§b§l[" + Message.MESSAGE_DOWNLOAD_LINK.getMessage(newestVersion) + "]");
        buttonsMessage.setChatStyle(buttonsMessage.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, main.getOnlineData().getVideoLink()))
                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§7" + Message.MESSAGE_CLICK_TO_OPEN_LINK.getMessage()))));
        buttonsMessage.appendSibling(new ChatComponentText(" "));

        if (isPatch && main.getOnlineData().getDirectDownload() != null) {
            ChatComponentText openModsFolder = new ChatComponentText("§c§l[" + Message.MESSAGE_DIRECT_DOWNLOAD.getMessage(newestVersion) + "]");
            openModsFolder.setChatStyle(openModsFolder.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, main.getOnlineData().getDirectDownload()))
                    .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§7" + Message.MESSAGE_CLICK_TO_OPEN_LINK.getMessage()))));
            openModsFolder.appendSibling(new ChatComponentText(" "));
            buttonsMessage.appendSibling(openModsFolder);
        }

        ChatComponentText openModsFolder = new ChatComponentText("§e§l[" + Message.MESSAGE_OPEN_MODS_FOLDER.getMessage(newestVersion) + "]");
        openModsFolder.setChatStyle(openModsFolder.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sba folder"))
                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§7" + Message.MESSAGE_CLICK_TO_OPEN_FOLDER.getMessage()))));
        buttonsMessage.appendSibling(openModsFolder);
        main.getUtils().sendMessage(buttonsMessage, false);

        ChatComponentText discord = new ChatComponentText("§b" + Message.MESSAGE_VIEW_PATCH_NOTES.getMessage() + " §9§l[" + Message.MESSAGE_JOIN_DISCORD.getMessage() + "]");
        discord.setChatStyle(discord.getChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/biscuit"))
                .setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§7" + Message.MESSAGE_CLICK_TO_OPEN_LINK.getMessage()))));
        main.getUtils().sendMessage(discord, false);

        main.getUtils().sendMessage("§7§m----------------------------------------------", false);
    }
}
