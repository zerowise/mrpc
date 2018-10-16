package com.github.zerowise.conf;

import com.typesafe.config.Config;

/**
 ** @createtime : 2018/10/15下午4:34
 **/
public class ProducerCnf {

    private String group;
    private String app;
    private int connectCnt;

    private String loadBalaceClass;

    private DiscoverCnf discover;

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

    public int getConnectCnt() {
        return connectCnt;
    }

    public void setConnectCnt(int connectCnt) {
        this.connectCnt = connectCnt;
    }

    public DiscoverCnf getDiscover() {
        return discover;
    }

    public void setDiscover(DiscoverCnf discover) {
        this.discover = discover;
    }

    public String getLoadBalaceClass() {
        return loadBalaceClass;
    }

    void parse(Config config){

    }
}
