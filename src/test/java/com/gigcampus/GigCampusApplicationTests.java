package com.gigcampus;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
class GigCampusApplicationTests {

    @Test
    void contextLoads() {
        // Confirms Spring Boot context boots cleanly, Flyway migrations run, and all beans initialize.
    }
}
