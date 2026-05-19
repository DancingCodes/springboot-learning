package com.example.springbootlearning;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Slf4j
@SpringBootApplication
public class SpringbootLearningApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootLearningApplication.class, args);
    }

    @Bean
    public CommandLineRunner printDocUrl() {
        return args -> {
            log.info("Knife4j API文档: http://localhost:8080/doc.html");
            log.info("OpenAPI JSON : http://localhost:8080/v3/api-docs");
        };
    }
}
