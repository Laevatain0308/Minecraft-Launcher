package com.github.Laevatain0308.javaFX.scenes.downloadScene;

import com.github.Laevatain0308.jsonProcessor.VersionManifestJson;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;

import java.io.IOException;

public class DownloadCell extends ListCell<VersionManifestJson.Version>
{
    private final FXMLLoader loader;
    private final DownloadCellController controller;

    public DownloadCell()
    {
        loader = new FXMLLoader(getClass().getResource("/com/github/Laevatain0308/fxml/download/DownloadCell.fxml"));

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
    protected void updateItem(VersionManifestJson.Version downloadVersion , boolean empty)
    {
        super.updateItem(downloadVersion , empty);

        if (empty || downloadVersion == null)
        {
            setGraphic(null);
        }
        else
        {
            controller.setDownloadVersion(downloadVersion);
            setGraphic(loader.getRoot());
        }
    }
}