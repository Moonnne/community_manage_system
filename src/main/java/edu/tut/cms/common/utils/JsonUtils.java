package edu.tut.cms.common.utils;

import com.alibaba.fastjson2.JSON;

import java.util.List;

public class JsonUtils {

    private JsonUtils() {
    }

    /**
     * 将对象序列化为JSON字符串
     */
    public static String toJson(Object obj) {
        return JSON.toJSONString(obj);
    }

    /**
     * 将JSON字符串反序列化为指定类型对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }

    /**
     * 将JSON字符串反序列化为指定类型的列表
     */
    public static <T> List<T> fromJsonList(String json, Class<T> clazz) {
        return JSON.parseArray(json, clazz);
    }
}
