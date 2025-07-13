package com.github.Laevatain0308.event.events;

import com.github.Laevatain0308.version.VersionsRepository;

public record RefreshRepositoryListEvent(VersionsRepository repository , RepositoryRefreshType refreshType)
{
    public enum RepositoryRefreshType { Add , Delete }
}