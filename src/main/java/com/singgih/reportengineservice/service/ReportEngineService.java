package com.singgih.reportengineservice.service;

import com.singgih.reportengineservice.config.ReportStorageProperties;
import com.singgih.reportengineservice.dto.report.GenerateReportRequest;
import com.singgih.reportengineservice.dto.report.GenerateReportResponse;
import com.singgih.reportengineservice.dto.report.ReportType;
import com.singgih.reportengineservice.entity.ReportHistory;
import com.singgih.reportengineservice.entity.ReportTemplate;
import com.singgih.reportengineservice.exception.ReportGenerationException;
import com.singgih.reportengineservice.exception.TemplateNotFoundException;
import com.singgih.reportengineservice.repository.ReportHistoryRepository;
import com.singgih.reportengineservice.repository.ReportTemplateRepository;
import com.singgih.reportengineservice.service.report.ReportGeneratorStrategy;
import com.singgih.reportengineservice.service.report.TemplateAssemblerService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportEngineService {

    private final ReportTemplateRepository templateRepository;
    private final ReportHistoryRepository historyRepository;
    private final TemplateAssemblerService assembler;
    private final ReportStorageProperties storageProperties;
    private final List<ReportGeneratorStrategy> generators;

    private Map<ReportType, ReportGeneratorStrategy> generatorMap;

    @PostConstruct
    void init() {
        generatorMap = generators.stream()
                .collect(Collectors.toMap(ReportGeneratorStrategy::getSupportedType, Function.identity()));
    }

    /**
     * Generates a report file for the given type.
     * If a file with the same name already exists in history and on disk, it is returned from cache.
     *
     * @return pair of [Resource bytes, GenerateReportResponse metadata]
     */
    @Transactional
    public ReportResult generate(ReportType type, GenerateReportRequest request) {
        String fullFileName = request.getFileName() + "." + type.name().toLowerCase();

        Optional<ReportHistory> existing = historyRepository.findByFileName(fullFileName);
        if (existing.isPresent()) {
            Path cachedPath = resolveFilePath(fullFileName);
            if (Files.exists(cachedPath)) {
                log.info("Cache hit — returning existing file: {}", fullFileName);
                return new ReportResult(readFile(cachedPath), buildResponse(fullFileName, type, true));
            }
            log.info("Cache entry found but file missing on disk — regenerating: {}", fullFileName);
        }

        ReportTemplate template = templateRepository
                .findByTemplateNameAndReportType(request.getTemplateName(), type)
                .orElseThrow(() -> new TemplateNotFoundException(request.getTemplateName(), type.name()));

        ReportGeneratorStrategy generator = generatorMap.get(type);
        String assembled = assembler.assemble(template);
        byte[] fileBytes = generator.generate(assembled, mergeWithSystemParams(request.getReportParam()));

        saveFile(fullFileName, fileBytes);
        saveHistory(template, fullFileName, existing);

        log.info("Report generated and saved: {}", fullFileName);
        return new ReportResult(new ByteArrayResource(fileBytes), buildResponse(fullFileName, type, false));
    }

    private void saveFile(String fileName, byte[] bytes) {
        try {
            Path dir = Paths.get(storageProperties.getPath());
            Files.createDirectories(dir);
            Files.write(dir.resolve(fileName), bytes);
        } catch (IOException e) {
            throw new ReportGenerationException("Gagal menyimpan file: " + fileName, e);
        }
    }

    private Resource readFile(Path path) {
        try {
            return new ByteArrayResource(Files.readAllBytes(path));
        } catch (IOException e) {
            throw new ReportGenerationException("Gagal membaca file: " + path.getFileName(), e);
        }
    }

    private void saveHistory(ReportTemplate template, String fileName, Optional<ReportHistory> existing) {
        ReportHistory history = existing.orElseGet(() ->
                ReportHistory.builder()
                        .reportTemplate(template)
                        .fileName(fileName)
                        .build());
        historyRepository.save(history);
    }

    /** Adds system-level variables (print_date, dll) tanpa mengubah params dari request user. */
    private Map<String, Object> mergeWithSystemParams(Map<String, Object> requestParams) {
        Map<String, Object> merged = new HashMap<>(requestParams);
        merged.put("print_date", LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        return merged;
    }

    private Path resolveFilePath(String fileName) {
        return Paths.get(storageProperties.getPath(), fileName);
    }

    private GenerateReportResponse buildResponse(String fileName, ReportType type, boolean cached) {
        return GenerateReportResponse.builder()
                .fileName(fileName)
                .reportType(type)
                .cached(cached)
                .build();
    }

    /** Wraps both the file bytes and metadata so the controller gets both in one call. */
    public record ReportResult(Resource resource, GenerateReportResponse metadata) {}
}
