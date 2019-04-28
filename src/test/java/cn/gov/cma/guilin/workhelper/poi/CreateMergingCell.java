package cn.gov.cma.guilin.workhelper.poi;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

public class CreateMergingCell {

	public static void main(String[] args) throws Exception, IOException {
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("new sheet");

		Row row = sheet.createRow(1);
		Cell cell = row.createCell(1);
		cell.setCellValue("This is a test of merging");

		sheet.addMergedRegion(new CellRangeAddress(1, // first row (0-based)
				3, // last row (0-based)
				1, // first column (0-based)
				5 // last column (0-based)
		));

		// Write the output to a file
		try (OutputStream fileOut = new FileOutputStream("workbook.xls")) {
			wb.write(fileOut);
		}

		wb.close();
	}

}
