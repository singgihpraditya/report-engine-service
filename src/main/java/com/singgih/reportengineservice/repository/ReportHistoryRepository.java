package com.singgih.reportengineservice.repository;

import com.singgih.reportengineservice.entity.ReportHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportHistoryRepository extends JpaRepository<ReportHistory, Long> {

    Optional<ReportHistory> findByFileName(String fileName);
}
