package com.github.Laevatain0308.javaFX.scenes.globalSettings;

import com.github.Laevatain0308.event.EventBus;
import com.github.Laevatain0308.event.events.RefreshJavaListEvent;
import com.github.Laevatain0308.event.events.RefreshStartGameButtonEvent;
import com.github.Laevatain0308.java.JavaInformation;
import com.github.Laevatain0308.java.JavaRepository;
import com.github.Laevatain0308.javaFX.NoSelectionModel;
import com.github.Laevatain0308.javaFX.SceneManager;
import com.github.Laevatain0308.launcher.GameSettings;
import com.github.Laevatain0308.launcher.MinecraftLauncher;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GlobalSettingsController
{
    @FXML private Pane titleBar;
    @FXML private Button closeButton;
    @FXML private Button minimizeButton;

    @FXML private Pane back;
    @FXML private Pane home;


    @FXML private Pane gameSettings;
    @FXML private VBox gameSettingsContent;

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


    @FXML private Pane javaManager;
    @FXML private VBox javaManagerContent;

    @FXML private Pane refresh;
    @FXML private Pane addJava;

    @FXML private ListView<JavaInformation> javaList;


    @FXML private Pane downloadSettings;
    @FXML private VBox downloadSettingsContent;

    @FXML private Pane threadPane;
    @FXML private Label threadLabel;
    @FXML private Slider threadSlider;
    @FXML private TextField threadTextField;
    @FXML private Label tipText_thread;
    @FXML private Line tipLine_thread;


    private double xOffset;
    private double yOffset;

    private ExecutorService executor;

    private Pane currentContentPane;
    private Map<Pane , VBox> paneContentDic;


    public void initialize()
    {
        //===== Title Bar =====//
        setTitalBar();


        //===== 页面切换 =====//
        // 准备对应字典
        paneContentDic = new HashMap<>();

        paneContentDic.put(gameSettings , gameSettingsContent);
        paneContentDic.put(javaManager , javaManagerContent);
        paneContentDic.put(downloadSettings , downloadSettingsContent);

        // 设定事件与初始隐藏内容
        for (Map.Entry<Pane , VBox> entry: paneContentDic.entrySet())
        {
            entry.getKey().setOnMouseClicked(event -> {
                if (currentContentPane != entry.getKey())
                    select(entry.getKey());
            });

            entry.getValue().setVisible(false);
        }

        // 初始化页面
        select(gameSettings);


        //===== Game Settings =====//
        // 游戏内存
        setMemorySettings();

        // 游戏窗口分辨率
        setWindowSettings();


        //===== Java Manager =====//
        // 按钮
        refresh.setOnMouseClicked(event -> {
            refresh();
        });
        refresh.setCursor(Cursor.HAND);

        addJava.setOnMouseClicked(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("选择 Java");

            if (MinecraftLauncher.instance.getOSName().equals("windows"))
                chooser.setInitialDirectory(new File("C:/"));
            else
                chooser.setInitialDirectory(new File(System.getProperty("user.home")));

            chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Java" , "java.exe"));

            Stage stage = (Stage) addJava.getScene().getWindow();
            File dir = chooser.showOpenDialog(stage);
            if (dir != null)
            {
                if (!JavaInformation.isValidJava(dir.toPath()))
                {
                    System.err.println("该文件不是有效的 Java");
                    return;
                }

                String version = JavaInformation.getVersion(dir.getPath());
                if (version == null)
                {
                    System.err.println("无法获取该 Java 版本信息");
                    return;
                }

                JavaInformation java = new JavaInformation(version , dir.toPath() , JavaInformation.isJDK(dir.toPath()));
                for (JavaInformation javaInfo : JavaRepository.instance.getJavas())
                {
                    if (java.equalTo(javaInfo))
                    {
                        System.out.println("已存在相同的 Java：" + javaInfo.getVersion() + "  [ " + javaInfo.getPath() + " ]");
                        return;
                    }
                }
                JavaRepository.instance.addJavaInformation(java);
                refresh();
                EventBus.getInstance().publish(new RefreshStartGameButtonEvent());
            }
        });
        addJava.setCursor(Cursor.HAND);

        // Java 列表
        setJavaList();


        //===== Download Settings =====//
        setDownloadThreads();
    }



    private void setTitalBar()
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
        memorySlider.adjustValue(GameSettings.globalSettings.getXmx());
        updateMemorySliderStyle(memorySlider , memorySlider.getValue());
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
            GameSettings.globalSettings.setXmx(newValue.intValue());

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
        widthTextField.setText(String.valueOf(GameSettings.globalSettings.getWidth()));
        widthTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("^[0-9]+$"))
            {
                tipText_width.setVisible(true);
                tipLine_width.setVisible(true);
                return;
            }

            tipText_width.setVisible(false);
            tipLine_width.setVisible(false);
            GameSettings.globalSettings.setWidth(Integer.parseInt(newValue));
        });

        heightTextField.setText(String.valueOf(GameSettings.globalSettings.getHeight()));
        heightTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("^[0-9]+$"))
            {
                tipText_height.setVisible(true);
                tipLine_height.setVisible(true);
                return;
            }

            tipText_height.setVisible(false);
            tipLine_height.setVisible(false);
            GameSettings.globalSettings.setHeight(Integer.parseInt(newValue));
        });

        tipText_width.setVisible(false);
        tipLine_width.setVisible(false);
        tipText_height.setVisible(false);
        tipLine_height.setVisible(false);
    }


    private void setJavaList()
    {
        javaList.setCellFactory(listView -> new JavaListCell());
        javaList.setSelectionModel(new NoSelectionModel<>());
        javaList.setItems(FXCollections.observableArrayList(JavaRepository.instance.getJavas()));
        EventBus.getInstance().subscribe(RefreshJavaListEvent.class , this::refreshJavaList);
    }


    private void setDownloadThreads()
    {
        threadSlider.setMin(GameSettings.MIN_DOWNLOAD_THREADS);
        threadSlider.setMax(GameSettings.MAX_DOWNLOAD_THREADS);
        threadSlider.adjustValue(GameSettings.globalSettings.getDownloadThreads());
        updateThreadSliderStyle(threadSlider , threadPane , threadLabel ,  threadSlider.getValue());
        threadSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
           if (newValue.equals(oldValue))
               return;

           threadTextField.setText(String.valueOf(newValue.intValue()));

           // 设置的影响重启应用后才生效
           GameSettings.globalSettings.setDownloadThreads(newValue.intValue());

           updateThreadSliderStyle(threadSlider , threadPane , threadLabel , newValue.doubleValue());
        });


        tipText_thread.setVisible(false);
        tipLine_thread.setVisible(false);


        threadTextField.setText(String.valueOf(threadSlider.valueProperty().intValue()));
        threadTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.equals(oldValue))
                return;

            if (!newValue.matches("^[0-9]+$"))
            {
                tipText_thread.setVisible(true);
                tipLine_thread.setVisible(true);
                threadSlider.adjustValue(threadSlider.getMin());
                return;
            }

            tipText_thread.setVisible(false);
            tipLine_thread.setVisible(false);

            int value = Integer.parseInt(newValue);
            threadSlider.adjustValue(value);
        });
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

        //String css = String.format("-slider-track-color: linear-gradient(to right ,"
        //                           + " rgba(220, 224, 235, 0.45),"
        //                           + " rgba(123, 153, 246, 0.96) %d%% ,"
        //                           + " rgba(220, 224, 235, 0.45));" , percent);

        float t = percent / 100f / 3;
        float r = lerp(220f , 123f , t);
        float g = lerp(224f , 153f , t);
        float b = lerp(235f , 246f , t);
        float a = lerp(0.45f , 0.96f , t);

        String css = String.format("-slider-track-color: linear-gradient(to right ,"
                                   + " rgba(%f, %f, %f, %f),"
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


    //===== Java Manager =====//
    private void refreshJavaList(RefreshJavaListEvent event)
    {
        switch (event.type())
        {
            case Add -> JavaRepository.instance.addJavaInformation(event.javaInfo());
            case Delete -> JavaRepository.instance.removeJavaInformation(event.javaInfo());
        }
        refresh();
    }

    private void refresh()
    {
        javaList.setItems(FXCollections.observableArrayList(JavaRepository.instance.getJavas()));
    }


    //===== Download Settings =====//
    private void updateThreadSliderStyle(Slider slider , Pane pane , Label label , double currentValue)
    {
        int percent = (int) (currentValue / slider.getMax() * 100);

        float t = percent / 100f / 2;
        float r = lerp(255f , 123f , t);
        float g = lerp(255f , 153f , t);
        float b = lerp(255f , 246f , t);
        float a = lerp(0.81f , 0.96f , t);

        float t1 = percent / 100f;
        float r1 = lerp(255f , 123f , t1);
        float g1 = lerp(255f , 153f , t1);
        float b1 = lerp(255f , 246f , t1);
        float a1 = lerp(0.81f , 0.96f , t1);

        String pane_css = String.format("-thread-pane-color: linear-gradient(to right ,"
                                   + " rgba(%f, %f, %f, %f),"
                                   + " rgba(%f, %f, %f, %f) %d%% ,"
                                   + " rgba(%f, %f, %f, %f));" , r , g , b , a , r1 , g1 , b1 , a1 , percent , r , g , b , a);
        pane.setStyle(pane_css);

        float r2 = lerp(0f , 255f , t1);
        float g2 = lerp(0f , 255f , t1);
        float b2 = lerp(0f , 255f , t1);
        float a2 = lerp(1f , 1f , t1);
        float r3 = lerp(0f , r2 , t1);
        float g3 = lerp(0f , g2 , t1);
        float b3 = lerp(0f , b2 , t1);
        float a3 = lerp(1f , a2 , t1);
        String label_css = String.format("-thread-label-color: rgba(%f, %f, %f, %f);" , r3 , g3 , b3 , a3);
        label.setStyle(label_css);
    }


    //===== 辅助函数 =====//
    private double byteToMB(long bytes) { return bytes / (1024.0 * 1024.0); }
    private double byteToGB(long bytes) { return bytes / (1024.0 * 1024.0 * 1024.0); }
    private double MBToGB(double mb) { return mb / 1024.0; }

    private float lerp(float a , float b , float t) { return a * (1 - t) + b * t; }
}