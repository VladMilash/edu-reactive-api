package com.mvo.edureactiveapi;

import org.springframework.boot.SpringApplication;

public class TestEduReactiveApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(EduReactiveApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
