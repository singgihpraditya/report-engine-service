package com.singgih.reportengineservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Standard response envelope untuk semua endpoint")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    @Schema(description = "Informasi error / status response")
    @JsonProperty("error_schema")
    private ErrorSchema errorSchema;

    @Schema(description = "Data hasil response (null jika error)")
    @JsonProperty("output_schema")
    private T outputSchema;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .errorSchema(ErrorSchema.of(ErrorCode.SUCCESS))
                .outputSchema(data)
                .build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .errorSchema(ErrorSchema.of(ErrorCode.CREATED))
                .outputSchema(data)
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .errorSchema(ErrorSchema.of(errorCode))
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, T data) {
        return ApiResponse.<T>builder()
                .errorSchema(ErrorSchema.of(errorCode))
                .outputSchema(data)
                .build();
    }

}
