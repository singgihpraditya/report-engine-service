package com.singgih.reportengineservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Request payload untuk membuat atau mengubah report")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {

    @Schema(description = "Judul report", example = "Monthly Sales Report", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Title is required")
    private String title;

    @Schema(description = "Tipe report", example = "PDF", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"PDF", "EXCEL", "CSV"})
    @NotBlank(message = "Type is required")
    private String type;

}
