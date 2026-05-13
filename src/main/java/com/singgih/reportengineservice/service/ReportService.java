package com.singgih.reportengineservice.service;

import com.singgih.reportengineservice.dto.ReportRequest;
import com.singgih.reportengineservice.model.Report;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class ReportService {

    private final ConcurrentHashMap<String, Report> reportStore = new ConcurrentHashMap<>();

    public List<Report> findAll() {
        return new ArrayList<>(reportStore.values());
    }

    public Optional<Report> findById(String id) {
        return Optional.ofNullable(reportStore.get(id));
    }

    public Report create(ReportRequest request) {
        String id = UUID.randomUUID().toString();
        Report report = Report.builder()
                .id(id)
                .title(request.getTitle())
                .type(request.getType())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        reportStore.put(id, report);
        return report;
    }

    public Optional<Report> update(String id, ReportRequest request) {
        Report existing = reportStore.get(id);
        if (existing == null) {
            return Optional.empty();
        }
        Report updated = Report.builder()
                .id(id)
                .title(request.getTitle())
                .type(request.getType())
                .status(existing.getStatus())
                .createdAt(existing.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
        reportStore.put(id, updated);
        return Optional.of(updated);
    }

    public boolean delete(String id) {
        return reportStore.remove(id) != null;
    }

}
