package cn.gov.cma.guilin.workhelper.poi;

import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;

/**
 * 
 * Created by it
 * Created in 2019年4月27日
 * Description:New Sheet
 */
public class CreateSheet {

	public static void main(String[] args) throws Exception {
		Workbook wb = new HSSFWorkbook();  // or new XSSFWorkbook();
	    Sheet sheet1 = wb.createSheet("new sheet");
	    Sheet sheet2 = wb.createSheet("second sheet");

	    // Note that sheet name is Excel must not exceed 31 characters
	    // and must not contain any of the any of the following characters:
	    // 0x0000
	    // 0x0003
	    // colon (:)
	    // backslash (\)
	    // asterisk (*)
	    // question mark (?)
	    // forward slash (/)
	    // opening square bracket ([)
	    // closing square bracket (])

	    // You can use org.apache.poi.ss.util.WorkbookUtil#createSafeSheetName(String nameProposal)}
	    // for a safe way to create valid names, this utility replaces invalid characters with a space (' ')
	    String safeName = WorkbookUtil.createSafeSheetName("[O'Brien's sales*?]"); // returns " O'Brien's sales   "
	    Sheet sheet3 = wb.createSheet(safeName);

	    try (OutputStream fileOut = new FileOutputStream("workbook.xls")) {
	        wb.write(fileOut);
	    }
	                     
	}

}
