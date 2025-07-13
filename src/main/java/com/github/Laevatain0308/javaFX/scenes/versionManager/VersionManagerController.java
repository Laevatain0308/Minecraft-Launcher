package com.github.Laevatain0308.javaFX.scenes.versionManager;

import com.github.Laevatain0308.event.EventBus;
import com.github.Laevatain0308.event.events.RefreshVersionManagerEvent;
import com.github.Laevatain0308.fileProcessor.FileManager;
import com.github.Laevatain0308.javaFX.NoSelectionModel;
import com.github.Laevatain0308.javaFX.SceneManager;
import com.github.Laevatain0308.javaFX.SceneType;
import com.github.Laevatain0308.version.Version;
import com.github.Laevatain0308.version.save.SaveData;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class VersionManagerController
{
    @FXML private Pane titleBar;
    @FXML private Button closeButton;
    @FXML private Button minimizeButton;

    @FXML private Pane back;
    @FXML private Pane home;

    @FXML private Label header;


    @FXML private Pane settings;
    @FXML private VBox settingsContent;

    @FXML private VBox settingsPane;
    @FXML private CheckBox specialSettings;
    @FXML private Button globalSettings;

    @FXML private Slider memorySlider;
    @FXML private TextField memoryTextField;
    @FXML private Label tipText_memory;
    @FXML private Line tipLine_memory;

    @FXML private ProgressBar upBar;
    @FXML private ProgressBar downBar;
    @FXML private Label memoryUsage;
    @FXML private Label memoryAllocation;

    @FXML private TextField widthTextField;
    @FXML private TextField heightTextField;
    @FXML private Label tipText_width;
    @FXML private Line tipLine_width;
    @FXML private Label tipText_height;
    @FXML private Line tipLine_height;


    @FXML private Pane saves;
    @FXML private VBox savesContent;

    @FXML private Pane refreshSaveList;

    @FXML private ListView<SaveData> saveList;


    @FXML private Pane fileBrowser;


    private double xOffset;
    private double yOffset;

    private Version version;

    private ExecutorService executor;

    private Pane currentContentPane;
    private Map<Pane , VBox> paneContentDic;



    public void initialize()
    {
        //===== 标题栏 =====//
        setTitleBar();


        //===== 广播事件 =====//
        // 每次打开页面时都会刷新指定版本
        EventBus.getInstance().subscribe(RefreshVersionManagerEvent.class , event -> {
            refreshScene(event.version());
        });


        //===== 页面切换 =====//
        // 准备对应字典
        paneContentDic = new HashMap<>();

        paneContentDic.put(settings , settingsContent);
        paneContentDic.put(saves , savesContent);

        // 设定事件与初始隐藏内容
        for (Map.Entry<Pane , VBox> entry: paneContentDic.entrySet())
        {
            entry.getKey().setOnMouseClicked(event -> {
                if (currentContentPane != entry.getKey())
                    select(entry.getKey());
            });

            entry.getKey().setCursor(Cursor.HAND);
            entry.getValue().setVisible(false);
        }

        // 初始化页面
        select(settings);


        //===== Game Settings =====//
        // 复选框与按钮
        specialSettings.selectedProperty().addListener((observable, oldValue, newValue) -> {
            settingsPane.setDisable(!newValue);
            version.setGlobalSettings(!newValue);
        });
        specialSettings.setCursor(Cursor.HAND);

        globalSettings.setOnAction(event -> {
            SceneManager.instance.loadNextScene(SceneType.GlobalSettings);
        });

        // 游戏内存
        setMemorySettings();

        // 游戏窗口分辨率
        setWindowSettings();


        //===== Saves =====//
        // 刷新按钮
        refreshSaveList.setOnMouseClicked(event -> {
            version.getSaveDataRepository().detectSaves();
            saveList.setItems(FXCollections.observableArrayList(version.getSaveDataRepository().getSaves()));
        });

        // 设置列表
        setSaveList();


        //===== 其余组件 =====//
        fileBrowser.setOnMouseClicked(event -> {
            FileManager.instance.browseFile(version.getVersionPath());
        });
        fileBrowser.setCursor(Cursor.HAND);
    }



    private void refreshScene(Version version)
    {
        this.version = version;

        header.setText("版本管理 - " + version.getVersionName());

        memorySlider.adjustValue(version.getGameSettings().getXmx());
        widthTextField.setText(String.valueOf(version.getGameSettings().getWidth()));
        heightTextField.setText(String.valueOf(version.getGameSettings().getHeight()));

        specialSettings.selectedProperty().set(!version.isGlobalSettings());
        settingsPane.setDisable(!specialSettings.isSelected());

        version.getSaveDataRepository().detectSaves();
        saveList.setItems(FXCollections.observableArrayList(version.getSaveDataRepository().getSaves()));
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

        back.setOnMouseClicked(event -> {
            executor.shutdown();
            SceneManager.instance.backScene();
        });
        back.setCursor(Cursor.HAND);

        home.setOnMouseClicked(event -> {
            executor.shutdown();
            SceneManager.instance.backToHome();
        });
        home.setCursor(Cursor.HAND);
    }

    private void setMemorySettings()
    {
        SystemInfo info = new SystemInfo();
        HardwareAbstractionLayer hal = info.getHardware();
        GlobalMemory memory = hal.getMemory();
        int total = (int)byteToMB(memory.getTotal());
        int available = (int)byteToMB(memory.getAvailable());

        updateMemoryRuntime(memoryUsage , upBar , hal);


        tipText_memory.setVisible(false);
        tipLine_memory.setVisible(false);


        memorySlider.setMax(total);
        memorySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(oldValue))
                return;

            // 同步文本框
            memoryTextField.setText(String.valueOf(newValue.intValue()));

            // 同步进度条
            downBar.setProgress(upBar.getProgress() + memorySlider.getValue() / (int)byteToMB(memory.getTotal()));

            // 同步提示文本
            memoryAllocation.setText(String.format("游戏分配 %.1f GB" , MBToGB(memorySlider.getValue())));

            // 同步设置
            version.getGameSettings().setXmx(newValue.intValue());

            // 更新滑条样式
            updateMemorySliderStyle(memorySlider , newValue.doubleValue());
        });


        memoryTextField.setText(String.valueOf(memorySlider.valueProperty().intValue()));
        memoryTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(oldValue))
                return;

            if (!newValue.matches("^[0-9]+$"))
            {
                tipText_memory.setVisible(true);
                tipLine_memory.setVisible(true);
                memorySlider.adjustValue(memorySlider.getMin());
                return;
            }

            tipText_memory.setVisible(false);
            tipLine_memory.setVisible(false);

            int value = Integer.parseInt(newValue);
            memorySlider.adjustValue(value);
        });


        upBar.progressProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(oldValue))
                return;

            downBar.setProgress(upBar.getProgress() + memorySlider.getValue() / (int)byteToMB(memory.getTotal()));
        });
        downBar.setProgress(upBar.getProgress() + memorySlider.getValue() / total);


        memoryAllocation.setText(String.format("游戏分配 %.1f GB" , MBToGB(memorySlider.getValue())));


        // 定时更新内存使用情况
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        Future<?> future = executor.submit(() -> {
            while (!Thread.currentThread().isInterrupted())
            {
                try
                {
                    Thread.sleep(1000);
                    Platform.runLater(() -> {
                        updateMemoryRuntime(memoryUsage , upBar , hal);
                    });
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setWindowSettings()
    {

        widthTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("^[0-9]+$"))
            {
                tipText_width.setVisible(true);
                tipLine_width.setVisible(true);
                return;
            }

            tipText_width.setVisible(false);
            tipLine_width.setVisible(false);
            version.getGameSettings().setWidth(Integer.parseInt(newValue));
        });

        heightTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("^[0-9]+$"))
            {
                tipText_height.setVisible(true);
                tipLine_height.setVisible(true);
                return;
            }

            tipText_height.setVisible(false);
            tipLine_height.setVisible(false);
            version.getGameSettings().setHeight(Integer.parseInt(newValue));
        });

        tipText_width.setVisible(false);
        tipLine_width.setVisible(false);
        tipText_height.setVisible(false);
        tipLine_height.setVisible(false);
    }


    private void setSaveList()
    {
        saveList.setCellFactory(listView -> new SaveDataCell());
        saveList.setSelectionModel(new NoSelectionModel<>());
        saveList.setItems(FXCollections.observableArrayList());
    }



    //===== 页面选择 =====//
    private void select(Pane nextPane)
    {
        // 隐藏上一页面的内容
        if (currentContentPane != null)
        {
            currentContentPane.getStyleClass().clear();
            currentContentPane.getStyleClass().add("left-list-item-unselected");
            paneContentDic.get(currentContentPane).setVisible(false);
            currentContentPane.setCursor(Cursor.HAND);
        }

        // 出现新页面的内容
        nextPane.getStyleClass().clear();
        nextPane.getStyleClass().add("left-list-item-selected");
        paneContentDic.get(nextPane).setVisible(true);
        nextPane.setCursor(Cursor.DEFAULT);

        // 修改当前页面指向
        currentContentPane = nextPane;
    }


    //===== Game Settings =====//
    private void updateMemorySliderStyle(Slider slider , double currentValue)
    {
        int percent = (int) (currentValue / slider.getMax() * 100);

        float t = percent / 100f / 3;
        float r = lerp(220f , 123f , t);
        float g = lerp(224f , 153f , t);
        float b = lerp(235f , 246f , t);
        float a = lerp(0.45f , 0.96f , t);

        String css = String.format("-slider-track-color: linear-gradient(to right ,"
                                   + " rgba(%f, %f, %f, %f) ,"
                                   + " rgba(123, 153, 246, 0.96) %d%% ,"
                                   + " rgba(%f, %f, %f, %f));" , r , g , b , a , percent , r , g , b , a);

        slider.setStyle(css);
    }

    private void updateMemoryRuntime(Label usage , ProgressBar upBar , HardwareAbstractionLayer hal)
    {
        try
        {
            GlobalMemory memory = hal.getMemory();

            long totalMemory = memory.getTotal();
            long usedMemory = totalMemory - memory.getAvailable();
            usage.setText(String.format("设备中已使用 %.2f GB / 设备总内存 %.2f GB" , byteToGB(usedMemory) , byteToGB(totalMemory)));
            upBar.setProgress((double) usedMemory / totalMemory);
        }
        catch (Exception e)
        {
            usage.setText("无法获取到设备内存使用情况");
            e.printStackTrace();
        }
    }


    //===== 辅助函数 =====//
    private double byteToMB(long bytes) { return bytes / (1024.0 * 1024.0); }
    private double byteToGB(long bytes) { return bytes / (1024.0 * 1024.0 * 1024.0); }
    private double MBToGB(double mb) { return mb / 1024.0; }

    private float lerp(float a , float b , float t) { return a * (1 - t) + b * t; }
}