package guthix.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Bart on 8/9/2015.
 *
 * Basic localisation features.
 */
public class L10n {

	private static final NumberFormat BRITISH = NumberFormat.getNumberInstance(Locale.UK);

	public static String format(long number) {
		return BRITISH.format(number);
	}

}
