package com.digio.backend.Service;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import java.util.*;


@Service
public class ConditionRelationServices {

    public List<Map<String, Object>> validateColumnRelations(Sheet sheet, List<Map<String, String>> rules) {
        List<Map<String, Object>> errorList = new ArrayList<>();

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;

            for (Map<String, String> rule : rules) {
                String columnA = rule.get("columnA");
                String columnB = rule.get("columnB");
                String valueA = rule.get("valueA");
                String valueB = rule.get("valueB");

                int indexA = getColumnIndex(sheet, columnA);
                int indexB = getColumnIndex(sheet, columnB);

                String cellA = getCellValue(row.getCell(indexA));
                String cellB = getCellValue(row.getCell(indexB));

                boolean conditionFailed = false;
                if (valueA.isEmpty() && valueB.isEmpty()) {
                    conditionFailed = cellA != null && (cellB == null || cellB.isEmpty()); // A มีค่า แต่ B ว่าง
                } else if (!valueA.isEmpty() && valueB.isEmpty()) {
                    conditionFailed = cellA.equals(valueA) && (cellB == null || cellB.isEmpty()); // A = XXX แต่ B ว่าง
                } else if (valueA.isEmpty() && !valueB.isEmpty()) {
                    conditionFailed = cellA != null && !cellB.equals(valueB); // A มีค่า แต่ B ไม่ตรง XXX
                } else {
                    conditionFailed = cellA.equals(valueA) && !cellB.equals(valueB); // A = XXX แต่ B != YYY
                }

                if (conditionFailed) {
                    Map<String, Object> errorDetails = new HashMap<>();
                    errorDetails.put("row", row.getRowNum() + 1);
                    errorDetails.put("columnA", columnA);
                    errorDetails.put("columnB", columnB);
                    errorDetails.put("message", "ค่าของ " + columnA + " ไม่ตรงกับเงื่อนไขของ " + columnB);
                    errorList.add(errorDetails);
                }
            }
        }

        return errorList;
    }

    int getColumnIndex(Sheet sheet, String columnName) {
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            throw new IllegalArgumentException("ไม่พบแถว Header ในไฟล์ Excel");
        }

        for (Cell cell : headerRow) {
            if (cell.getStringCellValue().trim().equalsIgnoreCase(columnName)) {
                return cell.getColumnIndex();
            }
        }

        throw new IllegalArgumentException("ไม่พบ column: " + columnName);
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

}
