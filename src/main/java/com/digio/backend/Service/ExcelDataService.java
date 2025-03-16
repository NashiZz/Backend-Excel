package com.digio.backend.Service;

import com.digio.backend.DTO.ExcelDataRequest;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelDataService {

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
}

