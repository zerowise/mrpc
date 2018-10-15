package com.github.zerowise.conf;

import com.typesafe.config.Config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 ** @createtime : 2018/10/15下午4:29
 **/
public class ConsumerCnf {
    private String group;
    private String app;
    private List<RegisterCnf> registers;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public List<RegisterCnf> getRegisters() {
        return registers;
    }

    public void setRegisters(List<RegisterCnf> registers) {
        this.registers = registers;
    }

    public void parse(Config config) {
        group = config.getString("group");
        app = config.getString("app");

        Set<String> serverList = new HashSet<>();
        List<? extends Config> registerConfigList = config.getConfigList("registers");
        registers = registerConfigList//
                .stream()//
                .map(registerConfig -> {
                    String serverAddr = registerConfig.getString("");
                    if (!serverList.add(serverAddr)) {
                        throw new RuntimeException("repeated server Addr:" + serverAddr);
                    }
                    return new RegisterCnf(registerConfig.getString("register.address")
                            , serverAddr
                            , registerConfig.getInt("server.weight")
                            , registerConfig.getString("register.class"));
                })//
                .collect(Collectors.toList());
    }
}
