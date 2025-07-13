package com.github.Laevatain0308.javaFX.scenes.globalSettings;

import com.github.Laevatain0308.event.EventBus;
import com.github.Laevatain0308.event.events.RefreshJavaListEvent;
import com.github.Laevatain0308.event.events.RefreshStartGameButtonEvent;
import com.github.Laevatain0308.fileProcessor.FileManager;
import com.github.Laevatain0308.java.JavaInformation;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

import java.nio.file.Paths;

public class JavaListCellController
{
    @FXML private Label type;
    @FXML private Label name;
    @FXML private Label path;
    @FXML private Pane fileBrowser;
    @FXML private Pane remove;


    public void setJavaInformation(JavaInformation info)
    {
        type.setText(info.isJDK() ? "JDK" : "JRE");
        type.getStyleClass().add(info.isJDK() ? "jdk-label" : "jre-label");

        name.setText(info.getVersion());
        path.setText(info.getPath().toString());

        fileBrowser.setOnMouseClicked(event -> {
            FileManager.instance.browseFile(Paths.get(info.getPath().toString().split("bin")[0]));
        });
        fileBrowser.setCursor(Cursor.HAND);

        remove.setOnMouseClicked(event -> {
            event.consume();
            EventBus.getInstance().publish(new RefreshJavaListEvent(info , RefreshJavaListEvent.RefreashJavaInfoType.Delete));
            EventBus.getInstance().publish(new RefreshStartGameButtonEvent());
        });
        remove.setCursor(Cursor.HAND);
    }
}