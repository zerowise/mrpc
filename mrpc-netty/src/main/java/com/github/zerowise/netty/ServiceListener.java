package com.github.zerowise.netty;

/**
 ** @createtime : 2018/10/15上午10:06
 **/
public interface ServiceListener {

    ServiceListener NONE = new ServiceListener() {
        @Override
        public void onSuccess() {

        }

        @Override
        public void onFailure(Throwable t) {

        }
    };

    void onSuccess();

    void onFailure(Throwable t);
}
