package com.singgih.reportengineservice.init;

import com.singgih.reportengineservice.dto.report.ReportType;
import com.singgih.reportengineservice.entity.BaseReportTemplate;
import com.singgih.reportengineservice.entity.ReportTemplate;
import com.singgih.reportengineservice.repository.BaseReportTemplateRepository;
import com.singgih.reportengineservice.repository.ReportTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

/**
 * Seed templates dipindah ke src/main/resources/db/data.sql
 * (H2 MERGE INTO, dijalankan otomatis via spring.sql.init.data-locations).
 *
 * Kelas ini dinonaktifkan — @Component dihapus agar tidak berjalan.
 * Simpan sebagai referensi atau hapus sesuai kebutuhan.
 */
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final String BASE_PDF  = "base-pdf";
    private static final String BASE_DOCX = "base-docx";
    private static final String BASE_CSV  = "base-csv";
    private static final String BASE_XLSX = "base-xlsx";

    private static final String FINANCIAL_TEMPLATE = "financial-report";

    private final BaseReportTemplateRepository baseTemplateRepository;
    private final ReportTemplateRepository     reportTemplateRepository;

    @Override
    public void run(String... args) {
        BaseReportTemplate basePdf  = upsertBase(BASE_PDF,  htmlBaseTemplate());
        BaseReportTemplate baseDocx = upsertBase(BASE_DOCX, htmlBaseTemplate());
        BaseReportTemplate baseCsv  = upsertBase(BASE_CSV,  csvBaseTemplate());
        BaseReportTemplate baseXlsx = upsertBase(BASE_XLSX, xlsxBaseTemplate());

        upsertReport(basePdf,  FINANCIAL_TEMPLATE, ReportType.PDF,  financialPdfBody());
        upsertReport(baseDocx, FINANCIAL_TEMPLATE, ReportType.DOCX, financialDocxBody());
        upsertReport(baseCsv,  FINANCIAL_TEMPLATE, ReportType.CSV,  financialCsvBody());
        upsertReport(baseXlsx, FINANCIAL_TEMPLATE, ReportType.XLSX, financialXlsxBody());

        log.info("Seed templates upserted: PDF, DOCX, CSV, XLSX");
    }

    // =========================================================================
    // Base templates
    // =========================================================================

    /** Dipakai oleh PDF dan DOCX — HTML mode Thymeleaf. */
    private String htmlBaseTemplate() {
        return """
                <!DOCTYPE html>
                <html xmlns:th="http://www.thymeleaf.org">
                <head>
                  <meta charset="UTF-8"/>
                  <style>
                    body   { font-family: Arial, sans-serif; font-size: 12px; margin: 30px; }
                    hr     { border: 1px solid #333; }
                    table  { width: 100%; border-collapse: collapse; margin-top: 10px; }
                    th, td { border: 1px solid #999; padding: 6px 10px; text-align: left; }
                    th     { background-color: #e0e0e0; font-weight: bold; }
                    .header, .footer { text-align: center; }
                  </style>
                </head>
                <body>
                  <div class="header">
                    <hr/>
                    <h2>PT Maju Sentosa</h2>
                    <hr/>
                  </div>

                  {{BODY}}

                  <div class="footer">
                    <hr/>
                    <p th:text="'Dicetak tanggal ' + ${print_date}">Dicetak tanggal</p>
                    <hr/>
                  </div>
                </body>
                </html>
                """;
    }

    /**
     * Dipakai oleh CSV — TEXT mode Thymeleaf.
     * Baris tanpa koma adalah info laporan; baris data (dengan koma) ada di {{BODY}}.
     */
    private String csvBaseTemplate() {
        return """
                PT Maju Sentosa
                Laporan Keuangan Bulan : [(${report_date})] Tahun : [(${report_year})]
                {{BODY}}
                Dicetak tanggal [(${print_date})]
                """;
    }

    /**
     * Dipakai oleh XLSX — TEXT mode Thymeleaf, pipe-delimited untuk kolom data.
     * Baris tanpa | adalah judul/footer; baris data (dengan |) ada di {{BODY}}.
     */
    private String xlsxBaseTemplate() {
        return """
                PT Maju Sentosa
                Laporan Keuangan Bulan : [(${report_date})] Tahun : [(${report_year})]
                {{BODY}}
                Dicetak tanggal [(${print_date})]
                """;
    }

    // =========================================================================
    // Body templates — financial report
    // =========================================================================

    private String financialPdfBody() {
        return """
                <div class="content">
                  <h3>Laporan Keuangan Bulan :
                    <span th:text="${report_date}">-</span>
                  </h3>
                  <p>Tahun : <span th:text="${report_year}">-</span></p>
                  <table>
                    <thead>
                      <tr>
                        <th>Tanggal</th>
                        <th>Pengeluaran</th>
                        <th>Debit/Credit</th>
                        <th>Keterangan</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr th:each="item : ${detail}">
                        <td th:text="${item['transaction_date']}">-</td>
                        <td th:text="${item['amount']}">-</td>
                        <td th:text="${item['transaction_type']}">-</td>
                        <td th:text="${item['description']}">-</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
                """;
    }

    private String financialDocxBody() {
        return """
                <div class="content">
                  <h3 th:text="'Laporan Keuangan Bulan : ' + ${report_date}">Laporan Keuangan</h3>
                  <p th:text="'Tahun : ' + ${report_year}">Tahun</p>
                  <table>
                    <thead>
                      <tr>
                        <th>Tanggal</th><th>Pengeluaran</th><th>Debit/Credit</th><th>Keterangan</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr th:each="item : ${detail}">
                        <td th:text="${item['transaction_date']}">-</td>
                        <td th:text="${item['amount']}">-</td>
                        <td th:text="${item['transaction_type']}">-</td>
                        <td th:text="${item['description']}">-</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
                """;
    }

    /**
     * Hanya berisi baris data CSV (tanpa header/footer — sudah ada di base-csv).
     * Baris pertama adalah header kolom.
     */
    private String financialCsvBody() {
        return """
                Tanggal,Pengeluaran,Debit/Credit,Keterangan
                [# th:each="item : ${detail}"][(${item['transaction_date']})],[(${item['amount']})],[(${item['transaction_type']})],[(${item['description']})]
                [/]""";
    }

    /**
     * Hanya berisi baris data XLSX pipe-delimited (tanpa judul/footer — sudah ada di base-xlsx).
     * Baris pertama adalah header kolom.
     */
    private String financialXlsxBody() {
        return """
                Tanggal|Pengeluaran|Debit/Credit|Keterangan
                [# th:each="item : ${detail}"][(${item['transaction_date']})]|[(${item['amount']})]|[(${item['transaction_type']})]|[(${item['description']})]
                [/]""";
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private BaseReportTemplate upsertBase(String name, String content) {
        BaseReportTemplate entity = baseTemplateRepository
                .findByBaseTemplateName(name)
                .orElseGet(BaseReportTemplate::new);
        entity.setBaseTemplateName(name);
        entity.setTemplate(content);
        return baseTemplateRepository.save(entity);
    }

    private void upsertReport(BaseReportTemplate base, String name, ReportType type, String body) {
        ReportTemplate entity = reportTemplateRepository
                .findByTemplateNameAndReportType(name, type)
                .orElseGet(ReportTemplate::new);
        entity.setBaseTemplate(base);
        entity.setTemplateName(name);
        entity.setReportType(type);
        entity.setTemplate(body);
        reportTemplateRepository.save(entity);
    }
}
