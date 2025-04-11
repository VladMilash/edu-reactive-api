package com.mvo.edureactiveapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class EduReactiveApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
