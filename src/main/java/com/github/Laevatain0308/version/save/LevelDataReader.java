package com.github.Laevatain0308.version.save;

import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.io.NBTInputStream;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.DoubleTag;
import net.querz.nbt.tag.ListTag;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class LevelDataReader
{
    private final SaveData saveData;

    private final CompoundTag data;

    public LevelDataReader(Path levelDataPath , SaveData saveData)
    {
        this.saveData = saveData;

        CompoundTag temp = null;
        try (NBTInputStream in = new NBTInputStream(new FileInputStream(levelDataPath.toFile())))
        {
            NBTDeserializer deserializer = new NBTDeserializer(true);
            NamedTag namedTag = deserializer.fromStream(in);

            if (namedTag.getTag() == null)
                throw new IOException("存档配置文件为空");

            temp = (CompoundTag) namedTag.getTag();
        }
        catch (IOException e)
        {
            System.err.println("无法读取存档配置文件：" + e.getMessage());
        }

        data = temp != null ? temp.getCompoundTag("Data") : null;
    }


    // 版本名称（而非存档文件夹名称）
    public String getLevelName()
    {
        if (data == null)
        {
            System.err.println("无法获取存档数据");
            return "";
        }

        if (!data.containsKey("LevelName"))
            return "";

        return data.getString("LevelName");
    }

    // 存储此存档时的游戏版本
    public String getGameVersion()
    {
        if (data == null)
        {
            System.err.println("无法获取存档数据");
            return "";
        }

        if (!data.containsKey("Version"))
            return "";

        // 若不存在则使用当前正在加载此存档的游戏的版本名称
        CompoundTag versionTag = data.getCompoundTag("Version");
        if (!versionTag.containsKey("Name"))
            return saveData.getRepository().getVersion().getVersionID();

        return versionTag.getString("Name");
    }

    // 世界种子
    public String getSeed()
    {
        if (data == null)
        {
            System.err.println("无法获取存档数据");
            return "";
        }

        if (!data.containsKey("Version"))
            return "";

        // 20w20a 版本前具有不同的标签结构
        CompoundTag versionTag = data.getCompoundTag("Version");
        if (versionTag.getInt("Id") < 2537 && data.containsKey("RandomSeed"))
            return String.valueOf(data.getLong("RandomSeed"));

        if (!data.containsKey("WorldGenSettings"))
            return "";

        return String.valueOf(data.getCompoundTag("WorldGenSettings").getLong("seed"));
    }

    // 上次保存此存档的时间戳
    public String getLastPlayed()
    {
        if (data == null)
        {
            System.err.println("无法获取存档数据");
            return "";
        }

        if (!data.containsKey("LastPlayed"))
            return "";

        Instant instant = Instant.ofEpochSecond(data.getLong("LastPlayed") / 1000L);
        LocalDateTime beijingTime = LocalDateTime.ofInstant(instant , ZoneId.of("Asia/Shanghai"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy 年 MM 月 dd 日 HH:mm:ss");
        return beijingTime.format(formatter);
    }

    // 游戏内时间
    public String getTime()
    {
        if (data == null)
        {
            System.err.println("无法获取存档数据");
            return "";
        }

        if (!data.containsKey("Time"))
            return "";

        long time = data.getLong("Time");
        int days = (int) (time / 24000);

        return days + " 天";
    }

    // 是否允许作弊
    public boolean allowCommands()
    {
        if (data == null)
            return false;

        // 1.3.1 版本前无"允许作弊"标签（先判断数据版本），如果此项不存在，则游戏判断整型 GameType 是否为1（创造模式）。如果是则为true，否则为false。
        if (!data.containsKey("allowCommands"))
            return data.getInt("GameType") == 1;

        return data.getBoolean("allowCommands");
    }

    // 是否生成建筑
    public boolean canGenerateFeatures()
    {
        if (data == null || !data.containsKey("Version"))
            return true;

        // 20w20a 版本前具有不同的标签结构
        CompoundTag versionTag = data.getCompoundTag("Version");
        if (versionTag.getInt("Id") < 2537 && data.containsKey("MapFeatures"))
            return data.getBoolean("MapFeatures");

        if (!data.containsKey("WorldGenSettings"))
            return true;

        return data.getCompoundTag("WorldGenSettings").getBoolean("generate_features");
    }

    // 游戏难度
    public String getDifficulty()
    {
        if (data == null)
        {
            System.err.println("无法获取存档数据");
            return "";
        }

        // 14w02a 版本前无难度标签
        if (!data.containsKey("Difficulty"))
            return "普通";

        byte difficulty = data.getByte("Difficulty");

        return switch (difficulty)
        {
            case 0 -> "和平";
            case 1 -> "简单";
            case 2 -> "普通";
            case 3 -> "困难";
            default -> "";
        };
    }

    // 玩家位置（单人模式）
    public String getPlayerPosition()
    {
        if (data == null)
        {
            System.err.println("无法获取存档数据");
            return "";
        }

        if (!data.containsKey("Player"))
            return "";

        CompoundTag playerTag = data.getCompoundTag("Player");
        if (!playerTag.containsKey("Pos"))
            return "";

        try
        {
            ListTag<DoubleTag> pos = (ListTag<DoubleTag>) playerTag.getListTag("Pos");
            return toStringFromDoubleTagList(pos);
        }
        catch (ClassCastException e)
        {
            System.err.println("玩家位置坐标数据类型转换失败：" + e.getMessage());
            return "";
        }
    }

    // 上次死亡位置
    public String getLastDeathLocation()
    {
        if (data == null)
        {
            System.err.println("无法获取存档数据");
            return "";
        }

        if (!data.containsKey("Player") || !data.getCompoundTag("Player").containsKey("LastDeathLocation"))
            return "";

        int[] pos = data.getCompoundTag("Player").getCompoundTag("LastDeathLocation").getIntArray("pos");
        return toStringFromIntArrayPos(pos);
    }

    // 床/重生锚位置
    public String getRespawnPosition()
    {
        if (data == null)
        {
            System.err.println("无法获取存档数据");
            return "";
        }

        if (!data.containsKey("Player"))
            return "";

        CompoundTag playerTag = data.getCompoundTag("Player");
        if (!playerTag.containsKey("respawn"))
            return "";

        int[] pos = playerTag.getIntArray("pos");
        return toStringFromIntArrayPos(pos);
    }

    // 游戏模式（单人模式下玩家目前的游戏模式，而非默认游戏模式）
    public String getGameType()
    {
        if (data == null)
        {
            System.err.println("无法获取存档数据");
            return "";
        }

        if (!data.containsKey("Player"))
            return "";

        CompoundTag playerTag = data.getCompoundTag("Player");
        if (!playerTag.containsKey("playerGameType"))
        {
            if (!data.containsKey("GameType"))
                return "生存";
            else
                return toStringFromIntGameType(data.getInt("GameType"));
        }

        int gameType = playerTag.getInt("playerGameType");
        return toStringFromIntGameType(gameType);
    }

    // 生命值
    public String getHealth()
    {
        if (data == null)
        {
            System.err.println("无法获取存档数据");
            return "";
        }

        if (!data.containsKey("Player"))
            return "";

        CompoundTag playerTag = data.getCompoundTag("Player");
        if (!playerTag.containsKey("Health"))
            return "20";

        return String.valueOf(playerTag.getFloat("Health"));
    }

    // 饥饿值
    public String getFoodLevel()
    {
        if (data == null)
        {
            System.err.println("无法获取存档数据");
            return "";
        }

        if (!data.containsKey("Player"))
            return "";

        CompoundTag playerTag = data.getCompoundTag("Player");
        if (!playerTag.containsKey("foodLevel"))
            return "20";

        return String.valueOf(playerTag.getInt("foodLevel"));
    }

    // 经验等级
    public String getXpLevel()
    {
        if (data == null)
        {
            System.err.println("无法获取存档数据");
            return "";
        }

        if (!data.containsKey("Player"))
            return "";

        CompoundTag playerTag = data.getCompoundTag("Player");
        if (!playerTag.containsKey("XpLevel"))
            return "0";

        return String.valueOf(playerTag.getInt("xpLevel"));
    }



    // 辅助函数
    private String toStringFromIntArrayPos(int[] pos)
    {
        if (pos == null || pos.length == 0)
            return "";

        StringBuilder posStr = new StringBuilder("( ");
        for (int i = 0 ; i < pos.length ; i++)
        {
            if (i != pos.length - 1)
                posStr.append(pos[i]).append(" , ");
            else
                posStr.append(pos[i]);
        }
        posStr.append(" )");
        return posStr.toString();
    }

    private String toStringFromDoubleTagList(ListTag<DoubleTag> pos)
    {
        if (pos == null || pos.size() == 0)
            return "";

        StringBuilder posStr = new StringBuilder("( ");
        for (int i = 0 ; i < pos.size() ; i++)
        {
            if (i != pos.size() - 1)
                posStr.append(String.format("%.2f" , pos.get(i).asFloat())).append(" , ");
            else
                posStr.append(String.format("%.2f" , pos.get(i).asFloat()));
        }
        posStr.append(" )");
        return posStr.toString();
    }


    private String toStringFromIntGameType(int gameType)
    {
        return switch (gameType)
        {
            case 0 -> "生存";
            case 1 -> "创造";
            case 2 -> "冒险";
            case 3 -> "旁观者";
            default -> "";
        };
    }
}
