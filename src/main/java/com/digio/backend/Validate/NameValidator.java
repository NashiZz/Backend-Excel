package com.digio.backend.Validate;

import org.apache.commons.validator.routines.EmailValidator;

import java.util.List;
import java.util.regex.Pattern;

public class NameValidator {
    private static final int MAX_NAME_LENGTH = 50;
    private static final int MIN_NAME_PART_LENGTH = 2;
    private static final List<String> INVALID_KEYWORDS = List.of("unknown", "invalid", "n/a", "not specified");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[ก-๙A-Za-z\\s]+$");

    public static String validate(String name) {
        if (name == null || name.isBlank()) {
            return "ชื่อไม่ควรว่าง";
        }

        String trimmedName = name.trim();
        if (INVALID_KEYWORDS.stream().anyMatch(trimmedName::equalsIgnoreCase)) {
            return "ชื่อไม่ถูกต้อง";
        }

        if (EmailValidator.getInstance().isValid(trimmedName)) {
            return "ชื่อไม่ควรเป็นอีเมล";
        }

        if (trimmedName.matches("^\\d{10}$")) {
            return "ชื่อไม่ควรเป็นหมายเลขโทรศัพท์";
        }

        if (!NAME_PATTERN.matcher(trimmedName).matches()) {
            return "ชื่อควรมีเฉพาะตัวอักษรไทยหรือภาษาอังกฤษ และไม่มีอักขระพิเศษ";
        }

        String[] nameParts = trimmedName.split("\\s+");
        if (nameParts.length < 2) {
            return "กรุณาระบุชื่อและนามสกุล";
        }

        for (String part : nameParts) {
            if (part.length() < MIN_NAME_PART_LENGTH || part.length() > MAX_NAME_LENGTH) {
                return "ชื่อและนามสกุลควรมีความยาวอย่างน้อย 2 ตัวอักษร และไม่เกิน 50 ตัวอักษร";
            }
        }

        return "success";
    }
}