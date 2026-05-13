package com.singgih.reportengineservice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "Report entity")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Schema(description = "Unique identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    @Schema(description = "Report title", example = "Monthly Sales Report")
    private String title;

    @Schema(description = "Report type", example = "PDF", allowableValues = {"PDF", "EXCEL", "CSV"})
    private String type;

    @Schema(description = "Report processing status", example = "PENDING",
            allowableValues = {"PENDING", "PROCESSING", "DONE", "FAILED"})
    private String status;

    @Schema(description = "Creation timestamp", example = "2026-05-13 10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp", example = "2026-05-13 10:05:00")
    private LocalDateTime updatedAt;

}
