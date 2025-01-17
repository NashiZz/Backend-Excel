package com.digio.backend.Validate;

public class PhoneValidator {
    public static String validate(String phoneNum) {
        if (phoneNum == null || !phoneNum.matches("^0[1-9][0-9]{8}$")) {
            return "หมายเลขโทรศัพท์ไม่ถูกต้อง";
        }
        return null;
    }
}

