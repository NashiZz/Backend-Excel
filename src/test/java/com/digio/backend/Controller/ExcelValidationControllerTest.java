package com.digio.backend.Controller;

import com.digio.backend.Service.DynamicValidationService;
import com.digio.backend.Service.TemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ExcelValidationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DynamicValidationService dynamicCheckingService;

    @MockitoBean
    private TemplateService templateService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void test_emptyExcelFile() throws Exception{
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "application/vnd.ms-excel", new byte[0]);

        mockMvc.perform(multipart("/api/excel/dynamic")
                        .file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(
                        Collections.singletonMap("message", "โปรดอัปโหลดไฟล์ Excel ที่ถูกต้อง")
                )));
    }

    @Test
    public void test_dynamicWithErrors() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile("file", "test.xlsx", "application/vnd.ms-excel", "dummy content".getBytes());

        // ปรับแก้คืนค่าที่เป็น List<Map<String, Object>> แทน List<String>
        Map<String, Object> error1 = new HashMap<>();
        error1.put("error", "Error 1");
        Map<String, Object> error2 = new HashMap<>();
        error2.put("error", "Error 2");

        Mockito.when(dynamicCheckingService.validateExcel(invalidFile))
                .thenReturn(List.of(error1, error2));

        mockMvc.perform(multipart("/api/excel/dynamic")
                        .file(invalidFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].error").value("Error 1"))
                .andExpect(jsonPath("$.errors[1].error").value("Error 2"));
    }

    @Test
    public void test_DynamicWithNoErrors() throws Exception {
        MockMultipartFile validFile = new MockMultipartFile("file", "test.xlsx", "application/vnd.ms-excel", "dummy content".getBytes());

        Mockito.when(dynamicCheckingService.validateExcel(validFile)).thenReturn(Collections.emptyList());

        mockMvc.perform(multipart("/api/excel/dynamic")
                        .file(validFile))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(
                        Collections.singletonMap("message", "ไฟล์ Excel ถูกต้อง ไม่มีข้อผิดพลาด")
                )));
    }

    @Test
    public void test_headerPartButNoHeadersProvided() throws Exception {
        MockMultipartFile validFile = new MockMultipartFile("file", "test.xlsx", "application/vnd.ms-excel", "dummy content".getBytes());

        mockMvc.perform(multipart("/api/excel/headers")
                        .file(validFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(
                        Collections.singletonMap("message", "โปรดระบุหัวข้อที่ต้องการตรวจสอบ")
                )));
    }

    @Test
    public void test_emptyTemplateExcelFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "application/vnd.ms-excel", new byte[0]);

        mockMvc.perform(multipart("/api/excel/template")
                        .file(emptyFile)
                        .param("condition", "header1,header2")
                        .param("calculater", "calc1")
                        .param("relation", "rel1")
                        .param("compare", "comp1"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(
                        Collections.singletonMap("message", "โปรดอัปโหลดไฟล์ Excel ที่ถูกต้อง")
                )));
    }

    @Test
    public void test_dynamicTemplateWithErrors() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile("file", "test.xlsx", "application/vnd.ms-excel", "dummy content".getBytes());

        // Mock ค่าผลลัพธ์เป็น List<Map<String, Object>>
        Map<String, Object> error1 = new HashMap<>();
        error1.put("error", "Missing Header");
        Map<String, Object> error2 = new HashMap<>();
        error2.put("error", "Invalid Data");

        Mockito.when(templateService.handleUploadWithTemplate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(List.of(error1, error2));

        mockMvc.perform(multipart("/api/excel/template")
                        .file(invalidFile)
                        .param("condition", "header1,header2")
                        .param("calculater", "calc1")
                        .param("relation", "rel1")
                        .param("compare", "comp1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].error").value("Missing Header"))
                .andExpect(jsonPath("$.errors[1].error").value("Invalid Data"));
    }

    @Test
    public void test_DynamicTemplateWithNoErrors() throws Exception {
        MockMultipartFile validFile = new MockMultipartFile("file", "test.xlsx", "application/vnd.ms-excel", "dummy content".getBytes());

        Mockito.when(templateService.handleUploadWithTemplate(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(multipart("/api/excel/template")
                        .file(validFile)
                        .param("condition", "header1,header2")
                        .param("calculater", "calc1")
                        .param("relation", "rel1")
                        .param("compare", "comp1"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(
                        Collections.singletonMap("message", "ไฟล์ Excel ถูกต้อง ไม่มีข้อผิดพลาด")
                )));
    }
}
