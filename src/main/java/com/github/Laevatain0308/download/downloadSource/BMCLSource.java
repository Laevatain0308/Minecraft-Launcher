package com.github.Laevatain0308.download.downloadSource;

import com.github.Laevatain0308.jsonProcessor.VersionJson;

public class BMCLSource implements IDownloadSource
{
    @Override
    public String getVersionManifestURL() { return "https://bmclapi2.bangbang93.com/mc/game/version_manifest.json"; }

    @Override
    public String getVersionJsonURLHead() { return "https://bmclapi2.bangbang93.com/"; }

    @Override
    public String getClientURL(VersionJson versionJson) { return "https://bmclapi2.bangbang93.com/version/" + versionJson.getId() + "/client"; }

    @Override
    public String getAssetIndexJsonURLHead() { return "https://bmclapi2.bangbang93.com/"; }

    @Override
    public String getAssetURLHead() { return "https://bmclapi2.bangbang93.com/assets/"; }

    @Override
    public String getLibrariesURLHead() { return "https://bmclapi2.bangbang93.com/maven/"; }
}
