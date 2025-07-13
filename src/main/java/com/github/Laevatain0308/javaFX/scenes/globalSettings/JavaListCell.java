package com.github.Laevatain0308.javaFX.scenes.globalSettings;

import com.github.Laevatain0308.java.JavaInformation;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.ListCell;

import java.io.IOException;

public class JavaListCell extends ListCell<JavaInformation>
{
    private final FXMLLoader loader;
    private final JavaListCellController controller;


    public JavaListCell()
    {
        loader = new FXMLLoader(getClass().getResource("/com/github/Laevatain0308/fxml/globalSettings/JavaListCell.fxml"));
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
    protected void updateItem(JavaInformation info , boolean empty)
    {
        super.updateItem(info , empty);

        if (empty || info == null)
        {
            setGraphic(null);
        }
        else
        {
            controller.setJavaInformation(info);
            setGraphic(loader.getRoot());

            setCursor(Cursor.HAND);
        }
    }
}
