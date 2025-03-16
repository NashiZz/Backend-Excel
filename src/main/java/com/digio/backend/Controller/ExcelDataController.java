package com.digio.backend.Controller;

import com.digio.backend.DTO.ExcelDataRequest;
import com.digio.backend.Service.ExcelDataService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

    @GetMapping("/exportExcel")
    public ResponseEntity<byte[]> exportExcel(@RequestParam String userToken, @RequestParam String docName, @RequestParam String templateId) {
        try {
            Firestore db = FirestoreClient.getFirestore();

            Query query = db.collection("Templates_Data")
                    .document(userToken)
                    .collection(templateId)
                    .whereEqualTo("file_name", docName)
                    .limit(1);

            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get().getDocuments();

            if (documents.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(("ไม่พบข้อมูล Template ที่มีชื่อไฟล์: " + docName + " ค่ะ").getBytes());
            }

            DocumentSnapshot firstDocument = documents.get(0);
            String documentId = firstDocument.getId();

            CollectionReference recordsRef = db.collection("Templates_Data")
                    .document(userToken)
                    .collection(templateId)
                    .document(documentId)
                    .collection("records");

            ApiFuture<QuerySnapshot> future = recordsRef.get();
            List<QueryDocumentSnapshot> records = future.get().getDocuments();

            if (records.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("ไม่พบข้อมูล Records".getBytes());
            }

            ByteArrayOutputStream outputStream = createExcelFile(records);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(docName + ".xlsx")
                    .build());

            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("เกิดข้อผิดพลาดขณะ Export ข้อมูล: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error exporting data: " + e.getMessage()).getBytes());
        }
    }

    @PostMapping("/saveNewUpdate")
    public ResponseEntity<String> saveNewDataAndUpdate(@RequestBody ExcelDataRequest request) {
        try {
            excelDataService.saveExcelData(request);
            return ResponseEntity.ok("บันทึกข้อมูลสำเร็จ!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("เกิดข้อผิดพลาดในการบันทึกข้อมูล: " + e.getMessage());
        }
    }

    @PostMapping("/saveExcelData")
    public ResponseEntity<String> saveExcelData(@Valid @RequestBody ExcelDataRequest request) {
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
            data.put("file_name", documentId); // 🔹

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

            return ResponseEntity.ok("✅ Data saved successfully with File Name: " + documentId);
        } catch (Exception e) {
            logger.error("❌ Error saving data: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Error saving data: " + e.getMessage());
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

    private ByteArrayOutputStream createExcelFile(List<QueryDocumentSnapshot> records) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Exported Data");

        Row headerRow = sheet.createRow(0);
        List<String> headers = new ArrayList<>(records.get(0).getData().keySet());

        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
        }

        int rowNum = 1;
        for (QueryDocumentSnapshot record : records) {
            Row row = sheet.createRow(rowNum++);
            Map<String, Object> data = record.getData();

            for (int i = 0; i < headers.size(); i++) {
                Cell cell = row.createCell(i);
                Object value = data.get(headers.get(i));

                if (value instanceof Number) {
                    cell.setCellValue(((Number) value).doubleValue());
                } else if (value != null) {
                    cell.setCellValue(value.toString());
                } else {
                    cell.setCellValue("");
                }
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream;
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
            logger.error("❌ Error fetching uploaded files: ", e);
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
            logger.error("❌ Error fetching records: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

