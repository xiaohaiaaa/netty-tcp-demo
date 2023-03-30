package org.example.client;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ReWind00
 * @date 2023/2/15 11:01
 */
public class NettyClientHolder {

    private static final ConcurrentHashMap<String, NettyClient> clientMap = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<String, NettyClient> get() {
        return clientMap;
    }

}
