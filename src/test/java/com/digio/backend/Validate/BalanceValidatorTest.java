package com.digio.backend.Validate;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BalanceValidatorTest {

    @Test
    void test_validateBalance(){
        String balance = "5000";
        String result = BalanceValidator.validate(balance);

        assertEquals("success", result);
    }

    @Test
    void test_validateNullBalance(){
        String balance = " ";
        String result = BalanceValidator.validate(balance);

        assertEquals("จำนวนเงินไม่ถูกต้อง",result);
    }

    @Test
    void test_validateTwoDecimalPoint(){
        String balance = "499.50";
        String result = BalanceValidator.validate(balance);

        assertEquals("success",result);
    }

    @Test
    void test_validateManyDecimalPoint(){
        String balance = "954.876";
        String result = BalanceValidator.validate(balance);

        assertEquals("จำนวนเงินไม่ถูกต้อง",result);
    }

    @Test
    void  test_validateCommaDigit(){
        String balance = "75,000";
        String result = BalanceValidator.validate(balance);

        assertEquals("จำนวนเงินไม่ถูกต้อง",result);
    }

    @Test
    void test_startWithDecimal(){
        String balance = ".57";
        String result = BalanceValidator.validate(balance);

        assertEquals("จำนวนเงินไม่ถูกต้อง",result);
    }

    @Test
    void test_endWithDecimal(){
        String balance = "869.";
        String result = BalanceValidator.validate(balance);

        assertEquals("จำนวนเงินไม่ถูกต้อง",result);
    }

    @Test
    void test_notANumber(){
        String balance = "my balance";
        String result = BalanceValidator.validate(balance);

        assertEquals("จำนวนเงินไม่ถูกต้อง",result);
    }

}
