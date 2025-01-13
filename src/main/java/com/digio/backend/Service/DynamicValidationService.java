package com.digio.backend.Service;

import com.digio.backend.Validate.*;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class DynamicValidationService {

    private final Map<Pattern, Function<String, String>> validationRules = new HashMap<>();

    public DynamicValidationService() {
        initializeDefaultValidationRules();
    }

    private String formatDuration(long durationMillis) {
        long seconds = durationMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return hours + " ชั่วโมง " + (minutes % 60) + " นาที";
        } else if (minutes > 0) {
            return minutes + " นาที " + (seconds % 60) + " วินาที";
        } else {
            return seconds + " วินาที";
        }
    }

    public List<String> validateExcelWithSelectedHeaders(MultipartFile file, List<String> selectedHeaders) {
        Map<Integer, StringBuilder> errorMap = new TreeMap<>();

        // เริ่มจับเวลา
        Instant startTime = Instant.now();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<String> headers = extractHeaders(sheet);

            List<Integer> selectedHeaderIndices = getSelectedHeaderIndices(headers, selectedHeaders);

            if (selectedHeaderIndices.isEmpty()) {
                throw new IllegalArgumentException("ไม่พบหัวข้อที่เลือกในไฟล์ Excel");
            }

            processRows(sheet, headers, selectedHeaderIndices, errorMap);
        } catch (IOException e) {
            throw new IllegalArgumentException("ไม่สามารถอ่านไฟล์ Excel ได้", e);
        }

        // จบจับเวลา
        Instant endTime = Instant.now();
        long durationMillis = Duration.between(startTime, endTime).toMillis();

        String timeTaken = formatDuration(durationMillis);
        System.out.println("เวลาในการประมวลผล: " + timeTaken);

        return formatErrorMap(errorMap);
    }

    public List<String> validateExcel(MultipartFile file) {
        Map<Integer, StringBuilder> errorMap = new TreeMap<>();

        // เริ่มจับเวลา
        Instant startTime = Instant.now();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<String> headers = extractHeaders(sheet);

            processRows(sheet, headers, null, errorMap);
        } catch (IOException e) {
            throw new IllegalArgumentException("ไม่สามารถอ่านไฟล์ Excel ได้", e);
        }

        // จบจับเวลา
        Instant endTime = Instant.now();
        long durationMillis = Duration.between(startTime, endTime).toMillis();

        String timeTaken = formatDuration(durationMillis);
        System.out.println("เวลาในการประมวลผล: " + timeTaken);

        return formatErrorMap(errorMap);
    }

    private List<String> extractHeaders(Sheet sheet) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalArgumentException("ไม่มีแถวหัวข้อในไฟล์ Excel");
        }

        return StreamSupport.stream(headerRow.spliterator(), false)
                .map(cell -> cell.getStringCellValue().trim().toLowerCase())
                .collect(Collectors.toList());
    }

    private List<Integer> getSelectedHeaderIndices(List<String> headers, List<String> selectedHeaders) {
        return selectedHeaders.stream()
                .map(headers::indexOf)
                .filter(index -> index >= 0)
                .collect(Collectors.toList());
    }

    private void processRows(Sheet sheet, List<String> headers, List<Integer> selectedHeaderIndices, Map<Integer, StringBuilder> errorMap) {
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            StringBuilder errorBuilder = new StringBuilder();
            if (selectedHeaderIndices != null) {
                validateRowWithSelectedHeaders(row, headers, selectedHeaderIndices, errorBuilder);
            } else {
                validateRow(row, headers, errorBuilder);
            }

            if (!errorBuilder.isEmpty()) {
                errorMap.put(row.getRowNum() + 1, errorBuilder);
            }
        }
    }

    private void validateRowWithSelectedHeaders(Row row, List<String> headers, List<Integer> selectedHeaderIndices, StringBuilder errorBuilder) {
        selectedHeaderIndices.forEach(index -> {
            String header = headers.get(index);
            String cellValue = getCellValue(row.getCell(index));
            applyValidationRules(header, cellValue, errorBuilder);
        });
    }

    private void validateRow(Row row, List<String> headers, StringBuilder errorBuilder) {
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            String cellValue = getCellValue(row.getCell(i));
            applyValidationRules(header, cellValue, errorBuilder);
        }
    }

    private void applyValidationRules(String header, String cellValue, StringBuilder errorBuilder) {
        boolean matched = validationRules.entrySet().stream()
                .anyMatch(entry -> {
                    if (entry.getKey().matcher(header).matches()) {
                        String error = entry.getValue().apply(cellValue);
                        if (error != null) {
                            appendError(errorBuilder, error);
                        }
                        return true;
                    }
                    return false;
                });

        if (!matched) {
            appendError(errorBuilder, "พบหัวข้อที่ไม่รู้จัก: " + header);
        }
    }

    private void initializeDefaultValidationRules() {
        validationRules.put(Pattern.compile("^(ชื่อ|name).*"), NameValidator::validate);
        validationRules.put(Pattern.compile("^(อีเมล|email).*$"), EmailValidate::validate);
        validationRules.put(Pattern.compile("^(บัตรประชาชน|citizenid).*$"), CitizenIdValidator::validate);
        validationRules.put(Pattern.compile("^(เบอร์โทร|phone).*$"), PhoneValidator::validate);
        validationRules.put(Pattern.compile("^(ที่อยู่|address).*$"), AddressValidator::validate);
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    private void appendError(StringBuilder errorBuilder, String errorMessage) {
        if (!errorBuilder.isEmpty()) {
            errorBuilder.append(", ");
        }
        errorBuilder.append(errorMessage);
    }

    private List<String> formatErrorMap(Map<Integer, StringBuilder> errorMap) {
        return errorMap.entrySet().stream()
                .map(entry -> "แถวที่ " + entry.getKey() + ": " + entry.getValue().toString())
                .collect(Collectors.toList());
    }
}

