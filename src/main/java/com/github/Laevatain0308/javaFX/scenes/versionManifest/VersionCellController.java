package com.github.Laevatain0308.javaFX.scenes.versionManifest;

import com.github.Laevatain0308.event.EventBus;
import com.github.Laevatain0308.event.events.RefreshVersionManagerEvent;
import com.github.Laevatain0308.javaFX.SceneManager;
import com.github.Laevatain0308.javaFX.SceneType;
import com.github.Laevatain0308.jsonProcessor.VersionJson;
import com.github.Laevatain0308.jsonProcessor.VersionJsonReader;
import com.github.Laevatain0308.launcher.MinecraftLauncher;
import com.github.Laevatain0308.version.Version;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

public class VersionCellController
{
    @FXML private Pane root;
    @FXML private ImageView icon;
    @FXML private Label name;
    @FXML private Label versionInformation;
    @FXML private Pane select;


    private Version version;


    public void setVersion(Version version)
    {
        this.version = version;

        VersionJsonReader jsonReader = version.getVersionJsonReader();
        String iconURL = "/com/github/Laevatain0308/icon/grass.png";
        if (jsonReader != null && jsonReader.getVersionJson() != null && jsonReader.getVersionJson().getType() != null)
        {
            iconURL = switch (jsonReader.getVersionJson().getType())
            {
                case "release" -> "/com/github/Laevatain0308/icon/grass.png";
                case "snapshot" -> "/com/github/Laevatain0308/icon/command.png";
                case "old_alpha" , "old_beta" -> "/com/github/Laevatain0308/icon/craft_table.png";
                default -> "/com/github/Laevatain0308/icon/grass.png";
            };
        }
        icon.setImage(new Image(getClass().getResource(iconURL).toString()));


        name.setText(version.getVersionName());
        versionInformation.setText(version.getVersionID());


        select.setOnMouseClicked(event -> {
            event.consume();

            if (MinecraftLauncher.instance.getCurrentVersion() != version)
            {
                MinecraftLauncher.instance.setCurrentVersion(version);
            }

            SceneManager.instance.backToHome();
        });
        select.setCursor(Cursor.HAND);


        root.setOnMouseClicked(event -> {
            EventBus.getInstance().publish(new RefreshVersionManagerEvent(version));
            SceneManager.instance.loadNextScene(SceneType.VersionManager);
        });
    }

    public Version getVersion() { return version; }
}