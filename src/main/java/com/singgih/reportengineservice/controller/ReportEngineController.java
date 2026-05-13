package com.singgih.reportengineservice.controller;

import com.singgih.reportengineservice.dto.report.GenerateReportRequest;
import com.singgih.reportengineservice.dto.report.ReportType;
import com.singgih.reportengineservice.service.ReportEngineService;
import com.singgih.reportengineservice.service.ReportEngineService.ReportResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/report-engine")
@RequiredArgsConstructor
@Tag(name = "Report Engine", description = "Generate laporan dalam format PDF, DOCX, CSV, dan XLSX")
public class ReportEngineController {

    private final ReportEngineService reportEngineService;

    @PostMapping("/generate/{type}")
    @Operation(
            summary = "Generate report",
            description = "Membuat file report berdasarkan template Thymeleaf. " +
                          "Jika file dengan nama yang sama sudah ada, akan dikembalikan dari cache."
    )
    public ResponseEntity<Resource> generate(
            @Parameter(description = "Format output: PDF, DOCX, CSV, XLSX")
            @PathVariable ReportType type,
            @Valid @RequestBody GenerateReportRequest request) {

        ReportResult result = reportEngineService.generate(type, request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(resolveMediaType(type));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(result.metadata().getFileName())
                .build());
        headers.add("X-Cached", String.valueOf(result.metadata().isCached()));

        return ResponseEntity.ok()
                .headers(headers)
                .body(result.resource());
    }

    private MediaType resolveMediaType(ReportType type) {
        return switch (type) {
            case PDF  -> MediaType.APPLICATION_PDF;
            case DOCX -> MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            case CSV  -> MediaType.parseMediaType("text/csv");
            case XLSX -> MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        };
    }
}
