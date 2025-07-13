package com.github.Laevatain0308.javaFX.scenes.downloadProgress;

import com.github.Laevatain0308.download.DownloadManager;
import com.github.Laevatain0308.download.DownloadService;
import com.github.Laevatain0308.event.EventBus;
import com.github.Laevatain0308.event.datas.DownloadVersionInformationData;
import com.github.Laevatain0308.event.events.RefreshDownloadProgressEvent;
import com.github.Laevatain0308.javaFX.NoSelectionModel;
import com.github.Laevatain0308.javaFX.SceneManager;
import com.github.Laevatain0308.launcher.MinecraftLauncher;
import com.github.Laevatain0308.version.Version;
import com.github.Laevatain0308.version.VersionMonitor;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;

public class DownloadProgressController
{
    @FXML private Label versionName;
    @FXML private ListView<DownloadService> progressList;
    @FXML private Pane overButton;


    public void initialize()
    {
        DownloadVersionInformationData data = EventBus.getInstance().output(DownloadVersionInformationData.class);
        versionName.setText(data.id());


        progressList.setCellFactory(listView -> new DownloadProgressCell());
        progressList.setSelectionModel(new NoSelectionModel<>());
        progressList.setItems(FXCollections.observableArrayList());
        EventBus.getInstance().subscribe(RefreshDownloadProgressEvent.class , this::refreshProgressList);


        overButton.setVisible(false);
        overButton.setOnMouseClicked(event -> {
            DownloadManager.getInstance().shutdown();
            SceneManager.instance.disableModalScene();
            SceneManager.instance.backToHome();
        });


        Version tempVersion = MinecraftLauncher.instance.getCurrentRepository().getTempVersion(data.id() , data.name());
        tempVersion.fileDetect()
                .thenRunAsync(() -> {
                    Platform.runLater(() -> {
                        overButton.setVisible(true);
                    });
                });
    }

    private void refreshProgressList(RefreshDownloadProgressEvent event)
    {
        switch (event.type())
        {
            case Add -> {
                progressList.getItems().add(event.service());
            }
            case Delete -> {
                progressList.getItems().remove(event.service());
            }
        }
    }
}
