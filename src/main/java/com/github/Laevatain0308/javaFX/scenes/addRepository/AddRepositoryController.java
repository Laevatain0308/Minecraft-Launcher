package com.github.Laevatain0308.javaFX.scenes.addRepository;

import com.github.Laevatain0308.javaFX.SceneManager;
import com.github.Laevatain0308.launcher.MinecraftLauncher;
import com.github.Laevatain0308.version.VersionsRepository;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class AddRepositoryController
{
    @FXML private Pane titleBar;
    @FXML private Button closeButton;
    @FXML private Button minimizeButton;

    @FXML private Pane back;
    @FXML private Pane home;

    @FXML private TextField textField;
    @FXML private Label tipText_Name;
    @FXML private Line tipLine;

    @FXML private Label repositoryPath;
    @FXML private Pane fileChooser;
    @FXML private Label tipText_Path;

    @FXML private Button saveButton;


    private double xOffset;
    private double yOffset;

    private boolean nameCorrect = false;
    private boolean pathCorrect = false;

    private String lastPath;


    public void initialize()
    {
        setTitleBar();


        textField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty())
            {
                tipText_Name.setText("必填项");
                tipText_Name.setVisible(true);
                tipLine.setVisible(true);
                nameCorrect = false;
                saveButton.setDisable(true);
                return;
            }

            if (!newValue.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9_\\-()\"《》。.]+$"))
            {
                tipText_Name.setText("名称格式错误");
                tipText_Name.setVisible(true);
                tipLine.setVisible(true);
                nameCorrect = false;
                saveButton.setDisable(true);
                return;
            }

            for (VersionsRepository repository : MinecraftLauncher.instance.getRepositories())
            {
                if (repository.getName().equals(newValue))
                {
                    tipText_Name.setText("该名称已存在");
                    tipText_Name.setVisible(true);
                    tipLine.setVisible(true);
                    nameCorrect = false;
                    saveButton.setDisable(true);
                    return;
                }
            }

            tipText_Name.setVisible(false);
            tipLine.setVisible(false);
            nameCorrect = true;
            saveButton.setDisable(!pathCorrect);
        });


        tipText_Path.setVisible(false);
        fileChooser.setOnMouseClicked(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("选择游戏版本文件夹");

            if (lastPath != null)
            {
                directoryChooser.setInitialDirectory(new File(lastPath));
            }
            else
            {
                if (MinecraftLauncher.instance.getOSName().equals("windows"))
                    directoryChooser.setInitialDirectory(new File("C:/"));
                else
                    directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            }

            Stage stage = (Stage) fileChooser.getScene().getWindow();
            File dir = directoryChooser.showDialog(stage);
            if (dir != null)
            {
                lastPath = dir.getPath();
                repositoryPath.setText(dir.getPath());

                for (VersionsRepository repository : MinecraftLauncher.instance.getRepositories())
                {
                    if (repository.getRootPath().equals(dir.toPath()))
                    {
                        tipText_Path.setVisible(true);
                        pathCorrect = false;
                        saveButton.setDisable(true);
                        return;
                    }
                }

                tipText_Path.setVisible(false);
                pathCorrect = true;
                saveButton.setDisable(!nameCorrect);
            }
        });
        fileChooser.setCursor(Cursor.HAND);


        saveButton.setOnAction(event -> {
            MinecraftLauncher.instance.createNewRepository(textField.getText() , repositoryPath.getText());
            SceneManager.instance.backScene();
        });
        saveButton.setDisable(true);
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