package com.digio.backend.Validate;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NameValidatorTest {

    @Test
    void validate_ShouldReturnError_WhenNameIsNull() {
        String name = null;

        String result = NameValidator.validate(name);

        assertEquals("ชื่อไม่ควรว่าง", result, "Null name should return an error message");
    }

    @Test
    void validate_ShouldReturnError_WhenNameIsBlank() {
        String name = "   ";

        String result = NameValidator.validate(name);

        assertEquals("ชื่อไม่ควรว่าง", result, "Blank name should return an error message");
    }

    @Test
    void validate_ShouldReturnError_WhenNameContainsInvalidKeyword() {
        String name = "unknown";

        String result = NameValidator.validate(name);

        assertEquals("ชื่อไม่ถูกต้อง", result, "Name containing invalid keyword should return an error message");
    }

    @Test
    void validate_ShouldReturnError_WhenNameIsAnEmail() {
        String name = "test@example.com";

        String result = NameValidator.validate(name);

        assertEquals("ชื่อไม่ควรเป็นอีเมล", result, "Name being an email should return an error message");
    }

    @Test
    void validate_ShouldReturnError_WhenNameIsPhoneNumber() {
        String name = "0123456789";

        String result = NameValidator.validate(name);

        assertEquals("ชื่อไม่ควรเป็นหมายเลขโทรศัพท์", result, "Name being a phone number should return an error message");
    }

    @Test
    void validate_ShouldReturnError_WhenNameContainsSpecialCharacters() {
        String name = "John@Doe";

        String result = NameValidator.validate(name);

        assertEquals("ชื่อควรมีเฉพาะตัวอักษรไทยหรือภาษาอังกฤษ และไม่มีอักขระพิเศษ", result, "Name containing special characters should return an error message");
    }

    @Test
    void validate_ShouldReturnError_WhenNameIsOnlyFirstOrLastName() {
        String name = "John";

        String result = NameValidator.validate(name);

        assertEquals("กรุณาระบุชื่อและนามสกุล", result, "Name with only first name should return an error message");
    }

    @Test
    void validate_ShouldReturnError_WhenNamePartIsTooShort() {
        String name = "J T";

        String result = NameValidator.validate(name);

        assertEquals("ชื่อและนามสกุลควรมีความยาวอย่างน้อย 2 ตัวอักษร และไม่เกิน 50 ตัวอักษร", result, "Name part too short should return an error message");
    }

    @Test
    void validate_ShouldReturnError_WhenNamePartIsTooLong() {
        String name = "A".repeat(51) + " B";

        String result = NameValidator.validate(name);

        assertEquals("ชื่อและนามสกุลควรมีความยาวอย่างน้อย 2 ตัวอักษร และไม่เกิน 50 ตัวอักษร", result, "Name part too long should return an error message");
    }

    @Test
    void validate_ShouldReturnSuccess_WhenNameIsValid() {
        String name = "John Doe";

        String result = NameValidator.validate(name);

        assertEquals("success", result, "Valid name should return success");

        name = "nashi nanase";

        result = NameValidator.validate(name);

        assertEquals("success", result, "Valid name in Thai should return success");
    }
}
