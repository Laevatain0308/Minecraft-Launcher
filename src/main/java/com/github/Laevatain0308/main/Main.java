package com.github.Laevatain0308.main;

import com.github.Laevatain0308.download.DownloadManager;
import com.github.Laevatain0308.download.DownloadVersionMonitor;
import com.github.Laevatain0308.javaFX.SceneManager;
import com.github.Laevatain0308.launcher.MinecraftLauncher;
import com.github.Laevatain0308.version.VersionMonitor;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application
{

    public static void main(String[] args)
    {
        Application.launch(args);
    }


    @Override
    public void init() throws Exception
    {
        super.init();

        // 检测器
        // Java 检测器先行于 版本检测器
        new VersionMonitor(MinecraftLauncher.instance.getRepositories()).start();
        new DownloadVersionMonitor(MinecraftLauncher.instance.officialDownloadVersions ,
                                   MinecraftLauncher.instance.snapshotDownloadVersions ,
                                   MinecraftLauncher.instance.ancientDownloadVersions);

        System.out.println("Initializing...");
    }

    @Override
    public void start(Stage stage) throws Exception
    {
        stage.setTitle(MinecraftLauncher.instance.getLauncherName());
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(SceneManager.instance.getCurrentScene());
        stage.show();

        System.out.println("Starting...");
    }

    @Override
    public void stop() throws Exception
    {
        super.stop();

        if (DownloadManager.instance != null)
            DownloadManager.instance.shutdown();

        System.out.println("Stopping...");
    }


    private boolean showExitConfirmation()
    {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("退出确认");
        alert.setHeaderText("有下载任务正在进行中");
        alert.setContentText("确定要终止下载并退出吗？");

        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }
}
