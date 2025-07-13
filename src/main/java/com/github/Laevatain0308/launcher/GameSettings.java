package com.github.Laevatain0308.launcher;

public class GameSettings
{
    public final static GameSettings globalSettings = new GameSettings(854 , 480 , 3840 , 64);

    public static final int MAX_DOWNLOAD_THREADS = 128;         // 最大下载线程数
    public static final int MIN_DOWNLOAD_THREADS = 10;          // 最小下载线程数

    private int width;                       // 分辨率 - 宽
    private int height;                      // 分辨率 - 高
    private int xmx;                         // 内存分配（单位：MB）
    private int downloadThreads;             // 下载线程数

    public GameSettings(int width , int height , int xmx , int downloadThreads)
    {
        this.width = width;
        this.height = height;
        this.xmx = xmx;
        this.downloadThreads = downloadThreads;
    }

    public GameSettings(GameSettings settings)
    {
        this.width = settings.width;
        this.height = settings.height;
        this.xmx = settings.xmx;
        this.downloadThreads = settings.downloadThreads;
    }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public int getXmx() { return xmx; }
    public void setXmx(int xmx) { this.xmx = xmx; }

    public int getDownloadThreads() { return downloadThreads; }
    public void setDownloadThreads(int downloadThreads) { this.downloadThreads = downloadThreads; }
}
