package com.singgih.reportengineservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.report.storage")
public class ReportStorageProperties {

    /** Directory where generated report files are stored */
    private String path = "./reports";
}
