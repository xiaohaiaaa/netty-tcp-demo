package org.example.client;

import org.example.client.model.NettyMsgModel;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * 本项目为演示使用本地队列 实际生产中应该使用消息中间件代替（rocketmq或rabbitmq）
 *
 * @author ReWind00
 * @date 2023/2/15 11:20
 */
public class QueueHolder {

    private static final ArrayBlockingQueue<NettyMsgModel> queue = new ArrayBlockingQueue<>(100);

    public static ArrayBlockingQueue<NettyMsgModel> get() {
        return queue;
    }
}
