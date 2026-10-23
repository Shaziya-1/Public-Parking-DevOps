package com.parking.dashboard.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.parking.dashboard.dto.AiAnalysisResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.parking.dashboard.model.AiAnalysisHistory;
import com.parking.dashboard.repository.AiAnalysisHistoryRepository;

import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

/**
 * Service orchestrating AI computer vision parking stall analysis and result persistence.
 * Municipal database models (ParkingLocation, ParkingEvent) remain strictly protected and unmutated.
 */
@Service
public class AiAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AiAnalysisService.class);

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "bmp", "webp");

    @Value("${parking.ai.enabled:true}")
    private boolean enabled;

    @Value("${parking.ai.python-path:python}")
    private String pythonPath;

    @Value("${parking.ai.model-path:models/parkwatch_yolov8n/weights/best.pt}")
    private String modelPath;

    @Value("${parking.ai.script-path:scripts/ai_inference_demo.py}")
    private String scriptPath;

    @Value("${parking.ai.default-conf:0.25}")
    private double defaultConf;

    private final ObjectMapper objectMapper;
    private final Path resultsDir;
    private final AiAnalysisHistoryRepository historyRepository;

    public AiAnalysisService(ObjectMapper objectMapper, AiAnalysisHistoryRepository historyRepository) {
        this.objectMapper = objectMapper;
        this.historyRepository = historyRepository;
        this.resultsDir = Paths.get("data", "ai_results").toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.resultsDir);
        } catch (IOException e) {
            log.warn("Could not create AI results directory: {}", e.getMessage());
        }
    }

    /**
     * Checks whether the trained YOLO checkpoint file exists locally.
     */
    public boolean isModelAvailable() {
        if (!enabled) {
            return false;
        }
        File modelFile = new File(modelPath);
        return modelFile.exists() && modelFile.isFile() && modelFile.length() > 0;
    }

    /**
     * Safely runs image inference on an uploaded frame.
     * Validates file size, raster image format, dimensions, and invokes python without shell interpolation.
     */
    public AiAnalysisResultDto analyzeImage(MultipartFile file, Double confThreshold) {
        if (!enabled) {
            return AiAnalysisResultDto.error("AI Analysis module is currently disabled in system configuration.");
        }

        double conf = (confThreshold != null && confThreshold > 0.0 && confThreshold <= 1.0)
                ? confThreshold : defaultConf;

        // 1. Basic File Validation
        if (file == null || file.isEmpty()) {
            return AiAnalysisResultDto.error("Please select a parking surveillance image to analyze.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return AiAnalysisResultDto.error("Uploaded image exceeds maximum allowed size of 10 MB.");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            return AiAnalysisResultDto.error("Invalid image filename. Expected extension like .jpg or .png.");
        }

        String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return AiAnalysisResultDto.error("Unsupported format: ." + ext + ". Allowed formats: JPG, JPEG, PNG, WEBP, BMP.");
        }

        // 2. Deep Image Inspection (Format & Dimensions)
        try {
            BufferedImage bimg = ImageIO.read(file.getInputStream());
            if (bimg == null) {
                return AiAnalysisResultDto.error("The uploaded file could not be parsed as a valid image.");
            }
            if (bimg.getWidth() < 64 || bimg.getHeight() < 64 || bimg.getWidth() > 4096 || bimg.getHeight() > 4096) {
                return AiAnalysisResultDto.error("Image dimensions (" + bimg.getWidth() + "x" + bimg.getHeight() + ") must be between 64x64 and 4096x4096 pixels.");
            }
        } catch (Exception e) {
            return AiAnalysisResultDto.error("Image verification failed. Please ensure the file is not corrupted.");
        }

        // 3. Model file existence check
        File modelFile = new File(modelPath);
        if (!modelFile.exists()) {
            return AiAnalysisResultDto.error("AI model checkpoint (best.pt) is missing. Please ensure the model has been trained.");
        }

        File scriptFile = new File(scriptPath);
        if (!scriptFile.exists()) {
            return AiAnalysisResultDto.error("Inference script is missing. Please check server configuration.");
        }

        // 4. Safe Temporary File Handling
        Path tempInput = null;
        String resultFilename = "ai_annotated_" + UUID.randomUUID() + ".jpg";
        Path tempOutput = resultsDir.resolve(resultFilename);

        long startTime = System.currentTimeMillis();

        try {
            tempInput = Files.createTempFile("ai_upload_", "." + ext);
            file.transferTo(tempInput.toFile());

            // 5. Secure ProcessBuilder Invocation (No shell, array arguments)
            List<String> command = List.of(
                    pythonPath,
                    scriptFile.getAbsolutePath(),
                    "--image", tempInput.toAbsolutePath().toString(),
                    "--model", modelFile.getAbsolutePath(),
                    "--conf", String.valueOf(conf),
                    "--iou", "0.20",
                    "--output", tempOutput.toAbsolutePath().toString(),
                    "--json"
            );

            log.info("Launching isolated AI inference process on image: {}", tempInput.getFileName());

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder outputLog = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputLog.append(line).append("\n");
                }
            }

            boolean completed = process.waitFor(45, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                return AiAnalysisResultDto.error("AI inference process timed out after 45 seconds.");
            }

            if (process.exitValue() != 0) {
                log.warn("AI process failed with exit code {}. Output:\n{}", process.exitValue(), outputLog);
                return AiAnalysisResultDto.error("Inference execution returned an error. Please try a different camera frame.");
            }

            // 6. Parse Structured JSON Response
            String outputStr = outputLog.toString();
            String startMarker = "__JSON_RESULT_START__";
            String endMarker = "__JSON_RESULT_END__";

            int sIdx = outputStr.indexOf(startMarker);
            int eIdx = outputStr.indexOf(endMarker);

            if (sIdx == -1 || eIdx == -1) {
                log.warn("Could not find JSON markers in inference output:\n{}", outputStr);
                return AiAnalysisResultDto.error("Failed to parse AI detection output.");
            }

            String jsonPayload = outputStr.substring(sIdx + startMarker.length(), eIdx).trim();
            JsonNode root = objectMapper.readTree(jsonPayload);

            AiAnalysisResultDto result = new AiAnalysisResultDto();
            result.setStatus(root.path("status").asText("SUCCESS"));
            result.setTotalDetected(root.path("totalDetected").asInt(0));
            result.setEmptyCount(root.path("emptyCount").asInt(0));
            result.setOccupiedCount(root.path("occupiedCount").asInt(0));
            result.setOccupancyPct(root.path("occupancyPct").asDouble(0.0));
            result.setMinConf(root.path("minConf").asDouble(0.0));
            result.setMaxConf(root.path("maxConf").asDouble(0.0));
            result.setAvgConf(root.path("avgConf").asDouble(0.0));
            result.setVehiclesDetected(root.path("vehiclesDetected").asInt(0));
            result.setCalibrationMode(root.path("calibrationMode").asText("UNCALIBRATED"));
            result.setReliable(root.path("reliable").asBoolean(true));
            if (root.hasNonNull("reliabilityWarning")) {
                result.setReliabilityWarning(root.path("reliabilityWarning").asText());
            }
            result.setResultImageFilename(resultFilename);
            result.setRequestedConfidence(conf);
            result.setExecutionTimeMs(System.currentTimeMillis() - startTime);

            log.info("AI Analysis completed: total={}, empty={}, occupied={}, reliable={} in {} ms",
                    result.getTotalDetected(), result.getEmptyCount(), result.getOccupiedCount(), result.isReliable(), result.getExecutionTimeMs());

            // 7. Permanently persist analysis history and original image
            try {
                String originalFilename = "ai_original_" + UUID.randomUUID() + "." + ext;
                Path originalSavedPath = resultsDir.resolve(originalFilename);
                Files.copy(tempInput, originalSavedPath, StandardCopyOption.REPLACE_EXISTING);

                String modeDisplay;
                if ("CALIBRATED".equalsIgnoreCase(result.getCalibrationMode())) {
                    modeDisplay = "Calibrated Layout";
                } else if ("PKLOT_SURVEILLANCE".equalsIgnoreCase(result.getCalibrationMode())) {
                    modeDisplay = "Surveillance Grid";
                } else {
                    modeDisplay = "Vehicle Detection Only";
                }

                AiAnalysisHistory history = new AiAnalysisHistory(
                        originalFilename,
                        originalSavedPath.toAbsolutePath().toString(),
                        resultFilename,
                        tempOutput.toAbsolutePath().toString(),
                        modeDisplay,
                        result.getExecutionTimeMs(),
                        conf,
                        result.getVehiclesDetected(),
                        result.getTotalDetected(),
                        result.getEmptyCount(),
                        result.getOccupiedCount(),
                        result.getOccupancyPct(),
                        LocalDateTime.now(),
                        result.isReliable(),
                        result.getReliabilityWarning()
                );
                history.setAverageConfidence(result.getAvgConf());

                AiAnalysisHistory savedRecord = historyRepository.save(history);
                result.setHistoryId(savedRecord.getId());
                result.setOriginalImageFilename(originalFilename);
                log.info("Persisted AI Analysis History record with ID: {}", savedRecord.getId());
            } catch (Exception ex) {
                log.warn("Failed to persist AI analysis history record: {}", ex.getMessage());
            }

            return result;

        } catch (Exception e) {
            log.error("AI inference encountered an unexpected error: {}", e.getMessage(), e);
            return AiAnalysisResultDto.error("An unexpected error occurred during image processing.");
        } finally {
            // Clean up temporary input file immediately
            if (tempInput != null) {
                try {
                    Files.deleteIfExists(tempInput);
                } catch (IOException ignored) {}
            }
        }
    }

    /**
     * Retrieves all saved historical AI analyses ordered newest first.
     */
    public List<AiAnalysisHistory> getAllHistory() {
        return historyRepository.findAllByOrderByAnalysisTimestampDesc();
    }

    /**
     * Retrieves a specific historical analysis record by ID.
     */
    public Optional<AiAnalysisHistory> getHistoryById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return historyRepository.findById(id);
    }

    /**
     * Safely reads an annotated result image bytes for UI display.
     * Prevents path traversal strictly.
     */
    public byte[] getResultImageBytes(String filename) throws IOException {
        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Invalid image filename.");
        }

        // Sanitize filename against path traversal
        String sanitized = filename.replaceAll("[^a-zA-Z0-9._-]", "");
        Path targetPath = resultsDir.resolve(sanitized).normalize();

        if (!targetPath.startsWith(resultsDir) || !Files.exists(targetPath)) {
            throw new IllegalArgumentException("Image file not found.");
        }

        return Files.readAllBytes(targetPath);
    }

    public double getDefaultConf() {
        return defaultConf;
    }
}
