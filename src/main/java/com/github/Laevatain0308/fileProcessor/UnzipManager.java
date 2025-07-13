package com.github.Laevatain0308.fileProcessor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnzipManager
{
    public static UnzipManager instance = new UnzipManager();


    /**
     * 解压依赖库 JAR 文件，并施以过滤条件
     * @param jarFile JAR 文件
     * @param targetPath 目标路径
     * @param excludes 过滤条件
     */
    public void unzipNatives(File jarFile , Path targetPath , List<String> excludes , List<CompletableFuture<Void>> downloadFutures)
    {
        try (ZipFile zipFile = new ZipFile(jarFile))
        {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();

                // 过滤条件
                if (excludes == null || excludes.stream().noneMatch(entry.getName()::startsWith))
                {
                    // Windows 系统下只接收 dll 格式文件
                    if (entry.getName().contains(".dll") && !entry.getName().endsWith(".dll"))
                        continue;

                    Path unzipPath = targetPath.resolve(entry.getName());

                    // 安全检查：防止路径穿越
                    if (!unzipPath.startsWith(targetPath))
                        throw new IOException("Unzip path doesn't start with " + targetPath);

                    // 若该文件已存在则跳过解压
                    if (unzipPath.toFile().exists())
                        continue;

                    // 解压文件
                    if (entry.isDirectory())
                    {
                        // 若为文件夹则创建目录
                        FileManager.instance.createDirectories(unzipPath);
                    }
                    else
                    {
                        CompletableFuture<Void> future = new CompletableFuture<>();
                        downloadFutures.add(future);

                        // 对文件夹内的文件创建父目录
                        FileManager.instance.createDirectories(unzipPath.getParent());

                        try (InputStream is = zipFile.getInputStream(entry);
                             FileOutputStream fos = new FileOutputStream(unzipPath.toFile()))
                        {
                            is.transferTo(fos);
                            System.out.println("解压: " + entry.getName() + "  至: " + unzipPath);

                            future.complete(null);
                        }
                        catch (IOException e)
                        {
                            future.completeExceptionally(e);
                        }
                    }
                }
            }

            System.out.println("解压依赖库 " + jarFile + " 完成");
        }
        catch (Exception e)
        {
            System.err.println("解压错误： " + jarFile.toPath() + "  检查文件是否损坏  错误：" + e.getMessage());
        }
    }
}
