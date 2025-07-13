package com.github.Laevatain0308.javaFX.scenes.downloadProgress;

import com.github.Laevatain0308.download.DownloadService;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListCell;

import java.io.IOException;

public class DownloadProgressCell extends ListCell<DownloadService>
{
    private final FXMLLoader loader;
    private final DownloadProgressCellController controller;


    public DownloadProgressCell()
    {
        loader = new FXMLLoader(getClass().getResource("/com/github/Laevatain0308/fxml/download/DownloadProgressCell.fxml"));
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
    protected void updateItem(DownloadService service , boolean empty)
    {
        super.updateItem(service , empty);

        if (empty || service == null)
        {
            setGraphic(null);
        }
        else
        {
            controller.setService(service);
            setGraphic(loader.getRoot());
        }
    }
}