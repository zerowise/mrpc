package com.github.zerowise.zk;

import java.io.Closeable;
import java.util.List;

/**
 ** @createtime : 2018/10/15下午4:54
 **/
public interface Discover extends Closeable {

    void init(List<String> serverList);
}
