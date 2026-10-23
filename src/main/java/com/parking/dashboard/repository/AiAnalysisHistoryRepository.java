package com.parking.dashboard.repository;

import com.parking.dashboard.model.AiAnalysisHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Spring Data JPA repository for persisting and querying AI Analysis History records.
 */
@Repository
public interface AiAnalysisHistoryRepository extends JpaRepository<AiAnalysisHistory, Long> {

    /**
     * Retrieve all historical AI analyses ordered newest first.
     */
    List<AiAnalysisHistory> findAllByOrderByAnalysisTimestampDesc();
}
