package com.singgih.reportengineservice.repository;

import com.singgih.reportengineservice.entity.BaseReportTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BaseReportTemplateRepository extends JpaRepository<BaseReportTemplate, Long> {

    Optional<BaseReportTemplate> findByBaseTemplateName(String baseTemplateName);
}
