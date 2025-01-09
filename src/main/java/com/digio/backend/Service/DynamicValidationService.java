package com.digio.backend.Service;

import com.digio.backend.Validate.*;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class DynamicValidationService {

    private final Map<String, Function<String, String>> validationRules = new HashMap<>();

    public DynamicValidationService() {
        initializeDefaultValidationRules();
    }

    public List<String> validateExcel(MultipartFile file) {
        Map<Integer, StringBuilder> errorMap = new TreeMap<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<String> headers = extractHeaders(sheet);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                StringBuilder errorBuilder = new StringBuilder();
                validateRow(row, headers, errorBuilder);

                if (!errorBuilder.isEmpty()) {
                    errorMap.put(row.getRowNum() + 1, errorBuilder);
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("ไม่สามารถอ่านไฟล์ Excel ได้", e);
        }

        return errorMap.entrySet().stream()
                .map(entry -> "แถวที่ " + entry.getKey() + ": " + entry.getValue().toString())
                .collect(Collectors.toList());
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

    private void validateRow(Row row, List<String> headers, StringBuilder errorBuilder) {
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            String cellValue = getCellValue(row.getCell(i));

            if (validationRules.containsKey(header)) {
                String error = validationRules.get(header).apply(cellValue);
                if (error != null) {
                    appendError(errorBuilder, error);
                }
            } else {
                appendError(errorBuilder, "พบหัวข้อที่ไม่รู้จัก");
            }
        }
    }

    private void initializeDefaultValidationRules() {
        validationRules.put("name", NameValidator::validate);
        validationRules.put("email", EmailValidate::validate);
        validationRules.put("citizenid", CitizenIdValidator::validate);
        validationRules.put("phone", PhoneValidator::validate);
        validationRules.put("address", AddressValidator::validate);
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
}

