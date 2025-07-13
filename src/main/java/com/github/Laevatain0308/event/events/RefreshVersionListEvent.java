package com.github.Laevatain0308.event.events;

import com.github.Laevatain0308.version.Version;

import java.util.List;

public record RefreshVersionListEvent(List<Version> versions) {}