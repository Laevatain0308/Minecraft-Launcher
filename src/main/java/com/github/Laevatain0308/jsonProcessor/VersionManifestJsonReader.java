package com.github.Laevatain0308.jsonProcessor;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;

public class VersionManifestJsonReader
{
    private VersionManifestJson versionManifestJson;
    public VersionManifestJson getVersionManifestJson() { return versionManifestJson; }


    public VersionManifestJsonReader(VersionManifestJson versionManifestJson) { this.versionManifestJson = versionManifestJson; }
    public VersionManifestJsonReader(Path jsonPath) { readJson(jsonPath); }


    /**
     * 读取版本列表 JSON 文件
     * @param jsonPath JSON 文件路径
     */
    public void readJson(Path jsonPath)
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            this.versionManifestJson = mapper.readValue(jsonPath.toFile() , VersionManifestJson.class);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
