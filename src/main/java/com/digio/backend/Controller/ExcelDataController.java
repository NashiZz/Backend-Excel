package com.digio.backend.Controller;

import com.digio.backend.DTO.ExcelDataRequest;
import com.digio.backend.Service.ExcelDataService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ExcelDataController {
    private static final Logger logger = LoggerFactory.getLogger(ExcelDataController.class);

    private final ExcelDataService excelDataService;

    @Autowired
    public ExcelDataController(ExcelDataService excelDataService) {
        this.excelDataService = excelDataService;
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
            DocumentReference docRef = db.collection("Templates_Data")
                    .document(request.getUserToken())
                    .collection(request.getTemplateId())
                    .document();

            Map<String, Object> data = new HashMap<>();
            data.put("uploaded_at", request.getUploadedAt());
            data.put("template_id", request.getTemplateId());
            data.put("update_at", request.getUpdateAt());

            docRef.set(data).get();
            CollectionReference recordsRef = docRef.collection("records");
            for (Map<String, Object> record : request.getRecords()) {
                recordsRef.add(record).get();
            }

            return ResponseEntity.ok("Data saved successfully with ID: " + docRef.getId());
        } catch (Exception e) {
            logger.error("Error saving data: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving data: " + e.getMessage());
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

}

