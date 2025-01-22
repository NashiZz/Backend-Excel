//package com.digio.backend.Validate;
//
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//public class CitizenIdValidatorTest {
//
//    @Test
//    void validate_ShouldReturnError_WhenCitizenIdIsNull() {
//        String citizenId = null;
//
//        String result = CitizenIdValidator.validate(citizenId);
//
//        assertEquals("บัตรประชาชนไม่ถูกต้อง", result, "Null ID should return an error message");
//    }
//
//    @Test
//    void validate_ShouldReturnError_WhenCitizenIdIsEmpty() {
//        String citizenId = "";
//
//        String result = CitizenIdValidator.validate(citizenId);
//
//        assertEquals("บัตรประชาชนไม่ถูกต้อง", result, "Empty ID should return an error message");
//    }
//
//    @Test
//    void validate_ShouldReturnError_WhenCitizenIdIsNot13Digits() {
//        String citizenId = "123456่";
//
//        String result = CitizenIdValidator.validate(citizenId);
//
//        assertEquals("บัตรประชาชนไม่ถูกต้อง", result, "Invalid ID should return an error message");
//    }
//
//    @Test
//    void validate_ShouldReturnError_WhenCitizenIdHasMoreThan13Digits() {
//        String citizenId = "14199023450876";
//
//        String result = CitizenIdValidator.validate(citizenId);
//
//        assertEquals("บัตรประชาชนไม่ถูกต้อง", result, "ID with more than 13 digits should return an error message");
//    }
//
//    @Test
//    void validate_ShouldReturnError_WhenCitizenIdIsInvalidCheckDigit() {
//        String citizenId = "1419902114907";
//
//        String result = CitizenIdValidator.validate(citizenId);
//
//        assertEquals("บัตรประชาชนไม่ถูกต้อง", result, "Invalid check digit should return an error message");
//    }
//
//    @Test
//    void validate_ShouldReturnError_WhenCitizenIdHasSpecialCharacters() {
//        String citizenId = "1234#6789012";
//
//        String result = CitizenIdValidator.validate(citizenId);
//
//        assertEquals("บัตรประชาชนไม่ถูกต้อง", result, "ID with special characters should return an error message");
//    }
//
//    @Test
//    void validate_ShouldReturnSuccess_WhenCitizenIdIsValid() {
//        String citizenId = "1419902114908";
//
//        String result = CitizenIdValidator.validate(citizenId);
//
//        assertEquals("success", result, "Valid ID should return success");
//    }
//}
