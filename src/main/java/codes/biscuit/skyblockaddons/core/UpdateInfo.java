package codes.biscuit.skyblockaddons.core;

import lombok.Getter;

import java.net.URL;

@Getter
public class UpdateInfo {
    private String latestRelease;
    private String releaseDownload;
    // Unused for now
    private String releaseChangelog;
    private String releaseShowcase;

    private String latestBeta;
    private String betaDownload;
    // Unused for now
    private String betaChangelog;
    private String betaShowcase;
}
