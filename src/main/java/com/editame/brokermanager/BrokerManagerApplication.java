package com.editame.brokermanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BrokerManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BrokerManagerApplication.class, args);
    }

}
