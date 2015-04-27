package com.github.diamond.utils;

import java.io.StringWriter;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author kanguangwen
 * @version $Id: JsonUtils.java, v 0.1 2015年2月3日 下午4:15:43 kanguangwen Exp $
 */
public class JsonUtils {
    private static final Logger       LOGGER = LoggerFactory.getLogger(JsonUtils.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String jsonFromObject(Object object) {
        StringWriter writer = new StringWriter();
        try {
            mapper.writeValue(writer, object);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unable to serialize to json: " + object, e);
            return null;
        }
        return writer.toString();
    }

    @SuppressWarnings("deprecation")
    public static <T> T objectFromJson(String json, Class<T> klass) {
        T object;
        try {// 设置输入时忽略JSON字符串中存在而Java对象实际没有的属性
            mapper.getDeserializationConfig().set(
                org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            object = mapper.readValue(json, klass);
        } catch (RuntimeException e) {
            LOGGER.error("Runtime exception during deserializing " + klass.getSimpleName() + " from "
                         + StringUtils.abbreviate(json, 80));
            throw e;
        } catch (Exception e) {
            LOGGER.error("Exception during deserializing " + klass.getSimpleName() + " from "
                         + StringUtils.abbreviate(json, 80));
            return null;
        }
        return object;
    }

    @SuppressWarnings("deprecation")
    public static <T> T objectFromJson(String json, TypeReference<T> klass) {
        T object;
        try {// 设置输入时忽略JSON字符串中存在而Java对象实际没有的属性
            mapper.getDeserializationConfig().set(
                org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            object = mapper.readValue(json, klass);
        } catch (RuntimeException e) {
            LOGGER.error("Runtime exception during deserializing from " + StringUtils.abbreviate(json, 80));
            throw e;
        } catch (Exception e) {
            LOGGER.error("Exception during deserializing from " + StringUtils.abbreviate(json, 80));
            return null;
        }
        return object;
    }
}
