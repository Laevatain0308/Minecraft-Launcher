package com.github.Laevatain0308.javaFX;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class SceneManager
{
    public static SceneManager instance = new SceneManager(SceneType.MainScene);

    private Scene currentScene;
    private final Map<SceneType , Parent> preloadedScenes = new HashMap<>();

    private final SceneType initialSceneType;
    private SceneType lastSceneType;
    private SceneType currentSceneType;
    private final Stack<SceneType> lastSceneStack;

    private Stage modalStage;


    public SceneManager(SceneType initialSceneType)
    {
        this.initialSceneType = initialSceneType;
        lastSceneStack = new Stack<>();

        // 预加载部分场景
        preloadScene();

        // 初始化场景
        loadScene(initialSceneType);
    }


    //===== 获取场景 =====//
    private String getScenePathByType(SceneType sceneType)
    {
        return switch (sceneType)
        {
            case MainScene -> "/com/github/Laevatain0308/fxml/main/MainScene.fxml";
            case VersionManifest -> "/com/github/Laevatain0308/fxml/version/VersionManifest.fxml";
            case AddRepository -> "/com/github/Laevatain0308/fxml/version/AddRepository.fxml";
            case DownloadScene -> "/com/github/Laevatain0308/fxml/download/DownloadScene.fxml";
            case DownloadVersion -> "/com/github/Laevatain0308/fxml/download/DownloadVersion.fxml";
            case DownloadProcess -> "/com/github/Laevatain0308/fxml/download/DownloadProgress.fxml";
            case GlobalSettings -> "/com/github/Laevatain0308/fxml/globalSettings/GlobalSettings.fxml";
            case VersionManager -> "/com/github/Laevatain0308/fxml/version/VersionManager.fxml";
            case SaveInformation -> "/com/github/Laevatain0308/fxml/version/SaveInformation.fxml";
            case AccountScene -> "/com/github/Laevatain0308/fxml/account/AccountScene.fxml";
            case AddAccount -> "/com/github/Laevatain0308/fxml/account/AddAccount.fxml";
        };
    }


    //===== 预加载 =====//
    private void preloadScene()
    {
        preloadedScenes.put(SceneType.MainScene , getSceneRoot(SceneType.MainScene));
        preloadedScenes.put(SceneType.DownloadScene , getSceneRoot(SceneType.DownloadScene));
        preloadedScenes.put(SceneType.GlobalSettings , getSceneRoot(SceneType.GlobalSettings));
        preloadedScenes.put(SceneType.VersionManager , getSceneRoot(SceneType.VersionManager));
    }


    //===== 窗口跳转 =====//
    public void loadNextScene(SceneType sceneType)
    {
        loadScene(sceneType);
        lastSceneStack.push(lastSceneType);
    }


    public void backScene()
    {
        if (lastSceneStack.isEmpty())
            return;

        loadScene(lastSceneStack.pop());
    }

    public void backToHome()
    {
        loadScene(initialSceneType);
        lastSceneStack.clear();
    }

    private void loadScene(SceneType sceneType)
    {
        Parent root = preloadedScenes.containsKey(sceneType) ? preloadedScenes.get(sceneType) : getSceneRoot(sceneType);

        if (root == null)
        {
            System.err.println("获取场景失败");
            return;
        }

        if (currentScene == null)
            currentScene = new Scene(root);
        else
            currentScene.setRoot(root);

        currentScene.setFill(null);

        lastSceneType = currentSceneType;
        currentSceneType = sceneType;
    }



    //===== 附加窗口 =====//
    public void loadModalScene(Stage ownerStage , SceneType sceneType)
    {
        Parent root = SceneManager.instance.getSceneRoot(SceneType.DownloadProcess);
        if (root == null)
            return;

        modalStage = new Stage();
        modalStage.initModality(Modality.WINDOW_MODAL);
        modalStage.initOwner(ownerStage);
        modalStage.initStyle(StageStyle.TRANSPARENT);
        Scene scene = new Scene(root);
        modalStage.setScene(scene);
        modalStage.show();
    }

    public void disableModalScene()
    {
        if (modalStage != null)
        {
            modalStage.close();
            modalStage = null;
        }
    }


    //===== 辅助函数 =====//
    private Parent getSceneRoot(SceneType sceneType)
    {
        try
        {
            return FXMLLoader.load(getClass().getResource(getScenePathByType(sceneType)));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            //System.err.println("无法获取场景：" + sceneType + "  错误：" + e.getMessage());
            return null;
        }
    }


    //===== getter / setter =====//
    public Scene getCurrentScene() { return currentScene; }
}
