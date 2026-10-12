package com.parking.dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring Boot application context smoke test.
 * Verifies the application context loads successfully.
 */
@SpringBootTest
@ActiveProfiles("test")
class ParkingDashboardApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that the Spring application context starts without errors
    }
}
