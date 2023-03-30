package org.example.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.example.client.NettyClient;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author ReWind00
 * @date 2023/2/15 10:09
 */
@Slf4j
@Component
@Scope("prototype")
public class DemoClientHandler extends BaseClientHandler {

    private final String imei;

    private final Map<String, Object> bizData;

    private final NettyClient nettyClient;

    private int allIdleCounter = 0;

    private static final int MAX_IDLE_TIMES = 3;

    public DemoClientHandler(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
        this.imei = nettyClient.getImei();
        this.bizData = nettyClient.getBizData();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端imei={}，通道激活成功", this.imei);
        synchronized (this.nettyClient) { //当通道激活后解锁队列线程，然后再发送消息
            this.nettyClient.notify();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn("客户端imei={}，通道断开连接", this.imei);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("客户端imei={}，收到消息:  {}", this.imei, msg);
        //处理业务...
        if ("shutdown".equals(msg)) {
            this.nettyClient.close();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端imei={}，接收消息完毕******", this.imei);
        super.channelReadComplete(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            boolean flag = false;
            if (e.state() == IdleState.ALL_IDLE) {
                this.allIdleCounter++;
                log.info("客户端imei={}触发闲读或写第{}次", this.imei, this.allIdleCounter);
                if (this.allIdleCounter >= MAX_IDLE_TIMES) {
                    flag = true;
                }
            }
            if (flag) {
                log.warn("读写超时达到{}次，主动断开连接", MAX_IDLE_TIMES);
                ctx.channel().close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("客户端imei={}，连接异常{}", imei, cause.getMessage(), cause);
    }
}
