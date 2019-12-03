package com.revolut.task.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalUtil {
	
	private BigDecimalUtil() {}
	
	public static BigDecimal getScaledDecimal(BigDecimal decimalToScale) {
		return decimalToScale.setScale(2, RoundingMode.HALF_EVEN);
	}

}
