package com.digio.backend.Validate;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GenderValidatorTest {

    @Test
    void validate_ShouldReturnSuccess_WhenGenderIsValid() {
        String gender = "ชาย";

        String result = GenderValidator.validateGender(gender);

        assertEquals("success", result, "Valid gender 'ชาย' should return success");
    }

    @Test
    void validate_ShouldReturnSuccess_WhenGenderIsValid_ThirdGender() {
        String gender = "เพศที่สาม";

        String result = GenderValidator.validateGender(gender);

        assertEquals("success", result, "Valid gender 'เพศที่สาม' should return success");
    }

    @Test
    void validate_ShouldReturnError_WhenGenderIsEmpty() {
        String gender = "";

        String result = GenderValidator.validateGender(gender);

        assertEquals("เพศไม่ควรว่าง", result, "Gender should not be empty");
    }

    @Test
    void validate_ShouldReturnError_WhenGenderIsInvalid() {
        String gender = "ไม่ใช่เพศ";

        String result = GenderValidator.validateGender(gender);

        assertEquals("เพศไม่ถูกต้อง", result, "Invalid gender 'ไม่ใช่เพศ' should return error");

        gender = "สมจิตร";

        result = GenderValidator.validateGender(gender);

        assertEquals("เพศไม่ถูกต้อง", result, "Invalid gender 'ไม่ใช่เพศ' should return error");
    }
}

