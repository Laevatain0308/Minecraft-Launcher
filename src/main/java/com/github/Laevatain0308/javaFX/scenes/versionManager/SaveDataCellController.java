package com.github.Laevatain0308.javaFX.scenes.versionManager;

import com.github.Laevatain0308.event.EventBus;
import com.github.Laevatain0308.fileProcessor.FileManager;
import com.github.Laevatain0308.javaFX.SceneManager;
import com.github.Laevatain0308.javaFX.SceneType;
import com.github.Laevatain0308.version.save.SaveData;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

public class SaveDataCellController
{
    @FXML private Pane root;
    @FXML private ImageView icon;
    @FXML private Label name;
    @FXML private Label versionID;
    @FXML private Label lastPlayDate;
    @FXML private Pane openFile;
    @FXML private Pane moreInformation;


    public void setData(SaveData data)
    {
        Image image = new Image(data.getIconPath().toUri().toString());
        icon.setImage(image);

        name.setText(data.getLevelDataReader().getLevelName());
        versionID.setText(data.getLevelDataReader().getGameVersion());
        lastPlayDate.setText(data.getLevelDataReader().getLastPlayed());

        openFile.setOnMouseClicked(event -> {
            FileManager.instance.browseFile(data.getRootPath());
        });

        moreInformation.setOnMouseClicked(event -> {
            EventBus.getInstance().input(data);
            SceneManager.instance.loadNextScene(SceneType.SaveInformation);
        });
    }
}