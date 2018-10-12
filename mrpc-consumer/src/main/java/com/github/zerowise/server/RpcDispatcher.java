package com.github.zerowise.server;

import com.github.zerowise.api.RpcService;
import com.github.zerowise.message.RpcReqMessage;
import com.github.zerowise.message.RpcRespMessage;
import io.netty.channel.ChannelHandlerContext;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 ** @createtime : 2018/10/12下午4:33
 **/
public abstract class RpcDispatcher {
    private ExecutorService executor;

    public RpcDispatcher() {
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    public void register(Object bean) {
        Class c1 = bean.getClass();
        if (!c1.isAnnotationPresent(RpcService.class)) {
            return;
        }

        while (!c1.equals(Object.class)) {
            Stream.of(c1.getInterfaces()).filter(cls -> cls.isAnnotationPresent(RpcService.class)).forEach(cls -> {
                Stream.of(cls.getMethods()).filter(method -> !method.isDefault())
                        .forEach(method -> registerBean(bean, cls, method));
            });
            c1 = c1.getSuperclass();
        }
    }

    public void execute(ChannelHandlerContext ctx, RpcReqMessage msg) {
        executor.execute(() -> {
            RpcRespMessage rpcRespMessage = new RpcRespMessage();
            rpcRespMessage.setMsgId(msg.getMsgId());
            FastInvoker fastInvoker = fastInvoker(msg.getServiceName(), msg.getMethodName(), msg.getParameterTypes());
            if (fastInvoker == null) {
                rpcRespMessage.setError(new RuntimeException("not exist the " + msg.getServiceName() + "." + msg.getMethodName()));
            } else {
                try {
                    rpcRespMessage.setResult(fastInvoker.invoke(msg.getArguments()));
                } catch (Exception e) {
                    rpcRespMessage.setError(e);
                }
            }
            ctx.writeAndFlush(rpcRespMessage);
        });
    }

    public void shutdown() {
        executor.shutdownNow();
    }


    protected abstract FastInvoker fastInvoker(String serviceName, String methodName, Class[] parameterTypes);


    protected abstract void registerBean(Object bean, Class clazzInter, Method method);


    private static class DefaultRpcDispatcher extends RpcDispatcher {
        private Map<String, FastInvoker> invokerCaches;

        public DefaultRpcDispatcher() {
            super();
            invokerCaches = new HashMap<>();
        }

        @Override
        protected FastInvoker fastInvoker(String serviceName, String methodName, Class[] parameterTypes) {
            return invokerCaches.get(getMethodKey(serviceName, methodName, parameterTypes));
        }

        @Override
        protected void registerBean(Object bean, Class clazzInter, Method method) {
            FastClass fastClass = FastClass.create(bean.getClass());
            invokerCaches.put(getMethodKey(clazzInter.getName(), method.getName(), method.getParameterTypes()), new FastInvoker(bean, fastClass.getMethod(method)));
        }

        protected String getMethodKey(String serviceName, String methodName, Class[] parameterTypes) {
            return serviceName + "_" + methodName + "_" + parameterTypes.length;
        }
    }


    private static class LazyRpcDispatcher extends RpcDispatcher {

        private Map<String, Object> beanCaches;

        public LazyRpcDispatcher() {
            super();
            this.beanCaches = new HashMap<>();
        }

        @Override
        protected FastInvoker fastInvoker(String serviceName, String methodName, Class[] parameterTypes) {
            Object bean = beanCaches.get(serviceName);
            if (bean == null) {
                return null;
            }
            FastClass fastClass = FastClass.create(bean.getClass());
            FastMethod method = fastClass.getMethod(methodName, parameterTypes);
            if (method == null) {
                return null;
            }
            return new FastInvoker(bean, method);
        }

        @Override
        protected void registerBean(Object bean, Class clazzInter, Method method) {
            beanCaches.putIfAbsent(clazzInter.getName(), bean);
        }
    }

    private static class TypeRpcDispatcher extends DefaultRpcDispatcher {
        @Override
        protected String getMethodKey(String serviceName, String methodName, Class[] parameterTypes) {
            StringBuilder builder = new StringBuilder();
            builder.append(serviceName).append("_").append(methodName).append("_");
            IntStream.of(parameterTypes.length).forEach(i -> {
                builder.append(i).append("@").append(parameterTypes[i].getName()).append("_");
            });
            return builder.toString();
        }
    }


    public static RpcDispatcher def() {
        return new DefaultRpcDispatcher();
    }

    public static RpcDispatcher lazy() {
        return new LazyRpcDispatcher();
    }


    public static RpcDispatcher type() {
        return new TypeRpcDispatcher();
    }
}
