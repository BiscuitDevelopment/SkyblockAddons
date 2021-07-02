package codes.biscuit.skyblockaddons.misc;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import codes.biscuit.skyblockaddons.core.Translations;
import codes.biscuit.skyblockaddons.core.UpdateInfo;
import lombok.Getter;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.versioning.ComparableVersion;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraftforge.common.ForgeVersion.Status.*;

/**
 * This class is the SkyblockAddons updater. It checks for updates by reading version information from {@link UpdateInfo}.
 */
public class Updater {

    private static final Pattern VERSION_PATTERN = Pattern.compile("(?<major>[0-9])\\.(?<minor>[0-9])\\.(?<patch>[0-9]).*");

    private final SkyblockAddons main = SkyblockAddons.getInstance();
    private final Logger LOGGER = SkyblockAddons.getLogger();

    private ComparableVersion target = null;

    @Getter
    private String messageToRender;
    private String downloadLink;
    private String changelogLink;
    private String showcaseLink;

    private boolean hasUpdate = false;
    private boolean isPatch = false;
    private boolean sentUpdateMessage = false;

    /**
     * Returns whether the update notification message has already been sent.
     *
     * @return {@code true} if the update notification message has already been sent, {@code false} otherwise
     */
    public boolean hasSentUpdateMessage() {
        return sentUpdateMessage;
    }

    /**
     * Returns whether there is an update available
     *
     * @return {@code true} if there is an update available, {@code false} otherwise.
     */
    public boolean hasUpdate() {
        return hasUpdate;
    }

    /**
     * Checks the online data for an update and sets the correct message to be displayed.
     */
    public void checkForUpdate() {
        LOGGER.info("Checking to see if an update is available...");
        UpdateInfo updateInfo = main.getOnlineData().getUpdateInfo();

        // Variables reset for testing update checker notifications
        sentUpdateMessage = false;
        main.getRenderListener().setUpdateMessageDisplayed(false);

        if (updateInfo == null) {
            LOGGER.error("Update check failed: Update info is null!");
            return;
        }

        ComparableVersion latestRelease = null;
        ComparableVersion latestBeta = null;
        ComparableVersion current = new ComparableVersion(SkyblockAddons.VERSION);
        boolean isCurrentBeta = isBetaVersion(current);
        boolean latestReleaseExists = updateInfo.getLatestRelease() != null && !updateInfo.getLatestRelease().equals("");
        boolean latestBetaExists = updateInfo.getLatestBeta() != null && !updateInfo.getLatestBeta().equals("");
        int releaseDiff = 0;
        int betaDiff = 0;

        if (latestReleaseExists) {
            latestRelease = new ComparableVersion(updateInfo.getLatestRelease());
            releaseDiff = latestRelease.compareTo(current);
        } else {
            if (!isCurrentBeta) {
                LOGGER.error("Update check failed: Current version is a release version and key `latestRelease` is null " +
                        "or empty.");
                return;
            } else {
                LOGGER.warn("Key `latestRelease` is null or empty, skipping!");
            }
        }

        if (isCurrentBeta) {
            if (latestBetaExists) {
                latestBeta = new ComparableVersion(updateInfo.getLatestBeta());
                betaDiff = latestBeta.compareTo(current);
            } else {
                if (latestRelease == null) {
                    LOGGER.error("Update check failed: Keys `latestRelease` and `latestBeta` are null or empty.");
                    return;
                } else {
                    LOGGER.warn("Key `latestBeta` is null or empty, skipping!");
                }
            }
        }

        ForgeVersion.Status status = null;
        if (!isCurrentBeta) {
            if (releaseDiff == 0) {
                status = UP_TO_DATE;
            } else if (releaseDiff < 0) {
                status = AHEAD;
            } else {
                status = OUTDATED;
                target = latestRelease;
            }
        } else {
            String currentVersionString = current.toString();

            // If release is newer than this beta, target release
            if (latestReleaseExists) {
                ComparableVersion currentWithoutPrerelease = new ComparableVersion(currentVersionString.substring(0,
                        currentVersionString.indexOf('-')));

                if (releaseDiff > 0 || latestRelease.compareTo(currentWithoutPrerelease) == 0) {
                    status = OUTDATED;
                    target = latestRelease;
                } else if (!latestBetaExists && releaseDiff < 0) {
                    status = AHEAD;
                } else if (releaseDiff == 0) {
                    LOGGER.warn("The current beta version (" + currentVersionString + ") matches the latest release " +
                            "version. There is probably something wrong with the online data.");
                    status = UP_TO_DATE;
                }
            }

            if (status == null) {
                if (betaDiff == 0) {
                    status = UP_TO_DATE;
                } else if (betaDiff < 0) {
                    status = AHEAD;
                } else {
                    status = BETA_OUTDATED;
                    target = latestBeta;
                }
            }
        }

        if (status == OUTDATED || status == BETA_OUTDATED) {
            hasUpdate = true;

            String currentVersion = current.toString();
            String targetVersion = target.toString();

            LOGGER.info("Found an update: " + targetVersion + ".");

            if (status == OUTDATED) {
                targetVersion = updateInfo.getLatestRelease();
                downloadLink = updateInfo.getReleaseDownload();
                changelogLink = updateInfo.getReleaseChangelog();
                showcaseLink = updateInfo.getReleaseShowcase();
            } else {
                targetVersion = updateInfo.getLatestBeta();
                downloadLink = updateInfo.getBetaDownload();
                changelogLink = updateInfo.getBetaChangelog();
                showcaseLink = updateInfo.getBetaShowcase();
            }

            try {
                Matcher currentMatcher = VERSION_PATTERN.matcher(currentVersion);
                Matcher targetMatcher = VERSION_PATTERN.matcher(targetVersion);

                // Its a patch if the major & minor numbers are the same & the player isn't upgrading from a beta.
                isPatch = currentMatcher.matches() && targetMatcher.matches() &&
                        currentMatcher.group("major").equals(targetMatcher.group("major")) &&
                        currentMatcher.group("minor").equals(targetMatcher.group("minor")) &&
                        !isCurrentBeta;
            } catch (Exception ex) {
                SkyblockAddons.getLogger().warn("Couldn't parse update version numbers... This shouldn't affect too much.");
                SkyblockAddons.getLogger().catching(ex);
            }

            if (isPatch) {
                messageToRender = Translations.getMessage("messages.updateChecker.notificationBox.patchAvailable", targetVersion);
            } else if(status == BETA_OUTDATED) {
                messageToRender = Translations.getMessage("messages.updateChecker.notificationBox.betaAvailable", targetVersion);
            } else {
                messageToRender = Translations.getMessage("messages.updateChecker.notificationBox.majorAvailable", targetVersion);
            }
        } else if (status == AHEAD) {
            LOGGER.info("The current version is newer than the latest version. Please tell an SBA developer to update" +
                    " the online data.");
        } else {
            LOGGER.info("Up to date!");
        }
    }

