package org.example;


import lombok.extern.slf4j.Slf4j;
import org.example.server.NettyServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class NettyServerApplication implements CommandLineRunner {

    @Autowired
    private NettyServer nettyServer;


    @Override
    public void run(String... args) throws Exception {
        nettyServer.start();
    }

    public static void main(String[] args) {
        SpringApplication.run(NettyServerApplication.class,args);
    }
}