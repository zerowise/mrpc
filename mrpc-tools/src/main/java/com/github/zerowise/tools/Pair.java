package com.github.zerowise.tools;

/**
 ** @createtime : 2018/10/15下午6:00
 **/
public class Pair<M, N> {
    private M m;
    private N n;

    public Pair(M m, N n) {
        this.m = m;
        this.n = n;
    }

    public M getM() {
        return m;
    }

    public void setM(M m) {
        this.m = m;
    }

    public N getN() {
        return n;
    }

    public void setN(N n) {
        this.n = n;
    }
}
