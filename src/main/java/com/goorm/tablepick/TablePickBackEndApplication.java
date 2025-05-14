package com.goorm.tablepick;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TablePickBackEndApplication {

    public static void main(String[] args) {
        SpringApplication.run(TablePickBackEndApplication.class, args);
    }

}
