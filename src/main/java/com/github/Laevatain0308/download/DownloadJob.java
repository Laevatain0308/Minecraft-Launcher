package com.github.Laevatain0308.download;

import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;
import java.util.function.Consumer;

public class DownloadJob implements Comparable<DownloadJob>
{
    public enum Priority
    {
        HIGH(3),      // 核心文件（如版本JSON）
        MEDIUM(2),    // 重要资源（如资源索引）
        LOW(1);       // 普通文件（如库文件）

        private final int value;

        Priority(int value) { this.value = value; }

        public int getValue() { return value; }
    }

    private final String id;
    private final String[] mirrorUrls;
    private final Path savePath;
    private final long estimatedSize;
    private final Priority priority;
    private final int maxRetries;
    private final Duration timeout;
    private final long multiThreadThreshold;
    private final int maxThreadsPerFile;

    private boolean completed;
    private boolean success;

    private Consumer<DownloadJob> onSuccess;
    private Consumer<DownloadJob> onFailure;

    // 构造器
    public DownloadJob(String[] mirrorUrls , Path savePath , long estimatedSize , Priority priority)
    {
        this(UUID.randomUUID().toString() , mirrorUrls , savePath , estimatedSize , priority , 3 , Duration.ofSeconds(30) , 2 * 1024 * 1024 , 4);
    }

    public DownloadJob(String[] mirrorUrlHeads , String urlEnd , Path savePath , long estimatedSize , Priority priority)
    {
        this(UUID.randomUUID().toString() , urlProcessor(mirrorUrlHeads , urlEnd) , savePath , estimatedSize , priority , 3 , Duration.ofSeconds(30) , 2 * 1024 * 1024 , 4);
    }

    // 完整构造器
    public DownloadJob(String id , String[] mirrorUrls , Path savePath , long estimatedSize , Priority priority , int maxRetries , Duration timeout , long multiThreadThreshold , int maxThreadsPerFile)
    {
        this.id = id;
        this.mirrorUrls = mirrorUrls;
        this.savePath = savePath;
        this.estimatedSize = estimatedSize;
        this.priority = priority;
        this.maxRetries = maxRetries;
        this.timeout = timeout;
        this.multiThreadThreshold = multiThreadThreshold;
        this.maxThreadsPerFile = maxThreadsPerFile;
    }


    public DownloadJob setOnSuccess(Consumer<DownloadJob> handler)
    {
        this.onSuccess = handler;
        return this; // 支持链式调用
    }

    public DownloadJob serOnFailure(Consumer<DownloadJob> handler)
    {
        this.onFailure = handler;
        return this;
    }


    // getters
    public String getId() { return id; }

    public String[] getMirrorUrls() { return mirrorUrls; }

    public Path getSavePath() { return savePath; }

    public long getEstimatedSize() { return estimatedSize; }

    public Priority getPriority() { return priority; }

    public int getMaxRetries() { return maxRetries; }

    public Duration getTimeout() { return timeout; }

    public long getMultiThreadThreshold() { return multiThreadThreshold; }

    public int getMaxThreadsPerFile() { return maxThreadsPerFile; }



    // 状态管理
    public boolean isCompleted() { return completed; }

    public boolean isSuccess() { return success; }

    void setCompleted(boolean completed) { this.completed = completed; }

    void setSuccess(boolean success)
    {
        this.success = success;

        if (success)
            onSuccess.accept(this);
        else
            onFailure.accept(this);
    }


    @Override
    public int compareTo(DownloadJob other)
    {
        return Integer.compare(other.priority.getValue() , this.priority.getValue()); // 降序排列
    }


    private static String[] urlProcessor(String[] urlHeads , String urlEnd)
    {
        String[] urls = new String[urlHeads.length];
        for (int i = 0; i < urlHeads.length; i++)
        {
            urls[i] = urlHeads[i] + urlEnd;
        }
        return urls;
    }
}