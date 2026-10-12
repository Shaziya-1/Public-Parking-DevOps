package com.parking.dashboard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Application configuration properties for parking dashboard.
 * Values are sourced from application.properties / environment variables.
 */
@Configuration
public class ParkingConfig {

    /**
     * Low-availability threshold as a percentage of total capacity.
     * Default: 85 (locations with >= 85% occupancy trigger low-availability alerts).
     */
    @Value("${parking.low-availability-threshold:85}")
    private int lowAvailabilityThreshold;

    /**
     * Stale data threshold in minutes.
     * Default: 60 (locations not updated within 60 minutes are flagged as stale).
     */
    @Value("${parking.stale-threshold-minutes:60}")
    private int staleThresholdMinutes;

    /**
     * Application port (used for configuration documentation).
     */
    @Value("${server.port:8080}")
    private int appPort;

    /**
     * Dashboard refresh interval in seconds (used in frontend).
     */
    @Value("${parking.dashboard-refresh-seconds:30}")
    private int dashboardRefreshSeconds;

    public int getLowAvailabilityThreshold() { return lowAvailabilityThreshold; }
    public int getStaleThresholdMinutes() { return staleThresholdMinutes; }
    public int getAppPort() { return appPort; }
    public int getDashboardRefreshSeconds() { return dashboardRefreshSeconds; }
}
