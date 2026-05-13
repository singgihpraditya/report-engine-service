package com.singgih.reportengineservice.entity;

import com.singgih.reportengineservice.dto.report.ReportType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_report_template",
        uniqueConstraints = @UniqueConstraint(columnNames = {"template_name", "report_type"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_template_id")
    private BaseReportTemplate baseTemplate;

    @Column(name = "template_name", nullable = false)
    private String templateName;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 10)
    private ReportType reportType;

    /** Body HTML or CSV text content processed by Thymeleaf */
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
