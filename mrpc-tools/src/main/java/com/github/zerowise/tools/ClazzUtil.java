package com.github.zerowise.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 ** @createtime : 2018/10/12上午10:52
 **/
public class ClazzUtil {

    private static final Logger log = LoggerFactory.getLogger(ClazzUtil.class);

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
                        log.error("{}", name, e);
                    }
                }
            }
        }

        return clazz;
    }


    private ClazzUtil() {
    }
}
