package com.singgih.reportengineservice.controller;

import com.singgih.reportengineservice.dto.ApiResponse;
import com.singgih.reportengineservice.dto.ErrorCode;
import com.singgih.reportengineservice.dto.ReportRequest;
import com.singgih.reportengineservice.model.Report;
import com.singgih.reportengineservice.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Report", description = "API untuk manajemen report")
@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(
            summary = "Get all reports",
            description = "Mengambil seluruh data report yang tersedia.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "SGH-000 — Berhasil",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "500",
                            description = "SGH-500 — Internal server error",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiResponse.class)))
            })
    @GetMapping
    public ResponseEntity<ApiResponse<List<Report>>> getAll() {
        log.info("Fetching all reports");
        List<Report> reports = reportService.findAll();
        log.debug("Found {} reports", reports.size());
        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    @Operation(
            summary = "Get report by ID",
            description = "Mengambil satu report berdasarkan ID.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "SGH-000 — Berhasil",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "SGH-002 — Data tidak ditemukan",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiResponse.class)))
            })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Report>> getById(
            @Parameter(description = "ID report", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id) {
        log.info("Fetching report id={}", id);
        return reportService.findById(id)
                .map(report -> ResponseEntity.ok(ApiResponse.<Report>success(report)))
                .orElseGet(() -> {
                    log.warn("Report not found id={}", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error(ErrorCode.NOT_FOUND));
                });
    }

    @Operation(
            summary = "Create report",
            description = "Membuat report baru.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "201",
                            description = "SGH-000 — Report berhasil dibuat",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "SGH-001 — Validation error",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiResponse.class)))
            })
    @PostMapping
    public ResponseEntity<ApiResponse<Report>> create(@Valid @RequestBody ReportRequest request) {
        log.info("Creating report title={} type={}", request.getTitle(), request.getType());
        Report report = reportService.create(request);
        log.info("Report created id={}", report.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(report));
    }

    @Operation(
            summary = "Update report",
            description = "Mengubah data report yang sudah ada.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "SGH-000 — Report berhasil diubah",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "SGH-001 — Validation error",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "SGH-002 — Data tidak ditemukan",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiResponse.class)))
            })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Report>> update(
            @Parameter(description = "ID report", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id,
            @Valid @RequestBody ReportRequest request) {
        log.info("Updating report id={}", id);
        return reportService.update(id, request)
                .map(report -> {
                    log.info("Report updated id={}", report.getId());
                    return ResponseEntity.ok(ApiResponse.<Report>success(report));
                })
                .orElseGet(() -> {
                    log.warn("Report not found for update id={}", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error(ErrorCode.NOT_FOUND));
                });
    }

    @Operation(
            summary = "Delete report",
            description = "Menghapus report berdasarkan ID.",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "SGH-000 — Report berhasil dihapus",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiResponse.class))),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "404",
                            description = "SGH-002 — Data tidak ditemukan",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ApiResponse.class)))
            })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "ID report", required = true, example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id) {
        log.info("Deleting report id={}", id);
        if (reportService.delete(id)) {
            log.info("Report deleted id={}", id);
            return ResponseEntity.ok(ApiResponse.success(null));
        }
        log.warn("Report not found for delete id={}", id);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ErrorCode.NOT_FOUND));
    }

}
