-- =============================================================================
-- Report Engine Service — Seed Templates
-- =============================================================================
-- Format   : H2 MERGE INTO (upsert, dijalankan setiap startup aplikasi)
-- Edit      : ubah nilai string di bawah, restart aplikasi → perubahan langsung aktif
-- Escaping : single quote ' di dalam string SQL harus ditulis ''
--            Contoh Thymeleaf:  th:text="'teks'"  →  th:text="''teks''"
--            Contoh ekspresi:   ${item['key']}    →  ${item[''key'']}
-- =============================================================================


-- =============================================================================
-- BASE TEMPLATES
-- =============================================================================

-- base-pdf : HTML mode — header dan footer untuk format PDF
-- =============================================================================
MERGE INTO tbl_base_report_template (base_template_name, template, created_date, last_updated_date)
KEY (base_template_name)
VALUES (
    'base-pdf',
    '<!DOCTYPE html>
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
    <p th:text="''Dicetak tanggal '' + ${print_date}">Dicetak tanggal</p>
    <hr/>
  </div>
</body>
</html>',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- base-docx : HTML mode — header dan footer untuk format DOCX
-- =============================================================================
MERGE INTO tbl_base_report_template (base_template_name, template, created_date, last_updated_date)
KEY (base_template_name)
VALUES (
    'base-docx',
    '<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
  <meta charset="UTF-8"/>
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
    <p th:text="''Dicetak tanggal '' + ${print_date}">Dicetak tanggal</p>
    <hr/>
  </div>
</body>
</html>',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- base-csv : TEXT mode — header dan footer untuk format CSV
-- Tidak ada single quote di sini, sintaks [(${expr})] tidak perlu escape.
-- =============================================================================
MERGE INTO tbl_base_report_template (base_template_name, template, created_date, last_updated_date)
KEY (base_template_name)
VALUES (
    'base-csv',
    'PT Maju Sentosa
Laporan Keuangan Bulan : [(${report_date})] Tahun : [(${report_year})]
{{BODY}}
Dicetak tanggal [(${print_date})]',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);

-- base-xlsx : TEXT mode, pipe-delimited — header dan footer untuk format XLSX
-- Baris tanpa | akan di-render sebagai judul/footer (bold/italic, merged cells).
-- =============================================================================
MERGE INTO tbl_base_report_template (base_template_name, template, created_date, last_updated_date)
KEY (base_template_name)
VALUES (
    'base-xlsx',
    'PT Maju Sentosa
Laporan Keuangan Bulan : [(${report_date})] Tahun : [(${report_year})]
{{BODY}}
Dicetak tanggal [(${print_date})]',
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
);


-- =============================================================================
-- REPORT TEMPLATES : financial-report
-- =============================================================================
-- Kolom base_template_id di-resolve via subquery ke tbl_base_report_template.
-- KEY (template_name, report_type) → update jika sudah ada, insert jika belum.
-- =============================================================================

-- financial-report — PDF
-- Body HTML berisi judul laporan dan tabel data.
-- ${item[''key'']} : akses map dengan key string (single quote di-escape jadi '')
-- =============================================================================
MERGE INTO tbl_report_template (template_name, report_type, base_template_id, template, created_date, last_updated_date)
KEY (template_name, report_type)
SELECT
    'financial-report',
    'PDF',
    b.id,
    '<div class="content">
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
        <td th:text="${item[''transaction_date'']}">-</td>
        <td th:text="${item[''amount'']}">-</td>
        <td th:text="${item[''transaction_type'']}">-</td>
        <td th:text="${item[''description'']}">-</td>
      </tr>
    </tbody>
  </table>
</div>',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM tbl_base_report_template b
WHERE b.base_template_name = 'base-pdf';

-- financial-report — DOCX
-- =============================================================================
MERGE INTO tbl_report_template (template_name, report_type, base_template_id, template, created_date, last_updated_date)
KEY (template_name, report_type)
SELECT
    'financial-report',
    'DOCX',
    b.id,
    '<div class="content">
  <h3 th:text="''Laporan Keuangan Bulan : '' + ${report_date}">Laporan Keuangan</h3>
  <p th:text="''Tahun : '' + ${report_year}">Tahun</p>
  <table>
    <thead>
      <tr>
        <th>Tanggal</th><th>Pengeluaran</th><th>Debit/Credit</th><th>Keterangan</th>
      </tr>
    </thead>
    <tbody>
      <tr th:each="item : ${detail}">
        <td th:text="${item[''transaction_date'']}">-</td>
        <td th:text="${item[''amount'']}">-</td>
        <td th:text="${item[''transaction_type'']}">-</td>
        <td th:text="${item[''description'']}">-</td>
      </tr>
    </tbody>
  </table>
</div>',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM tbl_base_report_template b
WHERE b.base_template_name = 'base-docx';

-- financial-report — CSV
-- Hanya berisi baris data; header/footer ada di base-csv.
-- Baris pertama  = header kolom.
-- Thymeleaf TEXT mode: [(${item[''key'']})] untuk akses map key.
-- =============================================================================
MERGE INTO tbl_report_template (template_name, report_type, base_template_id, template, created_date, last_updated_date)
KEY (template_name, report_type)
SELECT
    'financial-report',
    'CSV',
    b.id,
    'Tanggal,Pengeluaran,Debit/Credit,Keterangan
[# th:each="item : ${detail}"][(${item[''transaction_date'']})],[(${item[''amount'']})],[(${item[''transaction_type'']})],[(${item[''description'']})]
[/]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM tbl_base_report_template b
WHERE b.base_template_name = 'base-csv';

-- financial-report — XLSX
-- Hanya berisi baris data; header/footer ada di base-xlsx.
-- Baris pertama  = header kolom (bold + background abu-abu).
-- Delimiter kolom: | (pipe)
-- =============================================================================
MERGE INTO tbl_report_template (template_name, report_type, base_template_id, template, created_date, last_updated_date)
KEY (template_name, report_type)
SELECT
    'financial-report',
    'XLSX',
    b.id,
    'Tanggal|Pengeluaran|Debit/Credit|Keterangan
[# th:each="item : ${detail}"][(${item[''transaction_date'']})]|[(${item[''amount'']})]|[(${item[''transaction_type'']})]|[(${item[''description'']})]
[/]',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM tbl_base_report_template b
WHERE b.base_template_name = 'base-xlsx';
