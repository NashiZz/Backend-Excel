package com.digio.backend.Controller;

import com.digio.backend.Service.DynamicValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ExcelValidationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DynamicValidationService dynamicCheckingService;

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

        Mockito.when(dynamicCheckingService.validateExcel(invalidFile))
                .thenReturn(List.of("Error 1", "Error 2"));

        mockMvc.perform(multipart("/api/excel/dynamic")
                        .file(invalidFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value("Error 1"))
                .andExpect(jsonPath("$.errors[1]").value("Error 2"));
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
}
