package com.digio.backend.Controller;

import com.digio.backend.Service.ExcelValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/excel")
public class ExcelValidationController {

    @Autowired
    private ExcelValidationService validationService;

    @PostMapping("/validate")
    public ResponseEntity<?> validateExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("กรุณาอัปโหลดไฟล์ Excel");
        }

        try {
            validationService.validateAndRejectExcel(file);
            return ResponseEntity.ok("ตรวจสอบข้อมูลเรียบร้อย ไม่มีข้อผิดพลาด");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("เกิดข้อผิดพลาดภายในเซิร์ฟเวอร์");
        }
    }
}
