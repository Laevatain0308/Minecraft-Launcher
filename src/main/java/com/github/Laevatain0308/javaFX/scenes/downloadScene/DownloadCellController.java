package com.github.Laevatain0308.javaFX.scenes.downloadScene;

import com.github.Laevatain0308.event.EventBus;
import com.github.Laevatain0308.event.datas.DownloadVersionData;
import com.github.Laevatain0308.javaFX.SceneManager;
import com.github.Laevatain0308.javaFX.SceneType;
import com.github.Laevatain0308.jsonProcessor.VersionManifestJson;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

import java.awt.*;
import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DownloadCellController
{
    @FXML private Pane root;
    @FXML private ImageView icon;
    @FXML private Label type;
    @FXML private Label versionID;
    @FXML private Label releaseTime;
    @FXML private Pane wiki;


    public void setDownloadVersion(VersionManifestJson.Version downloadVersion)
    {
        switch (downloadVersion.getType())
        {
            case "release" -> {
                type.setText("正式版");
                icon.setImage(new Image(getClass().getResource("/com/github/Laevatain0308/icon/grass.png").toString()));
            }
            case "snapshot" -> {
                type.setText("快照版");
                icon.setImage(new Image(getClass().getResource("/com/github/Laevatain0308/icon/command.png").toString()));
            }
            case "old_alpha" , "old_beta" -> {
                type.setText("远古版");
                icon.setImage(new Image(getClass().getResource("/com/github/Laevatain0308/icon/craft_table.png").toString()));
            }
        }
        versionID.setText(downloadVersion.getId());
        releaseTime.setText(translateDate(downloadVersion.getReleaseTime()));


        wiki.setVisible(!downloadVersion.getType().equals("old_alpha") && !downloadVersion.getType().equals("old_beta"));
        wiki.setOnMouseClicked(event -> {
            // 跳转到浏览器链接
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE))
            {
                String url = "https://zh.minecraft.wiki/w/" + downloadVersion.getId();
                try
                {
                    desktop.browse(new URI(url));
                }
                catch (Exception e)
                {
                    System.err.println("无法打开网页 " + url + "  错误：" + e.getMessage());
                }
            }
        });
        wiki.setCursor(Cursor.HAND);


        root.setOnMouseClicked(event -> {
            EventBus.getInstance().input(new DownloadVersionData(downloadVersion));
            SceneManager.instance.loadNextScene(SceneType.DownloadVersion);
        });
        root.setCursor(Cursor.HAND);
    }


    private String translateDate(String releaseDate)
    {
        DateTimeFormatter formatter_1 = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        DateTimeFormatter formatter_2 = DateTimeFormatter.ofPattern("yyyy 年 MM 月 dd 日 HH:mm:ss");

        ZonedDateTime dateTime = ZonedDateTime.parse(releaseDate , formatter_1).withZoneSameInstant(ZoneId.systemDefault());
        return dateTime.format(formatter_2);
    }
}