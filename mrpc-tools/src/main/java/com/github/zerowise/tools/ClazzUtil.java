package com.github.zerowise.tools;

import java.util.concurrent.ConcurrentHashMap;

/**
 ** @createtime : 2018/10/12上午10:52
 **/
public class ClazzUtil {

    private static ConcurrentHashMap<String, Class> clazzCaches = new ConcurrentHashMap<>();

    public static Class findClazz(String name) {
        Class clazz = clazzCaches.get(name);
        if (clazz == null) {
            synchronized (ClazzUtil.class) {
                clazz = clazzCaches.get(name);
                if (clazz == null) {
                    try {
                        clazz = Class.forName(name);
                        clazzCaches.put(name, clazz);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return clazz;
    }


    private ClazzUtil() {
    }
}
