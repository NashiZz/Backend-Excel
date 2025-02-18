package com.digio.backend.Controller;

import com.digio.backend.Service.DynamicValidationService;
import com.digio.backend.Service.ExcelValidationService;
import com.digio.backend.Service.TemplateService;
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

    @Autowired
    private TemplateService templateService;

    @PostMapping("/dynamic")
    public ResponseEntity<?> validateExcelFile(@RequestParam("file") MultipartFile file) {
        ResponseEntity<?> fileValidation = validateFile(file);
        if (fileValidation != null) return fileValidation;

        try {
            List<Map<String, Object>> validationErrors = dynamicValidationService.validateExcel(file);
            System.out.println(validationErrors);

            if (validationErrors.isEmpty()) {
                return ResponseEntity.ok(Collections.singletonMap("message", "ไฟล์ Excel ถูกต้อง ไม่มีข้อผิดพลาด"));
            }

            return ResponseEntity.badRequest().body(Collections.singletonMap("errors", validationErrors));
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

    @PostMapping("/template")
    public ResponseEntity<?> handleUploadWithTemplate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("condition") List<String> expectedHeaders,
            @RequestParam("calculater") List<String> calculater,
            @RequestParam("relation") List<String> relation) {

        ResponseEntity<?> fileValidation = validateFile(file);
        if (fileValidation != null) return fileValidation;

        try {
            List<Map<String, Object>> validationErrors = templateService.handleUploadWithTemplate(file, expectedHeaders, calculater, relation);

            System.out.println(validationErrors);
            if (validationErrors.isEmpty() || !validationErrors.get(0).containsKey("summary")) {
                return ResponseEntity.ok(Collections.singletonMap("message", "ไฟล์ Excel ถูกต้อง ไม่มีข้อผิดพลาด"));
            } else {
                return ResponseEntity.badRequest().body(Collections.singletonMap("errors", validationErrors));
            }
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
