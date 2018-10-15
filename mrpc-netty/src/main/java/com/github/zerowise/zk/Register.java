package com.github.zerowise.zk;

import java.io.Closeable;

/**
 ** @createtime : 2018/10/15下午4:52
 **/
public interface Register extends Closeable {

    void init(String serverList);

    void register(String group, String app, String serverAddress, int serverWeight);

    void unregister(String group, String app, String serverAddress);
}
