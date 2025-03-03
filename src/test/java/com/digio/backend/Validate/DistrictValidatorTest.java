package com.digio.backend.Validate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DistrictValidatorTest {

    @Test
    void test_validateDistrict(){
        String district = "มีนบุรี";
        String result = DistrictValidator.validateDistrict(district);

        assertEquals("success",result);
    }

    @Test
    void test_validateRandomWord(){
        String district = " สวัสดี ";
        String result = DistrictValidator.validateDistrict(district);

        assertEquals("success",result);
    }

    @Test
    void test_validateWhiteSpace(){
        String district = "มีน บุ รี";
        String result = DistrictValidator.validateDistrict(district);

        assertEquals("success", result);
    }

    @Test
    void test_validateNull(){
        String district = "";
        String result = DistrictValidator.validateDistrict(district);

        assertEquals("ชื่ออำเภอไม่ควรว่าง",result);
    }

    @Test
    void test_validateNull2(){
        String district = " ";
        String result = DistrictValidator.validateDistrict(district);

        assertEquals("ชื่ออำเภอไม่ควรว่าง", result);
    }

    @Test
    void test_validateEnglishDistrict(){
        String district = "Bangna";
        String result = DistrictValidator.validateDistrict(district);

        assertEquals("รูปแบบชื่ออำเภอไม่ถูกต้อง",result);
    }

    @Test
    void test_validateNumber(){
        String district = "Bangna777";
        String result = DistrictValidator.validateDistrict(district);

        assertEquals("รูปแบบชื่ออำเภอไม่ถูกต้อง",result);
    }

    @Test
    void test_validateSpecificSymbol(){
        String district = "บางนา !";
        String result = DistrictValidator.validateDistrict(district);

        assertEquals("รูปแบบชื่ออำเภอไม่ถูกต้อง", result);
    }

    @Test
    void test_validateSpecificSymbol2(){
        String district = "มีน_บุรี ?";
        String result = DistrictValidator.validateDistrict(district);

        assertEquals("รูปแบบชื่ออำเภอไม่ถูกต้อง",result);
    }
}
