package com.digio.backend.Validate;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class AgeValidator {

    private static String validateDateOfBirth(String dob) {
        StringBuilder errorBuilder = new StringBuilder();

        if (dob == null || dob.trim().isEmpty()) {
            appendError(errorBuilder, "วันเกิดไม่ควรว่าง");
        } else {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate birthLocalDate = LocalDate.parse(dob, formatter);

                if (birthLocalDate.isAfter(LocalDate.now())) {
                    appendError(errorBuilder, "วันเกิดไม่สามารถเป็นวันที่ในอนาคต");
                }

                int age = Period.between(birthLocalDate, LocalDate.now()).getYears();
                if (age < 18) {
                    appendError(errorBuilder, "อายุไม่ถึงขั้นต่ำที่กำหนด (ต้องมีอายุอย่างน้อย 18 ปี)");
                }
            } catch (Exception e) {
                appendError(errorBuilder, "รูปแบบวันเกิดไม่ถูกต้อง ควรเป็น yyyy-MM-dd หรือ dd/MM/yyyy");
            }
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
