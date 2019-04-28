package cn.gov.cma.guilin.workhelper.poi;

import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * 
 * Created by it Created in 2019年4月27日 Description:New Sheet
 */
public class CreateCells {

	public static void main(String[] args) throws Exception {
		Workbook wb = new HSSFWorkbook();
		// Workbook wb = new XSSFWorkbook();
		CreationHelper createHelper = wb.getCreationHelper();
		Sheet sheet = wb.createSheet("new sheet");

		// Create a row and put some cells in it. Rows are 0 based.
		Row row = sheet.createRow(0);
		// Create a cell and put a value in it.
		Cell cell = row.createCell(0);
		cell.setCellValue(1);

		// Or do it on one line.
		row.createCell(1).setCellValue(1.2);
		row.createCell(2).setCellValue(createHelper.createRichTextString("This is a string"));
		row.createCell(3).setCellValue(true);

		// Write the output to a file
		try (OutputStream fileOut = new FileOutputStream("workbook.xls")) {
			wb.write(fileOut);
		}
	}

}
