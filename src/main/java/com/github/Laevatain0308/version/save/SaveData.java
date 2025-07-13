package com.github.Laevatain0308.version.save;

import java.nio.file.Path;

public class SaveData
{
    private final String fileName;
    private final SaveDataRepository repository;

    // Path
    private final Path rootPath;
    private final Path iconPath;
    private final Path levelDataPath;

    // NBT Reader
    private final LevelDataReader levelDataReader;


    public SaveData(String fileName , Path dataRootPath , SaveDataRepository repository)
    {
        this.fileName = fileName;
        this.repository = repository;

        this.rootPath = dataRootPath;
        this.iconPath = rootPath.resolve("icon.png");
        this.levelDataPath = rootPath.resolve("level.dat");

        this.levelDataReader = new LevelDataReader(levelDataPath , this);
    }


    public String getFileName() { return fileName; }
    public SaveDataRepository getRepository() { return repository; }

    public Path getRootPath() { return rootPath; }
    public Path getIconPath() { return iconPath; }
    public Path getLevelDataPath() { return levelDataPath; }

    public LevelDataReader getLevelDataReader() { return levelDataReader; }

    // 判断是否重复（仅以文件夹名称为判断标准，版本名称允许重复）
    public boolean repeatWith(SaveData other) { return fileName.equals(other.fileName); }
}
