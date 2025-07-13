package com.github.Laevatain0308.download;

import com.github.Laevatain0308.launcher.MinecraftLauncher;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class DownloadService extends Service<Void>
{

    // 配置参数
    private final String id;                     // UUID
    private final String[] urls;                 // 镜像URL列表
    private final Path filePath;                 // 保存路径
    private final int maxRetries;                // 最大重试次数
    private final Duration timeout;              // 超时时间
    private final long multiThreadThreshold;     // 启用多线程的阈值（默认2MB）
    private final int maxThreads;                // 最大线程数

    // 状态属性
    private final StringProperty currentUrl = new SimpleStringProperty();
    private final DoubleProperty downloadSpeed = new SimpleDoubleProperty();     // KB/s
    private final LongProperty downloadedBytes = new SimpleLongProperty();
    private final LongProperty totalBytes = new SimpleLongProperty();
    private final BooleanProperty multiThreadMode = new SimpleBooleanProperty();

    private Task<Void> task;


    public DownloadService(String id , String[] urls , Path filePath)
    {
        this(id , urls , filePath , 5 , Duration.ofSeconds(30) , 2 * 1024 * 1024 , 4);
    }

    public DownloadService(String id , String[] urls , Path filePath , int maxRetries , Duration timeout , long multiThreadThreshold , int maxThreads)
    {
        this.id = id;
        this.urls = urls;
        this.filePath = filePath;
        this.maxRetries = maxRetries;
        this.timeout = timeout;
        this.multiThreadThreshold = multiThreadThreshold;
        this.maxThreads = maxThreads;
        this.currentUrl.set(urls[0]);
    }

    @Override
    protected Task<Void> createTask()
    {
        task = new DownloadTask();
        return task;
    }

    private class DownloadTask extends Task<Void>
    {

        private ExecutorService executor;
        private volatile boolean paused = false;
        private final Object pauseLock = new Object();

        @Override
        protected Void call() throws Exception
        {
            executor = Executors.newFixedThreadPool(maxThreads);
            try
            {
                for (String url : urls)
                {
                    currentUrl.set(url);
                    if (tryDownload(url))
                        return null;
                }
                throw new IOException("所有镜像源均失败");
            }
            finally
            {
                executor.shutdown();
            }
        }

        private boolean tryDownload(String url) throws InterruptedException
        {
            for (int attempt = 0 ; attempt <= maxRetries ; attempt++)
            {
                if (isCancelled())
                    return false;

                try
                {
                    if (attempt > 0)
                    {
                        Thread.sleep(Duration.ofSeconds(2).toMillis() * attempt);
                    }
                    checkPaused();

                    long fileSize = getFileSize(url);
                    totalBytes.set(fileSize);

                    // 根据文件大小决定下载模式
                    boolean useMultiThread = fileSize > multiThreadThreshold;
                    multiThreadMode.set(useMultiThread);

                    updateMessage(useMultiThread ? "多线程下载中..." : "单线程下载中...");

                    if (useMultiThread)
                    {
                        downloadMultiThreaded(url , fileSize);
                    }
                    else
                    {
                        downloadSingleThreaded(url);
                    }
                    return true;

                }
                catch (Exception e)
                {
                    updateMessage("尝试 " + (attempt + 1) + "/" + (maxRetries + 1) + " 失败: " + e.getMessage());
                }
            }
            return false;
        }

        private void downloadSingleThreaded(String url) throws Exception
        {
            Path tempFile = createTempFile();
            long existingSize = Files.size(tempFile);

            HttpClient client = buildHttpClient();
            HttpRequest request = buildRequest(url , existingSize , -1);

            // 启动异步下载
            HttpResponse<Path> response = client.send(request , HttpResponse.BodyHandlers.ofFile(tempFile , existingSize > 0 ? new OpenOption[] { StandardOpenOption.WRITE , StandardOpenOption.APPEND } : new OpenOption[] { StandardOpenOption.WRITE , StandardOpenOption.TRUNCATE_EXISTING }));

            // 启动进度监控
            monitorDownloadProgress(tempFile);
            finalizeDownload(tempFile);
        }

        private void downloadMultiThreaded(String url , long fileSize) throws Exception
        {
            Path tempFile = createTempFile();

            // 预分配文件空间
            try (FileChannel channel = FileChannel.open(tempFile , StandardOpenOption.WRITE , StandardOpenOption.CREATE))
            {
                channel.truncate(fileSize);
            }

            int chunkCount = calculateChunkCount(fileSize);
            List<Future<?>> futures = new ArrayList<>();

            // 使用原子计数器跟踪已完成的字节数
            AtomicLong completedBytes = new AtomicLong(0);

            // 分块下载
            for (int i = 0 ; i < chunkCount ; i++)
            {
                long start = i * (fileSize / chunkCount);
                long end = (i == chunkCount - 1) ? fileSize - 1 : start + (fileSize / chunkCount) - 1;
                long chunkSize = end - start + 1;

                futures.add(executor.submit(() -> {
                    try
                    {
                        downloadChunk(url , tempFile , start , end);

                        // 更新已完成字节数
                        long completed = completedBytes.addAndGet(chunkSize);
                        downloadedBytes.set(completed);

                        // 更新进度
                        Platform.runLater(() -> {
                            updateProgress(completed , fileSize);
                        });

                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                    return null;
                }));
            }

            // 等待所有分块完成
            for (Future<?> future : futures)
            {
                try
                {
                    future.get(); // 等待完成并检查异常
                }
                catch (Exception e)
                {
                    // 取消其他未完成的任务
                    futures.forEach(f -> f.cancel(true));
                    throw new IOException("多线程下载失败: " + e.getMessage() , e);
                }
            }

            finalizeDownload(tempFile);
        }

        private void downloadChunk(String url , Path tempFile , long start , long end) throws Exception
        {
            HttpClient client = buildHttpClient();
            HttpRequest request = buildRequest(url , start , end);

            HttpResponse<byte[]> response = client.send(request , HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 206)
                throw new IOException("服务器不支持分块下载");

            try (FileChannel channel = FileChannel.open(tempFile , StandardOpenOption.WRITE , StandardOpenOption.CREATE))
            {
                channel.position(start);
                channel.write(ByteBuffer.wrap(response.body()));
            }
        }

        private void monitorDownloadProgress(Path tempFile) throws InterruptedException, IOException
        {
            long startTime = System.currentTimeMillis();
            long lastBytes = 0;
            long lastUpdateTime = startTime;
            long targetSize = totalBytes.get();

            while (!isCancelled())
            {
                checkPaused();

                long currentBytes = Files.size(tempFile);
                downloadedBytes.set(currentBytes);

                // 更新下载速度（每秒）
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastUpdateTime >= 1000)
                {
                    double speed = (currentBytes - lastBytes) / 1024.0; // KB/s
                    downloadSpeed.set(speed);

                    String speedUnit = speed > 1024 ? String.format("%.2f MB/s" , speed / 1024) : String.format("%.2f KB/s" , speed);
                    updateMessage(String.format("%.2f/%.2f MB (%s)" , currentBytes / (1024.0 * 1024.0) , targetSize / (1024.0 * 1024.0) , speedUnit));

                    lastUpdateTime = currentTime;
                    lastBytes = currentBytes;
                }

                // 更新进度
                updateProgress(currentBytes , targetSize);

                // 检查是否完成
                if (currentBytes >= targetSize)
                {
                    break;
                }

                Thread.sleep(200);
            }
        }

        // 辅助方法
        private Path createTempFile() throws IOException
        {
            Path tempFile = filePath.resolveSibling(filePath.getFileName() + ".downloading");
            if (!Files.exists(tempFile))
            {
                Files.createDirectories(tempFile.getParent());
                Files.write(tempFile , new byte[0] , StandardOpenOption.CREATE);
            }
            return tempFile;
        }

        private HttpClient buildHttpClient()
        {
            return HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).connectTimeout(timeout).build();
        }

        private HttpRequest buildRequest(String url , long rangeStart , long rangeEnd)
        {
            HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url)).timeout(timeout).header("User-Agent" , MinecraftLauncher.instance.getLauncherName() + " - " + MinecraftLauncher.instance.getLauncherVersion());

            if (rangeStart >= 0)
            {
                if (rangeEnd >= 0)
                {
                    builder.header("Range" , "bytes=" + rangeStart + "-" + rangeEnd);
                }
                else
                {
                    builder.header("Range" , "bytes=" + rangeStart + "-");
                }
            }
            return builder.build();
        }

        private int calculateChunkCount(long fileSize)
        {
            int chunks = (int) (fileSize / (1 * 1024 * 1024)); // 每块约1MB
            return Math.min(Math.max(chunks , 1) , maxThreads); // 限制在1-maxThreads之间
        }

        private boolean isDone(List<Future<?>> futures)
        {
            for (Future<?> future : futures)
            {
                if (!future.isDone())
                    return false;
            }
            return true;
        }

        private void finalizeDownload(Path tempFile) throws IOException
        {
            if (!isCancelled())
            {
                Files.move(tempFile , filePath , StandardCopyOption.REPLACE_EXISTING);
                System.out.println("下载完成，保存至：" + filePath);
            }
        }

        private long getFileSize(String url) throws Exception
        {
            HttpClient client = buildHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).method("HEAD" , HttpRequest.BodyPublishers.noBody()).build();

            HttpResponse<Void> response = client.send(request , HttpResponse.BodyHandlers.discarding());
            return Long.parseLong(response.headers().firstValue("Content-Length").orElseThrow(() -> new IOException("无法获取文件大小")));
        }

        private void checkPaused() throws InterruptedException
        {
            synchronized (pauseLock)
            {
                while (paused && !isCancelled())
                {
                    updateMessage("下载已暂停");
                    pauseLock.wait();
                }
            }
        }

        @Override
        protected void updateProgress(double workDone , double max)
        {
            if (max > 0)
            {
                double progress = Math.min(workDone / max , 1.0);
                super.updateProgress(workDone , max);

                // 确保进度值在有效范围内
                if (progress >= 0 && progress <= 1.0)
                {
                    Platform.runLater(() -> {
                        // 这里的进度会自动传递给绑定的进度条
                        System.out.println("进度更新: " + String.format("%.2f%%" , progress * 100));
                    });
                }
            }
        }
    }

    // 控制方法
    public void pause()
    {
        if (getState() == State.RUNNING)
        {
            ((DownloadTask) task).paused = true;
        }
    }

    public void resume()
    {
        if (getState() == State.RUNNING)
        {
            synchronized ((((DownloadTask) task).pauseLock))
            {
                (((DownloadTask) task)).paused = false;
                (((DownloadTask) task)).pauseLock.notifyAll();
            }
        }
    }


    public Path getFilePath() { return filePath; }

    public String getId() { return id; }


    // 属性访问器
    public StringProperty currentUrlProperty() { return currentUrl; }

    public DoubleProperty downloadSpeedProperty() { return downloadSpeed; }

    public LongProperty downloadedBytesProperty() { return downloadedBytes; }

    public LongProperty totalBytesProperty() { return totalBytes; }

    public BooleanProperty multiThreadModeProperty() { return multiThreadMode; }
}