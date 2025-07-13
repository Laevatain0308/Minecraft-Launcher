package com.github.Laevatain0308.fileProcessor;

import com.github.Laevatain0308.launcher.MinecraftLauncher;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileManager
{
    public static FileManager instance = new FileManager();


    /**
     * 创建文件目录
     * @param path 路径
     */
    public void createDirectories(Path path)
    {
        try
        {
            Files.createDirectories(path);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    public void browseFile(Path filePath)
    {
        try
        {
            if (MinecraftLauncher.instance.getOSName().equals("windows"))
            {
                Runtime.getRuntime().exec("explorer /select," + filePath.toString());
            }
            else
            {
                Desktop.getDesktop().open(filePath.toFile());
            }
        }
        catch (Exception e)
        {
            System.err.println("无法打开文件 " + filePath);
        }
    }
}
