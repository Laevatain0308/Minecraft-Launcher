package com.github.Laevatain0308.event.events;

import com.github.Laevatain0308.java.JavaInformation;

public record RefreshJavaListEvent(JavaInformation javaInfo , RefreashJavaInfoType type)
{
    public enum RefreashJavaInfoType
    {
        Add , Delete
    }
}
