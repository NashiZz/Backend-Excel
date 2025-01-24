package com.digio.backend.Controller;

import com.digio.backend.Service.DynamicValidationService;
import com.digio.backend.Service.ExcelValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/excel")
public class ExcelValidationController {

    @Autowired
    private ExcelValidationService validationService;

    @Autowired
    private DynamicValidationService dynamicValidationService;

    @PostMapping("/dynamic")
    public ResponseEntity<?> validateExcelFile(@RequestParam("file") MultipartFile file) {
        ResponseEntity<?> fileValidation = validateFile(file);
        if (fileValidation != null) return fileValidation;

        try {
            List<Map<String, Object>> validationErrors = dynamicValidationService.validateExcel(file);
            return validationErrors.isEmpty() ?
                    ResponseEntity.ok(Collections.singletonMap("message", "ไฟล์ Excel ถูกต้อง ไม่มีข้อผิดพลาด")) :
                    ResponseEntity.badRequest().body(Collections.singletonMap("errors", validationErrors));
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @PostMapping("/headers")
    public ResponseEntity<?> validateExcelWithSelectedHeaders(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "headers", required = false) List<String> selectedHeaders) {
        ResponseEntity<?> fileValidation = validateFile(file);
        if (fileValidation != null) return fileValidation;

        if (selectedHeaders == null || selectedHeaders.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "โปรดระบุหัวข้อที่ต้องการตรวจสอบ"));
        }

        try {
            List<Map<String, Object>> validationErrors = dynamicValidationService.validateExcelWithSelectedHeaders(file, selectedHeaders);
            return validationErrors.isEmpty() ?
                    ResponseEntity.ok(Collections.singletonMap("message", "ไฟล์ Excel ถูกต้อง ไม่มีข้อผิดพลาด")) :
                    ResponseEntity.badRequest().body(Collections.singletonMap("errors", validationErrors));
        } catch (Exception e) {
            return handleException(e);
        }
    }

    private ResponseEntity<?> validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "โปรดอัปโหลดไฟล์ Excel ที่ถูกต้อง"));
        }
        return null;
    }

    private ResponseEntity<?> handleException(Exception e) {
        return ResponseEntity.status(500).body(Collections.singletonMap("message", "เกิดข้อผิดพลาด: " + e.getMessage()));
    }
}
