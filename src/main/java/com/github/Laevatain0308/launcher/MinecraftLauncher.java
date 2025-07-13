package com.github.Laevatain0308.launcher;

import com.github.Laevatain0308.event.EventBus;
import com.github.Laevatain0308.event.events.RefreshMainSceneCurrentVersionEvent;
import com.github.Laevatain0308.jsonProcessor.VersionManifestJson;
import com.github.Laevatain0308.jsonProcessor.VersionManifestJsonReader;
import com.github.Laevatain0308.version.Version;
import com.github.Laevatain0308.version.VersionMonitor;
import com.github.Laevatain0308.version.VersionsRepository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MinecraftLauncher
{
    public static final MinecraftLauncher instance = new MinecraftLauncher();

    private final String osName;

    private String launcherName = "MinecraftLauncher";
    private String launcherVersion = "1.0.0";

    public Path versionManifestPath = Paths.get("D:/Code/Java/MinecraftLauncher/src/version_manifest.json");

    // 版本列表
    private List<VersionsRepository> repositories;
    private VersionsRepository currentRepository;
    private Version currentVersion;

    // 下载
    public List<VersionManifestJson.Version> officialDownloadVersions;
    public List<VersionManifestJson.Version> snapshotDownloadVersions;
    public List<VersionManifestJson.Version> ancientDownloadVersions;

    public VersionManifestJsonReader manifestJsonReader;


    public MinecraftLauncher()
    {
        osName = readOSName();

        repositories = new ArrayList<>();
        repositories.add(new VersionsRepository("Minecraft" , "D:/Code/Java/MinecraftLauncher/run/.minecraft"));
        //repositories.add(new VersionsRepository("Original" , "E:/Game/Minecraft/.minecraft"));

        if (!repositories.isEmpty())
            currentRepository = repositories.get(0);

        officialDownloadVersions = new ArrayList<>();
        snapshotDownloadVersions = new ArrayList<>();
        ancientDownloadVersions = new ArrayList<>();
    }


    public void rescanRepositories()
    {
        for (VersionsRepository repository : repositories)
        {
            VersionMonitor.fullScan(repository);
        }
    }


    public List<VersionsRepository> getRepositories() { return repositories; }

    public void createNewRepository(String name , String rootPath)
    {
        VersionsRepository repository = new VersionsRepository(name, rootPath);
        if (repositories.contains(repository))
            return;

        //VersionMonitor.fullScan(repository);

        if (repositories.isEmpty() && currentRepository == null)
            currentRepository = repository;

        repositories.add(repository);
    }

    public void addRepository(VersionsRepository repository)
    {
        if (repositories.contains(repository))
            return;

        if (repositories.isEmpty() && currentRepository == null)
        {
            currentRepository = repository;
        }

        repositories.add(repository);
    }

    public void removeRepository(VersionsRepository repository)
    {
        if (!repositories.remove(repository))
        {
            System.err.println("无版本库：" + repository.getName());
        }
        else
        {
            if (repositories.isEmpty())
            {
                resetCurrentVersion();
                currentRepository = null;
            }
            else
            {
                if (currentRepository == repository)
                {
                    resetCurrentVersion();
                    currentRepository = repositories.get(0);
                }
            }
        }
    }


    public VersionsRepository getCurrentRepository() { return currentRepository; }

    public void setCurrentRepository(VersionsRepository currentRepository)
    {
        if (!repositories.contains(currentRepository))
            return;

        this.currentRepository = currentRepository;
    }



    public Version getCurrentVersion() { return currentVersion; }

    public void setCurrentVersion(Version currentVersion)
    {
        this.currentVersion = currentVersion;
        EventBus.getInstance().publish(new RefreshMainSceneCurrentVersionEvent(currentVersion));
    }

    public void resetCurrentVersion()
    {
        currentVersion = null;
        EventBus.getInstance().publish(new RefreshMainSceneCurrentVersionEvent(null));
    }





    /**
     * 获取操作系统名
     * @return 操作系统名
     */
    private String readOSName()
    {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win"))
            return "windows";
        else if (osName.contains("mac"))
            return "osx";
        else
            return "linux";
    }

    public String getOSName() { return osName; }




    public String getLauncherName() { return launcherName; }
    public String getLauncherVersion() { return launcherVersion; }
}
