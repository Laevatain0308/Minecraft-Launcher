package com.github.Laevatain0308.javaFX.scenes.accountScene;

import com.github.Laevatain0308.account.Account;
import com.github.Laevatain0308.account.AccountManager;
import com.github.Laevatain0308.event.EventBus;
import com.github.Laevatain0308.event.events.RefreshAccountListEvent;
import com.github.Laevatain0308.javaFX.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

public class AccountCellController
{
    @FXML private Label name;
    @FXML private Label tip;
    @FXML private Label loadType;
    @FXML private Pane deleteUser;
    @FXML private Pane chooseUser;


    public void setAccount(Account user)
    {
        name.setText(user.getName());
        tip.setVisible(AccountManager.instance.isCurrentUser(user));
        loadType.setText(user.getLoadTypeAsString());

        deleteUser.setOnMouseClicked(event -> {
            EventBus.getInstance().publish(new RefreshAccountListEvent(user , RefreshAccountListEvent.RefreshAccountListType.Delete));
        });
        deleteUser.setCursor(Cursor.HAND);

        chooseUser.setOnMouseClicked(event -> {
            AccountManager.instance.setCurrentUser(user);
            SceneManager.instance.backScene();
        });
        chooseUser.setCursor(Cursor.HAND);
    }
}
