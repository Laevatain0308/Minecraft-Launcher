package com.github.Laevatain0308.event.events;

import com.github.Laevatain0308.download.DownloadService;

public record RefreshDownloadProgressEvent(DownloadService service , RefreshDownloadType type)
{
    public enum RefreshDownloadType
    {
        Add , Delete
    }
}
