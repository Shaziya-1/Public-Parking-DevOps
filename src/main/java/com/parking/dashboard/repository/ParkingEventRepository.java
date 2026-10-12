package com.parking.dashboard.repository;

import com.parking.dashboard.model.ParkingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for ParkingEvent entities.
 */
@Repository
public interface ParkingEventRepository extends JpaRepository<ParkingEvent, Long> {

    /** Find all events for a specific location, newest first */
    List<ParkingEvent> findByParkingLocationIdOrderByEventTimestampDesc(Long locationId);

    /** Find recent events (across all locations) */
    @Query("SELECT e FROM ParkingEvent e ORDER BY e.eventTimestamp DESC")
    List<ParkingEvent> findRecentEvents(org.springframework.data.domain.Pageable pageable);

    /** Find events after a given timestamp */
    @Query("SELECT e FROM ParkingEvent e WHERE e.eventTimestamp >= :since ORDER BY e.eventTimestamp DESC")
    List<ParkingEvent> findEventsSince(@Param("since") LocalDateTime since);

    /** Find events by type */
    List<ParkingEvent> findByEventTypeOrderByEventTimestampDesc(ParkingEvent.EventType eventType);

    /** Count events for a location */
    long countByParkingLocationId(Long locationId);

    /** Check for duplicate events within a time window (prevent double-submission) */
    @Query("SELECT COUNT(e) > 0 FROM ParkingEvent e WHERE " +
           "e.parkingLocation.id = :locationId AND " +
           "e.eventType = :eventType AND " +
           "e.reportedOccupancy = :occupancy AND " +
           "e.eventTimestamp >= :since")
    boolean existsDuplicateEvent(@Param("locationId") Long locationId,
                                  @Param("eventType") ParkingEvent.EventType eventType,
                                  @Param("occupancy") Integer occupancy,
                                  @Param("since") LocalDateTime since);
}
