package com.gatekeeper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class Gatekeeper {

    public static void main(String[] args) {
        SpringApplication.run(Gatekeeper.class, args);
    }

}
