package org.example.server;

import java.nio.ByteBuffer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @ClassName MessageEncode
 * @Description
 * @Author ZXH
 * @Date 2023/3/30 15:07
 * @Version 1.0
 **/
public class MessageEncode extends MessageToByteEncoder<String> {

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {
        //将字符串消息转换为字节数组
        byte[] bytes = msg.getBytes();
        //获取字节数组长度
        int len = bytes.length;
        //创建一个ByteBuffer缓存空间，空间大小为消息字节数组长度+4个字节（消息字节长度的长度int型占4个字节）
        ByteBuffer Bb = ByteBuffer.allocate(len+4+4);
        //添加消息字节长度的长度到缓存
        Bb.putInt(len);
        //添加消息字节数组到缓存
        Bb.put(bytes);
        //进行读写位置翻转
        Bb.flip();
        //将缓存数据传入ByteBuf进行传递
        out.writeBytes(Bb);
    }
}