    public void sendUpdateMessage() {
        if (sentUpdateMessage) {
            return;
        }

        String targetVersion = target.toString();

        main.getUtils().sendMessage("§7§m----------------§7[ §b§lSkyblockAddons §7]§7§m----------------", false);

        ChatComponentText newUpdate = new ChatComponentText("§b" + Translations.getMessage(
                "messages.updateChecker.newUpdateAvailable", targetVersion) + "\n");
        ChatComponentText viewChangelog = new ChatComponentText("§b" + Translations.getMessage(
                "messages.updateChecker.wantToViewPatchNotes") + "\n");
        ChatComponentText joinDiscord = new ChatComponentText("§b" + Translations.getMessage(
                "messages.updateChecker.joinDiscord") + "\n");
        newUpdate.appendSibling(viewChangelog).appendSibling(joinDiscord);
        main.getUtils().sendMessage(newUpdate, false);

        ChatComponentText showcaseButton = null;
        ChatComponentText downloadButton;
        ChatComponentText openModsFolderButton;
        ChatComponentText changelogButton;

        if (showcaseLink != null && !showcaseLink.equals("")) {
            showcaseButton = new ChatComponentText("§b§l[" + Translations.getMessage("messages.updateChecker.watchShowcase", targetVersion) + "]");
            showcaseButton.setChatStyle(showcaseButton.getChatStyle().setChatClickEvent(
                    new ClickEvent(ClickEvent.Action.OPEN_URL, downloadLink)).setChatHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§7" +
                                    Translations.getMessage("messages.clickToOpenLink")))));
            showcaseButton.appendSibling(new ChatComponentText(" "));
        }

        downloadButton = new ChatComponentText("§b§l[" + Translations.getMessage(
                "messages.updateChecker.downloadButton", targetVersion) + "]");

        if (downloadLink != null && !downloadLink.equals("")) {
            downloadButton.setChatStyle(downloadButton.getChatStyle().setChatClickEvent(
                    new ClickEvent(ClickEvent.Action.OPEN_URL, downloadLink)).setChatHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§7" +
                                    Translations.getMessage("messages.clickToOpenLink")))));
        } else {
            downloadButton.setChatStyle(downloadButton.getChatStyle().setStrikethrough(true).setChatHoverEvent(
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("§7" +
                            Translations.getMessage("messages.updateChecker.noDownloadAvailable")))));
        }

        downloadButton.appendSibling(new ChatComponentText(" "));

        if (showcaseButton != null) {
            showcaseButton.appendSibling(downloadButton);
        }

        openModsFolderButton = new ChatComponentText("§e§l[" + Translations.getMessage(
                "messages.updateChecker.openModFolderButton") + "]");
        openModsFolderButton.setChatStyle(openModsFolderButton.getChatStyle().setChatClickEvent(
                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sba folder")).setChatHoverEvent(
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ChatComponentText("§7" + Translations.getMessage("messages.clickToOpenFolder")))));
        downloadButton.appendSibling(openModsFolderButton);

        if (changelogLink != null && !changelogLink.equals("")) {
            changelogButton = new ChatComponentText(" §9§l[" + Translations.getMessage(
                    "messages.updateChecker.joinDiscordButton") + "]");
            changelogButton.setChatStyle(changelogButton.getChatStyle().setChatClickEvent
                    (new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/zWyr3f5GXz")).setChatHoverEvent(
                            new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    new ChatComponentText("§7" + Translations.getMessage("messages.clickToOpenLink")))));
            downloadButton.appendSibling(changelogButton);
        }

        if (showcaseButton != null) {
            main.getUtils().sendMessage(showcaseButton, false);
        } else {
            main.getUtils().sendMessage(downloadButton, false);
        }

        main.getUtils().sendMessage("§7§m-----------------------------------------------------", false);

        sentUpdateMessage = true;
    }

    /**
     * Returns whether the given version is a beta version
     *
     * @param version the version to check
     * @return {@code true} if the given version is a beta version, {@code false} otherwise
     */
    private boolean isBetaVersion(ComparableVersion version) {
        return version.toString().contains("b");
    }
}
