package com.github.zerowise.netty;

/**
 ** @createtime : 2018/10/12下午1:01
 **/
public interface Service {

    void start(ServiceListener listener);

    void stop(ServiceListener listener);
}
