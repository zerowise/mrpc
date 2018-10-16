package com.github.zerowise.conf;

/**
 ** @createtime : 2018/10/15下午4:34
 **/
public class DiscoverCnf {

    private String className;
    private String address;

    public DiscoverCnf() {
    }

    public DiscoverCnf(String className, String address) {
        this.className = className;
        this.address = address;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
