package com.digio.backend.Validate;

public class PhoneValidator {
    public static String validate(String phoneNum) {
        StringBuilder errorBuilder = new StringBuilder();

        if (phoneNum == null || !phoneNum.matches("^0[1-9][0-9]{8}$")) {
            appendError(errorBuilder, "หมายเลขโทรศัพท์ไม่ถูกต้อง");
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
