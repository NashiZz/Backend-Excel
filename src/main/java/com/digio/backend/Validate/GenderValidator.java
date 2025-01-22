package com.digio.backend.Validate;

import java.util.Arrays;
import java.util.List;

public class GenderValidator {

    private static List<String> validGenders;

    static {
        validGenders = Arrays.asList("ชาย", "หญิง", "เพศที่สาม", "ทรานส์เจนเดอร์", "ไม่ระบุ");
    }

    public static String validateGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            return "เพศไม่ควรว่าง";
        }

        if (validGenders.contains(gender.trim())) {
            return "success";
        } else {
            return "เพศไม่ถูกต้อง";
        }
    }
}
