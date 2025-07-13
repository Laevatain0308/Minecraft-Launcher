package com.github.Laevatain0308.jsonProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;

public class VersionJsonReader
{
    private VersionJson versionJson;
    public VersionJson getVersionJson() { return versionJson; }


    public VersionJsonReader(VersionJson versionJson) { this.versionJson = versionJson; }
    public VersionJsonReader(Path jsonPath) { readJson(jsonPath); }


    /**
     * 读取版本 JSON 配置文件，将其反序列化
     * @param jsonPath 配置文件路径
     */
    public void readJson(Path jsonPath)
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            this.versionJson = mapper.readValue(jsonPath.toFile() , VersionJson.class);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
