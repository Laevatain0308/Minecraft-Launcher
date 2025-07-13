package com.github.Laevatain0308.download;

import com.github.Laevatain0308.jsonProcessor.VersionManifestJson;
import com.github.Laevatain0308.jsonProcessor.VersionManifestJsonReader;
import com.github.Laevatain0308.launcher.MinecraftLauncher;

import java.nio.file.Path;
import java.util.List;

public class DownloadVersionMonitor
{
    public DownloadVersionMonitor(List<VersionManifestJson.Version> official , List<VersionManifestJson.Version> snapshot , List<VersionManifestJson.Version> ancient)
    {
        Path manifestPath = MinecraftLauncher.instance.versionManifestPath;

        if (!manifestPath.toFile().exists())
        {
            URLConstructor constructor = new URLConstructor(null);
            DownloadManager.getInstance()
                    .addDownload(
                            new DownloadJob(constructor.getVersionManifestURL() ,
                                            manifestPath ,
                                            180 * 1024 ,
                                            DownloadJob.Priority.HIGH)
                                    .setOnSuccess(e -> readJson(official , snapshot , ancient , manifestPath)));
        }
        else
        {
            readJson(official , snapshot , ancient , manifestPath);
        }
    }

    private void readJson(List<VersionManifestJson.Version> official , List<VersionManifestJson.Version> snapshot , List<VersionManifestJson.Version> ancient , Path path)
    {
        VersionManifestJsonReader reader = new VersionManifestJsonReader(path);
        for (VersionManifestJson.Version version : reader.getVersionManifestJson().getVersions())
        {
            String type = version.getType();
            switch (type)
            {
                case "release" -> official.add(version);
                case "snapshot" -> snapshot.add(version);
                case "old_alpha" , "old_beta" -> ancient.add(version);
            }
        }

        MinecraftLauncher.instance.manifestJsonReader = reader;
    }
}
