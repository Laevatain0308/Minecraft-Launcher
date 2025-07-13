package com.github.Laevatain0308.download;

import com.github.Laevatain0308.download.downloadSource.BMCLSource;
import com.github.Laevatain0308.download.downloadSource.IDownloadSource;
import com.github.Laevatain0308.download.downloadSource.OfficialSource;
import com.github.Laevatain0308.jsonProcessor.VersionJson;


public class URLConstructor
{
    private final VersionJson versionJson;

    private final IDownloadSource[] downloadSources = new IDownloadSource[]
            {
                    new BMCLSource() ,
                    new OfficialSource()
            };



    public URLConstructor(VersionJson versionJson) { this.versionJson = versionJson; }



    public String[] getVersionManifestURL()
    {
        String[] urls = new String[downloadSources.length];
        for (int i = 0; i < downloadSources.length; i++)
        {
            urls[i] = downloadSources[i].getVersionManifestURL();
        }
        return urls;
    }

    public String[] getVersionJsonURLHead()
    {
        String[] urls = new String[downloadSources.length];
        for (int i = 0; i < downloadSources.length; i++)
        {
            urls[i] = downloadSources[i].getVersionJsonURLHead();
        }
        return urls;
    }

    public String[] getClientURL()
    {
        String[] urls = new String[downloadSources.length];
        for (int i = 0; i < downloadSources.length; i++)
        {
            urls[i] = downloadSources[i].getClientURL(versionJson);
        }
        return urls;
    }

    public String[] getAssetIndexJsonURLHead()
    {
        String[] urls = new String[downloadSources.length];
        for (int i = 0; i < downloadSources.length; i++)
        {
            urls[i] = downloadSources[i].getAssetIndexJsonURLHead();
        }
        return urls;
    }

    public String[] getAssetURLHead()           // 下载资源库时镜像源更好
    {
        String[] urls = new String[downloadSources.length];
        for (int i = 0; i < downloadSources.length; i++)
        {
            urls[i] = downloadSources[i].getAssetURLHead();
        }
        return urls;
    }

    public String[] getLibrariesURLHead()       // 下载依赖库时官方源更好
    {
        return new String[] {
                downloadSources[1].getLibrariesURLHead() ,
                downloadSources[0].getLibrariesURLHead()
        };
    }
}
