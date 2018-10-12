package com.github.zerowise.message;

/**
 ** @createtime : 2018/10/12下午3:37
 **/
public class RpcRespMessage {
    private String msgId;
    private Object result;
    private Exception error;

    public RpcRespMessage() {
    }

    public RpcRespMessage(String msgId, Object result) {
        this(msgId, result, null);
    }

    public RpcRespMessage(String msgId, Exception error) {
        this(msgId, null, error);
    }

    public RpcRespMessage(String msgId, Object result, Exception error) {
        this.msgId = msgId;
        this.result = result;
        this.error = error;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Exception getError() {
        return error;
    }

    public void setError(Exception error) {
        this.error = error;
    }


}
