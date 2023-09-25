package com.crypto.wallet.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import static java.math.RoundingMode.HALF_UP;
import static java.util.Locale.ENGLISH;

public class NumberUtils {

    private static final NumberFormat numberFormat = NumberFormat.getNumberInstance(ENGLISH);
    private static final DecimalFormat decimalFormat = (DecimalFormat) numberFormat;
    private NumberUtils() {
    }

    public static String twoDecimal(BigDecimal value) {
        value = value.setScale(2, HALF_UP);
        decimalFormat.setMaximumFractionDigits(2);
        decimalFormat.setMinimumFractionDigits(2);
        decimalFormat.setGroupingUsed(false);
        return decimalFormat.format(value);
    }

}
