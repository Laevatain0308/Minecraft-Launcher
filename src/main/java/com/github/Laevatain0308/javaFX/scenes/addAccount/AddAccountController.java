package com.github.Laevatain0308.javaFX.scenes.addAccount;

import com.github.Laevatain0308.account.Account;
import com.github.Laevatain0308.account.AccountManager;
import com.github.Laevatain0308.account.LoadType;
import com.github.Laevatain0308.event.EventBus;
import com.github.Laevatain0308.event.datas.AccountLoadTypeData;
import com.github.Laevatain0308.javaFX.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class AddAccountController
{
    @FXML private Pane titleBar;
    @FXML private Button closeButton;
    @FXML private Button minimizeButton;

    @FXML private Pane back;
    @FXML private Pane home;

    @FXML private Label header;


    @FXML private TextField nameTextField;
    @FXML private Label tipText_Name;
    @FXML private Line tipLine_Name;

    @FXML private TextField uuidTextField;
    @FXML private Label tipText_UUID;
    @FXML private Line tipLine_UUID;


    @FXML private Button saveButton;


    private double xOffset;
    private double yOffset;


    private boolean nameCorrect = false;
    private boolean uuidCorrect = true;


    public void initialize()
    {
        LoadType type = EventBus.getInstance().output(AccountLoadTypeData.class).loadType();

        setTitleBar(type);

        nameTextField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty())
            {
                tipText_Name.setText("必填项");
                tipText_Name.setVisible(true);
                tipLine_Name.setVisible(true);
                nameCorrect = false;
                saveButton.setDisable(true);
                return;
            }

            if (!newValue.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9_\\-()\"《》。.]+$"))
            {
                tipText_Name.setText("名称格式错误");
                tipText_Name.setVisible(true);
                tipLine_Name.setVisible(true);
                nameCorrect = false;
                saveButton.setDisable(true);
                return;
            }

            for (Account account : AccountManager.instance.getUsers())
            {
                if (account.getName().equals(newValue))
                {
                    tipText_Name.setText("该账户已存在");
                    tipText_Name.setVisible(true);
                    tipLine_Name.setVisible(true);
                    nameCorrect = false;
                    saveButton.setDisable(true);
                    return;
                }
            }

            tipText_Name.setVisible(false);
            tipLine_Name.setVisible(false);
            nameCorrect = true;
            uuidTextField.setPromptText(Account.getUUIDByName(newValue));
            saveButton.setDisable(!uuidCorrect);
        });

        tipText_UUID.setVisible(false);
        tipLine_UUID.setVisible(false);
        uuidTextField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty())
            {
                uuidTextField.setPromptText(Account.getUUIDByName(nameTextField.getText()));
                tipText_UUID.setVisible(false);
                tipLine_UUID.setVisible(false);
                uuidCorrect = true;
                saveButton.setDisable(!nameCorrect);
                return;
            }

            if (!newValue.matches("^[a-zA-Z0-9_\\-]+$"))
            {
                tipText_UUID.setText("UUID格式错误");
                tipText_UUID.setVisible(true);
                tipLine_UUID.setVisible(true);
                uuidCorrect = false;
                saveButton.setDisable(true);
                return;
            }

            for (Account account : AccountManager.instance.getUsers())
            {
                if (account.getUUID().equals(newValue))
                {
                    tipText_UUID.setText("该UUID对应账户已存在");
                    tipText_UUID.setVisible(true);
                    tipLine_UUID.setVisible(true);
                    uuidCorrect = false;
                    saveButton.setDisable(true);
                    return;
                }
            }

            tipText_UUID.setVisible(false);
            tipLine_UUID.setVisible(false);
            uuidCorrect = true;
            saveButton.setDisable(!nameCorrect);
        });


        saveButton.setOnAction(event -> {
            Account newUser;
            if (uuidTextField.getText().isEmpty())
                newUser = new Account(nameTextField.getText() , type);
            else
                newUser = new Account(uuidTextField.getText() , uuidTextField.getText() , type);
            AccountManager.instance.addUser(newUser);

            SceneManager.instance.backScene();
        });
        saveButton.setDisable(true);
    }


    private void setTitleBar(LoadType loadType)
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

        header.setText("添加" + Account.getLoadTypeAsString(loadType) + "账户");
    }
}