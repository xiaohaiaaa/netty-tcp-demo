package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.client.MessageProcessor;
import org.example.client.QueueHolder;
import org.example.client.model.NettyMsgModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author ReWind00
 * @date 2023/2/15 11:13
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class NettyTests {


    @Test
    public void testMsg() {
        QueueHolder.get().offer(NettyMsgModel.create("87655421","Hello World!"));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        QueueHolder.get().offer(NettyMsgModel.create("87655421","Hello World Too!"));
    }
}
