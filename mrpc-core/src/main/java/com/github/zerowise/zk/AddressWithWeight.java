package com.github.zerowise.zk;

import java.nio.charset.Charset;

/**
 ** @createtime : 2018/10/15下午5:12
 **/
public class AddressWithWeight {
    private String serverAddr;
    private int weight;

    public AddressWithWeight() {
    }

    public AddressWithWeight(String serverAddr, int weight) {
        this.serverAddr = serverAddr;
        this.weight = weight;
    }

    public AddressWithWeight(byte[] data) {
        String str = new String(data, Charset.defaultCharset());
        String[] tempArr = str.split("@");
        serverAddr = tempArr[0];
        weight = Integer.parseInt(tempArr[1]);
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public byte[] toBytes() {
        return (serverAddr + "@" + weight).getBytes(Charset.defaultCharset());
    }
}
