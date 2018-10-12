package com.github.zerowise.server;

import net.sf.cglib.reflect.FastMethod;

import java.lang.reflect.InvocationTargetException;

/**
 ** @createtime : 2018/10/12下午5:06
 **/
public class FastInvoker {
    private final Object bean;
    private final FastMethod fastMethod;

    public FastInvoker(Object bean, FastMethod fastMethod) {
        this.bean = bean;
        this.fastMethod = fastMethod;
    }


    public Object invoke(Object... args) throws InvocationTargetException {
        return fastMethod.invoke(bean, args);
    }
}
