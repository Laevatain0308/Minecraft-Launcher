package com.github.Laevatain0308.download;

import com.github.Laevatain0308.event.EventBus;
import com.github.Laevatain0308.event.events.RefreshDownloadProgressEvent;
import com.github.Laevatain0308.launcher.GameSettings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadManager
{

    public static DownloadManager instance;
    public static DownloadManager getInstance()
    {
        if (instance == null)
            instance = new DownloadManager(GameSettings.globalSettings.getDownloadThreads());

        return instance;
    }

    public static int count = 0;

    // 按优先级划分的任务队列和线程池
    private final PriorityBlockingQueue<DownloadJob>[] priorityQueues;
    private final ExecutorService[] priorityExecutors;
    private final int[] maxThreadsPerPriority;

    private final Map<String , DownloadService> runningJobs = new ConcurrentHashMap<>();
    private final ObservableList<DownloadJob> completedJobs = FXCollections.observableArrayList();

    // 状态统计属性
    private final IntegerProperty activeCount = new SimpleIntegerProperty(0);
    private final LongProperty totalBytes = new SimpleLongProperty(0);
    private final LongProperty downloadedBytes = new SimpleLongProperty(0);
    private final IntegerProperty[] priorityCounts = new IntegerProperty[3];

    private volatile boolean isShuttingDown = false;
    private final int totalConcurrency;

    public DownloadManager(int totalConcurrency)
    {
        this.totalConcurrency = totalConcurrency;

        // 初始化优先级队列和线程池
        priorityQueues = new PriorityBlockingQueue[3];
        priorityExecutors = new ExecutorService[3];
        maxThreadsPerPriority = new int[3];

        // 分配线程资源 (HIGH:10%, MEDIUM:30%, LOW:40%)
        maxThreadsPerPriority[0] = (int) (totalConcurrency * 0.1); // HIGH
        maxThreadsPerPriority[1] = (int) (totalConcurrency * 0.3); // MEDIUM
        maxThreadsPerPriority[2] = totalConcurrency - maxThreadsPerPriority[0] - maxThreadsPerPriority[1]; // LOW

        for (int i = 0 ; i < 3 ; i++)
        {
            priorityQueues[i] = new PriorityBlockingQueue<>();
            priorityExecutors[i] = new ThreadPoolExecutor(maxThreadsPerPriority[i] , maxThreadsPerPriority[i] , 60L , TimeUnit.SECONDS , new LinkedBlockingQueue<>() , new PriorityThreadFactory(i));

            priorityCounts[i] = new SimpleIntegerProperty(0);
        }

        startQueueProcessors();
    }

    private void startQueueProcessors()
    {
        // 为每个优先级启动独立的处理器线程
        for (int i = 0 ; i < 3 ; i++)
        {
            final int priorityLevel = i;
            Thread processor = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted())
                {
                    try
                    {
                        DownloadJob job = priorityQueues[priorityLevel].take();

                        // 检查是否正在关闭
                        if (isShuttingDown)
                        {
                            priorityQueues[priorityLevel].put(job);
                            break;
                        }

                        // 获取执行许可
                        if (((ThreadPoolExecutor) priorityExecutors[priorityLevel]).getActiveCount() < maxThreadsPerPriority[priorityLevel])
                        {

                            priorityExecutors[priorityLevel].execute(() -> startDownloadJob(job));
                        }
                        else
                        {
                            // 放回队列稍后处理
                            priorityQueues[priorityLevel].put(job);
                            Thread.sleep(100); // 避免忙等待
                        }
                    }
                    catch (InterruptedException e)
                    {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            processor.setDaemon(true);
            processor.setName("Priority-" + priorityLevel + "-Processor");
            processor.start();
        }
    }

    private void startDownloadJob(DownloadJob job)
    {
        DownloadService service = createDownloadService(job);
        runningJobs.put(job.getId() , service);

        // 更新状态
        activeCount.set(runningJobs.size());
        priorityCounts[job.getPriority().ordinal()].set(priorityCounts[job.getPriority().ordinal()].get() - 1);

        // 启动下载
        service.start();
    }

    private DownloadService createDownloadService(DownloadJob job)
    {
        DownloadService service = new DownloadService(
                job.getId() ,
                job.getMirrorUrls() ,
                job.getSavePath() ,
                job.getMaxRetries() ,
                job.getTimeout() ,
                job.getMultiThreadThreshold() ,
                job.getMaxThreadsPerFile());

        service.setOnSucceeded(e -> {
            completeJob(job , true);
        });
        service.setOnFailed(e -> {
            completeJob(job , false);
        });

        service.downloadedBytesProperty().addListener((obs , oldVal , newVal) -> {
            long delta = newVal.longValue() - oldVal.longValue();
            downloadedBytes.set(downloadedBytes.get() + delta);
        });

        EventBus.getInstance().publish(new RefreshDownloadProgressEvent(service , RefreshDownloadProgressEvent.RefreshDownloadType.Add));

        return service;
    }

    public void addDownload(DownloadJob job)
    {
        int priorityIndex = job.getPriority().ordinal();
        priorityQueues[priorityIndex].put(job);

        // 更新统计
        totalBytes.set(totalBytes.get() + job.getEstimatedSize());
        priorityCounts[priorityIndex].set(priorityCounts[priorityIndex].get() + 1);
    }

    private void completeJob(DownloadJob job , boolean success)
    {
        DownloadService service = runningJobs.remove(job.getId());
        job.setCompleted(true);
        job.setSuccess(success);

        EventBus.getInstance().publish(new RefreshDownloadProgressEvent(service , RefreshDownloadProgressEvent.RefreshDownloadType.Delete));

        // 更新状态
        activeCount.set(runningJobs.size());
        completedJobs.add(job);

        // 关闭检查
        if (isShuttingDown && runningJobs.isEmpty())
        {
            shutdownNow();
        }
    }

    public void shutdown()
    {
        isShuttingDown = true;

        // 1. 停止接受新任务
        for (PriorityBlockingQueue<DownloadJob> queue : priorityQueues)
        {
            queue.clear();
        }

        // 2. 停止运行中的服务
        runningJobs.values().forEach(DownloadService::cancel);

        // 3. 关闭线程池
        if (runningJobs.isEmpty())
        {
            shutdownNow();
        }

        isShuttingDown = false;
    }

    private void shutdownNow()
    {
        for (ExecutorService executor : priorityExecutors)
        {
            executor.shutdownNow();
        }
        cleanTempFiles();
    }

    private void cleanTempFiles()
    {
        runningJobs.values().forEach(service -> {
            Path tempFile = service.getFilePath().resolveSibling(service.getFilePath().getFileName() + ".downloading");
            try
            {
                Files.deleteIfExists(tempFile);
            }
            catch (IOException ignored)
            {
            }
        });
    }

    // 优先级线程工厂
    private static class PriorityThreadFactory implements ThreadFactory
    {

        private final int priorityLevel;
        private final AtomicInteger counter = new AtomicInteger(0);

        public PriorityThreadFactory(int priorityLevel)
        {
            this.priorityLevel = priorityLevel;
        }

        @Override
        public Thread newThread(Runnable r)
        {
            Thread t = new Thread(r);
            t.setName("DownloadWorker-P" + priorityLevel + "-" + counter.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }

    // 状态访问方法
    public ObservableList<DownloadJob> getCompletedJobs() { return completedJobs; }

    public ReadOnlyIntegerProperty activeCountProperty() { return activeCount; }

    public ReadOnlyLongProperty totalBytesProperty() { return totalBytes; }

    public ReadOnlyLongProperty downloadedBytesProperty() { return downloadedBytes; }

    public ReadOnlyIntegerProperty getPriorityCountProperty(DownloadJob.Priority priority)
    {
        return priorityCounts[priority.ordinal()];
    }

    // 优先级提升方法
    public boolean promoteJob(String jobId)
    {
        for (int i = 0 ; i < priorityQueues.length - 1 ; i++)
        {
            for (DownloadJob job : priorityQueues[i])
            {
                if (job.getId().equals(jobId))
                {
                    // 已经在更高优先级队列中
                    return false;
                }
            }
        }

        // 检查最低优先级队列
        for (DownloadJob job : priorityQueues[priorityQueues.length - 1])
        {
            if (job.getId().equals(jobId))
            {
                if (priorityQueues[priorityQueues.length - 1].remove(job))
                {
                    DownloadJob promotedJob = new DownloadJob(job.getMirrorUrls() , job.getSavePath() , job.getEstimatedSize() , DownloadJob.Priority.values()[job.getPriority().ordinal() - 1]);
                    addDownload(promotedJob);
                    return true;
                }
            }
        }
        return false;
    }
}