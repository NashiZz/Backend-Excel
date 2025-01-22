package com.digio.backend.Validate;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AgeValidatorTest {

    @Test
    void validate_ShouldReturnError_WhenDateOfBirthIsEmpty() {
        String dob = "";

        String result = AgeValidator.validateDateOfBirth(dob);

        assertEquals("วันเกิดไม่ควรว่าง", result, "Date of birth should not be empty");
    }

    @Test
    void validate_ShouldReturnError_WhenDateOfBirthIsInvalidFormat() {
        String dob = "31-12-1990";

        String result = AgeValidator.validateDateOfBirth(dob);

        assertEquals("รูปแบบวันเกิดไม่ถูกต้อง", result, "Date of birth should have a valid format");
    }

    @Test
    void validate_ShouldReturnError_WhenDateOfBirthIsFutureDate() {
        String dob = "01/12/2025";

        String result = AgeValidator.validateDateOfBirth(dob);

        assertEquals("วันเกิดไม่สามารถเป็นวันที่ในอนาคต", result, "Date of birth should not be in the future");
    }

    @Test
    void validate_ShouldReturnError_WhenAgeIsUnder18() {
        String dob = "01/01/2010";

        String result = AgeValidator.validateDateOfBirth(dob);

        assertEquals("อายุไม่ถึงขั้นต่ำที่กำหนด (ต้องมีอายุอย่างน้อย 18 ปี)", result, "Age should be at least 18 years old");
    }

    @Test
    void validate_ShouldReturnSuccess_WhenAgeIs18OrAbove() {
        String dob = "01/01/2005";

        String result = AgeValidator.validateDateOfBirth(dob);

        assertEquals("success", result, "Age should be 18 or above");
    }

    @Test
    void validate_ShouldReturnSuccess_WhenDateOfBirthIsValid() {
        String dob = "2003-01-15";

        String result = AgeValidator.validateDateOfBirth(dob);

        assertEquals("success", result, "Valid date of birth should return success");
    }
}

