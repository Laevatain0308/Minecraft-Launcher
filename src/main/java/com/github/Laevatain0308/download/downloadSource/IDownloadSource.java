package com.github.Laevatain0308.download.downloadSource;

import com.github.Laevatain0308.jsonProcessor.VersionJson;

public interface IDownloadSource
{

    public String getVersionManifestURL();

    public String getVersionJsonURLHead();

    public String getClientURL(VersionJson versionJson);

    public String getAssetIndexJsonURLHead();

    public String getAssetURLHead();

    public String getLibrariesURLHead();
}
