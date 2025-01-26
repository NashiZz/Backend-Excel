package com.digio.backend.Validate;

import java.util.List;

public class AddressValidator {
    private static final int MAX_ADDRESS_LENGTH = 100;
    private static final int MIN_ADDRESS_LENGTH = 10;
    private static final List<String> INVALID_KEYWORDS = List.of("unknown", "invalid", "n/a", "not specified");

    public static String validate(String address) {
        if (address == null || address.isBlank()) {
            return "ที่อยู่ไม่ควรว่าง";
        }

        String trimmedAddress = address.trim().toLowerCase();

        if (!trimmedAddress.matches("^[ก-๙A-Za-z0-9\\s,.-/]+$") || trimmedAddress.matches(".*[<>#&@!].*")) {
            return "รูปแบบที่อยู่ไม่ถูกต้อง";
        }

        if (INVALID_KEYWORDS.stream().anyMatch(trimmedAddress::contains)) {
            return "ที่อยู่ไม่ถูกต้อง";
        }

        if (trimmedAddress.length() < MIN_ADDRESS_LENGTH || trimmedAddress.length() > MAX_ADDRESS_LENGTH) {
            return "ที่อยู่มีความยาวหรือสั้นเกินไป";
        }

        if (trimmedAddress.matches(".*(.)\\1{4,}.*")) {
            return "ที่อยู่ไม่ควรมีตัวอักษรหรือตัวเลขซ้ำกัน";
        }

        return "success";
    }
}