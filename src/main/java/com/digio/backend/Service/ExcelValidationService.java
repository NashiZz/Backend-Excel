package com.digio.backend.Service;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

@Service
public class ExcelValidationService {

    public List<String> validateAndRejectExcel(MultipartFile file) {
        Map<Integer, StringBuilder> errorMap = new ConcurrentSkipListMap<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            sheet.forEach(row -> {
                if (row.getRowNum() == 0) return;

                StringBuilder errorBuilder = new StringBuilder();
                String email = getCellValue(row.getCell(0));
                String citizenId = getCellValue(row.getCell(1));

                if (!isValidEmail(email)) {
                    errorBuilder.append("อีเมลไม่ถูกต้อง");
                }

                if (!isValidCitizenId(citizenId)) {
                    if (!errorBuilder.isEmpty()) {
                        errorBuilder.append(", ");
                    }
                    errorBuilder.append("บัตรประชาชนไม่ถูกต้อง");
                }

                if (!errorBuilder.isEmpty()) {
                    errorMap.put(row.getRowNum() + 1, errorBuilder);
                }
            });
        } catch (IOException e) {
            throw new IllegalArgumentException("ไม่สามารถอ่านไฟล์ Excel ได้ โปรดตรวจสอบไฟล์ที่อัปโหลด", e);
        }

        return errorMap.entrySet().stream()
                .map(entry -> "แถวที่ " + entry.getKey() + ": " + entry.getValue().toString())
                .collect(Collectors.toList());
    }

    private boolean isValidEmail(String email) {
        return email != null && EmailValidator.getInstance().isValid(email);
    }

    private boolean isValidCitizenId(String citizenId) {
        return citizenId != null && citizenId.matches("^\\d{13}$");
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }
}
