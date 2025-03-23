package com.digio.backend.Controller;

import com.digio.backend.DTO.ExcelDataRequest;
import com.digio.backend.Service.ExcelDataService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api")
public class ExcelDataController {
    private static final Logger logger = LoggerFactory.getLogger(ExcelDataController.class);

    private final ExcelDataService excelDataService;

    @Autowired
    public ExcelDataController(ExcelDataService excelDataService) {
        this.excelDataService = excelDataService;
    }

    @PostMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcels(@RequestBody Map<String, Object> requestData) {
        try {
            logger.info("Received request for exporting Excel");

            if (requestData == null || !requestData.containsKey("identicalRecords") || !requestData.containsKey("headers")) {
                logger.error("Invalid request: missing 'identicalRecords' or 'headers'");
                return ResponseEntity.badRequest().body(null);
            }

            List<Map<String, Object>> identicalRecords = (List<Map<String, Object>>) requestData.get("identicalRecords");
            List<String> headers = (List<String>) requestData.get("headers");

            if (identicalRecords == null || headers == null) {
                logger.error("Identical records or headers are null");
                return ResponseEntity.badRequest().body(null);
            }

            logger.info("Processing {} identical records with {} headers", identicalRecords.size(), headers.size());

            byte[] excelFile = excelDataService.generateExcelFile(identicalRecords, headers);

            HttpHeaders headersResponse = new HttpHeaders();
            headersResponse.set("Content-Disposition", "attachment; filename=identical_records_comparison.xlsx");

            return new ResponseEntity<>(excelFile, headersResponse, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error generating Excel file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/exportExcel")
    public ResponseEntity<byte[]> exportExcel(@RequestBody Map<String, Object> requestBody) {
        try {
            String userToken = (String) requestBody.get("userToken");
            String docName = (String) requestBody.get("fileName");
            String templateId = (String) requestBody.get("templateId");
            List<String> orderedHeaders = (List<String>) requestBody.get("orderedHeaders");

            Firestore db = FirestoreClient.getFirestore();
            DocumentReference docRef = db.collection("Templates_Data")
                    .document(userToken)
                    .collection(templateId)
                    .document(docName);

            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (!document.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(("ไม่พบข้อมูล Template: " + docName).getBytes(StandardCharsets.UTF_8));
            }

            CollectionReference recordsRef = docRef.collection("records");
            ApiFuture<QuerySnapshot> recordsFuture = recordsRef.get();
            List<QueryDocumentSnapshot> records = recordsFuture.get().getDocuments();

            if (records.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("ไม่พบข้อมูล Records".getBytes(StandardCharsets.UTF_8));
            }

            ByteArrayOutputStream outputStream = createExcelFile(records, orderedHeaders);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(docName + ".xlsx", StandardCharsets.UTF_8)
                    .build());

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("เกิดข้อผิดพลาดขณะ Export ข้อมูล: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error exporting data: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
        }
    }

    private ByteArrayOutputStream createExcelFile(List<QueryDocumentSnapshot> records, List<String> orderedHeaders) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Data");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(25);
            for (int i = 0; i < orderedHeaders.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(orderedHeaders.get(i));
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            int rowNum = 1;
            for (QueryDocumentSnapshot record : records) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < orderedHeaders.size(); i++) {
                    String columnName = orderedHeaders.get(i);
                    Object value = record.get(columnName);
                    String cellValue = (value != null) ? value.toString() : "";
                    row.createCell(i).setCellValue(cellValue);
                }
            }

            workbook.write(outputStream);
            return outputStream;

        } catch (IOException e) {
            throw new RuntimeException("Error creating Excel file", e);
        }
    }

