package com.github.zerowise.client;

import java.io.Closeable;

/**
 ** @createtime : 2018/10/15下午8:41
 **/
public interface Sender extends Closeable {

    void send(Object msg);
}
