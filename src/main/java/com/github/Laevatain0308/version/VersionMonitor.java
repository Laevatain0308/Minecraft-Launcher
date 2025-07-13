package com.github.Laevatain0308.version;

import com.github.Laevatain0308.jsonProcessor.VersionJsonReader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.*;

public class VersionMonitor
{
    private final List<VersionsRepository> repositories;          // 版本库
    private final ScheduledExecutorService executor;              // 版本检测线程
    private final ConcurrentMap<Path , Long> lastModifiedMap;     // 时间戳记录
    private final long scheduledDelay;                            // 定期增量检测延迟（秒）


    public VersionMonitor(List<VersionsRepository> repositories , long scheduledDelay)
    {
        this.repositories = repositories;
        executor = Executors.newSingleThreadScheduledExecutor(
                runnable -> {
                    Thread thread = new Thread(runnable , "VersionMonitor");
                    thread.setDaemon(true);
                    return thread;
                });

        lastModifiedMap = new ConcurrentHashMap<>();
        this.scheduledDelay = scheduledDelay;
    }

    public VersionMonitor(List<VersionsRepository> repositories) { this( repositories , 5); }


    public void start()
    {
        // 初始全量检测
        executor.execute(this::fullScan);

        // 定期增量检测
        executor.scheduleWithFixedDelay(this::incrementalCheck , scheduledDelay , scheduledDelay , TimeUnit.SECONDS);

        // 实时 WatchService 监听
        executor.execute(this::setUpFileWatcher);
    }


    public void fullScan()
    {
        for (VersionsRepository repository : repositories)
        {
            fullScan(repository);
        }
    }

    public static void fullScan(VersionsRepository repository)
    {
        repository.getVersions().clear();

        // 定位到 versions 文件夹
        Path versionsPath = repository.getRootPath().resolve("versions");

        if (!versionsPath.toFile().exists())
        {
            System.out.println("该目录下无游戏： " + repository.getRootPath());
            return;
        }

        // 定位到各版本文件夹
        for (File versionDir : versionsPath.toFile().listFiles(File::isDirectory))
        {
            // 定位到 Json 文件
            File jsonFile = new File(versionDir , versionDir.getName() + ".json");
            Path jsonPath = null;

            if (jsonFile.exists())
            {
                jsonPath = jsonFile.toPath();
            }
            else
            {
                for (File file : versionDir.listFiles(File::isFile))
                {
                    if (file.getName().endsWith(".json"))
                    {
                        try
                        {
                            Files.move(file.toPath() , versionDir.toPath().resolve(versionDir.getName() + ".json"));

                            Path jarPath = file.toPath().getParent().resolve(file.toPath().getFileName().toString().replace(".json" , ".jar"));
                            Path nativesPath = file.toPath().getParent().resolve(file.toPath().getFileName().toString().replace(".json" , "-natives"));

                            if (jarPath.toFile().exists())
                                Files.move(jarPath , versionDir.toPath().resolve(versionDir.getName() + ".jar"));
                            if (nativesPath.toFile().exists())
                                Files.move(nativesPath , versionDir.toPath().resolve(versionDir.getName() + "-natives"));

                            jsonPath = versionDir.toPath().resolve(versionDir.getName() + ".json");
                        }
                        catch (IOException e)
                        {
                            System.err.println("无法重命名版本相关文件" + file.getAbsolutePath());
                        }

                        break;
                    }
                }
            }

            if (jsonPath != null)
            {
                VersionJsonReader reader = new VersionJsonReader(jsonPath);
                String id = reader.getVersionJson().getId();
                String versionName = versionDir.getName();
                repository.createNewVersion(id , versionName);
            }
        }
    }


    private void incrementalCheck()
    {

    }


    private void setUpFileWatcher()
    {

    }
}
