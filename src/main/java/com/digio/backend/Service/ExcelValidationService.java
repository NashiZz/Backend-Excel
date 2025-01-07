package com.digio.backend.Service;

import org.springframework.stereotype.Service;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
public class ExcelValidationService {

    public void validateAndRejectExcel(MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // ข้าม Header

                String email = getCellValue(row.getCell(0));
                String citizenId = getCellValue(row.getCell(1));

                if (!isValidEmail(email)) {
                    throw new IllegalArgumentException("แถวที่ " + (row.getRowNum() + 1) +
                            ": อีเมลไม่ถูกต้อง");
                }
                if (!isValidCitizenId(citizenId)) {
                    throw new IllegalArgumentException("แถวที่ " + (row.getRowNum() + 1) +
                            ": บัตรประชาชนไม่ถูกต้อง");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("เกิดข้อผิดพลาดในการอ่านไฟล์ Excel", e);
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[ก-ฮa-zA-Zสระ]+$");
    }

    private boolean isValidCitizenId(String citizenId) {
        return citizenId != null && citizenId.matches("^\\d{13}$");
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long) cell.getNumericCellValue());
        }
        return null;
    }
}
