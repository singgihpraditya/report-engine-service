package com.singgih.reportengineservice.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "Request body untuk generate report")
public class GenerateReportRequest {

    @NotBlank
    @Schema(description = "Nama template yang digunakan", example = "financial-report",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateName;

    @NotBlank
    @Schema(description = "Nama file output (tanpa ekstensi)", example = "laporan-keuangan-jan-2024",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileName;

    @NotNull
    @Schema(description = "Parameter yang diteruskan ke template Thymeleaf",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<String, Object> reportParam;
}
