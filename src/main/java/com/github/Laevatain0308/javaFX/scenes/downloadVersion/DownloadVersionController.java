package com.github.Laevatain0308.javaFX.scenes.downloadVersion;

import com.github.Laevatain0308.event.EventBus;
import com.github.Laevatain0308.event.datas.DownloadVersionData;
import com.github.Laevatain0308.event.datas.DownloadVersionInformationData;
import com.github.Laevatain0308.javaFX.SceneManager;
import com.github.Laevatain0308.javaFX.SceneType;
import com.github.Laevatain0308.jsonProcessor.VersionManifestJson;
import com.github.Laevatain0308.launcher.MinecraftLauncher;
import com.github.Laevatain0308.version.Version;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class DownloadVersionController
{
    @FXML private BorderPane root;

    @FXML private Pane titleBar;
    @FXML private Button closeButton;
    @FXML private Button minimizeButton;

    @FXML private Pane back;
    @FXML private Pane home;

    @FXML private Label header;
    @FXML private Label tip_header;

    @FXML private Pane centerPane;

    @FXML private TextField textField;
    @FXML private Line tipLine;
    @FXML private Label tipText;
    @FXML private Button downloadButton;

    private double xOffset;
    private double yOffset;


    public void initialize()
    {
        setTitleBar();

        if (MinecraftLauncher.instance.getCurrentRepository() == null)
        {
            tip_header.setVisible(true);
            header.setVisible(false);
            centerPane.setDisable(true);
            return;
        }
        else
        {
            tip_header.setVisible(false);
            header.setVisible(true);
            centerPane.setDisable(false);
        }

        VersionManifestJson.Version downloadVersion = EventBus.getInstance().output(DownloadVersionData.class).downloadVersion();
        header.setText("安装新游戏 - " + downloadVersion.getId() + "（ 版本库：" + MinecraftLauncher.instance.getCurrentRepository().getName() + " ）");


        tipText.setVisible(false);
        tipLine.setVisible(false);
        textField.textProperty().addListener((obs , oldValue , newValue) -> {
            if (newValue == null || newValue.isEmpty())
            {
                tipText.setText("必填项");
                tipText.setVisible(true);
                tipLine.setVisible(true);
                downloadButton.setDisable(true);
                return;
            }

            if (!newValue.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9_\\-()\"《》。.]+$"))
            {
                tipText.setText("名称格式错误");
                tipText.setVisible(true);
                tipLine.setVisible(true);
                downloadButton.setDisable(true);
                return;
            }

            for (Version version : MinecraftLauncher.instance.getCurrentRepository().getVersions())
            {
                if (version.getVersionName().equals(newValue))
                {
                    tipText.setText("该游戏版本已存在");
                    tipText.setVisible(true);
                    tipLine.setVisible(true);
                    downloadButton.setDisable(true);
                    return;
                }
            }

            tipText.setVisible(false);
            tipLine.setVisible(false);
            downloadButton.setDisable(false);
        });
        textField.setText(downloadVersion.getId());


        downloadButton.setOnAction(event -> {
            root.setDisable(true);
            EventBus.getInstance().input(new DownloadVersionInformationData(downloadVersion.getId() , textField.getText()));
            SceneManager.instance.loadModalScene((Stage) downloadButton.getScene().getWindow() , SceneType.DownloadProcess);
        });
    }


    private void setTitleBar()
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
    }
}
