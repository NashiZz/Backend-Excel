package com.digio.backend.Controller;

import com.digio.backend.Service.ExcelValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
            List<String> errors = validationService.validateAndRejectExcel(file);

            if (errors.isEmpty()) {
                return ResponseEntity.ok("ตรวจสอบข้อมูลเรียบร้อย ไม่มีข้อผิดพลาด");
            } else {
                return ResponseEntity.badRequest().body(errors);
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("เกิดข้อผิดพลาดภายในเซิร์ฟเวอร์: " + e.getMessage());
        }
    }
}
