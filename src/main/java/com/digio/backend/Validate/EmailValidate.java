package com.digio.backend.Validate;

import org.apache.commons.validator.routines.EmailValidator;

public class EmailValidate {
    private static final int MAX_NAME_LENGTH = 50;

    public static String validate(String email) {
        StringBuilder errorBuilder = new StringBuilder();

        if (email == null || !EmailValidator.getInstance().isValid(email) || email.length() > MAX_NAME_LENGTH) {
            appendError(errorBuilder, "อีเมลไม่ถูกต้อง");
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
