package com.example.java_template;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JavaTemplateApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaTemplateApplication.class, args);
    }

}
