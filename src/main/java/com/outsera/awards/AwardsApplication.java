package com.outsera.awards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AwardsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AwardsApplication.class, args);
    }
}
