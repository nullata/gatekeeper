package com.gatekeeper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Gatekeeper {

    public static void main(String[] args) {
        SpringApplication.run(Gatekeeper.class, args);
    }

}
