package com.github.Laevatain0308.javaFX.scenes.versionManifest;

import com.github.Laevatain0308.version.VersionsRepository;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.control.ListCell;

import java.io.IOException;

public class VersionsRepositoryCell extends ListCell<VersionsRepository>
{
    private final FXMLLoader loader;
    private final VersionsRepositoryCellController controller;

    public VersionsRepositoryCell()
    {
        loader = new FXMLLoader(getClass().getResource("/com/github/Laevatain0308/fxml/version/VersionsRepositoryCell.fxml"));
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
            throw new RuntimeException("加载FXML失败" , e);
        }
    }

    @Override
    protected void updateItem(VersionsRepository versionsRepository , boolean empty)
    {
        super.updateItem(versionsRepository , empty);

        if (empty || versionsRepository == null)
        {
            setGraphic(null);
        }
        else
        {
            controller.setRepository(versionsRepository);
            setGraphic(loader.getRoot());

            selectedProperty().addListener((obs, wasSelected, isSelectedNow) -> {
                if (isSelectedNow)
                    setCursor(Cursor.DEFAULT);
                else
                    setCursor(Cursor.HAND);
            });

            if (!isSelected())
                setCursor(Cursor.HAND);
            else
                setCursor(Cursor.DEFAULT);
        }
    }
}
