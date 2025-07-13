package com.github.Laevatain0308.javaFX.scenes.downloadProgress;

import com.github.Laevatain0308.download.DownloadService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;

public class DownloadProgressCellController
{
    @FXML private Pane root;
    @FXML private Label name;
    @FXML private ProgressBar bar;


    public void setService(DownloadService service)
    {
        name.setText("下载 " + service.currentUrlProperty().getValue());
        bar.progressProperty().bind(service.progressProperty());
    }
}