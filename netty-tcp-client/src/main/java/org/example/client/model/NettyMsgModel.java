package org.example.client.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ReWind00
 * @date 2023/2/15 10:36
 */
@Data
@Accessors(chain = true)
public class NettyMsgModel implements Serializable {

    private String imei;

    private String msg;

    private Map<String, Object> bizData;

    public static NettyMsgModel create(String imei, String msg) {
        return new NettyMsgModel().setBizData(new HashMap<>()).setMsg(msg).setImei(imei);
    }
}