    @GetMapping("/checkFileExists")
    public ResponseEntity<Map<String, Object>> checkFileExists(
            @RequestParam String userToken,
            @RequestParam String templateId) {
        try {
            Firestore db = FirestoreClient.getFirestore();

            CollectionReference templateRef = db.collection("Templates_Data")
                    .document(userToken)
                    .collection(templateId);

            ApiFuture<QuerySnapshot> fileFuture = templateRef.limit(1).get();
            List<QueryDocumentSnapshot> fileDocs = fileFuture.get().getDocuments();

            boolean exists = !fileDocs.isEmpty();
            String docId = exists ? fileDocs.get(0).getId() : null;

            Map<String, Object> response = new HashMap<>();
            response.put("exists", exists);
            response.put("fileName", docId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error checking file existence: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "เกิดข้อผิดพลาดในเซิร์ฟเวอร์"));
        }
    }

    @PostMapping("/saveNewUpdate")
    public ResponseEntity<Map<String, String>> saveNewDataAndUpdate(@RequestBody ExcelDataRequest request) {
        try {
            excelDataService.saveExcelData(request);

            Map<String, String> response = new HashMap<>();
            response.put("message", "บันทึกข้อมูลสำเร็จ!");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "เกิดข้อผิดพลาด: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/saveExcelData")
    public ResponseEntity<Map<String, String>> saveExcelData(@Valid @RequestBody ExcelDataRequest request) {
        try {
            Firestore db = FirestoreClient.getFirestore();

            String documentId = request.getFileName() != null && !request.getFileName().trim().isEmpty()
                    ? request.getFileName()
                    : String.valueOf(System.currentTimeMillis());

            DocumentReference docRef = db.collection("Templates_Data")
                    .document(request.getUserToken())
                    .collection(request.getTemplateId())
                    .document(documentId);

            Map<String, Object> data = new HashMap<>();
            data.put("uploaded_at", request.getUploadedAt());
            data.put("template_id", request.getTemplateId());
            data.put("update_at", request.getUpdateAt());
            data.put("file_name", documentId);

            docRef.set(data).get();
            CollectionReference recordsRef = docRef.collection("records");

            ApiFuture<QuerySnapshot> future = recordsRef.get();
            List<QueryDocumentSnapshot> existingDocs = future.get().getDocuments();
            int currentSize = existingDocs.size();
            int newId = currentSize + 1;

            for (Map<String, Object> record : request.getRecords()) {
                String recordId = String.format("%04d", newId);
                record.put("documentId", recordId);
                recordsRef.document(recordId).set(record).get();
                newId++;
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Data saved successfully");
            response.put("fileName", documentId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error saving data: ", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error saving data: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/checkExistingRecords")
    public ResponseEntity<Map<String, Object>> checkExistingRecords(
            @RequestParam String userToken,
            @RequestParam String templateId) {
        try {
            Firestore db = FirestoreClient.getFirestore();

            CollectionReference templateRef = db.collection("Templates_Data")
                    .document(userToken)
                    .collection(templateId);

            ApiFuture<QuerySnapshot> querySnapshot = templateRef.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

            if (documents.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No records found for the provided userToken and templateId"));
            }

            String documentId = documents.get(0).getId();
            CollectionReference recordsRef = templateRef.document(documentId).collection("records");

            List<Map<String, Object>> existingRecords = new ArrayList<>();
            recordsRef.get().get().getDocuments().forEach(document -> {
                existingRecords.add(document.getData());
            });

            if (existingRecords.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "No records found inside documentId: " + documentId));
            }

            return ResponseEntity.ok(Map.of("existingRecords", existingRecords));

        } catch (Exception e) {
            logger.error("Error fetching existing records: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error fetching data: " + e.getMessage()));
        }
    }

    @PostMapping("/getUploadedFiles")
    public ResponseEntity<Map<String, Object>> getUploadedFiles(@RequestBody Map<String, Object> requestData) {
        try {
            String userToken = (String) requestData.get("userToken");
            List<String> templateIDs = (List<String>) requestData.get("templateIDs");

            if (userToken == null || templateIDs == null || templateIDs.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "ข้อมูลไม่ครบถ้วน"));
            }

            Firestore db = FirestoreClient.getFirestore();
            List<Map<String, Object>> fileList = new ArrayList<>();

            for (String templateID : templateIDs) {
                CollectionReference templateCollection = db.collection("Templates_Data")
                        .document(userToken)
                        .collection(templateID);

                ApiFuture<QuerySnapshot> querySnapshot = templateCollection.get();
                List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

                for (QueryDocumentSnapshot doc : documents) {
                    Map<String, Object> fileData = new HashMap<>();
                    fileData.put("id", doc.getId());
                    fileData.put("file_name", doc.getString("file_name"));

                    Object uploadedAt = doc.get("uploaded_at");
                    if (uploadedAt instanceof Long) {
                        fileData.put("uploaded_at", uploadedAt);
                    } else if (uploadedAt instanceof String) {
                        try {
                            fileData.put("uploaded_at", Long.parseLong((String) uploadedAt));
                        } catch (NumberFormatException e) {
                            fileData.put("uploaded_at", null);
                        }
                    } else {
                        fileData.put("uploaded_at", null);
                    }

                    fileData.put("template_id", templateID);
                    fileData.put("file_details", doc.getData());

                    fileList.add(fileData);
                }
            }

            if (fileList.isEmpty()) {
                System.out.println("⚠ ไม่พบไฟล์ที่อัปโหลด");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "ไม่พบไฟล์ที่อัปโหลด"));
            }

            return ResponseEntity.ok(Map.of("files", fileList));

        } catch (Exception e) {
            logger.error("Error fetching uploaded files: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "เกิดข้อผิดพลาดในการดึงข้อมูล"));
        }
    }

    @GetMapping("/getRecords")
    public ResponseEntity<List<Map<String, Object>>> getRecords(
            @RequestParam String userToken,
            @RequestParam String templateId) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            CollectionReference recordsRef = db.collection("Templates_Data")
                    .document(userToken)
                    .collection("records");

            ApiFuture<QuerySnapshot> querySnapshot = recordsRef.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

            if (documents.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            List<Map<String, Object>> recordsList = new ArrayList<>();
            for (QueryDocumentSnapshot doc : documents) {
                Map<String, Object> record = doc.getData();
                record.put("id", doc.getId());
                recordsList.add(record);
            }

            return ResponseEntity.ok(recordsList);
        } catch (Exception e) {
            logger.error("Error fetching records: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

