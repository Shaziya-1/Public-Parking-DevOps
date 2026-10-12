package com.parking.dashboard.repository;

import com.parking.dashboard.model.ParkingLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Spring Data JPA repository for ParkingLocation entities.
 */
@Repository
public interface ParkingLocationRepository extends JpaRepository<ParkingLocation, Long> {

    /** Find all active parking locations */
    List<ParkingLocation> findByActiveTrue();

    /** Search by name, area, or address (case-insensitive) */
    @Query("SELECT p FROM ParkingLocation p WHERE p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.area) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.address) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<ParkingLocation> searchLocations(@Param("query") String query);

    /** Find by area */
    List<ParkingLocation> findByAreaContainingIgnoreCaseAndActiveTrue(String area);

    /** Find full locations (occupied >= capacity) */
    @Query("SELECT p FROM ParkingLocation p WHERE p.active = true AND p.occupiedSpaces >= p.totalCapacity")
    List<ParkingLocation> findFullLocations();

    /** Find low-availability locations (occupancy >= threshold percentage of capacity) */
    @Query("SELECT p FROM ParkingLocation p WHERE p.active = true AND " +
           "p.occupiedSpaces >= (p.totalCapacity * :thresholdPct / 100.0) AND " +
           "p.occupiedSpaces < p.totalCapacity")
    List<ParkingLocation> findLowAvailabilityLocations(@Param("thresholdPct") double thresholdPct);

    /** Count of active locations */
    long countByActiveTrue();

    /** Sum of total capacity */
    @Query("SELECT COALESCE(SUM(p.totalCapacity), 0) FROM ParkingLocation p WHERE p.active = true")
    long sumTotalCapacity();

    /** Sum of occupied spaces */
    @Query("SELECT COALESCE(SUM(p.occupiedSpaces), 0) FROM ParkingLocation p WHERE p.active = true")
    long sumOccupiedSpaces();

    /** Find stale locations (not updated since a given timestamp) */
    @Query("SELECT p FROM ParkingLocation p WHERE p.active = true AND p.lastUpdated < :since")
    List<ParkingLocation> findStaleLocations(@Param("since") java.time.LocalDateTime since);
}
