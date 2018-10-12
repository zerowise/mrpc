package com.github.zerowise.rpc;

import com.github.zerowise.client.RpcClient;
import com.github.zerowise.message.RpcReqMessage;
import com.github.zerowise.message.RpcRespMessage;

import java.util.concurrent.ConcurrentHashMap;

/**
 ** @createtime : 2018/10/12下午3:31
 **/
public class RpcInvoker {

    private ConcurrentHashMap<String, RpcResult> rpcResultCaches;

    private RpcClient rpcClient;

    public RpcInvoker(RpcClient rpcClient) {
        this.rpcClient = rpcClient;
        this.rpcResultCaches = new ConcurrentHashMap<>();
        this.rpcClient.setInvoker(this);
    }

    public RpcResult send(RpcReqMessage rpcReqMessage) {
        RpcResult rpcResult = new RpcResult();
        rpcResultCaches.put(rpcReqMessage.getMsgId(), rpcResult);
        rpcClient.writeMessage(rpcReqMessage);
        return rpcResult;
    }

    public void onMessageReceive(RpcRespMessage rpcRespMessage) {
        RpcResult rpcResult = rpcResultCaches.remove(rpcRespMessage.getMsgId());
        if (rpcResult != null) {
            rpcResult.onResult(rpcRespMessage.getResult(), rpcRespMessage.getError());
        }
    }
}
