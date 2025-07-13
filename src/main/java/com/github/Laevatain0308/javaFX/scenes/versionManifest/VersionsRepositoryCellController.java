package com.github.Laevatain0308.javaFX.scenes.versionManifest;

import com.github.Laevatain0308.event.EventBus;
import com.github.Laevatain0308.event.datas.TriggerData;
import com.github.Laevatain0308.event.events.RefreshRepositoryListEvent;
import com.github.Laevatain0308.event.events.RefreshVersionListEvent;
import com.github.Laevatain0308.launcher.MinecraftLauncher;
import com.github.Laevatain0308.version.VersionsRepository;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;


public class VersionsRepositoryCellController
{
    @FXML private Pane root;
    @FXML private Label name;
    @FXML private Label path;
    @FXML private Circle button;


    private VersionsRepository repository;


    public void setRepository(VersionsRepository repository)
    {
        this.repository = repository;
        name.setText(repository.getName());
        path.setText(repository.getRootPath().toString());


        button.setOnMouseReleased(event -> {
            event.consume();
            EventBus.getInstance().publish(new RefreshRepositoryListEvent(repository , RefreshRepositoryListEvent.RepositoryRefreshType.Delete));
            EventBus.getInstance().input(new TriggerData());
        });
        button.setCursor(Cursor.HAND);


        root.setOnMouseClicked(event -> {
            if (EventBus.getInstance().output(TriggerData.class) != null)
                return;

            if (MinecraftLauncher.instance.getCurrentRepository() != repository)
            {
                MinecraftLauncher.instance.setCurrentRepository(repository);
                EventBus.getInstance().publish(new RefreshVersionListEvent(repository.getVersions()));
            }
        });
    }

    public VersionsRepository getRepository() { return repository; }
}
