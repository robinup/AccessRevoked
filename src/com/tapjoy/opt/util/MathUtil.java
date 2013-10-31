package com.tapjoy.opt.util;

import java.math.BigDecimal;

public class MathUtil {
	
	public static double round(double d, int scale) {
		// see the Javadoc about why we use a String in the constructor
		// http://java.sun.com/j2se/1.5.0/docs/api/java/math/BigDecimal.html#BigDecimal(double)
		
		BigDecimal bigDec = new BigDecimal(Double.toString(d));
		bigDec = bigDec.setScale(scale, BigDecimal.ROUND_HALF_UP);
		return bigDec.doubleValue();
	}

	/**
	 * Since java arithmetic returns the reminder for % instead of modulo.
	 * Modulo i%j for all positive j's
	 * @param i
	 * @param j
	 * @return
	 */
	public static int modulo(int i, int j)
	{
		assert(j>0) ;
		return i%j<0 ? (i%j)+j : i%j ;
	}
}
