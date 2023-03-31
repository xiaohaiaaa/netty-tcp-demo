package org.example.client;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.example.client.handler.BaseClientHandler;
import org.example.core.util.SpringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ReWind00
 * @date 2023/2/15 9:59
 */
@Slf4j
@Component
@Scope("prototype")
@Getter
@NoArgsConstructor
public class NettyClient implements Runnable {

    @Value("${netty.server.port}")
    private int port;

    @Value("${netty.server.host}")
    private String host;
    //客户端唯一标识
    private String imei;
    //自定义业务数据
    private Map<String, Object> bizData;

    private EventLoopGroup workGroup;

    private Class<BaseClientHandler> clientHandlerClass;

    private ChannelFuture channelFuture;

    public NettyClient(String imei, Map<String, Object> bizData, EventLoopGroup workGroup, Class<BaseClientHandler> clientHandlerClass) {
        this.imei = imei;
        this.bizData = bizData;
        this.workGroup = workGroup;
        this.clientHandlerClass = clientHandlerClass;
    }

    @Override
    public void run() {
        try {
            this.init();
            log.info("客户端启动imei={}", imei);
        } catch (Exception e) {
            log.error("客户端启动失败:{}", e.getMessage(), e);
        }
    }

    public void close() {
        if (null != this.channelFuture) {
            this.channelFuture.channel().close();
        }
        NettyClientHolder.get().remove(this.imei);
    }

    public void send(String message) {
        try {
            if (!this.channelFuture.channel().isActive()) {
                log.info("通道不活跃imei={}", this.imei);
                return;
            }
            if (!StringUtils.isEmpty(message)) {
                log.info("队列消息发送===>{}", message);
                this.channelFuture.channel().writeAndFlush(message);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void init() throws Exception {
        //将本实例传递到handler
        BaseClientHandler clientHandler = SpringUtils.getBean(clientHandlerClass, this);
        Bootstrap b = new Bootstrap();
        //2 通过辅助类去构造server/client
        b.group(workGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .option(ChannelOption.SO_RCVBUF, 1024 * 32)
                .option(ChannelOption.SO_SNDBUF, 1024 * 32)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //ch.pipeline().addLast(new DelimiterBasedFrameDecoder(1024 * 1024, Unpooled.copiedBuffer("了".getBytes())));
                        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4));
                        ch.pipeline().addLast(new MessageEncode());// 自定义编码
                        //ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));// String编码。
                        ch.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));// String解码。
                        // 心跳设置
                        ch.pipeline().addLast(new IdleStateHandler(0, 0, 600, TimeUnit.SECONDS));
                        ch.pipeline().addLast(clientHandler);
                    }
                });
        this.connect(b);
    }

    private void connect(Bootstrap b) throws InterruptedException {
        long c1 = System.currentTimeMillis();
        final int maxRetries = 2; //重连2次
        final AtomicInteger count = new AtomicInteger();
        final AtomicBoolean flag = new AtomicBoolean(false);
        try {
            this.channelFuture = b.connect(host, port).addListener(
                    new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (!future.isSuccess()) {
                                if (count.incrementAndGet() > maxRetries) {
                                    log.warn("imei={}重连超过{}次", imei, maxRetries);
                                } else {
                                    log.info("imei={}重连第{}次", imei, count);
                                    b.connect(host, port).addListener(this);
                                }

                            } else {
                                log.info("imei={}连接成功,连接IP:{}连接端口:{}", imei, host, port);
                                flag.set(true);
                            }
                        }
                    }).sync(); //同步连接
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.info("设备imei={}，channelId={}连接耗时={}ms", imei, channelFuture.channel().id(), System.currentTimeMillis() - c1);
        if (flag.get()) {
            channelFuture.channel().closeFuture().sync(); //连接成功后将持续阻塞该线程
        }
    }
}
