package com.digio.backend.Validate;

import java.util.List;

public class AddressValidator {
    private static final int MAX_ADDRESS_LENGTH = 100;
    private static final List<String> INVALID_KEYWORDS = List.of("unknown", "invalid", "n/a", "not specified");

    public static String validate(String address) {
        StringBuilder errorBuilder = new StringBuilder();

        if (address == null || address.trim().isEmpty()) {
            appendError(errorBuilder, "ที่อยู่ไม่ควรว่าง");
        } else if (address.length() > MAX_ADDRESS_LENGTH) {
            appendError(errorBuilder, "ที่อยู่ยาวเกินไป");
        } else if (INVALID_KEYWORDS.stream().anyMatch(keyword -> address.trim().equalsIgnoreCase(keyword))) {
            appendError(errorBuilder, "ที่อยู่่ไม่ถูกต้อง");
        } else if (address.matches(".*[<>#&@].*")) {
            appendError(errorBuilder, "ที่อยู่ไม่ควรมีอักขระพิเศษ");
        } else if (!address.matches("^[ก-๙A-Za-z0-9\\s,.-]+$")) {
            appendError(errorBuilder, "รูปแบบที่อยู่ไม่ถูกต้อง");
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
