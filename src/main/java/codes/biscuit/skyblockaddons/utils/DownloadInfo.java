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

    void setMessageType(EnumUtils.UpdateMessageType messageType) {
        this.messageType = messageType;
        if (messageType != EnumUtils.UpdateMessageType.DOWNLOADING) main.getScheduler().schedule(Scheduler.CommandType.RESET_UPDATE_MESSAGE, 10, messageType);
        if (messageType == EnumUtils.UpdateMessageType.FAILED) main.getUtils().sendUpdateMessage(true,false);
        if (messageType == EnumUtils.UpdateMessageType.DOWNLOAD_FINISHED) main.getUtils().sendUpdateMessage(false,false);
    }

    void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    String getDownloadLink() {
        return downloadLink;
    }

    void setPatch() {
        this.patch = true;
    }

    public boolean isPatch() {
        return patch;
    }

    void setNewestVersion(String newestVersion) {
        this.newestVersion = newestVersion;
    }

    void setDownloadedBytes(long downloadedBytes) {
        this.downloadedBytes = downloadedBytes;
    }

    void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public EnumUtils.UpdateMessageType getMessageType() {
        return messageType;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public long getDownloadedBytes() {
        return downloadedBytes;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public String getNewestVersion() {
        return newestVersion;
    }
}
