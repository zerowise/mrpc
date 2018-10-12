package com.github.zerowise.rpc;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 ** @createtime : 2018/10/12下午3:51
 **/
public class RpcResult {

    private Object result;
    private Exception error;

    private Lock lock = new ReentrantLock();

    private Condition condition = lock.newCondition();

    public Object getResult() throws Exception{
        try {
            lock.lock();
            condition.await(100, TimeUnit.SECONDS);
            if (result == null && error != null) {
                throw error;
            }
            return result;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;
    }

    public void onResult(Object result, Exception error) {
        try {
            lock.lock();
            this.result = result;
            this.error = error;
            condition.signal();
        } finally {
            lock.unlock();
        }
    }
}
