package com.github.Laevatain0308.account;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Account
{
    private String name;
    private String uuid;
    private final LoadType loadType;

    private final boolean specialUUID;


    public Account(String name , LoadType loadType)
    {
        this.name = name;
        this.uuid = getUUIDByName(name);
        this.loadType = loadType;
        this.specialUUID = false;
    }

    public Account(String name , String uuid , LoadType loadType)
    {
        this.name = name;
        this.uuid = uuid;
        this.loadType = loadType;
        this.specialUUID = true;
    }


    public String getName() { return name; }
    public String getUUID() { return uuid; }
    public LoadType getLoadType() { return loadType; }
    public String getLoadTypeAsString() { return getLoadTypeAsString(loadType); }

    public void rename(String newName)
    {
        name = newName;

        if (!specialUUID)
            uuid = getUUIDByName(name);
    }


    public static String getUUIDByName(String userName)
    {
        if (userName == null || userName.isEmpty())
            return "";

        return String.join("" , UUID.nameUUIDFromBytes(("OfflinePlayer:" + userName).getBytes(StandardCharsets.UTF_8)).toString().split("-"));
    }

    public static String getLoadTypeAsString(LoadType loadType)
    {
        return switch (loadType)
        {
            case Offline -> "离线模式";
            case Online-> "微软登录";
        };
    }
}
