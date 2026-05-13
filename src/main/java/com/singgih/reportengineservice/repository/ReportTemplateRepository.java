package com.singgih.reportengineservice.repository;

import com.singgih.reportengineservice.dto.report.ReportType;
import com.singgih.reportengineservice.entity.ReportTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportTemplateRepository extends JpaRepository<ReportTemplate, Long> {

    Optional<ReportTemplate> findByTemplateNameAndReportType(String templateName, ReportType reportType);
}
