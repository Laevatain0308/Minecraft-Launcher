package com.github.Laevatain0308.javaFX.scenes.accountScene;

import com.github.Laevatain0308.account.Account;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;

import java.io.IOException;

public class AccountCell extends ListCell<Account>
{
    private final FXMLLoader loader;
    private final AccountCellController controller;


    public AccountCell()
    {
        loader = new FXMLLoader(getClass().getResource("/com/github/Laevatain0308/fxml/account/AccountCell.fxml"));
        try
        {
            loader.load();
            controller = loader.getController();

            if (controller == null)
            {
                throw new RuntimeException("控制器未初始化！检查 FXML 的 fx:controller 属性");
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("加载 FXML 失败" , e);
        }
    }


    @Override
    protected void updateItem(Account user , boolean empty)
    {
        super.updateItem(user , empty);

        if (empty || user == null)
        {
            setGraphic(null);
        }
        else
        {
            controller.setAccount(user);
            setGraphic(loader.getRoot());
        }
    }
}
