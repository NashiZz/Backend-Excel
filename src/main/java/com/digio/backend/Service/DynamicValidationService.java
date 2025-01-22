package com.digio.backend.Service;

import com.digio.backend.Validate.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    public List<String> validateExcelWithSelectedHeaders(MultipartFile file, List<String> selectedHeaders) {
        Map<Integer, StringBuilder> errorMap = new TreeMap<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<String> headers = extractHeaders(sheet);

            List<String> lowercaseSelectedHeaders = selectedHeaders.stream()
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());

            List<String> unknownHeaders = lowercaseSelectedHeaders.stream()
                    .filter(header -> !headers.contains(header))
                    .toList();

            if (!unknownHeaders.isEmpty()) {
                throw new IllegalArgumentException("หัวข้อที่ไม่รู้จัก: " + String.join(", ", unknownHeaders));
            }

            List<Integer> selectedHeaderIndices = getSelectedHeaderIndices(headers, lowercaseSelectedHeaders);

            if (selectedHeaderIndices.isEmpty()) {
                throw new IllegalArgumentException("ไม่พบหัวข้อที่เลือกในไฟล์ Excel");
            }

            processRows(sheet, headers, selectedHeaderIndices, errorMap);
        } catch (IOException e) {
            throw new IllegalArgumentException("ไม่สามารถอ่านไฟล์ Excel ได้", e);
        }

        return formatErrorMap(errorMap);
    }

    public List<String> validateExcel(MultipartFile file) {
        Map<Integer, StringBuilder> errorMap = new TreeMap<>();

        if (file.isEmpty()) {
            throw new IllegalArgumentException("ไม่สามารถอ่านไฟล์ Excel ได้: ไฟล์ว่างเปล่า");
        }

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<String> headers = extractHeaders(sheet);

            List<String> unknownHeaders = findUnknownHeaders(headers);
            if (!unknownHeaders.isEmpty()) {
                throw new IllegalArgumentException("พบหัวข้อที่ไม่สามารถตรวจสอบได้: " + unknownHeaders);
            }

            processRows(sheet, headers, null, errorMap);
        } catch (IOException e) {
            throw new RuntimeException("เกิดข้อผิดพลาดในการอ่านไฟล์ Excel: " + e.getMessage(), e);
        }

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
                        if (error != null && !error.equals("success")) {
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
        validationRules.put(Pattern.compile("^(ชื่อ|name|ชื่อนามสกุล|fullname).*"), NameValidator::validate);
        validationRules.put(Pattern.compile("^(อีเมล|email).*$"), EmailValidate::validate);
        validationRules.put(Pattern.compile("^(บัตรประชาชน|citizenid).*$"), CitizenIdValidator::validate);
        validationRules.put(Pattern.compile("^(เบอร์โทร|phone).*$"), PhoneValidator::validate);
        validationRules.put(Pattern.compile("^(ที่อยู่|address).*$"), AddressValidator::validate);
        validationRules.put(Pattern.compile("^(อายุ|age).*$"), AgeValidator::validateDateOfBirth);
        validationRules.put(Pattern.compile("^(เพศ|gender).*$"), GenderValidator::validateGender);
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

    private List<String> findUnknownHeaders(List<String> headers) {
        return headers.stream()
                .filter(header -> validationRules.keySet().stream().noneMatch(pattern -> pattern.matcher(header).matches()))
                .collect(Collectors.toList());
    }

}

