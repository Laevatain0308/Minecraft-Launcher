package com.github.Laevatain0308.javaFX.scenes.mainScene;

import com.github.Laevatain0308.account.Account;
import com.github.Laevatain0308.account.AccountManager;
import com.github.Laevatain0308.event.EventBus;
import com.github.Laevatain0308.event.events.RefreshMainSceneAccountEvent;
import com.github.Laevatain0308.event.events.RefreshMainSceneCurrentVersionEvent;
import com.github.Laevatain0308.event.events.RefreshStartGameButtonEvent;
import com.github.Laevatain0308.event.events.RefreshVersionManagerEvent;
import com.github.Laevatain0308.java.JavaRepository;
import com.github.Laevatain0308.javaFX.SceneManager;
import com.github.Laevatain0308.javaFX.SceneType;
import com.github.Laevatain0308.launcher.MinecraftLauncher;
import com.github.Laevatain0308.version.Version;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainSceneController
{
    @FXML private Button closeButton;
    @FXML private Button minimizeButton;
    @FXML private Button startGameButton;

    @FXML private Pane titleBar;

    @FXML private Pane account;
    @FXML private Pane versionManager;
    @FXML private Pane versionManifest;
    @FXML private Pane download;
    @FXML private Pane settings;

    @FXML private Label userName;
    @FXML private Label loadType;

    @FXML private Label tipText_status;
    @FXML private Label tipText_version;

    @FXML private Label versionInformation;


    private double xOffset;
    private double yOffset;


    public void initialize()
    {
        setTitleBar();

        account.setOnMouseClicked(event -> {
            SceneManager.instance.loadNextScene(SceneType.AccountScene);
        });
        account.setCursor(Cursor.HAND);


        versionManager.setOnMouseClicked(event -> {
            if (MinecraftLauncher.instance.getCurrentVersion() != null)
            {
                EventBus.getInstance().publish(new RefreshVersionManagerEvent(MinecraftLauncher.instance.getCurrentVersion()));
                SceneManager.instance.loadNextScene(SceneType.VersionManager);
            }
            else
            {
                SceneManager.instance.loadNextScene(SceneType.VersionManifest);
            }
        });
        versionManager.setCursor(Cursor.HAND);


        versionManifest.setOnMouseClicked(event -> SceneManager.instance.loadNextScene(SceneType.VersionManifest));
        versionManifest.setCursor(Cursor.HAND);


        download.setOnMouseClicked(event -> SceneManager.instance.loadNextScene(SceneType.DownloadScene));
        download.setCursor(Cursor.HAND);


        settings.setOnMouseClicked(event -> SceneManager.instance.loadNextScene(SceneType.GlobalSettings));
        settings.setCursor(Cursor.HAND);

        checkCanStart();
        startGameButton.setOnAction(event -> {
            if (MinecraftLauncher.instance.getCurrentVersion() != null
                && AccountManager.instance.getCurrentUser() != null
                && !JavaRepository.instance.getJavas().isEmpty())
            {
                System.out.println("启动游戏...");
                MinecraftLauncher.instance.getCurrentVersion().startGame();
            }
        });
        EventBus.getInstance().subscribe(RefreshStartGameButtonEvent.class , event -> {
            checkCanStart();
        });


        currentVersion(MinecraftLauncher.instance.getCurrentVersion());
        EventBus.getInstance().subscribe(RefreshMainSceneCurrentVersionEvent.class , event -> {
            currentVersion(event.version());
            checkCanStart();
        });


        currentUser(AccountManager.instance.getCurrentUser());
        EventBus.getInstance().subscribe(RefreshMainSceneAccountEvent.class , event -> {
            currentUser(event.currentUser());
            checkCanStart();
        });
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
    }


    private void checkCanStart()
    {
        if (MinecraftLauncher.instance.getCurrentVersion() == null)
        {
            versionInformation.setText("未选择游戏版本");
            startGameButton.setDisable(true);
            return;
        }

        if (AccountManager.instance.getCurrentUser() == null)
        {
            versionInformation.setText("未选择游戏账户");
            startGameButton.setDisable(true);
            return;
        }

        if (JavaRepository.instance.getSuitableJava(MinecraftLauncher.instance.getCurrentVersion()) == null)
        {
            versionInformation.setText("未找到兼容的Java版本");
            startGameButton.setDisable(true);
            return;
        }

        versionInformation.setText(MinecraftLauncher.instance.getCurrentVersion().getVersionName());
        startGameButton.setDisable(false);
    }


    private void currentVersion(Version version)
    {
        if (version != null)
        {
            tipText_status.setText("版本管理");
            tipText_version.setText(version.getVersionName());
        }
        else
        {
            tipText_status.setText("未选择游戏版本");
            tipText_version.setText("进入版本列表选择版本");
        }
    }

    private void currentUser(Account account)
    {
        if (account != null)
        {
            userName.setText(account.getName());
            loadType.setText("离线账户");
        }
        else
        {
            userName.setText("没有游戏账户");
            loadType.setText("点击此处添加账户");
        }
    }
}
