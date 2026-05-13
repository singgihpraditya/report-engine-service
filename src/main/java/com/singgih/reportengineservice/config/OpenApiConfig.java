package com.singgih.reportengineservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Report Engine Service API")
                        .description("""
                                REST API untuk Report Engine Service.

                                **Pola Response:**
                                Semua endpoint menggunakan envelope `ApiResponse<T>`:
                                ```json
                                {
                                  "error_schema": {
                                    "error_code": "SGH-000",
                                    "error_message": { "english": "...", "indonesian": "..." }
                                  },
                                  "output_schema": { ... }
                                }
                                ```

                                **Error Codes:**
                                | Kode | HTTP | Kondisi |
                                |------|------|---------|
                                | SGH-000 | 200/201 | Sukses |
                                | SGH-001 | 400 | Validation error |
                                | SGH-002 | 404 | Resource not found |
                                | SGH-003 | 409 | Business rule violation |
                                | SGH-401 | 401 | Unauthorized |
                                | SGH-403 | 403 | Forbidden |
                                | SGH-500 | 500 | Internal server error |
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Singgih Praditya")
                                .email("singgihpraditya@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Local Development Server")));
    }

}
