package com.digio.backend.Validate;

import org.apache.commons.validator.routines.EmailValidator;

import java.util.List;

public class NameValidator {
    private static final int MAX_NAME_LENGTH = 50;
    private static final int PHONE_NUMBER_LENGTH = 10;
    private static final List<String> INVALID_KEYWORDS = List.of("unknown", "invalid", "n/a", "not specified");

    public static String validate(String name) {
        StringBuilder errorBuilder = new StringBuilder();

        if (name == null || name.trim().isEmpty()) {
            appendError(errorBuilder, "ชื่อไม่ควรว่าง");
            return errorBuilder.toString();
        }

        String trimmedName = name.trim().toLowerCase();

        if (INVALID_KEYWORDS.stream().anyMatch(trimmedName::contains)) {
            appendError(errorBuilder, "ชื่อไม่ถูกต้อง");
        }

        if (EmailValidator.getInstance().isValid(name)) {
            appendError(errorBuilder, "ชื่อไม่ควรเป็นอีเมล");
        }

        if (name.matches("^\\d{" + PHONE_NUMBER_LENGTH + "}$")) {
            appendError(errorBuilder, "ชื่อไม่ควรเป็นหมายเลขโทรศัพท์");
        }

        if (name.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            appendError(errorBuilder, "ชื่อไม่ควรมีอักขระพิเศษ");
        }

        if (name.matches(".*\\d.*")) {
            appendError(errorBuilder, "ชื่อไม่ควรมีตัวเลข");
        }

        if (name.contains("  ")) {
            appendError(errorBuilder, "ชื่อไม่ควรมีช่องว่างซ้ำ");
        }

        if (!name.matches("^[ก-๙A-Za-z\\s]+$")) {
            appendError(errorBuilder, "ชื่อควรมีเฉพาะตัวอักษรไทยหรือภาษาอังกฤษ");
        }

        String[] nameParts = name.split("\\s+");
        if (nameParts.length < 2) {
            appendError(errorBuilder, "กรุณาระบุชื่อและนามสกุล");
        } else {
            String firstName = nameParts[0];
            if (firstName.length() > MAX_NAME_LENGTH) {
                appendError(errorBuilder, "ชื่อยาวเกินไป");
            } else if (firstName.length() < 2) {
                appendError(errorBuilder, "ชื่อควรมีความยาวอย่างน้อย 2 ตัวอักษร");
            }

            String lastName = nameParts[nameParts.length - 1];
            if (lastName.length() > MAX_NAME_LENGTH) {
                appendError(errorBuilder, "นามสกุลยาวเกินไป");
            } else if (lastName.length() < 2) {
                appendError(errorBuilder, "นามสกุลควรมีความยาวอย่างน้อย 2 ตัวอักษร");
            }
        }

        return errorBuilder.isEmpty() ? null : errorBuilder.toString();
    }

    private static void appendError(StringBuilder errorBuilder, String errorMessage) {
        if (!errorBuilder.isEmpty()) {
            errorBuilder.append(", ");
        }
        errorBuilder.append(errorMessage);
    }
}