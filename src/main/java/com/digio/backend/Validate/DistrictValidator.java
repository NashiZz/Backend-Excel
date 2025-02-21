package com.digio.backend.Validate;

public class DistrictValidator {
    public static String validateDistrict(String district) {
        if (district == null || district.isBlank()) {
            return "ชื่ออำเภอไม่ควรว่าง";
        }

        String trimmedDistrict = district.trim();

        if (!trimmedDistrict.matches("^[ก-๙\s]+$")) {
            return "รูปแบบชื่ออำเภอไม่ถูกต้อง";
        }

        return "success";
    }
}
