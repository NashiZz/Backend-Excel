package com.digio.backend.Service;

import com.digio.backend.DTO.ExcelDataRequest;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelDataService {
    private static final Logger logger = LoggerFactory.getLogger(ExcelDataService.class);

    public void saveExcelData(ExcelDataRequest request) throws Exception {
        Firestore db = FirestoreClient.getFirestore();

        Query query = db.collection("Templates_Data")
                .document(request.getUserToken())
                .collection(request.getTemplateId())
                .limit(1);

        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

        if (documents.isEmpty()) {
            throw new Exception("ไม่พบเอกสารที่ต้องการ");
        }

        DocumentSnapshot firstDocument = documents.get(0);
        String documentId = firstDocument.getId();

        DocumentReference docRef = db.collection("Templates_Data")
                .document(request.getUserToken())
                .collection(request.getTemplateId())
                .document(documentId);

        Map<String, Object> data = Map.of(
                "update_at", request.getUpdateAt(),
                "template_id", request.getTemplateId()
        );

        docRef.set(data, SetOptions.merge()).get();
        CollectionReference recordsRef = docRef.collection("records");

        ApiFuture<QuerySnapshot> future = recordsRef.get();
        List<QueryDocumentSnapshot> existingDocs = future.get().getDocuments();
        int currentSize = existingDocs.size();
        int newId = currentSize + 1;

        for (Map<String, Object> record : request.getRecords()) {
            if (record.containsKey("documentId")) {
                String recordDocumentId = (String) record.get("documentId");
                DocumentReference recordRef = recordsRef.document(recordDocumentId);

                Map<String, Object> updateData = new HashMap<>(record);
                updateData.remove("documentId");

                recordRef.set(updateData, SetOptions.merge()).get();
                System.out.println("🔄 Updated Record ID: " + recordDocumentId);
            } else {
                String newDocumentId = String.format("%04d", newId);
                record.put("documentId", newDocumentId);

                recordsRef.document(newDocumentId).set(record).get();
                System.out.println("🆕 Added New Record with ID: " + newDocumentId);

                newId++;
            }
        }
    }

    public byte[] generateExcelFile(List<Map<String, Object>> identicalRecords, List<String> headers) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            logger.info("Generating Excel file...");

            if (identicalRecords == null || identicalRecords.isEmpty()) {
                logger.warn("No identical records found");
            }
            if (headers == null || headers.isEmpty()) {
                logger.warn("No headers found");
            }

            createSheet(workbook, "ข้อมูลใน DB", identicalRecords, headers);
            createSheet(workbook, "ข้อมูลที่ส่งไปรีวิว", identicalRecords, headers);

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void createSheet(Workbook workbook, String sheetName, List<Map<String, Object>> records, List<String> headers) {
        Sheet sheet = workbook.createSheet(sheetName);

        Row groupHeaderRow = sheet.createRow(0);
        CellStyle headerStyle = getHeaderStyle(workbook);

        Cell groupHeaderCell = groupHeaderRow.createCell(0);
        groupHeaderCell.setCellValue(sheetName);
        groupHeaderCell.setCellStyle(headerStyle);

        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.size() - 1));

        groupHeaderRow.setHeightInPoints(40);
        groupHeaderCell.setCellStyle(headerStyle);
        groupHeaderCell.getCellStyle().setAlignment(HorizontalAlignment.CENTER);

        Row headerRow = sheet.createRow(1);
        CellStyle dataHeaderStyle = getHeaderStyle(workbook);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(dataHeaderStyle);
            sheet.setColumnWidth(i, 5500);
        }

        int rowNum = 2;
        for (Map<String, Object> record : records) {
            if (record == null) continue;

            Map<String, Object> dataToUse;
            if (sheetName.equals("ข้อมูลใน DB")) {
                dataToUse = (Map<String, Object>) record.get("existingData");
            } else {
                dataToUse = (Map<String, Object>) record.get("newData");
            }

            if (dataToUse == null) continue;

            Row row = sheet.createRow(rowNum++);
            boolean hasChanges = false;

            CellStyle changeCellStyle = workbook.createCellStyle();
            changeCellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            changeCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < headers.size(); i++) {
                Object value = dataToUse.get(headers.get(i));
                Cell cell = row.createCell(i);

                if (value instanceof Integer) {
                    cell.setCellValue((Integer) value);
                } else if (value instanceof Double) {
                    cell.setCellValue((Double) value);
                } else if (value instanceof Boolean) {
                    cell.setCellValue((Boolean) value);
                } else {
                    cell.setCellValue(value != null ? value.toString() : "-");
                }

                if (sheetName.equals("ข้อมูลที่ส่งไปรีวิว") && record.containsKey("differences")) {
                    Map<String, Map<String, Object>> differences = (Map<String, Map<String, Object>>) record.get("differences");
                    if (differences != null && differences.containsKey(headers.get(i))) {
                        hasChanges = true;
                        cell.setCellStyle(changeCellStyle);
                    }
                }
            }

            if (hasChanges) {
                Row changeRow = sheet.createRow(rowNum++);
                for (int i = 0; i < headers.size(); i++) {
                    String header = headers.get(i);

                    if (record.containsKey("differences")) {
                        Map<String, Map<String, Object>> differences = (Map<String, Map<String, Object>>) record.get("differences");
                        if (differences != null && differences.containsKey(header)) {
                            Map<String, Object> diff = differences.get(header);
                            Cell changeCell = changeRow.createCell(i);
                            changeCell.setCellValue(formatValue(diff.get("old")) + " -> " + formatValue(diff.get("new")));

                            changeCell.setCellStyle(changeCellStyle);
                            changeCell.getCellStyle().setAlignment(HorizontalAlignment.RIGHT);  // จัดชิดขวา
                        }
                    }
                }
            }
        }
    }

    private CellStyle getHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        return headerStyle;
    }

    private String formatValue(Object value) {
        if (value instanceof Integer || value instanceof Double || value instanceof Boolean) {
            return value.toString();
        }
        return value != null ? value.toString() : "-";
    }

}

