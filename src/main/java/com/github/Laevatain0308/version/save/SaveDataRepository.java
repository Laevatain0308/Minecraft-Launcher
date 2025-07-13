package com.github.Laevatain0308.version.save;

import com.github.Laevatain0308.version.Version;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class SaveDataRepository
{
    private final Version version;
    private final Path path;
    private final Map<String , SaveData> saves;


    public SaveDataRepository(Path path , Version version)
    {
        this.version = version;
        this.path = path;

        saves = new HashMap<>();
    }


    public Version getVersion() { return version; }
    public Path getPath() { return path; }

    public Collection<SaveData> getSaves() { return saves.values(); }

    public void addSaveData(SaveData save) { saves.put(save.getFileName() , save); }
    public void removeSaveData(SaveData save) { saves.remove(save.getFileName()); }


    /**
     * 检测存档
     */
    public void detectSaves()
    {
        if (!path.toFile().exists())
            return;

        List<String> fileNames = new ArrayList<>();

        for (File file : path.toFile().listFiles())
        {
            if (!file.isDirectory())
                return;

            String name = file.getName();

            // 确定该文件夹为存档文件夹（判断是否有存档配置文件）
            if (!file.toPath().resolve("level.dat").toFile().exists())
            {
                // 若文件夹中不包含存档配置文件，则从字典中删去
                saves.remove(name);
                continue;
            }

            fileNames.add(name);

            // 若存档字典中不含该文件，则加入该存档；若存档字典中包含该文件，替换存档
            saves.put(name , new SaveData(name , file.toPath() , this));
        }

        // 还需判断一种情况：存档字典中包含而文件列表中不含，则从字典中去除该存档
        saves.forEach( (name, data) -> {
            if (!fileNames.contains(name))
            {
                saves.remove(name);
                return;
            }
        });
    }
}
