package com.github.Laevatain0308.javaFX.scenes.versionManifest;

import com.github.Laevatain0308.event.EventBus;
import com.github.Laevatain0308.event.events.RefreshRepositoryListEvent;
import com.github.Laevatain0308.event.events.RefreshVersionListEvent;
import com.github.Laevatain0308.javaFX.NoSelectionModel;
import com.github.Laevatain0308.javaFX.SceneManager;
import com.github.Laevatain0308.javaFX.SceneType;
import com.github.Laevatain0308.launcher.MinecraftLauncher;
import com.github.Laevatain0308.version.Version;
import com.github.Laevatain0308.version.VersionMonitor;
import com.github.Laevatain0308.version.VersionsRepository;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class VersionManifestController
{
    @FXML private Pane titleBar;
    @FXML private Button closeButton;
    @FXML private Button minimizeButton;

    @FXML private Pane back;

    @FXML private ListView<VersionsRepository> repositoryList;
    @FXML private ListView<Version> versionList;

    @FXML private Pane addRepository;

    @FXML private Pane download;
    @FXML private Pane refresh;
    @FXML private Pane globalSettings;

    private double xOffset;
    private double yOffset;


    public void initialize()
    {
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        closeButton.setOnAction(event -> ((Stage) closeButton.getScene().getWindow()).close());
        minimizeButton.setOnAction(event -> ((Stage) minimizeButton.getScene().getWindow()).setIconified(true));

        back.setOnMouseClicked(event -> SceneManager.instance.backScene());
        back.setCursor(Cursor.HAND);


        // 重检测所有版本库内容
        MinecraftLauncher.instance.rescanRepositories();


        //===== 版本库列表 =====//
        // 设置工厂
        repositoryList.setCellFactory(listView -> new VersionsRepositoryCell());
        // 绑定版本库列表
        repositoryList.setItems(FXCollections.observableArrayList(MinecraftLauncher.instance.getRepositories()));
        // 添加监听器，处理版本库增添后并发的事项
        repositoryList.getItems().addListener((ListChangeListener<VersionsRepository>) change -> {
            while (change.next())
            {
                refresh();
            }
        });
        // 初始化选择项
        if (!repositoryList.getItems().isEmpty())
        {
            if (MinecraftLauncher.instance.getCurrentRepository() != null)
                repositoryList.getSelectionModel().select(MinecraftLauncher.instance.getCurrentRepository());
            else
                repositoryList.getSelectionModel().selectFirst();
        }
        // 初始化列表长度
        repositoryList.setPrefHeight(41 * (Math.min(repositoryList.getItems().size() , 8)));
        // 广播版本库增添事件
        EventBus.getInstance().subscribe(RefreshRepositoryListEvent.class , event -> {
            refreshRepositoryList(event.repository() , event.refreshType());
        });


        // 新建版本时需要判断是否已存在版本名
        //===== 版本列表 =====//
        // 设置工厂
        versionList.setCellFactory(listView -> new VersionCell());
        // 设置选择模式为：无选择模式
        versionList.setSelectionModel(new NoSelectionModel<>());
        // 初始化列表
        if (!repositoryList.getItems().isEmpty())
        {
            versionList.setItems(FXCollections.observableArrayList(repositoryList.getSelectionModel().getSelectedItem().getVersions()));
        }
        // 广播版本列表更新事件
        EventBus.getInstance().subscribe(RefreshVersionListEvent.class , event -> versionList.setItems(FXCollections.observableArrayList(event.versions())));


        addRepository.setOnMouseClicked(event -> {
            SceneManager.instance.loadNextScene(SceneType.AddRepository);
        });
        addRepository.setCursor(Cursor.HAND);


        download.setOnMouseClicked(event -> {
            SceneManager.instance.loadNextScene(SceneType.DownloadScene);
        });
        download.setCursor(Cursor.HAND);

        refresh.setOnMouseClicked(event -> {
            MinecraftLauncher.instance.rescanRepositories();
            repositoryList.setItems(FXCollections.observableArrayList(MinecraftLauncher.instance.getRepositories()));
            refresh();
        });
        refresh.setCursor(Cursor.HAND);

        globalSettings.setOnMouseClicked(event -> {
            SceneManager.instance.loadNextScene(SceneType.GlobalSettings);
        });
        globalSettings.setCursor(Cursor.HAND);
    }


    private void refresh()
    {
        VersionsRepository repository = MinecraftLauncher.instance.getCurrentRepository();

        if (repository != null)
        {
            repositoryList.getSelectionModel().select(repository);
            versionList.setItems(FXCollections.observableArrayList(repository.getVersions()));
        }
        else
        {
            versionList.getItems().clear();
        }

        repositoryList.setPrefHeight(41 * (Math.min(repositoryList.getItems().size() , 8)));
    }

    private void refreshRepositoryList(VersionsRepository repository , RefreshRepositoryListEvent.RepositoryRefreshType refreshType)
    {
        switch (refreshType)
        {
            case Add -> addRepository(repository);
            case Delete -> removeRepository(repository);
        }
    }

    private void addRepository(VersionsRepository repository)
    {
        MinecraftLauncher.instance.addRepository(repository);
        repositoryList.getItems().add(repository);
    }

    private void removeRepository(VersionsRepository repository)
    {
        MinecraftLauncher.instance.removeRepository(repository);
        repositoryList.getItems().remove(repository);
    }
}
