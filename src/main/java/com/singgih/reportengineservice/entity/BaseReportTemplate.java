package com.singgih.reportengineservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_base_report_template")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseReportTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "base_template_name", nullable = false, unique = true)
    private String baseTemplateName;

    /** Header and footer HTML content with {{BODY}} placeholder */
    @Column(name = "template", columnDefinition = "CLOB", nullable = false)
    private String template;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "last_updated_date")
    private LocalDateTime lastUpdatedDate;

    @PrePersist
    void onCreate() {
        createdDate = LocalDateTime.now();
        lastUpdatedDate = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        lastUpdatedDate = LocalDateTime.now();
    }
}
