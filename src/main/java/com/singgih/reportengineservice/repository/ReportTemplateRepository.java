package com.singgih.reportengineservice.repository;

import com.singgih.reportengineservice.config.CacheConfig;
import com.singgih.reportengineservice.dto.report.ReportType;
import com.singgih.reportengineservice.entity.ReportTemplate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportTemplateRepository extends JpaRepository<ReportTemplate, Long> {

    /**
     * Lookup template + base template dalam satu query (JOIN FETCH mencegah LazyInitializationException
     * saat entity dipakai dari cache setelah keluar dari transaction).
     * Hasil di-cache per kombinasi templateName + reportType.
     */
    @Query("""
            SELECT rt FROM ReportTemplate rt
            LEFT JOIN FETCH rt.baseTemplate
            WHERE rt.templateName = :templateName
              AND rt.reportType   = :reportType
            """)
    @Cacheable(value = CacheConfig.REPORT_TEMPLATES,
               key   = "#templateName + ':' + #reportType.name()")
    Optional<ReportTemplate> findByTemplateNameAndReportType(
            @Param("templateName") String templateName,
            @Param("reportType")   ReportType reportType);
}
