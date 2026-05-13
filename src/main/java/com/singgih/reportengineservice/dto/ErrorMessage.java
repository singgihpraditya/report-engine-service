package com.singgih.reportengineservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Pesan error dalam dua bahasa")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorMessage {

    @Schema(description = "Pesan dalam Bahasa Inggris", example = "Success")
    private String english;

    @Schema(description = "Pesan dalam Bahasa Indonesia", example = "Berhasil")
    private String indonesian;

}
