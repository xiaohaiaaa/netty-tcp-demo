package org.example;

import lombok.extern.slf4j.Slf4j;
import org.example.client.MessageProcessor;
import org.example.client.QueueHolder;
import org.example.client.ThreadFactoryImpl;
import org.example.client.model.NettyMsgModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@SpringBootApplication
@Slf4j
@Configuration
public class NettyClientApplication implements CommandLineRunner {

    @Autowired
    private MessageProcessor messageProcessor;

    private static final Integer MAIN_THREAD_POOL_SIZE = 4;

    public static void main(String[] args) {
        SpringApplication.run(NettyClientApplication.class, args);
    }

    private final ExecutorService executor = Executors.newFixedThreadPool(MAIN_THREAD_POOL_SIZE,
            new ThreadFactoryImpl("Demo_TestThread_", false));

    @Override
    public void run(String... args) throws Exception {
        Thread loopThread = new Thread(new LoopThread());
        loopThread.start();
    }

    public class LoopThread implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < MAIN_THREAD_POOL_SIZE; i++) {
                executor.execute(() -> {
                    while (true) {
                        //取走BlockingQueue里排在首位的对象,若BlockingQueue为空,阻断进入等待状态直到
                        try {
                            NettyMsgModel nettyMsgModel = QueueHolder.get().take();
                            messageProcessor.process(nettyMsgModel);
                        } catch (InterruptedException e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                });
            }
        }
    }
}