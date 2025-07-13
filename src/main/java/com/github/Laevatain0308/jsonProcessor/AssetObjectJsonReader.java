package com.github.Laevatain0308.jsonProcessor;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.github.Laevatain0308.download.DownloadJob;
import com.github.Laevatain0308.download.DownloadManager;
import com.github.Laevatain0308.download.URLConstructor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AssetObjectJsonReader
{

    /**
     * 从 资源索引 JSON 文件 中读取资源文件下载地址，并自动补全（性能消耗较大）
     * @param jsonPath 资源索引 JSON 文件 路径
     * @param assetObjectsPath 资源文件父路径
     */
    public static void getAssetObjectsFromJson(Path jsonPath , Path assetObjectsPath , List<CompletableFuture<Void>> downloadFutures)
    {
        JsonFactory factory = new JsonFactory();
        try (JsonParser parser = factory.createParser(jsonPath.toFile()))
        {
            URLConstructor urlConstructor = new URLConstructor(null);

            if (parser.nextToken() != JsonToken.START_OBJECT)
                throw new IllegalStateException("Expected start of object : " + parser.getCurrentToken());

            while (parser.nextToken() != JsonToken.END_OBJECT)
            {
                String name = parser.getCurrentName();

                parser.nextToken();

                if (name.equals("objects"))
                {
                    if (parser.getCurrentToken() != JsonToken.START_OBJECT)
                        throw new IllegalStateException("Expected start of object : " + parser.getCurrentToken());

                    while (parser.nextToken() != JsonToken.END_OBJECT)
                    {
                        if (parser.nextToken() != JsonToken.START_OBJECT)
                            throw new IllegalStateException("Expected start of object : " + parser.getCurrentToken());

                        while (parser.nextToken() != JsonToken.END_OBJECT)
                        {
                            String tokenName = parser.getCurrentName();
                            parser.nextToken();

                            if (tokenName.equals("hash"))
                            {
                                String hash = parser.getText();
                                String relativePath = hash.substring(0 , 2) + "/" + hash;

                                Path path = assetObjectsPath.resolve(relativePath);
                                if (!path.toFile().exists())
                                {
                                    CompletableFuture<Void> future = new CompletableFuture<>();
                                    downloadFutures.add(future);
                                    DownloadManager.getInstance().addDownload(
                                            new DownloadJob(urlConstructor.getAssetURLHead() , relativePath , path , 0 , DownloadJob.Priority.LOW)
                                                    .setOnSuccess(event -> {
                                                        future.complete(null);
                                                    })
                                                    .serOnFailure(event -> {
                                                        future.completeExceptionally(new IOException("无法下载资源文件：" + path));
                                                    }));
                                }
                            }
                        }
                    }
                }
                else
                {
                    parser.skipChildren();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

