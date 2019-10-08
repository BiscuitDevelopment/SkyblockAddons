package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;

public class DownloadInfo {

    private SkyblockAddons main;

    private boolean patch = false;
    private EnumUtils.UpdateMessageType messageType = null;
    private long downloadedBytes = 0;
    private long totalBytes = 0;
    private String outputFileName = "";
    private String newestVersion = "";
    private String downloadLink = "";

    public DownloadInfo(SkyblockAddons main) {
        this.main = main;
    }

    String getDownloadLink() {
        return downloadLink;
    }

    void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    void setPatch() {
        this.patch = true;
    }

    public boolean isPatch() {
        return patch;
    }

    public EnumUtils.UpdateMessageType getMessageType() {
        return messageType;
    }

    void setMessageType(EnumUtils.UpdateMessageType messageType) {
        this.messageType = messageType;
        if (messageType != EnumUtils.UpdateMessageType.DOWNLOADING)
            main.getScheduler().schedule(Scheduler.CommandType.RESET_UPDATE_MESSAGE, 10, messageType);
        if (messageType == EnumUtils.UpdateMessageType.FAILED) main.getUtils().sendUpdateMessage(true, false);
        if (messageType == EnumUtils.UpdateMessageType.DOWNLOAD_FINISHED)
            main.getUtils().sendUpdateMessage(false, false);
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public long getDownloadedBytes() {
        return downloadedBytes;
    }

    void setDownloadedBytes(long downloadedBytes) {
        this.downloadedBytes = downloadedBytes;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public String getNewestVersion() {
        return newestVersion;
    }

    void setNewestVersion(String newestVersion) {
        this.newestVersion = newestVersion;
    }
}
