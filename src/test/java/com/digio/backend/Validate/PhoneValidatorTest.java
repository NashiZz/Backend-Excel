package com.digio.backend.Validate;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PhoneValidatorTest {

    @Test
    void validate_ShouldReturnSuccess_WhenPhoneNumberIsValid() {
        String phoneNumber = "0812345678";

        String result = PhoneValidator.validate(phoneNumber);

        assertEquals("success", result, "Valid phone number should return success");
    }

    @Test
    void validate_ShouldReturnError_WhenPhoneNumberIsNull() {
        String phoneNumber = null;

        String result = PhoneValidator.validate(phoneNumber);

        assertEquals("หมายเลขโทรศัพท์ไม่ถูกต้อง", result, "Null phone number should return an error message");
    }

    @Test
    void validate_ShouldReturnError_WhenPhoneNumberIsEmpty() {
        String phoneNumber = "";

        String result = PhoneValidator.validate(phoneNumber);

        assertEquals("หมายเลขโทรศัพท์ไม่ถูกต้อง", result, "Empty phone number should return an error message");
    }

    @Test
    void validate_ShouldReturnError_WhenPhoneNumberHasInvalidFormat() {
        String phoneNumber = "08123";

        String result = PhoneValidator.validate(phoneNumber);

        assertEquals("หมายเลขโทรศัพท์ไม่ถูกต้อง", result, "Phone number with incorrect format should return an error message");
    }

    @Test
    void validate_ShouldReturnError_WhenPhoneNumberStartsWithInvalidDigit() {
        String phoneNumber = "09123456781";

        String result = PhoneValidator.validate(phoneNumber);

        assertEquals("หมายเลขโทรศัพท์ไม่ถูกต้อง", result, "Phone number starting with an invalid digit should return an error message");
    }
}

