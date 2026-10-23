package com.parking.dashboard.controller;

import com.parking.dashboard.repository.ParkingEventRepository;
import com.parking.dashboard.repository.ParkingLocationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AI Parking Stall Analysis web controller and service.
 * Verifies route rendering, file validations, path traversal protection,
 * and confirms strict database isolation (zero mutation of municipal records).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AiAnalysisController Integration Tests")
class AiAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParkingLocationRepository locationRepository;

    @Autowired
    private ParkingEventRepository eventRepository;

    @Autowired
    private com.parking.dashboard.repository.AiAnalysisHistoryRepository historyRepository;

    @Test
    @DisplayName("GET /ai-analysis should render page with disclaimer and benchmark metrics")
    void getAiAnalysisPage_RendersSuccessfully() throws Exception {
        mockMvc.perform(get("/ai-analysis"))
                .andExpect(status().isOk())
                .andExpect(view().name("ai-analysis"))
                .andExpect(model().attributeExists("modelMetrics"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(content().string(containsString("AI Parking Stall Analysis")))
                .andExpect(content().string(containsString("Academic AI Demonstration")))
                .andExpect(content().string(containsString("PKLot v2 640")));
    }

    @Test
    @DisplayName("POST /ai-analysis with empty file should reject and display friendly error")
    void postAiAnalysis_EmptyFile_ReturnsErrorMessage() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/ai-analysis").file(emptyFile))
                .andExpect(status().isOk())
                .andExpect(view().name("ai-analysis"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(content().string(containsString("Please select a parking surveillance image")));
    }

    @Test
    @DisplayName("POST /ai-analysis with invalid extension should reject unsupported formats")
    void postAiAnalysis_InvalidExtension_RejectsFormat() throws Exception {
        MockMultipartFile textFile = new MockMultipartFile("file", "test.txt", "text/plain", "sample text content".getBytes());

        mockMvc.perform(multipart("/ai-analysis").file(textFile))
                .andExpect(status().isOk())
                .andExpect(view().name("ai-analysis"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(content().string(containsString("Unsupported format: .txt")));
    }

    @Test
    @DisplayName("POST /ai-analysis with corrupted image data should fail raster parse")
    void postAiAnalysis_CorruptedImage_RejectsData() throws Exception {
        MockMultipartFile corruptedFile = new MockMultipartFile("file", "corrupted.jpg", "image/jpeg", new byte[]{0, 1, 2, 3, 4, 5});

        mockMvc.perform(multipart("/ai-analysis").file(corruptedFile))
                .andExpect(status().isOk())
                .andExpect(view().name("ai-analysis"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(content().string(containsString("could not be parsed as a valid image")));
    }

    @Test
    @DisplayName("GET /ai-analysis/image/ with path traversal should return 404 Not Found")
    void getImage_PathTraversal_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/ai-analysis/image/..%2F..%2Fsecret.txt"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/ai-analysis/image/non_existent.jpg"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("STRICT DATA SAFETY: AI analysis execution must NEVER alter municipal parking records")
    void postAiAnalysis_NeverMutatesMunicipalDatabase() throws Exception {
        long initialLocationCount = locationRepository.count();
        long initialEventCount = eventRepository.count();

        // Generate valid small image in memory
        BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(0, 0, 128, 128);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        MockMultipartFile validFile = new MockMultipartFile("file", "test_frame.jpg", "image/jpeg", baos.toByteArray());

        mockMvc.perform(multipart("/ai-analysis")
                        .file(validFile)
                        .param("conf", "0.01"))
                .andExpect(status().isOk());

        // Assert zero mutation of municipal records
        assertEquals(initialLocationCount, locationRepository.count(), "Municipal location count must remain unchanged");
        assertEquals(initialEventCount, eventRepository.count(), "Municipal event records must remain unchanged");
    }

    @Test
    @DisplayName("GET /ai-analysis/history should render history log page")
    void getAiAnalysisHistoryPage_RendersSuccessfully() throws Exception {
        mockMvc.perform(get("/ai-analysis/history"))
                .andExpect(status().isOk())
                .andExpect(view().name("ai-history"))
                .andExpect(model().attributeExists("historyList"))
                .andExpect(model().attributeExists("pageTitle"))
                .andExpect(content().string(containsString("AI Analysis Result History")));
    }

    @Test
    @DisplayName("GET /ai-analysis/history/{id} with non-existent ID should redirect to history list")
    void getAiAnalysisHistoryDetail_NonExistent_Redirects() throws Exception {
        mockMvc.perform(get("/ai-analysis/history/999999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/ai-analysis/history"));
    }

    @Test
    @DisplayName("GET /ai-analysis/history/{id} with valid record should render detail view")
    void getAiAnalysisHistoryDetail_ValidRecord_RendersDetail() throws Exception {
        com.parking.dashboard.model.AiAnalysisHistory history = new com.parking.dashboard.model.AiAnalysisHistory(
                "test_original.png",
                "data/ai_results/test_original.png",
                "test_annotated.jpg",
                "data/ai_results/test_annotated.jpg",
                "Calibrated Layout",
                1250L,
                0.25,
                15,
                27,
                12,
                15,
                55.6,
                java.time.LocalDateTime.now(),
                true,
                null
        );
        com.parking.dashboard.model.AiAnalysisHistory saved = historyRepository.save(history);

        mockMvc.perform(get("/ai-analysis/history/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("ai-history-detail"))
                .andExpect(model().attributeExists("record"))
                .andExpect(content().string(containsString("Saved Analysis Record")))
                .andExpect(content().string(containsString("Calibrated Layout")));
    }
}
