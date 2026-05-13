package com.singgih.reportengineservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Informasi error / status response")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorSchema {

    @Schema(description = "Kode status response", example = "SGH-000")
    @JsonProperty("error_code")
    private String errorCode;

    @Schema(description = "Pesan status response")
    @JsonProperty("error_message")
    private ErrorMessage errorMessage;

    public static ErrorSchema of(ErrorCode errorCode) {
        return ErrorSchema.builder()
                .errorCode(errorCode.getCode())
                .errorMessage(ErrorMessage.builder()
                        .english(errorCode.getEnglishMessage())
                        .indonesian(errorCode.getIndonesianMessage())
                        .build())
                .build();
    }

}
