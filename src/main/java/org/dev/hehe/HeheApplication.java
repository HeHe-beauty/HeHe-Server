package org.dev.hehe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HeheApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeheApplication.class, args);
    }

}
