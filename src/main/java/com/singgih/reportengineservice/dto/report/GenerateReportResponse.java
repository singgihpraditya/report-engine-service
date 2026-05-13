package com.singgih.reportengineservice.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "Metadata file report yang dihasilkan")
public class GenerateReportResponse {

    @Schema(description = "Nama file output", example = "laporan-keuangan-jan-2024.pdf")
    private String fileName;

    @Schema(description = "Tipe report yang digenerate")
    private ReportType reportType;

    @Schema(description = "Apakah file diambil dari cache (tidak digenerate ulang)")
    private boolean cached;
}
