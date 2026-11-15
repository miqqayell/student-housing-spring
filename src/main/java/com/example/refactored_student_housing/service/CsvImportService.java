package com.example.refactored_student_housing.service;

import com.example.refactored_student_housing.dto.StudentImportRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Week 12 - bulk import of students from a CSV file.
 *
 * Expected format (one record per line; an optional header line is skipped):
 *   name,birthDate(yyyy-MM-dd),yearOfStudy,gpa,international
 *
 * {@link #importStudents(String)} is {@code @Async} and runs on the dedicated
 * "csvImportExecutor" pool (see
 * {@link com.example.refactored_student_housing.config.AsyncCachingConfig}), so
 * the HTTP request thread returns to the browser immediately.
 *
 * This service only parses text; creating and persisting the students is
 * delegated to {@link StudentService}, which owns the transaction and the cache
 * eviction.
 */
@Service
public class CsvImportService {

    private static final Logger log = LoggerFactory.getLogger(CsvImportService.class);
    private static final int EXPECTED_COLUMNS = 5;

    private final StudentService studentService;

    public CsvImportService(StudentService studentService) {
        this.studentService = studentService;
    }

    @Async("csvImportExecutor")
    public void importStudents(String csv) {
        List<StudentImportRow> rows = new ArrayList<>();
        int skipped = 0;

        for (String raw : csv.split("\\r?\\n")) {
            String line = raw.trim();
            if (line.isEmpty() || isHeader(line)) {
                continue;
            }
            StudentImportRow row = parseLine(line);
            if (row == null) {
                skipped++;
            } else {
                rows.add(row);
            }
        }

        int created = rows.isEmpty() ? 0 : studentService.importStudents(rows);
        log.info("CSV import finished on thread '{}': created={} skipped={}",
                Thread.currentThread().getName(), created, skipped);
    }

    private static boolean isHeader(String line) {
        return line.toLowerCase().startsWith("name,");
    }

    private StudentImportRow parseLine(String line) {
        String[] cols = line.split(",");
        if (cols.length < EXPECTED_COLUMNS) {
            log.warn("CSV: skipping line with {} column(s), expected {}: '{}'", cols.length, EXPECTED_COLUMNS, line);
            return null;
        }
        try {
            return new StudentImportRow(
                    cols[0].trim(),
                    LocalDate.parse(cols[1].trim()),
                    Integer.parseInt(cols[2].trim()),
                    Double.parseDouble(cols[3].trim()),
                    Boolean.parseBoolean(cols[4].trim()));
        } catch (RuntimeException ex) {
            log.warn("CSV: skipping malformed line '{}': {}", line, ex.getMessage());
            return null;
        }
    }
}
