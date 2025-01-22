package com.digio.backend.Validate;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailValidateTest {

    @Test
    void validate_ShouldReturnError_WhenEmailIsNull() {
        String email = null;

        String result = EmailValidate.validate(email);

        assertEquals("อีเมลไม่ถูกต้อง", result, "Null email should return an error message");
    }

    @Test
    void validate_ShouldReturnError_WhenEmailIsEmpty() {
        String email = "";

        String result = EmailValidate.validate(email);

        assertEquals("อีเมลไม่ถูกต้อง", result, "Empty email should return an error message");
    }

    @Test
    void validate_ShouldReturnError_WhenEmailIsInvalidFormat() {
        String email = "plainaddress";

        String result = EmailValidate.validate(email);

        assertEquals("อีเมลไม่ถูกต้อง", result, "Invalid email format should return an error message");
    }

    @Test
    void validate_ShouldReturnError_WhenEmailHasInvalidCharacters() {
        String email = "user@.com";

        String result = EmailValidate.validate(email);

        assertEquals("อีเมลไม่ถูกต้อง", result, "Email with invalid characters should return an error message");
    }

    @Test
    void validate_ShouldReturnError_WhenEmailIsTooLong() {
        String email = "a".repeat(51) + "@example.com";

        String result = EmailValidate.validate(email);

        assertEquals("อีเมลไม่ถูกต้อง", result, "Email with more than 50 characters should return an error message");
    }

    @Test
    void validate_ShouldReturnSuccess_WhenEmailIsValid() {
        String email = "test@example.com";

        String result = EmailValidate.validate(email);

        assertEquals("success", result, "Valid email should return success");

        email = "user.name+tag+sorting@example.com";

        result = EmailValidate.validate(email);

        assertEquals("success", result, "Valid email with special characters should return success");
    }
}

