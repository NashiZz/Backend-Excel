package com.digio.backend.Validate;

import org.apache.commons.validator.routines.EmailValidator;

import java.util.List;
import java.util.regex.Pattern;

public class NameValidator {
    private static final int MAX_NAME_LENGTH = 50;
    private static final int PHONE_NUMBER_LENGTH = 10;
    private static final List<String> INVALID_KEYWORDS = List.of("unknown", "invalid", "n/a", "not specified");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile(".*[!@#$%^&*(),.?\":{}|<>].*");
    private static final Pattern NUMBER_PATTERN = Pattern.compile(".*\\d.*");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[ก-๙A-Za-z\\s]+$");

    public static String validate(String name) {
        if (isNullOrEmpty(name)) {
            return "ชื่อไม่ควรว่าง";
        }

        String trimmedName = name.trim().toLowerCase();

        if (containsInvalidKeyword(trimmedName)) {
            return "ชื่อไม่ถูกต้อง";
        }

        if (isEmail(name)) {
            return "ชื่อไม่ควรเป็นอีเมล";
        }

        if (isPhoneNumber(name)) {
            return "ชื่อไม่ควรเป็นหมายเลขโทรศัพท์";
        }

        if (hasSpecialCharacters(name)) {
            return "ชื่อไม่ควรมีอักขระพิเศษ";
        }

        if (hasNumbers(name)) {
            return "ชื่อไม่ควรมีตัวเลข";
        }

        if (hasMultipleSpaces(name)) {
            return "ชื่อไม่ควรมีช่องว่างซ้ำ";
        }

        if (!isValidNameFormat(name)) {
            return "ชื่อควรมีเฉพาะตัวอักษรไทยหรือภาษาอังกฤษ";
        }

        String[] nameParts = name.split("\\s+");
        if (nameParts.length < 2) {
            return "กรุณาระบุชื่อและนามสกุล";
        }

        String firstName = nameParts[0];
        if (isInvalidFirstName(firstName)) {
            return "ชื่อควรมีความยาวอย่างน้อย 2 ตัวอักษร หรือชื่อยาวเกินไป";
        }

        String lastName = nameParts[nameParts.length - 1];
        if (isInvalidLastName(lastName)) {
            return "นามสกุลควรมีความยาวอย่างน้อย 2 ตัวอักษร หรือยาวเกินไป";
        }

        return "success";
    }

    private static boolean isNullOrEmpty(String name) {
        return name == null || name.trim().isEmpty();
    }

    private static boolean containsInvalidKeyword(String name) {
        return INVALID_KEYWORDS.stream().anyMatch(name::contains);
    }

    private static boolean isEmail(String name) {
        return EmailValidator.getInstance().isValid(name);
    }

    private static boolean isPhoneNumber(String name) {
        return name.matches("^\\d{" + PHONE_NUMBER_LENGTH + "}$");
    }

    private static boolean hasSpecialCharacters(String name) {
        return SPECIAL_CHAR_PATTERN.matcher(name).matches();
    }

    private static boolean hasNumbers(String name) {
        return NUMBER_PATTERN.matcher(name).matches();
    }

    private static boolean hasMultipleSpaces(String name) {
        return name.contains("  ");
    }

    private static boolean isValidNameFormat(String name) {
        return NAME_PATTERN.matcher(name).matches();
    }

    private static boolean isInvalidFirstName(String firstName) {
        return firstName.length() < 2 || firstName.length() > MAX_NAME_LENGTH;
    }

    private static boolean isInvalidLastName(String lastName) {
        return lastName.length() < 2 || lastName.length() > MAX_NAME_LENGTH;
    }
}