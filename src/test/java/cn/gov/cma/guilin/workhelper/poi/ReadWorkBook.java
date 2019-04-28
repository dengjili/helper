package cn.gov.cma.guilin.workhelper.poi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;

public class ReadWorkBook {

	public static void main(String[] args) throws Exception {
		POIFSFileSystem fs = new POIFSFileSystem(new File("guilin-template.xls"));
		 //读取excel模板 .xls
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        for (Sheet sheet : wb ) {
            for (Row row : sheet) {
                for (Cell cell : row) {
                	 /*CellReference cellRef = new CellReference(row.getRowNum(), cell.getColumnIndex());
                     System.out.print(cellRef.formatAsString());
                     System.out.print(" - ");

                     // Alternatively, get the value and format it yourself
                     switch (cell.getCellType()) {
                         case STRING:
                             System.out.println(cell.getRichStringCellValue().getString());
                             break;
                         case NUMERIC:
                             if (DateUtil.isCellDateFormatted(cell)) {
                                 System.out.println(cell.getDateCellValue());
                             } else {
                                 System.out.println(cell.getNumericCellValue());
                             }
                             break;
                         case BOOLEAN:
                             System.out.println(cell.getBooleanCellValue());
                             break;
                         case FORMULA:
                             System.out.println(cell.getCellFormula());
                             break;
                         case BLANK:
                             System.out.println();
                             break;
                         default:
                             System.out.println();
                     }
                     
                     if ("B2".equals(cellRef.formatAsString())) {
						cell.setCellValue(cell.getRichStringCellValue().getString()+"20日");
					 }*/
                }
            }
        }
        
        HSSFSheet sheet = wb.getSheet("Sheet1");
        HSSFCell cell = sheet.getRow(1).getCell(1);
        String dateStr = "        单位：桂林市气象局高空站         观测系统名称：L波段系统综合探测          统计时段：%s              ";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月");
        cell.setCellValue(String.format(dateStr, sdf.format(new Date())));
        
        try (OutputStream fileOut = new FileOutputStream("workbook1.xls")) {
        	wb.write(fileOut);
        }
	}
}
