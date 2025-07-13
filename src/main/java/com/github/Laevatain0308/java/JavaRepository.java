package com.github.Laevatain0308.java;

import com.github.Laevatain0308.version.Version;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JavaRepository
{
    public final static JavaRepository instance = new JavaRepository();

    private List<JavaInformation> javas;


    public JavaRepository()
    {
        javas = new ArrayList<>();
        javas.add(new JavaInformation("1.8.0_441" , Paths.get("D:/Java/jdk-1.8/bin/java.exe") , true));
    }


    public List<JavaInformation> getJavas() { return javas; }
    public void setJavas(List<JavaInformation> javas) { this.javas = javas; }

    public void addJavaInformation(JavaInformation java) { javas.add(java); }
    public void removeJavaInformation(JavaInformation java) { javas.remove(java); }


    public JavaInformation getSuitableJava(Version version)
    {
        //for (JavaInformation java : javas)
        //{
        //    // 判断逻辑
        //    if (true)
        //    {
        //
        //        return java;
        //    }
        //}
        //
        //return null;

        return javas != null && !javas.isEmpty() ? javas.get(0) : null;
    }
}
