package com.digio.backend.Validate;

import org.apache.commons.validator.routines.EmailValidator;

public class EmailValidate {
    private static final int MAX_NAME_LENGTH = 50;

    public static String validate(String email) {
        if (email == null || !EmailValidator.getInstance().isValid(email) || email.length() > MAX_NAME_LENGTH) {
            return "อีเมลไม่ถูกต้อง";
        }
        return "success";
    }
}

