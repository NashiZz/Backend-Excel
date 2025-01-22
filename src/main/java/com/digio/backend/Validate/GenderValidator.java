package com.digio.backend.Validate;

import java.util.Arrays;
import java.util.List;

public class GenderValidator {

    private static final List<String> VALID_GENDER_VALUES = Arrays.asList("ชาย", "หญิง", "ไม่ระบุ");

    public static String validateGender(String gender) {

        if (gender == null || gender.trim().isEmpty()) {
            return "เพศไม่ควรว่าง";
        } else if (!VALID_GENDER_VALUES.contains(gender.trim())) {
            return "เพศไม่ถูกต้อง";
        }

        return "success";
    }
}
