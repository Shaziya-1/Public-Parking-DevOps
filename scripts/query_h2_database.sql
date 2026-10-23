-- =============================================================================
-- ParkWatch - H2 Database SQL Query File
-- Database File: data/parkingdb.mv.db
-- JDBC URL: jdbc:h2:file:./data/parkingdb;AUTO_SERVER=TRUE;MODE=MySQL
-- User: sa | Password: (empty)
-- =============================================================================

-- 1. All saved AI Analysis History records (Newest first, showing all requested fields)
SELECT 
    ID,
    ANALYSIS_TIMESTAMP,
    ORIGINAL_IMAGE_PATH,
    ANNOTATED_IMAGE_PATH,
    DETECTION_MODE,
    PROCESSING_TIME_MS,
    CONFIDENCE_THRESHOLD,
    AVERAGE_CONFIDENCE,
    VEHICLES_COUNT,
    TOTAL_STALLS,
    EMPTY_STALLS,
    OCCUPIED_STALLS,
    OCCUPANCY_PERCENTAGE
FROM AI_ANALYSIS_HISTORY
ORDER BY ANALYSIS_TIMESTAMP DESC;

-- 2. Clean tabular summary for terminal display (Newest first)
SELECT 
    ID,
    ANALYSIS_TIMESTAMP,
    DETECTION_MODE,
    PROCESSING_TIME_MS AS PROC_TIME_MS,
    CONFIDENCE_THRESHOLD AS CONF_THRESH,
    AVERAGE_CONFIDENCE AS AVG_CONF,
    VEHICLES_COUNT,
    TOTAL_STALLS,
    EMPTY_STALLS,
    OCCUPIED_STALLS,
    OCCUPANCY_PERCENTAGE
FROM AI_ANALYSIS_HISTORY
ORDER BY ID DESC;

-- 3. Summary Analytics for AI Parking Runs
SELECT 
    COUNT(*) AS TOTAL_ANALYSES_RUN,
    ROUND(AVG(OCCUPANCY_PERCENTAGE), 1) AS AVG_OCCUPANCY_PCT,
    ROUND(AVG(PROCESSING_TIME_MS), 0) AS AVG_LATENCY_MS,
    SUM(VEHICLES_COUNT) AS TOTAL_VEHICLES_DETECTED,
    SUM(TOTAL_STALLS) AS TOTAL_STALLS_EVALUATED
FROM AI_ANALYSIS_HISTORY;

-- 4. View all Municipal Parking Locations (26 records)
SELECT 
    ID,
    NAME,
    LOCATION_CODE,
    AREA,
    TOTAL_CAPACITY,
    OCCUPIED_SPACES,
    ACTIVE
FROM PARKING_LOCATION
ORDER BY ID ASC;
