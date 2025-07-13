package com.github.Laevatain0308.javaFX.scenes.accountScene;

import com.github.Laevatain0308.account.Account;
import com.github.Laevatain0308.account.AccountManager;
import com.github.Laevatain0308.account.LoadType;
import com.github.Laevatain0308.event.EventBus;
import com.github.Laevatain0308.event.datas.AccountLoadTypeData;
import com.github.Laevatain0308.event.events.RefreshAccountListEvent;
import com.github.Laevatain0308.javaFX.NoSelectionModel;
import com.github.Laevatain0308.javaFX.SceneManager;
import com.github.Laevatain0308.javaFX.SceneType;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class AccountSceneController
{
    @FXML private Pane titleBar;
    @FXML private Button closeButton;
    @FXML private Button minimizeButton;

    @FXML private Pane back;


    @FXML private Pane addOfflineUser;
    @FXML private ListView<Account> userList;


    private double xOffset;
    private double yOffset;


    public void initialize()
    {
        setTitleBar();

        userList.setCellFactory(listView -> new AccountCell());
        userList.setSelectionModel(new NoSelectionModel<>());
        userList.setItems(FXCollections.observableArrayList(AccountManager.instance.getUsers()));
        EventBus.getInstance().subscribe(RefreshAccountListEvent.class , event -> {
            switch (event.type())
            {
                case Add -> AccountManager.instance.addUser(event.account());
                case Delete -> AccountManager.instance.removeUser(event.account());
            }
            refresh();
        });


        addOfflineUser.setOnMouseClicked(event -> {
            EventBus.getInstance().input(new AccountLoadTypeData(LoadType.Offline));
            SceneManager.instance.loadNextScene(SceneType.AddAccount);
        });
        addOfflineUser.setCursor(Cursor.HAND);
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

        back.setOnMouseClicked(event -> {
            SceneManager.instance.backScene();
        });
        back.setCursor(Cursor.HAND);
    }


    private void refresh() { userList.getItems().setAll(AccountManager.instance.getUsers()); }
}