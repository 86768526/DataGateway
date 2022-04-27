package com.takeoff.iot.modbus;

import com.takeoff.iot.modbus.aggregate.controller.CollectController;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;

@SpringBootApplication
public class App implements ApplicationRunner {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

//    @PreDestroy
//    public void destory() {
//
//    }
    @Override
    public void run(ApplicationArguments args) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "node-red");
        Process start = processBuilder.start();
    }

}
