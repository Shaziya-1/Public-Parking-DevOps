package com.parking.dashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * DevOps-Enabled Public Parking Availability Dashboard
 * Academic Project - Student: Sajiya Shaikh, Roll: 24102A2005
 *
 * Main Spring Boot application entry point.
 */
@SpringBootApplication
@EnableScheduling
public class ParkingDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParkingDashboardApplication.class, args);
    }
}
