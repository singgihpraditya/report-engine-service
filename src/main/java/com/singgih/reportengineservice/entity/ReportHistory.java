package com.singgih.reportengineservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tbl_report_history")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_template_id", nullable = false)
    private ReportTemplate reportTemplate;

    @Column(name = "file_name", nullable = false, unique = true)
    private String fileName;

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
