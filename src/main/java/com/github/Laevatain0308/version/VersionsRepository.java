package com.github.Laevatain0308.version;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class VersionsRepository
{

    private final List<Version> versions;

    private String name;    // 版本库名称


    //===== 路径准备 =====//
    private Path rootPath;
    private final Path librariesPath;
    private final Path assetsPath;
    private final Path assetIndexesPath;
    private final Path assetObjectsPath;


    public List<Version> getVersions() { return versions; }

    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    public void setRootPath(Path rootPath) { this.rootPath = rootPath; }
    public Path getRootPath() { return rootPath; }

    public Path getLibrariesPath() { return librariesPath; }

    public Path getAssetsPath() { return assetsPath; }

    public Path getAssetIndexesPath() { return assetIndexesPath; }

    public Path getAssetObjectsPath() { return assetObjectsPath; }


    public VersionsRepository(String name , String root)
    {
        this.name = name;
        this.rootPath = Paths.get(root);
        librariesPath = rootPath.resolve("libraries");
        assetsPath = rootPath.resolve("assets");
        assetIndexesPath = assetsPath.resolve("indexes");
        assetObjectsPath = assetsPath.resolve("objects");

        versions = new ArrayList<>();
    }


    public Version createNewVersion(String versionID , String versionName)
    {
        Version newVersion = new Version(this , versionID , versionName);
        versions.add(newVersion);
        return newVersion;
    }

    public Version getTempVersion(String versionID , String versionName) { return new Version(this , versionID , versionName); }

    public void addVersion(Version version)
    {
        if (version.getRepository() != this)
            return;

        if (!versions.contains(version))
            versions.add(version);
    }
}