package com.github.zerowise.example;

import com.github.zerowise.api.RpcService;

/**
 ** @createtime : 2018/10/12下午4:55
 **/

@RpcService
public class RpcCalServiceImpl implements RpcCalService {
    @Override
    public int add(int a, int b) {
        return a + b;
    }
}
