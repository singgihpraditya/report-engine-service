package com.singgih.reportengineservice.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    SUCCESS("SGH-000", HttpStatus.OK,
            "Success",
            "Berhasil"),

    CREATED("SGH-000", HttpStatus.CREATED,
            "Data created successfully",
            "Data berhasil dibuat"),

    VALIDATION_ERROR("SGH-001", HttpStatus.BAD_REQUEST,
            "Validation error",
            "Kesalahan validasi"),

    NOT_FOUND("SGH-002", HttpStatus.NOT_FOUND,
            "Resource not found",
            "Data tidak ditemukan"),

    BUSINESS_RULE_VIOLATION("SGH-003", HttpStatus.CONFLICT,
            "Business rule violation",
            "Pelanggaran aturan bisnis"),

    TEMPLATE_NOT_FOUND("SGH-004", HttpStatus.NOT_FOUND,
            "Report template not found",
            "Template report tidak ditemukan"),

    REPORT_GENERATION_FAILED("SGH-005", HttpStatus.INTERNAL_SERVER_ERROR,
            "Report generation failed",
            "Gagal membuat report"),

    UNAUTHORIZED("SGH-401", HttpStatus.UNAUTHORIZED,
            "Unauthorized",
            "Tidak diotorisasi"),

    FORBIDDEN("SGH-403", HttpStatus.FORBIDDEN,
            "Forbidden",
            "Akses ditolak"),

    INTERNAL_SERVER_ERROR("SGH-500", HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal server error",
            "Kesalahan server internal");

    private final String code;
    private final HttpStatus httpStatus;
    private final String englishMessage;
    private final String indonesianMessage;

}
