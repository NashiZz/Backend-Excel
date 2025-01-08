package com.digio.backend.Service;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

@Service
public class ExcelValidationService {

    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_ADDRESS_LENGTH = 100;
    private static final int PHONE_NUMBER_LENGTH = 10;
    private static final int CITIZEN_ID_LENGTH = 13;

    public List<String> validateAndRejectExcel(MultipartFile file) {
        Map<Integer, StringBuilder> errorMap = new ConcurrentSkipListMap<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                StringBuilder errorBuilder = new StringBuilder();
                validateRow(row, errorBuilder);

                if (!errorBuilder.isEmpty()) {
                    errorMap.put(row.getRowNum() + 1, errorBuilder);
                }
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("ไม่สามารถอ่านไฟล์ Excel ได้ โปรดตรวจสอบไฟล์ที่อัปโหลด", e);
        }

        return errorMap.entrySet().stream()
                .map(entry -> "แถวที่ " + entry.getKey() + ": " + entry.getValue().toString())
                .collect(Collectors.toList());
    }

    private void validateRow(Row row, StringBuilder errorBuilder) {
        String name = getCellValue(row.getCell(0));
        String email = getCellValue(row.getCell(1));
        String citizenId = getCellValue(row.getCell(2));
        String address = getCellValue(row.getCell(3));
        String phoneNum = getCellValue(row.getCell(4));

        validateName(name, errorBuilder);
        validateEmail(email, errorBuilder);
        validateCitizenId(citizenId, errorBuilder);
        validateAddress(address, errorBuilder);
        validatePhoneNum(phoneNum, errorBuilder);
    }

    private void validateName(String name, StringBuilder errorBuilder) {
        if (name == null || name.trim().isEmpty() || name.length() > MAX_NAME_LENGTH) {
            appendError(errorBuilder, "ชื่อไม่ถูกต้อง");
        } else if (name.length() < 2) {
            appendError(errorBuilder, "ชื่อควรมีความยาวอย่างน้อย 2 ตัวอักษร");
        } else if (EmailValidator.getInstance().isValid(name)) {
            appendError(errorBuilder, "ชื่อไม่ควรเป็นอีเมล");
        } else if (name.matches("^\\d{" + PHONE_NUMBER_LENGTH + "}$")) {
            appendError(errorBuilder, "ชื่อไม่ควรเป็นหมายเลขโทรศัพท์");
        } else if (name.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            appendError(errorBuilder, "ชื่อไม่ควรมีตัวอักษรพิเศษ");
        } else if (name.matches(".*\\d.*")) {
            appendError(errorBuilder, "ชื่อไม่ควรมีตัวเลข");
        } else if (name.contains("  ")) {
            appendError(errorBuilder, "ชื่อไม่ควรมีช่องว่างซ้ำ");
        } else if (!name.matches("^[\\p{L}\\s]+$")) {
            appendError(errorBuilder, "ชื่อไม่ถูกต้อง");
        } else if (!name.matches("^[ก-๙A-Za-z\\s]+$")) {
            appendError(errorBuilder, "ชื่อไม่ควรมีอักขระที่ไม่ใช่ภาษาไทยหรือภาษาอังกฤษ");
    }
    }

    private void validateEmail(String email, StringBuilder errorBuilder) {
        if (email == null || !EmailValidator.getInstance().isValid(email) || email.length() > MAX_NAME_LENGTH) {
            appendError(errorBuilder, "อีเมลไม่ถูกต้อง");
        }
    }

    private void validateCitizenId(String citizenId, StringBuilder errorBuilder) {
        if (citizenId == null || !citizenId.matches("^\\d{" + CITIZEN_ID_LENGTH + "}$")) {
            appendError(errorBuilder, "บัตรประชาชนไม่ถูกต้อง");
        }
    }

    private void validateAddress(String address, StringBuilder errorBuilder) {
        if (address == null || address.trim().isEmpty() || address.length() > MAX_ADDRESS_LENGTH
                || address.matches(".*[<>#&@].*")) {
            appendError(errorBuilder, "ที่อยู่ไม่ถูกต้อง");
        }
    }

    private void validatePhoneNum(String phoneNum, StringBuilder errorBuilder) {
        if (phoneNum == null || !phoneNum.matches("^0[1-9][0-9]{8}$")) {
            appendError(errorBuilder, "หมายเลขโทรศัพท์ไม่ถูกต้อง");
        }
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

    private void appendError(StringBuilder errorBuilder, String message) {
        if (!errorBuilder.isEmpty()) {
            errorBuilder.append(", ");
        }
        errorBuilder.append(message);
    }
}
