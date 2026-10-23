package com.parking.dashboard.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity representing persistent history of an AI Parking Occupancy Analysis run.
 * Preserves the original uploaded image, the annotated result image,
 * and the exact detection metrics returned by the analysis.
 */
@Entity
@Table(name = "ai_analysis_history")
public class AiAnalysisHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_image_filename", nullable = false, length = 255)
    private String originalImageFilename;

    @Column(name = "original_image_path", nullable = false, length = 500)
    private String originalImagePath;

    @Column(name = "annotated_image_filename", nullable = false, length = 255)
    private String annotatedImageFilename;

    @Column(name = "annotated_image_path", nullable = false, length = 500)
    private String annotatedImagePath;

    @Column(name = "detection_mode", length = 50)
    private String detectionMode;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Column(name = "confidence_threshold")
    private Double confidenceThreshold;

    @Column(name = "average_confidence")
    private Double averageConfidence;

    @Column(name = "vehicles_count")
    private Integer vehiclesCount;

    @Column(name = "total_stalls")
    private Integer totalStalls;

    @Column(name = "empty_stalls")
    private Integer emptyStalls;

    @Column(name = "occupied_stalls")
    private Integer occupiedStalls;

    @Column(name = "occupancy_percentage")
    private Double occupancyPercentage;

    @Column(name = "analysis_timestamp", nullable = false)
    private LocalDateTime analysisTimestamp;

    @Column(name = "reliable")
    private Boolean reliable = true;

    @Column(name = "reliability_warning", length = 1000)
    private String reliabilityWarning;

    public AiAnalysisHistory() {
        this.analysisTimestamp = LocalDateTime.now();
    }

    public AiAnalysisHistory(String originalImageFilename,
                             String originalImagePath,
                             String annotatedImageFilename,
                             String annotatedImagePath,
                             String detectionMode,
                             Long processingTimeMs,
                             Double confidenceThreshold,
                             Integer vehiclesCount,
                             Integer totalStalls,
                             Integer emptyStalls,
                             Integer occupiedStalls,
                             Double occupancyPercentage,
                             LocalDateTime analysisTimestamp,
                             Boolean reliable,
                             String reliabilityWarning) {
        this.originalImageFilename = originalImageFilename;
        this.originalImagePath = originalImagePath;
        this.annotatedImageFilename = annotatedImageFilename;
        this.annotatedImagePath = annotatedImagePath;
        this.detectionMode = detectionMode;
        this.processingTimeMs = processingTimeMs;
        this.confidenceThreshold = confidenceThreshold;
        this.vehiclesCount = vehiclesCount;
        this.totalStalls = totalStalls;
        this.emptyStalls = emptyStalls;
        this.occupiedStalls = occupiedStalls;
        this.occupancyPercentage = occupancyPercentage;
        this.analysisTimestamp = analysisTimestamp != null ? analysisTimestamp : LocalDateTime.now();
        this.reliable = reliable;
        this.reliabilityWarning = reliabilityWarning;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalImageFilename() {
        return originalImageFilename;
    }

    public void setOriginalImageFilename(String originalImageFilename) {
        this.originalImageFilename = originalImageFilename;
    }

    public String getOriginalImagePath() {
        return originalImagePath;
    }

    public void setOriginalImagePath(String originalImagePath) {
        this.originalImagePath = originalImagePath;
    }

    public String getAnnotatedImageFilename() {
        return annotatedImageFilename;
    }

    public void setAnnotatedImageFilename(String annotatedImageFilename) {
        this.annotatedImageFilename = annotatedImageFilename;
    }

    public String getAnnotatedImagePath() {
        return annotatedImagePath;
    }

    public void setAnnotatedImagePath(String annotatedImagePath) {
        this.annotatedImagePath = annotatedImagePath;
    }

    public String getDetectionMode() {
        return detectionMode;
    }

    public void setDetectionMode(String detectionMode) {
        this.detectionMode = detectionMode;
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public Double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(Double confidenceThreshold) {
        this.confidenceThreshold = confidenceThreshold;
    }

    public Double getAverageConfidence() {
        return averageConfidence;
    }

    public void setAverageConfidence(Double averageConfidence) {
        this.averageConfidence = averageConfidence;
    }

    public Integer getVehiclesCount() {
        return vehiclesCount;
    }

    public void setVehiclesCount(Integer vehiclesCount) {
        this.vehiclesCount = vehiclesCount;
    }

    public Integer getTotalStalls() {
        return totalStalls;
    }

    public void setTotalStalls(Integer totalStalls) {
        this.totalStalls = totalStalls;
    }

    public Integer getEmptyStalls() {
        return emptyStalls;
    }

    public void setEmptyStalls(Integer emptyStalls) {
        this.emptyStalls = emptyStalls;
    }

    public Integer getOccupiedStalls() {
        return occupiedStalls;
    }

    public void setOccupiedStalls(Integer occupiedStalls) {
        this.occupiedStalls = occupiedStalls;
    }

    public Double getOccupancyPercentage() {
        return occupancyPercentage;
    }

    public void setOccupancyPercentage(Double occupancyPercentage) {
        this.occupancyPercentage = occupancyPercentage;
    }

    public LocalDateTime getAnalysisTimestamp() {
        return analysisTimestamp;
    }

    public void setAnalysisTimestamp(LocalDateTime analysisTimestamp) {
        this.analysisTimestamp = analysisTimestamp;
    }

    public Boolean getReliable() {
        return reliable;
    }

    public void setReliable(Boolean reliable) {
        this.reliable = reliable;
    }

    public String getReliabilityWarning() {
        return reliabilityWarning;
    }

    public void setReliabilityWarning(String reliabilityWarning) {
        this.reliabilityWarning = reliabilityWarning;
    }
}
