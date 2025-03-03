package com.digio.backend.Validate;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AddressValidatorTest {

    @Test
    void validate_ShouldReturnError_WhenAddressIsBlank() {
        String address = "   ";

        String result = AddressValidator.validate(address);

        assertEquals("ที่อยู่ไม่ควรว่าง", result, "Blank address should return an error message");
    }

    @Test
    void validate_ShouldReturnError_WhenAddressContainsInvalidKeyword() {
        String address = "This is an unknown address";

        String result = AddressValidator.validate(address);

        assertEquals("ที่อยู่ไม่ถูกต้อง", result, "Address containing invalid keywords should return an error message");
    }

    @Test
    void validate_ShouldReturnError_WhenAddressIsTooShort() {
        String address = "A street";

        String result = AddressValidator.validate(address);

        assertEquals("ที่อยู่มีความยาวหรือสั้นเกินไป", result, "Address shorter than minimum length should return an error message");
    }

    @Test
    void validate_ShouldReturnError_WhenAddressIsTooLong() {
        String address = "A very long address that exceeds the allowed limit of one hundred characters for the address validation check in the system";

        String result = AddressValidator.validate(address);

        assertEquals("ที่อยู่มีความยาวหรือสั้นเกินไป", result, "Address longer than maximum length should return an error message");
    }

    @Test
    void validate_ShouldReturnError_WhenAddressHasRepeatedCharacters() {
        String address = "aaaaa address";

        String result = AddressValidator.validate(address);

        assertEquals("ที่อยู่ไม่ควรมีตัวอักษรหรือตัวเลขซ้ำกัน", result, "Address with repeated characters should return an error message");
    }

    @Test
    void validate_ShouldReturnError_WhenAddressHasSpecialCharacters() {
        String address = "Invalid @address #123";

        String result = AddressValidator.validate(address);

        assertEquals("รูปแบบที่อยู่ไม่ถูกต้อง", result, "Address with special characters should return an error message");
    }

    @Test
    void validate_ShouldReturnError_WhenAddressHasInvalidFormat() {
        String address = "123 Invalid Address!";

        String result = AddressValidator.validate(address);

        assertEquals("รูปแบบที่อยู่ไม่ถูกต้อง", result, "Address with invalid format should return an error message");
    }

    @Test
    void validate_ShouldReturnSuccess_WhenAddressIsValid() {
        String address = "123 Main Street, Test City";

        String result = AddressValidator.validate(address);

        assertEquals("success", result, "Valid address should return success");
    }

    @Test
    void validate_ShouldReturnSuccess_WhenAddressIsValidWithDifferentCharacters() {
        String address = "บ้านเลขที่ 123 ถนนทดสอบ";

        String result = AddressValidator.validate(address);

        assertEquals("success", result, "Valid address in Thai should return success");
    }
}

