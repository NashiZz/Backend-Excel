package com.digio.backend.Service;

import com.digio.backend.Validate.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

@Service
public class DynamicValidationService {

    private final Map<Pattern, Function<String, String>> validationRules = new HashMap<>();

    public DynamicValidationService() {
        initializeDefaultValidationRules();
    }

    public List<Map<String, Object>> validateExcelWithSelectedHeaders(MultipartFile file, List<String> selectedHeaders) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("ไฟล์ว่างเปล่า ไม่สามารถอ่านข้อมูลได้");
        }

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<String> headers = extractHeaders(sheet);

            if (isRowsEmpty(sheet)) {
                throw new IllegalArgumentException("ไฟล์นี้ไม่มีข้อมูล");
            }

            validateUnknown(selectedHeaders, headers);

            List<Integer> selectedIndices = getHeaderIndices(headers, selectedHeaders);
            if (selectedIndices.isEmpty()) {
                throw new IllegalArgumentException("ไม่พบหัวข้อที่เลือกในไฟล์ Excel");
            }

            return processRows(sheet, headers, selectedIndices);
        } catch (IOException e) {
            throw new IllegalArgumentException("ไม่สามารถอ่านไฟล์ Excel ได้", e);
        }
    }

    public List<Map<String, Object>> validateExcel(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("ไฟล์ว่างเปล่า ไม่สามารถอ่านข้อมูลได้");
        }

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<String> headers = extractHeaders(sheet);

            if (isRowsEmpty(sheet)) {
                throw new IllegalArgumentException("ไฟล์นี้ไม่มีข้อมูล");
            }

            validateUnknownHeaders(headers);

            return processRows(sheet, headers, null);
        } catch (IOException e) {
            throw new IllegalArgumentException("ไม่สามารถอ่านไฟล์ Excel ได้", e);
        }
    }

    private List<String> extractHeaders(Sheet sheet) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalArgumentException("ไม่มีแถวหัวข้อในไฟล์ Excel");
        }

        return StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(headerRow.cellIterator(), Spliterator.ORDERED), false
                )
                .map(this::getCellValue)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .toList();
    }

    private List<Integer> getHeaderIndices(List<String> headers, List<String> selectedHeaders) {
        List<String> lowerHeaders = headers.stream().map(String::toLowerCase).toList();
        List<String> lowerSelected = selectedHeaders.stream().map(String::toLowerCase).toList();

        return lowerSelected.stream()
                .map(lowerHeaders::indexOf)
                .filter(index -> index >= 0)
                .toList();
    }

    private List<Map<String, Object>> processRows(Sheet sheet, List<String> headers, List<Integer> selectedIndices) {
        List<Map<String, Object>> errorList = new ArrayList<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // ข้ามแถวหัวข้อ

            for (int i = 0; i < headers.size(); i++) {
                if (selectedIndices != null && !selectedIndices.contains(i)) continue;

                String header = headers.get(i);
                String cellValue = getCellValue(row.getCell(i));

                String errorMessage = validateCellAndGetMessage(header, cellValue);
                if (errorMessage != null && !errorMessage.equals("success")) {
                    Map<String, Object> errorDetails = new HashMap<>();
                    errorDetails.put("row", row.getRowNum()); // Row เริ่มจาก 1
                    errorDetails.put("column", i); // Column เริ่มจาก 1
                    errorDetails.put("header", header); // ชื่อหัวข้อของคอลัมน์
                    errorDetails.put("message", errorMessage);

                    errorList.add(errorDetails);
                }
            }
        }

        return errorList;
    }

    private String validateCellAndGetMessage(String header, String cellValue) {
        return validationRules.entrySet().stream()
                .filter(entry -> entry.getKey().matcher(header).matches())
                .findFirst()
                .map(entry -> entry.getValue().apply(cellValue))
                .orElse("ไม่สามารถตรวจสอบหัวข้อนี้ได้: " + header);
    }

    private void validateRowWithIndices(Row row, List<String> headers, List<Integer> selectedIndices, StringBuilder errorBuilder) {
        for (int index : selectedIndices) {
            validateCell(headers.get(index), getCellValue(row.getCell(index)), errorBuilder);
        }
    }

    private void validateRow(Row row, List<String> headers, StringBuilder errorBuilder) {
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            String cellValue = getCellValue(row.getCell(i));
            validateCell(header, cellValue, errorBuilder);
        }
    }


    private void validateCell(String header, String cellValue, StringBuilder errorBuilder) {
        validationRules.entrySet().stream()
                .filter(entry -> entry.getKey().matcher(header).matches())
                .findFirst()
                .ifPresentOrElse(
                        entry -> appendError(errorBuilder, entry.getValue().apply(cellValue)),
                        () -> appendError(errorBuilder, "พบหัวข้อที่ไม่รู้จัก: " + header)
                );
    }

    private void validateUnknown(List<String> selectedHeaders, List<String> headers) {
        List<String> lowerHeaders = headers.stream().map(String::toLowerCase).toList();
        List<String> lowerSelectedHeaders = selectedHeaders.stream().map(String::toLowerCase).toList();

        List<String> unknownHeaders = lowerSelectedHeaders.stream()
                .filter(header -> validationRules.keySet().stream().noneMatch(pattern -> pattern.matcher(header).matches()))
                .toList();

        if (!unknownHeaders.isEmpty()) {
            throw new IllegalArgumentException("พบหัวข้อที่ไม่สามารถตรวจสอบได้: " + String.join(", ", unknownHeaders));
        }
    }

    private void validateUnknownHeaders(List<String> headers) {
        List<String> unknownHeaders = headers.stream()
                .filter(header -> validationRules.keySet().stream().noneMatch(pattern -> pattern.matcher(header).matches()))
                .toList();

        if (!unknownHeaders.isEmpty()) {
            throw new IllegalArgumentException("พบหัวข้อที่ไม่รู้จัก: " + String.join(", ", unknownHeaders));
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private void appendError(StringBuilder errorBuilder, String error) {
        if (error != null && !error.equals("success")) {
            if (!errorBuilder.isEmpty()) {
                errorBuilder.append(", ");
            }
            errorBuilder.append(error);
        }
    }

    private List<String> formatErrorMessages(Map<Integer, String> errorMap) {
        return errorMap.entrySet().stream()
                .map(entry -> "แถวที่ " + entry.getKey() + ": " + entry.getValue())
                .toList();
    }


    private boolean isRowsEmpty(Sheet sheet) {
        for (int rowIndex = 1; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row != null) {
                for (Cell cell : row) {
                    if (cell != null && !getCellValue(cell).trim().isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
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
}

