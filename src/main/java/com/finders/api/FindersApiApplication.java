package com.finders.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FindersApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FindersApiApplication.class, args);
    }
}
