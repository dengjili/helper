package cn.gov.cma.guilin.workhelper.poi;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * 
 * Created by it Created in 2019年4月27日 Description:New Sheet
 */
public class CreateDifferentCells {

	public static void main(String[] args) throws Exception {
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("new sheet");
		
		// 第三行
		Row row = sheet.createRow(2);
		row.createCell(0).setCellValue(1.1);
		row.createCell(1).setCellValue(new Date());
		row.createCell(2).setCellValue(Calendar.getInstance());
		row.createCell(3).setCellValue("a string");
		row.createCell(4).setCellValue(true);
		row.createCell(5).setCellType(CellType.ERROR);

		// Write the output to a file
		try (OutputStream fileOut = new FileOutputStream("workbook.xls")) {
			wb.write(fileOut);
		}
	}

}
