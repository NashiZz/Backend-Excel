package com.digio.backend.Validate;

public class BalanceValidator {
    public static String validate(String balance) {
        if (balance == null || balance.isEmpty()) {
            return "จำนวนเงินไม่ถูกต้อง";
        }

        if (!balance.matches("^\\d+(\\.\\d+)?$")) {
            return "จำนวนเงินไม่ถูกต้อง";
        }

        return "success";
    }
}

