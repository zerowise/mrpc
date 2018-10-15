package com.github.zerowise.conf;

import com.typesafe.config.Config;

/**
 ** @createtime : 2018/10/15下午4:32
 **/
public class RegisterCnf {

    private String registerAddr;
    private String serverAddr;
    private int weight;

    public RegisterCnf() {
    }

    public RegisterCnf(String registerAddr, String serverAddr, int weight) {
        this.registerAddr = registerAddr;
        this.serverAddr = serverAddr;
        this.weight = weight;
    }

    public String getRegisterAddr() {
        return registerAddr;
    }

    public void setRegisterAddr(String registerAddr) {
        this.registerAddr = registerAddr;
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
}
