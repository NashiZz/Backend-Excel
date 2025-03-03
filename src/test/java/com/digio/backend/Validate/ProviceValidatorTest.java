package com.digio.backend.Validate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProvinceValidatorTest {

    @Test
    void Test_validateProvince(){
        String province = "กรุงเทพมหานคร";
        String result = ProvinceValidator.validateProvince(province);

        assertEquals("success",result);
    }

    @Test
    void Test_validateNull(){
        String province = "";
        String result = ProvinceValidator.validateProvince(province);

        assertEquals("ชื่อจังหวัดไม่ควรว่าง",result);
    }

    @Test
    void Test_validateSpaceProvince(){
        String province = "กระ บี่";
        String result = ProvinceValidator.validateProvince(province);

        assertEquals("ชื่อจังหวัดไม่ถูกต้อง", result);
    }

    @Test
    void Test_validateEnglishProvince(){
        String province = "Bangkok";
        String result = ProvinceValidator.validateProvince(province);

        assertEquals("รูปแบบชื่อจังหวัดไม่ถูกต้อง",result);
    }

    @Test
    void Test_validateNumber(){
        String province = "948375";
        String result = ProvinceValidator.validateProvince(province);

        assertEquals("รูปแบบชื่อจังหวัดไม่ถูกต้อง",result);
    }

    @Test
    void Test_validateSpamWord(){
        String province = "กรุงเทพมหานครกระบี่กาญจนบุรี";
        String result = ProvinceValidator.validateProvince(province);

        assertEquals("ชื่อจังหวัดไม่ถูกต้อง",result);
    }

    @Test
    void Test_validateSpecialChar(){
        String province = "ระยอง!!";
        String result = ProvinceValidator.validateProvince(province);

        assertEquals("รูปแบบชื่อจังหวัดไม่ถูกต้อง",result);
    }

}
