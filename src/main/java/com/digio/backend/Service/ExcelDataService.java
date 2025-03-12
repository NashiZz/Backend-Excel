package com.digio.backend.Service;

import com.digio.backend.DTO.ExcelDataRequest;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

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

        for (Map<String, Object> record : request.getRecords()) {
            recordsRef.add(record).get();
        }
    }
}

