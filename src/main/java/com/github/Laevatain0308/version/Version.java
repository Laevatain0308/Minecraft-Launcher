package com.github.Laevatain0308.version;

import com.github.Laevatain0308.account.AccountManager;
import com.github.Laevatain0308.download.DownloadJob;
import com.github.Laevatain0308.download.DownloadManager;
import com.github.Laevatain0308.download.URLConstructor;
import com.github.Laevatain0308.fileProcessor.FileManager;
import com.github.Laevatain0308.fileProcessor.UnzipManager;
import com.github.Laevatain0308.java.JavaInformation;
import com.github.Laevatain0308.java.JavaRepository;
import com.github.Laevatain0308.jsonProcessor.AssetObjectJsonReader;
import com.github.Laevatain0308.jsonProcessor.VersionJson;
import com.github.Laevatain0308.jsonProcessor.VersionJsonReader;
import com.github.Laevatain0308.jsonProcessor.VersionManifestJson;
import com.github.Laevatain0308.launcher.GameSettings;
import com.github.Laevatain0308.launcher.MinecraftLauncher;
import com.github.Laevatain0308.version.save.SaveDataRepository;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Version
{

    private final MinecraftLauncher minecraftLauncher = MinecraftLauncher.instance;
    private final VersionsRepository repository;


    //========== 版本属性 ==========//
    private final String versionID;
    private String versionName;

    //========== 路径准备 ==========//
    // 版本路径
    private final Path versionPath;
    // 配置文件路径
    private final Path jsonPath;
    // 游戏本体路径
    private final Path gamePath;
    // 本地库路径
    private final Path nativesPath;
    // 日志工具路径
    private final Path log4j2Path;

    //========== 版本配置文件读取器 ==========//
    private VersionJsonReader versionJsonReader;

    //========== 版本设置 ==========//
    private GameSettings gameSettings;
    private boolean isGlobalSettings;

    //========== 存档 ==========//
    private final SaveDataRepository saveDataRepository;



    //========== 构造函数 ==========//
    public Version(VersionsRepository repository , String versionID , String versionName)
    {
        //===== 初始化版本 =====//
        this.repository = repository;
        this.versionID = versionID;
        this.versionName = versionName;

        // 路径初始化
        versionPath = repository.getRootPath().resolve(Paths.get("versions" , versionName));
        jsonPath = versionPath.resolve(Paths.get(versionName + ".json"));
        gamePath = versionPath.resolve(Paths.get(versionName + ".jar"));
        nativesPath = repository.getRootPath().resolve(Paths.get("versions" , versionName , versionName + "-natives"));
        log4j2Path = versionPath.resolve("log4j2.xml");


        // 暂时设定为拷贝全局设置，后改成读取配置文件
        gameSettings = new GameSettings(GameSettings.globalSettings);
        isGlobalSettings = true;


        // 存档仓库
        saveDataRepository = new SaveDataRepository(versionPath.resolve("saves") , this);
    }



    //========== 文件补全 ==========//

    /**
     * 异步检测并补全文件
     *
     * @return CompletableFuture 返回所有文件补全任务完成
     */
    public CompletableFuture<Void> fileDetect()
    {
        // 创建目录
        FileManager.instance.createDirectories(versionPath);

        // 存储所有下载任务的 Future
        List<CompletableFuture<Void>> downloadFutures = new ArrayList<>();

        // 检测 Json 文件
        if (!jsonPath.toFile().exists())
        {
            CompletableFuture<Void> future = new CompletableFuture<>();
            downloadFutures.add(future);
            downloadJsonFile(future);
        }
        else
        {
            System.out.println("存在Json配置文件");
            detectOtherFiles(downloadFutures);
        }

        // 检测结果
        if (downloadFutures.isEmpty())
        {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.allOf(downloadFutures.toArray(new CompletableFuture[0]));
    }

    // 下载 Json 文件
    private void downloadJsonFile(CompletableFuture<Void> future)
    {
        URLConstructor constructor = new URLConstructor(null);

        for (VersionManifestJson.Version version : minecraftLauncher.manifestJsonReader.getVersionManifestJson().getVersions())
        {
            if (version.getId().equals(versionID))
            {
                int i = version.getUrl().indexOf("v1/");
                DownloadManager.getInstance().addDownload(
                        new DownloadJob(constructor.getVersionJsonURLHead() , version.getUrl().substring(i) , jsonPath , 29 * 1024 , DownloadJob.Priority.HIGH)
                                .setOnSuccess(event -> {
                                    List<CompletableFuture<Void>> downloadFutures = new ArrayList<>();
                                    downloadFutures.add(future);
                                    detectOtherFiles(downloadFutures);
                                    future.complete(null);
                                })
                                .serOnFailure(event -> {
                                    future.completeExceptionally(new IOException("Json文件下载失败"));
                                }));
                return;
            }
        }

        future.completeExceptionally(new IOException("未找到该版本"));
    }

    // 检测除 Json 文件外的文件
    private void detectOtherFiles(List<CompletableFuture<Void>> downloadFutures)
    {
        // 初始化
        versionJsonReader = new VersionJsonReader(jsonPath);
        if (versionJsonReader.getVersionJson() == null)
        {
            System.err.println("无法解析Json文件...");
            return;
        }

        URLConstructor urlConstructor = new URLConstructor(versionJsonReader.getVersionJson());

        // 检测并下载游戏本体
        if (!gamePath.toFile().exists())
        {
            CompletableFuture<Void> future = new CompletableFuture<>();
            downloadFutures.add(future);
            DownloadManager.getInstance().addDownload(new DownloadJob(urlConstructor.getClientURL() , gamePath , 17 * 1024 * 1024 , DownloadJob.Priority.MEDIUM).setOnSuccess(event -> {
                future.complete(null);
            }).serOnFailure(event -> {
                future.completeExceptionally(new IOException("游戏客户端下载失败"));
            }));
        }
        else
        {
            System.out.println("存在游戏客户端文件");
        }

        // 检测并下载依赖库
        detectLibraries(urlConstructor , downloadFutures);

        // 检测并下载资源文件
        detectAssets(urlConstructor , downloadFutures);

        // 检测并下载日志配置文件
        detectLogConfig(downloadFutures);
    }

    // 检测并下载依赖库
    private void detectLibraries(URLConstructor urlConstructor , List<CompletableFuture<Void>> downloadFutures)
    {
        Path librariesPath = repository.getLibrariesPath();           // 依赖库路径
        FileManager.instance.createDirectories(librariesPath);
        FileManager.instance.createDirectories(nativesPath);

        for (VersionJson.Library library : versionJsonReader.getVersionJson().getLibraries())
        {
            // 判断加载规则
            if (!isNeedToLoad(library , MinecraftLauncher.instance.getOSName()))
                continue;

            // 补全需解压依赖库，并解压文件
            if (library.getNatives() != null && library.getDownloads().getClassifiers() != null)
            {
                handleNativeLibrary(urlConstructor , downloadFutures , library , MinecraftLauncher.instance.getOSName() , librariesPath , nativesPath);
            }

            // 补全常规依赖库
            if (library.getExtract() == null && library.getDownloads().getArtifact() != null)
            {
                handleRegularLibrary(urlConstructor , downloadFutures , library , librariesPath);
            }
        }
    }

    // 检测并下载资源文件
    private void detectAssets(URLConstructor urlConstructor , List<CompletableFuture<Void>> downloadFutures)
    {
        Path assetIndexesPath = repository.getAssetIndexesPath();     // 资源索引路径
        Path assetObjectsPath = repository.getAssetObjectsPath();     // 资源文件路径
        FileManager.instance.createDirectories(assetIndexesPath);
        FileManager.instance.createDirectories(assetObjectsPath);

        VersionJson.AssetIndex assetIndex = versionJsonReader.getVersionJson().getAssetIndex();
        Path assetIndexJsonPath = assetIndexesPath.resolve(Paths.get(assetIndex.getId() + ".json"));
        if (!assetIndexJsonPath.toFile().exists())
        {
            CompletableFuture<Void> future = new CompletableFuture<>();
            downloadFutures.add(future);
            DownloadManager.getInstance().addDownload(new DownloadJob(urlConstructor.getAssetIndexJsonURLHead() , assetIndex.getUrl().split(".com/")[1] , assetIndexJsonPath , 290 * 1024 , DownloadJob.Priority.MEDIUM).setOnSuccess(event -> {
                future.complete(null);
                AssetObjectJsonReader.getAssetObjectsFromJson(assetIndexJsonPath , assetObjectsPath , downloadFutures);
            }).serOnFailure(event -> {
                future.completeExceptionally(new IOException("无法下载资源索引文件"));
            }));
        }
        else
        {
            // 获取资源文件
            AssetObjectJsonReader.getAssetObjectsFromJson(assetIndexJsonPath , assetObjectsPath , downloadFutures);

            System.out.println("存在资源索引文件");
        }
    }

    // 检测并下载日志配置文件
    private void detectLogConfig(List<CompletableFuture<Void>> downloadFutures)
    {
        if (!log4j2Path.toFile().exists())
        {
            CompletableFuture<Void> future = new CompletableFuture<>();
            downloadFutures.add(future);
            String[] url = { versionJsonReader.getVersionJson().getLogging().getClient().getFile().getUrl() };
            DownloadManager.getInstance().addDownload(
                    new DownloadJob(url , log4j2Path , 1024 , DownloadJob.Priority.MEDIUM)
                            .setOnSuccess(event -> {
                                future.complete(null);
                            })
                            .serOnFailure(event -> {
                                future.completeExceptionally(new IOException("无法下载日志配置文件"));
                            }));
        }
        else
        {
            System.out.println("存在日志配置文件");
        }
    }


    //========== 启动游戏 ==========//

    /**
     * 启动游戏
     *
     */
    public void startGame()
    {
        fileDetect().thenRunAsync(() -> {
            new Thread(() -> {
                try
                {
                    System.out.println("检查完成，正在启动游戏...");

                    String launchParam = getLaunchParam(isGlobalSettings ? GameSettings.globalSettings : gameSettings);
                    if (launchParam == null)
                    {
                        System.out.println("游戏启动失败...");
                        return;
                    }

                    ProcessBuilder builder = new ProcessBuilder("cmd.exe" , "/c" , launchParam);
                    builder.directory(versionPath.toFile());    // 跳转工作目录
                    builder.redirectErrorStream(true);
                    Process process = builder.start();


                    process.onExit().thenAccept(event -> {
                        Platform.runLater(() -> {
                            if (process.exitValue() == 0)
                                System.out.println("游戏进程已退出...");
                            else
                                System.out.println("游戏启动失败...");
                        });
                    });

                    readProcessOutput(process);
                }
                catch (IOException e)
                {
                    Platform.runLater(() -> {
                        System.out.println("游戏启动失败...");
                    });
                }
            }).start();
        });
    }

    private void readProcessOutput(Process process)
    {
        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    System.out.println(line);
                }
            }
            catch (IOException e)
            {
                System.err.println("读取进程输出时出错: " + e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
    }


    //========== 启动参数拼接 ==========//
    private String getLaunchParam(GameSettings settings)
    {
        StringBuilder launchParam = new StringBuilder();

        // JAVA 路径部分
        JavaInformation javaInformation = JavaRepository.instance.getSuitableJava(this);
        if (javaInformation == null)
        {
            System.err.println("缺少Java环境...");
            return null;
        }
        String java = javaInformation.getPath().toString();
        launchParam.append(java);

        // JVM 参数部分
        launchParam.append(getJVMArgs(settings));

        // 启动器信息设置
        launchParam.append(" -Dminecraft.launcher.brand=").append(minecraftLauncher.getLauncherName());
        launchParam.append(" -Dminecraft.launcher.version=").append(minecraftLauncher.getLauncherVersion());

        launchParam.append(" -cp ");

        // 依赖库导入
        launchParam.append(getLibraries());
        // 游戏本体导入
        launchParam.append(gamePath);
        // 主类导入
        launchParam.append(" net.minecraft.client.main.Main");

        // 游戏参数部分
        launchParam.append(getGameArgs(settings));

        return launchParam.toString();
    }

    private StringBuilder getJVMArgs(GameSettings settings)
    {
        StringBuilder jvmArgs = new StringBuilder();

        jvmArgs.append(" -Xmx").append(settings.getXmx()).append("m")
                .append(" -Dfile.encoding=GB18030")
                .append(" -Dsun.stdout.encoding=GB18030")
                .append(" -Dsun.stderr.encoding=GB18030")
                .append(" -Djava.rmi.server.useCodebaseOnly=true")
                .append(" -Dcom.sun.jndi.rmi.object.trustURLCodebase=false")
                .append(" -Dcom.sun.jndi.cosnaming.object.trustURLCodebase=false");

        // 启用 log4j2 日志系统
        //jvmArgs.append(" -Dlog4j2.formatMsgNoLookups=true").append(" -Dlog4j.configurationFile=").append(log4j2Path);

        jvmArgs.append(" -Dminecraft.client.jar=").append(gamePath)

                .append(" -XX:+UnlockExperimentalVMOptions")
                .append(" -XX:+UseG1GC")
                .append(" -XX:G1NewSizePercent=20")
                .append(" -XX:G1ReservePercent=20")
                .append(" -XX:MaxGCPauseMillis=50")
                .append(" -XX:G1HeapRegionSize=32m")
                .append(" -XX:-UseAdaptiveSizePolicy")
                .append(" -XX:-OmitStackTraceInFastThrow")
                .append(" -XX:-DontCompileHugeMethods")
                .append(" -Dfml.ignoreInvalidMinecraftCertificates=true")
                .append(" -Dfml.ignorePatchDiscrepancies=true")
                .append(" -XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump")

                .append(" -Djava.library.path=").append(nativesPath);    // 本地库路径

        return jvmArgs;
    }

    private StringBuilder getGameArgs(GameSettings settings)
    {
        StringBuilder gameArgs = new StringBuilder();

        gameArgs.append(" --username ").append(AccountManager.instance.getCurrentUser().getName());
        gameArgs.append(" --version ").append(versionName);
        gameArgs.append(" --gameDir ").append(versionPath);
        gameArgs.append(" --assetsDir ").append(repository.getAssetsPath());
        gameArgs.append(" --assetIndex ").append(versionJsonReader.getVersionJson().getAssets());
        gameArgs.append(" --uuid ").append(AccountManager.instance.getCurrentUser().getUUID());
        gameArgs.append(" --accessToken ").append("0");
        gameArgs.append(" --userType ").append("mojang");
        gameArgs.append(" --versionType ").append("\"" + minecraftLauncher.getLauncherName() + " " + minecraftLauncher.getLauncherVersion() + "\"");
        gameArgs.append(" --width ").append(settings.getWidth());
        gameArgs.append(" --height ").append(settings.getHeight());

        return gameArgs;
    }


    //========== 依赖库处理 ==========//

    /**
     * 获取依赖库参数
     *
     * @return 依赖库参数
     */
    public StringBuilder getLibraries()
    {
        //===== 路径准备 =====//
        Path librariesPath = repository.getRootPath().resolve("libraries");

        //===== 依赖库分析 =====//
        StringBuilder libraries = new StringBuilder();
        for (VersionJson.Library library : versionJsonReader.getVersionJson().getLibraries())
        {
            //===== 判断加载规则 =====//
            if (!isNeedToLoad(library , MinecraftLauncher.instance.getOSName()))
                continue;

            //===== 加载常规文件 =====//
            if (library.getNatives() == null)
            {
                // 添加启动参数
                Path path = librariesPath.resolve(library.getDownloads().getArtifact().getPath());
                libraries.append(path).append(";");
            }
        }

        return libraries;
    }

    /**
     * 根据规则判断该依赖库是否加载
     *
     * @param library 依赖库
     * @param osName  操作系统名
     * @return 判断结果
     */
    private static boolean isNeedToLoad(VersionJson.Library library , String osName)
    {
        if (library.getRules() == null)
            return true;

        for (VersionJson.Library.Rule rule : library.getRules())
        {
            if (rule.getAction().equals("disallow") && (rule.getOs() == null || rule.getOs().getName().equals(osName)))
                return false;

            if (rule.getAction().equals("allow") && !(rule.getOs() == null || rule.getOs().getName().equals(osName)))
                return false;
        }

        return true;
    }

    /**
     * 补全 需解压依赖库 文件，并将其解压
     *
     * @param library       依赖库
     * @param osName        操作系统名
     * @param librariesPath 依赖库路径
     * @param nativesPath   文件解压路径
     */
    private void handleNativeLibrary(URLConstructor urlConstructor , List<CompletableFuture<Void>> downloadFutures , VersionJson.Library library , String osName , Path librariesPath , Path nativesPath)
    {
        String key = library.getNatives().get(osName);
        VersionJson.Library.Downloads.Artifact jarClassifiers = library.getDownloads().getClassifiers().get(key);
        Path jarClassifiersPath = librariesPath.resolve(Paths.get(jarClassifiers.getPath()));

        // 部分解压依赖库的 Artifact 部分需要下载
        VersionJson.Library.Downloads.Artifact jarArtifact;
        if ((jarArtifact = library.getDownloads().getArtifact()) != null)
        {
            Path jarArtifactPath = librariesPath.resolve(Paths.get(jarArtifact.getPath()));
            if (!jarArtifactPath.toFile().exists())
            {
                CompletableFuture<Void> future = new CompletableFuture<>();
                downloadFutures.add(future);
                DownloadManager.getInstance().addDownload(new DownloadJob(urlConstructor.getLibrariesURLHead() , jarArtifact.getPath() , jarArtifactPath , 0 , DownloadJob.Priority.MEDIUM).setOnSuccess(event -> {
                    future.complete(null);
                }).serOnFailure(event -> {
                    future.completeExceptionally(new IOException("无法下载依赖库：" + jarArtifactPath));
                }));
            }
            else
            {
                System.out.println("存在依赖库文件：" + jarArtifactPath);
            }
        }

        // 若依赖库 Classifiers部分 文件不存在，则下载文件
        if (!jarClassifiersPath.toFile().exists())
        {
            CompletableFuture<Void> future = new CompletableFuture<>();
            downloadFutures.add(future);
            DownloadManager.getInstance().addDownload(new DownloadJob(urlConstructor.getLibrariesURLHead() , jarClassifiers.getPath() , jarClassifiersPath , 0 , DownloadJob.Priority.MEDIUM).setOnSuccess(event -> {
                future.complete(null);
                UnzipManager.instance.unzipNatives(jarClassifiersPath.toFile() , nativesPath , library.getExtract() != null ? library.getExtract().getExclude() : null , downloadFutures);
            }).serOnFailure(event -> {
                future.completeExceptionally(new IOException("无法下载依赖库：" + jarClassifiersPath));
            }));
        }
        else
        {
            // 解压 Classifiers部分 文件（自动判断是否已有解压文件）    // 可能需要写点什么来判断依赖库文件够不够，通过这个方法解压时判断太浪费性能了（汗
            UnzipManager.instance.unzipNatives(jarClassifiersPath.toFile() , nativesPath , library.getExtract() != null ? library.getExtract().getExclude() : null , downloadFutures);
            System.out.println("存在解压依赖库文件：" + jarClassifiersPath);
        }
    }

    /**
     * 补全 常规依赖库 文件
     *
     * @param library       依赖库
     * @param librariesPath 依赖库路径
     */
    private void handleRegularLibrary(URLConstructor urlConstructor , List<CompletableFuture<Void>> downloadFutures , VersionJson.Library library , Path librariesPath)
    {
        VersionJson.Library.Downloads.Artifact artifact = library.getDownloads().getArtifact();
        Path path = librariesPath.resolve(artifact.getPath());
        if (!path.toFile().exists())
        {
            CompletableFuture<Void> future = new CompletableFuture<>();
            downloadFutures.add(future);
            DownloadManager.getInstance().addDownload(new DownloadJob(urlConstructor.getLibrariesURLHead() , artifact.getPath() , path , 0 , DownloadJob.Priority.MEDIUM).setOnSuccess(event -> {
                future.complete(null);
            }).serOnFailure(event -> {
                future.completeExceptionally(new IOException("无法下载依赖库：" + path));
            }));
        }
        else
        {
            System.out.println("存在常规依赖库文件：" + path);
        }
    }


    //========== 修改版本属性 ==========//



    //========== getter / setter ==========//
    public VersionsRepository getRepository() { return repository; }

    public String getVersionName() { return versionName; }
    public void setVersionName(String versionName) { this.versionName = versionName; }

    public String getVersionID() { return versionID; }

    public Path getVersionPath() { return versionPath; }

    public VersionJsonReader getVersionJsonReader() { return versionJsonReader; }

    public GameSettings getGameSettings() { return gameSettings; }
    public void setGameSettings(GameSettings gameSettings) { this.gameSettings = gameSettings; }

    public boolean isGlobalSettings() { return isGlobalSettings; }
    public void setGlobalSettings(boolean isGlobalSettings) { this.isGlobalSettings = isGlobalSettings; }

    public SaveDataRepository getSaveDataRepository() { return saveDataRepository; }
}
