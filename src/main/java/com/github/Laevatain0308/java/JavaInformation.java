package com.github.Laevatain0308.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;

public class JavaInformation
{
    private final String version;
    private final Path path;
    private final boolean isJDK;

    public JavaInformation(String version , Path path , boolean isJDK)
    {
        this.version = version;
        this.path = path;
        this.isJDK = isJDK;
    }

    public String getVersion() { return version; }
    public Path getPath() { return path; }
    public boolean isJDK() { return isJDK; }

    public boolean equalTo(JavaInformation other)
    {
        return path.equals(other.getPath());
    }


    public static boolean isValidJava(Path path)
    {
        try
        {
            Process process = new ProcessBuilder(path.toString() , "-version").start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        }
        catch (IOException | InterruptedException e)
        {
            System.err.println("无法获取该路径下的 Java 版本信息...");
            return false;
        }
    }


    public static boolean isJDK(Path javaExePath)
    {
        File parent = javaExePath.getParent().toFile();

        for (File file : parent.listFiles())
        {
            if (file.getName().endsWith("javac.exe"))
                return true;
        }

        return false;
    }


    public static String getVersion(String exePath)
    {
        String version = null;
        try
        {
            Process process = new ProcessBuilder(exePath , "-version").start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream())))
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    if (line.contains("java version"))
                    {
                        return line.split("\"")[1];
                    }
                }
            }
            catch (IOException e)
            {
                System.err.println("读取进程输出时出错: " + e.getMessage());
                return null;
            }

            return null;
        }
        catch (IOException e)
        {
            System.err.println("无法获取该 Java 版本信息");
            return null;
        }
    }
}