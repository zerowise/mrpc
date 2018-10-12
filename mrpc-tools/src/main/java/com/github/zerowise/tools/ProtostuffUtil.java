package com.github.zerowise.tools;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.concurrent.ConcurrentHashMap;

/**
 ** @createtime : 2018/10/12上午10:53
 **/
public class ProtostuffUtil {
    private static ConcurrentHashMap<Class, Schema> schemaCaches = new ConcurrentHashMap<>();

    private static Schema getSchema(Class clazz) {
        Schema schema = schemaCaches.get(clazz);
        if (schema == null) {
            synchronized (ProtostuffUtil.class) {
                schema = schemaCaches.get(clazz);
                if (schema == null) {
                    schema = RuntimeSchema.createFrom(clazz);
                    schemaCaches.put(clazz, schema);
                }
            }
        }
        return schema;
    }

    public static byte[] toByteArray(Object obj) {
        Class clazz = obj.getClass();
        Schema schema = getSchema(clazz);
        LinkedBuffer linkedBuffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            return ProtobufIOUtil.toByteArray(obj, schema, linkedBuffer);
        } finally {
            linkedBuffer.clear();
        }
    }

    public static <T> T newInstance(Class<T> clazz, byte[] bytes) {
        Schema schema = getSchema(clazz);
        try {
            T t = clazz.newInstance();
            ProtobufIOUtil.mergeFrom(bytes, t, schema);
            return t;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    private ProtostuffUtil() {
    }
}
