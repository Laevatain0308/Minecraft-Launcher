package com.github.Laevatain0308.event;

import javafx.application.Platform;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class EventBus
{
    private static EventBus instance;
    private final Map<Class<?> , Consumer<Object>> subscribers = new HashMap<>();
    private final Map<Class<?> , Object> datas = new HashMap<>();

    public static EventBus getInstance()
    {
        if (instance == null)
        {
            instance = new EventBus();
        }
        return instance;
    }

    //===== 事件通道 =====//
    public <T> void subscribe(Class<T> eventType, Consumer<T> listener)     // 发布
    {
        subscribers.put(eventType, (Consumer<Object>) listener);
    }

    public <T> void publish(T event)    // 接收
    {
        Consumer<Object> listener = subscribers.get(event.getClass());
        if (listener != null)
        {
            Platform.runLater(() -> listener.accept(event));
        }
    }


    //===== 数据传递通道 =====//
    public <T> void input(Object data) { datas.put(data.getClass() , data); }

    public <T> T output(Class<T> dataType) { return (T) datas.remove(dataType); }

    public <T> T getInput(Class<T> dataType) { return (T) datas.get(dataType); }
}