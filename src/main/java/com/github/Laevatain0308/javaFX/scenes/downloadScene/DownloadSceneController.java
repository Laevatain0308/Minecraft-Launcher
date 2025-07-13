package com.github.Laevatain0308.javaFX.scenes.downloadScene;

import com.github.Laevatain0308.javaFX.NoSelectionModel;
import com.github.Laevatain0308.javaFX.SceneManager;
import com.github.Laevatain0308.jsonProcessor.VersionManifestJson;
import com.github.Laevatain0308.launcher.MinecraftLauncher;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class DownloadSceneController
{
    @FXML private Pane titleBar;
    @FXML private Button closeButton;
    @FXML private Button minimizeButton;

    @FXML private Pane back;
    @FXML private Pane home;

    @FXML private Pane BMCLTip;

    @FXML private CheckBox officialVersion;
    @FXML private CheckBox snapshotVersion;
    @FXML private CheckBox ancientVersion;

    @FXML private Pane search;
    @FXML private Pane refresh;

    @FXML private Pane searchPane;
    @FXML private TextField searchTextField;
    @FXML private Button overSearchButton;

    @FXML private ListView<VersionManifestJson.Version> versionList;

    private double xOffset;
    private double yOffset;

    private List<VersionManifestJson.Version> currentVersions;


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

        home.setOnMouseClicked(event -> SceneManager.instance.backToHome());
        home.setCursor(Cursor.HAND);


        BMCLTip.setOnMouseClicked(event -> {
            // 跳转到浏览器链接
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE))
            {
                String url = "https://bmclapidoc.bangbang93.com/";
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
        BMCLTip.setCursor(Cursor.HAND);


        officialVersion.setSelected(true);
        officialVersion.setOnAction(event -> {
            if (officialVersion.isSelected())
            {
                snapshotVersion.setSelected(false);
                ancientVersion.setSelected(false);
            }
            else
            {
                if (!snapshotVersion.isSelected() && !ancientVersion.isSelected())
                    versionList.setItems(FXCollections.observableArrayList());
            }

            overSearch();
        });

        snapshotVersion.setSelected(false);
        snapshotVersion.setOnAction(event -> {
            if (snapshotVersion.isSelected())
            {
                officialVersion.setSelected(false);
                ancientVersion.setSelected(false);
            }
            else
            {
                if (!officialVersion.isSelected() && !ancientVersion.isSelected())
                    versionList.setItems(FXCollections.observableArrayList());
            }

            overSearch();
        });

        ancientVersion.setSelected(false);
        ancientVersion.setOnAction(event -> {
            if (ancientVersion.isSelected())
            {
                officialVersion.setSelected(false);
                snapshotVersion.setSelected(false);
            }
            else
            {
                if (!officialVersion.isSelected() && !snapshotVersion.isSelected())
                    versionList.setItems(FXCollections.observableArrayList());
            }

            overSearch();
        });


        search.setOnMouseClicked(event -> {
            searchPane.setVisible(true);
            search.setVisible(false);
            refresh.setVisible(false);
        });
        search.setCursor(Cursor.HAND);


        refresh.setOnMouseClicked(event -> {
            refresh();
        });
        refresh.setCursor(Cursor.HAND);


        searchPane.setVisible(false);
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            List<VersionManifestJson.Version> versions = new ArrayList<>();
            for (VersionManifestJson.Version version : currentVersions)
            {
                if (version.getId().contains(newValue))
                    versions.add(version);
            }
            versionList.setItems(FXCollections.observableArrayList(versions));
        });
        overSearchButton.setOnAction(event -> {
            overSearch();
        });


        //===== 下载版本列表 =====//
        // 设置工厂
        versionList.setCellFactory(listView -> new DownloadCell());
        // 设置初始版本库
        versionList.setItems(FXCollections.observableArrayList(MinecraftLauncher.instance.officialDownloadVersions));
        currentVersions = MinecraftLauncher.instance.officialDownloadVersions;
        // 设置选择模式为：无选择模式
        versionList.setSelectionModel(new NoSelectionModel<>());
    }


    private void refresh()
    {
        if (officialVersion.isSelected())
        {
            versionList.setItems(FXCollections.observableArrayList(MinecraftLauncher.instance.officialDownloadVersions));
            currentVersions = MinecraftLauncher.instance.officialDownloadVersions;
        }
        else if (snapshotVersion.isSelected())
        {
            versionList.setItems(FXCollections.observableArrayList(MinecraftLauncher.instance.snapshotDownloadVersions));
            currentVersions = MinecraftLauncher.instance.snapshotDownloadVersions;
        }
        else if (ancientVersion.isSelected())
        {
            versionList.setItems(FXCollections.observableArrayList(MinecraftLauncher.instance.ancientDownloadVersions));
            currentVersions = MinecraftLauncher.instance.ancientDownloadVersions;
        }
        else
        {
            versionList.setItems(FXCollections.observableArrayList());
            currentVersions = null;
        }
    }

    private void overSearch()
    {
        searchTextField.clear();
        searchPane.setVisible(false);
        search.setVisible(true);
        refresh.setVisible(true);
        refresh();
    }
}