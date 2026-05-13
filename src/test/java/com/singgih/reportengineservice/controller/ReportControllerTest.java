package com.singgih.reportengineservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.singgih.reportengineservice.dto.ReportRequest;
import com.singgih.reportengineservice.model.Report;
import com.singgih.reportengineservice.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReportService reportService;

    @Test
    void getAll_shouldReturnSuccessResponse() throws Exception {
        Report report = Report.builder()
                .id("test-id")
                .title("Test Report")
                .type("PDF")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        when(reportService.findAll()).thenReturn(List.of(report));

        mockMvc.perform(get("/api/v1/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_schema.error_code").value("SGH-000"))
                .andExpect(jsonPath("$.output_schema[0].id").value("test-id"));
    }

    @Test
    void getById_shouldReturnReport_whenExists() throws Exception {
        Report report = Report.builder()
                .id("test-id")
                .title("Test Report")
                .type("PDF")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        when(reportService.findById("test-id")).thenReturn(Optional.of(report));

        mockMvc.perform(get("/api/v1/reports/test-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.error_schema.error_code").value("SGH-000"))
                .andExpect(jsonPath("$.output_schema.id").value("test-id"));
    }

    @Test
    void getById_shouldReturnSGH002_whenNotFound() throws Exception {
        when(reportService.findById("missing-id")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/reports/missing-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_schema.error_code").value("SGH-002"))
                .andExpect(jsonPath("$.error_schema.error_message.english").value("Resource not found"))
                .andExpect(jsonPath("$.error_schema.error_message.indonesian").value("Data tidak ditemukan"));
    }

    @Test
    void create_shouldReturnCreated() throws Exception {
        ReportRequest request = ReportRequest.builder()
                .title("New Report")
                .type("EXCEL")
                .build();

        Report created = Report.builder()
                .id("new-id")
                .title("New Report")
                .type("EXCEL")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        when(reportService.create(any(ReportRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.error_schema.error_code").value("SGH-000"))
                .andExpect(jsonPath("$.output_schema.id").value("new-id"));
    }

    @Test
    void create_shouldReturnSGH001_whenInvalidRequest() throws Exception {
        ReportRequest request = ReportRequest.builder().build();

        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_schema.error_code").value("SGH-001"))
                .andExpect(jsonPath("$.output_schema.title").exists());
    }

    @Test
    void delete_shouldReturnSGH002_whenNotFound() throws Exception {
        when(reportService.delete("missing-id")).thenReturn(false);

        mockMvc.perform(delete("/api/v1/reports/missing-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_schema.error_code").value("SGH-002"));
    }

}
