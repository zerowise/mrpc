package com.github.zerowise.rpc;

import com.github.zerowise.api.RpcService;
import com.github.zerowise.message.RpcReqMessage;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 ** @createtime : 2018/10/12下午3:34
 **/
public class RpcProxy {

    private RpcInvoker rpcInvoker;

    public RpcProxy(RpcInvoker rpcInvoker) {
        this.rpcInvoker = rpcInvoker;
    }

    public <T> T newProxy(Class<T> clazz) {
        if (!clazz.isInterface() || !clazz.isAnnotationPresent(RpcService.class)) {
            System.out.println(clazz.getName() + " is not a interface!!");
            return null;
        }

        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, (Object proxy, Method method, Object[] args) -> {

            //默认方法
            if(method.isDefault()){
                Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class
                        .getDeclaredConstructor(Class.class, int.class);
                constructor.setAccessible(true);

                Class<?> declaringClass = method.getDeclaringClass();
                int allModes = MethodHandles.Lookup.PUBLIC | MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED | MethodHandles.Lookup.PACKAGE;
                return constructor.newInstance(declaringClass, allModes)
                        .unreflectSpecial(method, declaringClass)
                        .bindTo(proxy)
                        .invokeWithArguments(args);
            }


            RpcReqMessage rpcReqMessage = new RpcReqMessage();
            rpcReqMessage.setMsgId(UUID.randomUUID().toString());
            rpcReqMessage.setArguments(args);
            rpcReqMessage.setMethodName(method.getName());
            rpcReqMessage.setParameterTypes(method.getParameterTypes());
            rpcReqMessage.setServiceName(clazz.getName());
            RpcResult rpcResult = rpcInvoker.send(rpcReqMessage);
            return rpcResult.getResult();
        });
    }
}
