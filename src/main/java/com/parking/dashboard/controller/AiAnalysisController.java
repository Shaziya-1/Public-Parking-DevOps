package com.parking.dashboard.controller;

import com.parking.dashboard.dto.AiAnalysisResultDto;
import com.parking.dashboard.service.AiAnalysisService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Web Controller for the AI Parking Stall Analysis page (/ai-analysis).
 * Completely isolated from municipal parking database persistence.
 */
@Controller
@RequestMapping("/ai-analysis")
public class AiAnalysisController {

    private final AiAnalysisService aiService;

    // Measured Improved Model Benchmark Metrics (Verified from held-out test split evaluation)
    private static final Map<String, Object> MODEL_METRICS = Map.of(
            "modelName", "YOLOv8n (Nano) - Improved Fine-Tuned",
            "weightsFile", "models/parkwatch_yolov8n_improved/weights/best.pt",
            "weightsSize", "6.24 MB",
            "parameters", "3.01 Million",
            "dataset", "PKLot v2 640 (UFPR Brazil, CC BY 4.0)",
            "testMap50", "0.8612 (86.12%)",
            "testMap50_95", "0.4854 (48.54%)",
            "testPrecision", "0.7995 (79.95%)",
            "testRecall", "0.8328 (83.28%)",
            "trainingType", "6-Epoch Fine-Tuning (300 Stratified Images)"
    );

    public AiAnalysisController(AiAnalysisService aiService) {
        this.aiService = aiService;
    }

    /**
     * Renders the initial AI Analysis page.
     */
    @GetMapping
    public String showAiAnalysisPage(Model model) {
        populateModelAttributes(model, null);
        model.addAttribute("pageTitle", "AI Parking Stall Analysis");
        return "ai-analysis";
    }

    /**
     * Handles image upload and runs the YOLO inference process.
     */
    @PostMapping
    public String analyzeUploadedImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "conf", required = false) Double conf,
            Model model) {

        populateModelAttributes(model, conf);
        model.addAttribute("pageTitle", "AI Parking Stall Analysis");

        AiAnalysisResultDto result = aiService.analyzeImage(file, conf);

        if ("ERROR".equals(result.getStatus())) {
            model.addAttribute("errorMessage", result.getErrorMessage());
        } else {
            model.addAttribute("analysisResult", result);
        }

        return "ai-analysis";
    }

    /**
     * Serves generated annotated result images and original uploaded images securely.
     */
    @GetMapping("/image/{filename:.+}")
    public ResponseEntity<byte[]> getAnnotatedImage(@PathVariable String filename) {
        try {
            byte[] imageBytes = aiService.getResultImageBytes(filename);
            HttpHeaders headers = new HttpHeaders();
            String lower = filename.toLowerCase();
            if (lower.endsWith(".png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (lower.endsWith(".webp")) {
                headers.setContentType(MediaType.valueOf("image/webp"));
            } else {
                headers.setContentType(MediaType.IMAGE_JPEG);
            }
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Renders the persistent AI Analysis History page showing previously saved analyses, newest first.
     */
    @GetMapping("/history")
    public String showAiAnalysisHistory(Model model) {
        List<com.parking.dashboard.model.AiAnalysisHistory> historyList = aiService.getAllHistory();
        model.addAttribute("historyList", historyList);
        model.addAttribute("pageTitle", "AI Analysis History");
        return "ai-history";
    }

    /**
     * Displays an individual saved analysis record in detail.
     */
    @GetMapping("/history/{id}")
    public String showAiAnalysisHistoryDetail(@PathVariable Long id, Model model) {
        Optional<com.parking.dashboard.model.AiAnalysisHistory> recordOpt = aiService.getHistoryById(id);
        if (recordOpt.isEmpty()) {
            return "redirect:/ai-analysis/history";
        }
        model.addAttribute("record", recordOpt.get());
        model.addAttribute("pageTitle", "Analysis Result #" + id);
        return "ai-history-detail";
    }

    private void populateModelAttributes(Model model, Double selectedConf) {
        model.addAttribute("modelAvailable", aiService.isModelAvailable());
        model.addAttribute("modelMetrics", MODEL_METRICS);
        double activeConf = (selectedConf != null && selectedConf >= 0.01 && selectedConf <= 1.0)
                ? selectedConf
                : aiService.getDefaultConf();
        model.addAttribute("defaultConf", aiService.getDefaultConf());
        model.addAttribute("selectedConf", activeConf);
    }
}
