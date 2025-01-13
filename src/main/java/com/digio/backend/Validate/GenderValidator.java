package com.digio.backend.Validate;

import java.util.List;

public class GenderValidator {

    private static final List<String> VALID_GENDER_VALUES = List.of("ชาย", "หญิง", "ไม่ระบุ");

    public static String validateGender(String gender) {
        StringBuilder errorBuilder = new StringBuilder();

        if (gender == null || gender.trim().isEmpty()) {
            appendError(errorBuilder, "เพศไม่ควรว่าง");
        } else if (!VALID_GENDER_VALUES.contains(gender.trim())) {
            appendError(errorBuilder, "เพศไม่ถูกต้อง กรุณากรอก 'ชาย', 'หญิง', หรือ 'ไม่ระบุ'");
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
