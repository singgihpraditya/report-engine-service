package com.singgih.reportengineservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.logging")
public class LoggingProperties {

    /**
     * Panjang maksimal pesan log sebelum di-trim.
     * Configurable via app.logging.max-message-length (default: 5000).
     */
    private int maxMessageLength = 5000;

}
