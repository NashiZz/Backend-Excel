package com.digio.backend.Validate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AgeValidator {

    public static String validateDateOfBirth(String dob) {
        if (dob == null || dob.trim().isEmpty()) {
            return "วันเกิดไม่ควรว่าง";
        }

        LocalDate birthLocalDate;
        try {
            birthLocalDate = parseDate(dob);
        } catch (IllegalArgumentException e) {
            return "รูปแบบวันเกิดไม่ถูกต้อง";
        }

        if (birthLocalDate.isAfter(LocalDate.now())) {
            return "วันเกิดไม่สามารถเป็นวันที่ในอนาคต";
        }

        int age = java.time.Period.between(birthLocalDate, LocalDate.now()).getYears();
        if (age < 18) {
            return "อายุไม่ถึงขั้นต่ำที่กำหนด (ต้องมีอายุอย่างน้อย 18 ปี)";
        }

        return "success";
    }

    private static LocalDate parseDate(String dob) {
        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(dob, formatter);
            } catch (DateTimeParseException ignored) {

            }
        }

        throw new IllegalArgumentException("วันเกิดมีรูปแบบไม่ถูกต้อง");
    }
}
