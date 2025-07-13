package com.github.Laevatain0308.javaFX.scenes.versionManifest;

import com.github.Laevatain0308.version.Version;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.ListCell;

import java.io.IOException;

public class VersionCell extends ListCell<Version>
{
    private final FXMLLoader loader;
    private final VersionCellController controller;


    public VersionCell()
    {
        loader = new FXMLLoader(getClass().getResource("/com/github/Laevatain0308/fxml/version/VersionCell.fxml"));
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
    protected void updateItem(Version version , boolean empty)
    {
        super.updateItem(version , empty);

        if (empty || version == null)
        {
            setGraphic(null);
        }
        else
        {
            controller.setVersion(version);
            setGraphic(loader.getRoot());

            setCursor(Cursor.HAND);
        }
    }
}