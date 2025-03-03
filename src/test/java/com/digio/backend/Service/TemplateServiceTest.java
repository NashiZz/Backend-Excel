package com.digio.backend.Service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    @InjectMocks
    private TemplateService templateService;

    @BeforeEach
    void setup(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void test_uploadEmptyFile(){
        MultipartFile emptyFile = new MockMultipartFile("file.xlsx", new byte[0]);

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> templateService.handleUploadWithTemplate(emptyFile, List.of(),List.of(), List.of(), List.of())
        );

        assertEquals("ไฟล์ว่างเปล่า ไม่สามารถอ่านข้อมูลได้", thrown.getMessage());
    }

    @Test
    public void test_uploadWithEmptySheet() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (Workbook workbook = new XSSFWorkbook()) {
            workbook.createSheet("Sheet1");
            workbook.write(out);
        } catch (IOException e) {
            fail("เกิดข้อผิดพลาดขณะเขียนไฟล์ Excel: " + e.getMessage());
        }

        MultipartFile file = new MockMultipartFile("file.xlsx", out.toByteArray());

        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> templateService.handleUploadWithTemplate(file, List.of(), List.of(), List.of(), List.of())
        );

        assertEquals("ไฟล์นี้ไม่มีข้อมูล", thrown.getMessage());
    }

    @Test
    public void testHandleUploadWithValidFile() throws IOException {
        MultipartFile validFile = createExcelFile(new String[][]{
                {"ชื่อ", "อีเมล", "บัตรประชาชน"},
                {"สมชาย", "test@example.com", "1234567890123"}
        });

        List<Map<String, Object>> result = templateService.handleUploadWithTemplate(
                validFile,
                List.of("ชื่อ", "อีเมล", "บัตรประชาชน"),
                List.of(), List.of(), List.of()
        );

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    private MultipartFile createExcelFile(String[][] data) throws IOException {
        Workbook workbook = WorkbookFactory.create(true);
        var sheet = workbook.createSheet("Sheet1");

        for (int i = 0; i < data.length; i++) {
            var row = sheet.createRow(i);
            for (int j = 0; j < data[i].length; j++) {
                row.createCell(j).setCellValue(data[i][j]);
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        return new MockMultipartFile("file.xlsx", out.toByteArray());
    }


    @Test
    public void testValidateExcel_WithErrors() throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ชื่อ");
        headerRow.createCell(1).setCellValue("อีเมล");
        headerRow.createCell(2).setCellValue("บัตรประชาชน");

        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue("สมชาย");
        row.createCell(1).setCellValue("test@example.com");
        row.createCell(2).setCellValue("123"); // ค่าผิด

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        workbook.write(byteArrayOutputStream);
        byte[] fileBytes = byteArrayOutputStream.toByteArray();
        workbook.close();

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "testFile.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                fileBytes);

        List<Map<String, Object>> result = templateService.handleUploadWithTemplate(
                mockFile,
                List.of("ชื่อ", "อีเมล", "บัตรประชาชน"),
                List.of(), List.of(), List.of());

        assertNotNull(result);
    }

}
