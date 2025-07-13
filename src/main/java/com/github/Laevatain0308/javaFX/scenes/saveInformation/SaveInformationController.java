package com.github.Laevatain0308.javaFX.scenes.saveInformation;

import com.github.Laevatain0308.event.EventBus;
import com.github.Laevatain0308.javaFX.SceneManager;
import com.github.Laevatain0308.version.save.LevelDataReader;
import com.github.Laevatain0308.version.save.SaveData;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class SaveInformationController
{
    @FXML private Pane titleBar;
    @FXML private Button closeButton;
    @FXML private Button minimizeButton;

    @FXML private Pane back;
    @FXML private Pane home;


    @FXML private Label levelName;
    @FXML private Label gameVersion;
    @FXML private Label seed;
    @FXML private Label lastPlayedDate;
    @FXML private Label gameTime;
    @FXML private Slider commandToggle;
    @FXML private Slider buildingToggle;
    @FXML private TextField difficultyTextField;
    @FXML private Label playerPos;
    @FXML private Label lastDeathPos;
    @FXML private Label respawnPos;
    @FXML private TextField gameTypeTextField;
    @FXML private TextField healthTextField;
    @FXML private TextField foodLevelTextField;
    @FXML private TextField xpLevelTextField;


    private double xOffset;
    private double yOffset;


    public void initialize()
    {
        setTitleBar();

        SaveData data = EventBus.getInstance().output(SaveData.class);
        LevelDataReader reader = data.getLevelDataReader();

        levelName.setText(reader.getLevelName());
        gameVersion.setText(reader.getGameVersion());
        seed.setText(reader.getSeed());
        lastPlayedDate.setText(reader.getLastPlayed());
        gameTime.setText(reader.getTime());

        commandToggle.setMouseTransparent(true);
        setToggle(commandToggle , reader.allowCommands());

        buildingToggle.setMouseTransparent(true);
        setToggle(buildingToggle , reader.canGenerateFeatures());

        difficultyTextField.setMouseTransparent(true);
        difficultyTextField.setText(reader.getDifficulty());

        playerPos.setText(reader.getPlayerPosition());
        lastDeathPos.setText(reader.getLastDeathLocation());
        respawnPos.setText(reader.getRespawnPosition());

        gameTypeTextField.setMouseTransparent(true);
        gameTypeTextField.setText(reader.getGameType());

        healthTextField.setMouseTransparent(true);
        healthTextField.setText(reader.getHealth());

        foodLevelTextField.setMouseTransparent(true);
        foodLevelTextField.setText(reader.getFoodLevel());

        xpLevelTextField.setMouseTransparent(true);
        xpLevelTextField.setText(reader.getXpLevel());
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


    private void setToggle(Slider slider , boolean state)
    {
        slider.getStyleClass().clear();
        slider.getStyleClass().add("slider");
        slider.getStyleClass().add(state ? "slider-active" : "slider-inactive");

        slider.setValue(state ? slider.getMax() : slider.getMin());
    }
}