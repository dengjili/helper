package cn.gov.cma.guilin.workhelper.poi;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 
 * Created by it 
 * Created in 2019年4月27日 
 * Description: How to create a new workbook
 */
public class CreateWorkbook {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		// .xls 后缀
		Workbook wb = new HSSFWorkbook();
		try (OutputStream fileOut = new FileOutputStream("workbook1.xls")) {
			wb.write(fileOut);
		}

		// .xlsx 后缀
		Workbook wb2 = new XSSFWorkbook();
		try (OutputStream fileOut = new FileOutputStream("workbook2.xlsx")) {
			wb.write(fileOut);
		}
	}
}
