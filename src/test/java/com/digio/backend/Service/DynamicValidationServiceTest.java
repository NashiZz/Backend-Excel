package com.digio.backend.Service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DynamicValidationServiceTest {

    private final DynamicValidationService dynamicCheckingService = new DynamicValidationService();

    @Test
    public void testValidateExcel_NoErrors() throws Exception {
        MockMultipartFile validFile = createExcelFile(new String[][]{
                {"ชื่อ", "อีเมล", "บัตรประชาชน", "เบอร์โทร", "ที่อยู่"},
                {"สมชาย ใจดี", "example@test.com", "1419902114908", "0812345678", "123 Main St"}
        });

        List<Map<String, Object>> errors = dynamicCheckingService.validateExcel(validFile);
        assertEquals(0, errors.size(), "ควรไม่มีข้อผิดพลาด");
    }

    @Test
    public void testValidateExcel_WithErrors() throws Exception {
        MockMultipartFile invalidFile = createExcelFile(new String[][]{
                {"ชื่อ", "อีเมล", "บัตรประชาชน", "เบอร์โทร", "ที่อยู่"},
                {"สมชาย123", "invalid-email", "123", "12345", "<invalid>"}
        });

        List<Map<String, Object>> errors = dynamicCheckingService.validateExcel(invalidFile);
        assertTrue(errors.size() > 0, "ควรมีข้อผิดพลาดในแถวที่ 2");

        // Check if error details exist
        Map<String, Object> errorDetails = errors.get(0);
        assertEquals(2, errorDetails.get("row"));
        assertTrue(errorDetails.containsKey("message"), "ต้องมีข้อความข้อผิดพลาด");
    }

    @Test
    public void testValidateExcel_NoHeaders() throws Exception {
        MockMultipartFile noHeaderFile = createExcelFile(new String[][]{});

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                dynamicCheckingService.validateExcel(noHeaderFile));
        assertEquals("ไม่มีแถวหัวข้อในไฟล์ Excel", exception.getMessage());
    }

    @Test
    public void testValidateExcelWithSelectedHeaders_NotFound() throws Exception {
        MockMultipartFile validFile = createExcelFile(new String[][]{
                {"ชื่อ", "อีเมล" ,"เบอร์โทร"},
                {"สมชาย ใจดี", "example@test.com", "0938073675"}
        });

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                dynamicCheckingService.validateExcelWithSelectedHeaders(validFile, List.of("วันเกิด")));
        assertEquals("พบหัวข้อที่ไม่สามารถตรวจสอบได้: วันเกิด", exception.getMessage());
    }

    @Test
    public void testValidateExcel_EmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[0]);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                dynamicCheckingService.validateExcel(emptyFile));
        assertEquals("ไฟล์ว่างเปล่า ไม่สามารถอ่านข้อมูลได้", exception.getMessage());
    }

    private MockMultipartFile createExcelFile(String[][] data) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();

        for (int i = 0; i < data.length; i++) {
            Row row = sheet.createRow(i);
            for (int j = 0; j < data[i].length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(data[i][j]);
            }
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return new MockMultipartFile("file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new ByteArrayInputStream(outputStream.toByteArray()));
    }
}
