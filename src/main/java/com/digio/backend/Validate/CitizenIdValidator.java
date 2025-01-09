package com.digio.backend.Validate;

public class CitizenIdValidator {
    public static String validate(String citizenId) {
        StringBuilder errorBuilder = new StringBuilder();

        if (citizenId == null || !citizenId.matches("^\\d{13}$")) {
            appendError(errorBuilder, "บัตรประชาชนไม่ถูกต้อง");
        } else if (!isValidCitizenId(citizenId)) {
            appendError(errorBuilder, "บัตรประชาชนไม่ถูกต้อง");
        }

        return errorBuilder.isEmpty() ? null : errorBuilder.toString();
    }

    private static boolean isValidCitizenId(String citizenId) {
        if (citizenId.length() != 13) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(citizenId.charAt(i));
            sum += digit * (13 - i);
        }
        int checkDigit = (11 - (sum % 11)) % 10;
        int lastDigit = Character.getNumericValue(citizenId.charAt(12));
        return checkDigit == lastDigit;
    }

    private static void appendError(StringBuilder errorBuilder, String errorMessage) {
        if (!errorBuilder.isEmpty()) {
            errorBuilder.append(", ");
        }
        errorBuilder.append(errorMessage);
    }
}
