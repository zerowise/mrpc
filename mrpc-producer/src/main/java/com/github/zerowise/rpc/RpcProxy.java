package com.github.zerowise.rpc;

import com.github.zerowise.message.RpcReqMessage;

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
        if (!clazz.isInterface()) {
            System.out.println(clazz.getName() + " is not a interface!!");
            return null;
        }

        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, (Object proxy, Method method, Object[] args) -> {
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
