package com.github.Laevatain0308.download.downloadSource;

import com.github.Laevatain0308.jsonProcessor.VersionJson;

public class OfficialSource implements IDownloadSource
{
    @Override
    public String getVersionManifestURL() { return "https://piston-meta.mojang.com/mc/game/version_manifest.json"; }

    @Override
    public String getVersionJsonURLHead() { return "https://piston-meta.mojang.com/"; }

    @Override
    public String getClientURL(VersionJson versionJson) { return versionJson.getDownloads().getClient().getUrl(); }

    @Override
    public String getAssetIndexJsonURLHead() { return "https://piston-meta.mojang.com/"; }

    @Override
    public String getAssetURLHead() { return "http://resources.download.minecraft.net/"; }

    @Override
    public String getLibrariesURLHead() { return "https://libraries.minecraft.net/"; }
}
