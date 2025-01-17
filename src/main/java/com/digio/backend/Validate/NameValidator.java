package com.digio.backend.Validate;

import org.apache.commons.validator.routines.EmailValidator;

import java.util.List;

public class NameValidator {
    private static final int MAX_NAME_LENGTH = 50;
    private static final int PHONE_NUMBER_LENGTH = 10;
    private static final List<String> INVALID_KEYWORDS = List.of("unknown", "invalid", "n/a", "not specified");

    public static String validate(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "ชื่อไม่ควรว่าง";
        }

        String trimmedName = name.trim().toLowerCase();

        if (INVALID_KEYWORDS.stream().anyMatch(trimmedName::contains)) {
            return "ชื่อไม่ถูกต้อง";
        }

        if (EmailValidator.getInstance().isValid(name)) {
            return "ชื่อไม่ควรเป็นอีเมล";
        }

        if (name.matches("^\\d{" + PHONE_NUMBER_LENGTH + "}$")) {
            return "ชื่อไม่ควรเป็นหมายเลขโทรศัพท์";
        }

        if (name.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            return "ชื่อไม่ควรมีอักขระพิเศษ";
        }

        if (name.matches(".*\\d.*")) {
            return "ชื่อไม่ควรมีตัวเลข";
        }

        if (name.contains("  ")) {
            return "ชื่อไม่ควรมีช่องว่างซ้ำ";
        }

        if (!name.matches("^[ก-๙A-Za-z\\s]+$")) {
            return "ชื่อควรมีเฉพาะตัวอักษรไทยหรือภาษาอังกฤษ";
        }

        String[] nameParts = name.split("\\s+");
        if (nameParts.length < 2) {
            return "กรุณาระบุชื่อและนามสกุล";
        }

        String firstName = nameParts[0];
        if (firstName.length() > MAX_NAME_LENGTH) {
            return "ชื่อยาวเกินไป";
        }
        if (firstName.length() < 2) {
            return "ชื่อควรมีความยาวอย่างน้อย 2 ตัวอักษร";
        }

        String lastName = nameParts[nameParts.length - 1];
        if (lastName.length() > MAX_NAME_LENGTH) {
            return "นามสกุลยาวเกินไป";
        }
        if (lastName.length() < 2) {
            return "นามสกุลควรมีความยาวอย่างน้อย 2 ตัวอักษร";
        }

        return null;
    }
}