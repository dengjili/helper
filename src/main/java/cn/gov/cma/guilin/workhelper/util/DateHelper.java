package cn.gov.cma.guilin.workhelper.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public interface DateHelper {
	// 根据传入文件名解析月份，根据年月获取当月天数
	public static int getDays(String fileName) throws Exception {
		String yyyymmddhh = fileName.substring(6);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd.HH");
		Date date = sdf.parse(yyyymmddhh);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		// 获取当月最大天数
		return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	public static boolean isBetween(String beginTime, String start, String end) throws Exception {
		String hhmmss = toHHmmss(beginTime);
		
		Date calcTime = toDate(hhmmss);
		Date startTime = toDate(start);
		Date endTime = toDate(end);
		
		// 满足区间计算
		if (calcTime.after(startTime) && calcTime.before(endTime)) {
			return true;
		}
		
		return false;
	}
	
	static Date toDate(String hhmmss) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("HH.mm.ss");
		Date date = sdf.parse(hhmmss);
		return date;
	}

	public static String toHHmmss(String beginTime) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("dd日HH时mm分ss秒");
		Date date = sdf.parse(beginTime);
		sdf = new SimpleDateFormat("HH.mm.ss");
		return sdf.format(date);
	}
	
	// 根据传入文件名解析月份，根据年月获取当月天数
	public static Date getDate(String fileName) throws Exception {
		String yyyymmddhh = fileName.substring(6);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd.HH");
		Date date = sdf.parse(yyyymmddhh);
		return date;
	}
	
	
}
