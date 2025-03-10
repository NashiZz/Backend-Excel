package com.digio.backend.Controller;

import com.digio.backend.DTO.ExcelDataRequest;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.DocumentReference;
import com.google.firebase.cloud.FirestoreClient;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ExcelDataController {
    private static final Logger logger = LoggerFactory.getLogger(ExcelDataController.class);

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

            // บันทึกข้อมูล
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
}

