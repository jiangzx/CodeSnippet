/**
 *
 */
package pkg.demo.common.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.google.common.base.Strings;

/**
 * @ClassName: DateTimeUtil
 * @Description: TODO
 * @author Wallace Zhang
 *
 */

public class DateTimeUtils {

	public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private static final ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat(DEFAULT_FORMAT);
		}
	};

	private static final ThreadLocal<DateFormat> dfus = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat(DEFAULT_FORMAT);
		}
	};

	private static final ThreadLocal<DateFormat> dfusday = new ThreadLocal<DateFormat>() {
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat(DEFAULT_FORMAT);
		}
	};

	/**
	 * default yyyy-MM-dd HH:mm:ss GMT
	 *
	 * @param date
	 * @return
	 */
	public static String toString(Date date) {
		df.get().setTimeZone(TimeZone.getTimeZone("GMT"));
		return df.get().format(date);
	}
	
	public static String toUSString(Date date) {
		dfus.get().setTimeZone(TimeZone.getTimeZone("GMT"));
		return dfus.get().format(date);
	}

	public static String toUSDayString(Date date) {
		dfusday.get().setTimeZone(TimeZone.getTimeZone("GMT"));
		return dfusday.get().format(date);
	}

	/**
	 * default yyyy-MM-dd HH:mm:ss GMT
	 *
	 * @param str
	 * @return
	 * @throws ParseException
	 */
	public static Date toDate(String str) throws ParseException {
		if (Strings.isNullOrEmpty(str)) {
			return null;
		}
		df.get().setTimeZone(TimeZone.getTimeZone("GMT"));
		return df.get().parse(str);
	}

	public static Date getStartTime(Date date) {
		Calendar calendar = Calendar.getInstance();
		if (date != null) {
			calendar.setTime(date);
		}
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		Date d = new Date(calendar.getTime().getTime());
		return d;
	}

	public static Date getEndTime(Date date) {
		Calendar calendar = Calendar.getInstance();
		if (date != null) {
			calendar.setTime(date);
		}
		calendar.set(Calendar.HOUR, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		Date d = new Date(calendar.getTime().getTime());
		return d;
	}

	public static Date getDateTimeBefore(int days) {
		Date now = getEndTime(null);
		return new Date(now.getTime() - (long) days * 24 * 60 * 60 * 1000);
	}

	public static Date getLastDateOfMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		return cal.getTime();
	}
}
