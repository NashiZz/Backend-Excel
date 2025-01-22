package com.digio.backend.Validate;

import java.util.List;

public class AddressValidator {
    private static final int MAX_ADDRESS_LENGTH = 100;
    private static final int MIN_ADDRESS_LENGTH = 10;
    private static final List<String> INVALID_KEYWORDS = List.of("unknown", "invalid", "n/a", "not specified");

    public static String validate(String address) {
        if (isNullOrEmpty(address)) {
            return "ที่อยู่ไม่ควรว่าง";
        }

        String trimmedAddress = address.trim().toLowerCase();

        if (containsInvalidKeywords(trimmedAddress)) {
            return "ที่อยู่ไม่ถูกต้อง";
        }

        if (trimmedAddress.length() > MAX_ADDRESS_LENGTH) {
            return "ที่อยู่ยาวเกินไป";
        }

        if (trimmedAddress.length() < MIN_ADDRESS_LENGTH) {
            return "ที่อยู่สั้นเกินไป";
        }

        if (hasRepeatedCharacters(trimmedAddress)) {
            return "ที่อยู่ไม่ควรมีตัวอักษรหรือตัวเลขซ้ำกัน";
        }

        if (hasSpecialCharacters(trimmedAddress)) {
            return "ที่อยู่ไม่ควรมีอักขระพิเศษ";
        }

        if (!isValidFormat(trimmedAddress)) {
            return "รูปแบบที่อยู่ไม่ถูกต้อง";
        }

        return "success";
    }

    private static boolean isNullOrEmpty(String address) {
        return address == null || address.trim().isEmpty();
    }

    private static boolean containsInvalidKeywords(String address) {
        return INVALID_KEYWORDS.stream().anyMatch(address::contains);
    }

    private static boolean hasRepeatedCharacters(String address) {
        return address.matches(".*(.)\\1{4,}.*");
    }

    private static boolean hasSpecialCharacters(String address) {
        return address.matches(".*[<>#&@].*");
    }

    private static boolean isValidFormat(String address) {
        return address.matches("^[ก-๙A-Za-z0-9\\s,.-/]+$");
    }
}