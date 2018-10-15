package com.github.zerowise.conf;

import com.github.zerowise.tools.ClazzUtil;

/**
 ** @createtime : 2018/10/15下午4:32
 **/
public class RegisterCnf {

    private String registerAddr;
    private String serverAddr;
    private int weight;

    private Class<?> registerClazz;

    public RegisterCnf() {
    }

    public RegisterCnf(String registerAddr, String serverAddr, int weight, String registerClazzName) {
        this.registerAddr = registerAddr;
        this.serverAddr = serverAddr;
        this.weight = weight;
        this.registerClazz = ClazzUtil.findClazz(registerClazzName);
    }

    public String getRegisterAddr() {
        return registerAddr;
    }

    public String getServerAddr() {
        return serverAddr;
    }

    public int getWeight() {
        return weight;
    }


    public Class<?> getRegisterClazz() {
        return registerClazz;
    }
}
