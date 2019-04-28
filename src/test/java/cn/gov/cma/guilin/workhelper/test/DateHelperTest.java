package cn.gov.cma.guilin.workhelper.test;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.gov.cma.guilin.workhelper.util.DateHelper;

public class DateHelperTest {

	public static void main(String[] args) throws Exception {
		int days = DateHelper.getDays("s5795720010223.07");
		System.out.println(days);
		
		String abc = "06日07时15分24秒";
		SimpleDateFormat sdf = new SimpleDateFormat("dd日HH时mm分ss秒");
		Date date = sdf.parse(abc);
		sdf = new SimpleDateFormat("HH.mm");
		System.out.println(sdf.format(date));
	}

}
