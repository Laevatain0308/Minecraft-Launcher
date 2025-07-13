package com.github.Laevatain0308.javaFX.scenes.versionManager;

import com.github.Laevatain0308.version.save.SaveData;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.ListCell;

import java.io.IOException;

public class SaveDataCell extends ListCell<SaveData>
{
    private final FXMLLoader loader;
    private final SaveDataCellController controller;


    public SaveDataCell()
    {
        loader = new FXMLLoader(getClass().getResource("/com/github/Laevatain0308/fxml/version/SaveDataCell.fxml"));
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
    protected void updateItem(SaveData data , boolean empty)
    {
        super.updateItem(data , empty);

        if (empty || data == null)
        {
            setGraphic(null);
        }
        else
        {
            controller.setData(data);
            setGraphic(loader.getRoot());

            setCursor(Cursor.HAND);
        }
    }
}
