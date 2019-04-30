package cn.gov.cma.guilin.workhelper.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.gov.cma.guilin.workhelper.model.Work;
import cn.gov.cma.guilin.workhelper.util.DateHelper;
import cn.gov.cma.guilin.workhelper.util.FileHelper;

public class Controller {

	// 日志输出
	private static Logger logger = LoggerFactory.getLogger(Controller.class);
	
	private String originPath;
	private String destPath;
	private String fileName;
	private String newFilePath;
	
	public Controller(String originPath, String destPath) {
		this.originPath = originPath;
		this.destPath = destPath;
		
		// 获取文件名
		fileName = originPath.substring(originPath.lastIndexOf("\\") + 1);
		// 替换文件名后缀
		fileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".xls";
		
		if (Objects.isNull(destPath) || "".equals(destPath)) {
			newFilePath = originPath.substring(0, originPath.lastIndexOf("\\")) + File.separator + fileName;
		} else {
			newFilePath = destPath + File.separator + fileName;
		}
		
		
	}

	public Map<String, String> doConver() {
		Map<String, String> result = new HashMap<>();
		if (Objects.isNull(originPath) || "".equals(originPath)) {
			result.put("flag", "1");
			result.put("message", "源文件未设置");
			return result;
		}

		String coder = FileHelper.codeString(originPath);
		// 生成内容的转换
		List<Work> dataAll = new ArrayList<>(32);
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(originPath), coder));) {
			String tempValue = null;
			while ((tempValue = br.readLine()) != null) {
				// 判断文件是否已s开头
				if (!(tempValue == null || "".equals(tempValue)) && tempValue.matches("^s.*")) {
					// 以空格为分隔符，将结果存为一维数组
					String[] strList = tempValue.split(" +", -1);
					// 判断是否探空终止高度为空
		               if (strList.length == 10)
		               {
		                   Work work = new Work();
		                   work.setFileName(strList[0]);
		                   work.setBeginTime(strList[1]);
		                   work.setEndTime(strList[2]);
		                   work.setAirStopReason(strList[3]);
		                   work.setWindStopReason(strList[4]);
		                   work.setAirStopHight(strList[5]);
		                   work.setWindStopHight(strList[6]);
		                   work.setCalculator(strList[7]);
		                   work.setChecktor(strList[8]);
		                   work.setApprovetor(strList[9]);
		                   work.setNomalMajor(true);
		                   dataAll.add(work);
		               }
		               else
		               {
		            	   Work work = new Work();
		            	   work.setFileName(strList[0]);
		            	   work.setBeginTime(strList[1]);
		                   work.setEndTime(strList[2]);
		                   work.setAirStopReason(strList[3]);
		                   work.setWindStopReason(strList[4]);
		                   work.setAirStopHight(null);
		                   work.setWindStopHight(strList[5]);
		                   work.setCalculator(strList[6]);
		                   work.setChecktor(strList[7]);
		                   work.setApprovetor(strList[8]);
		                   work.setNomalMajor(false);
		                   dataAll.add(work);
		               }
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			result.put("flag", "1");
			result.put("message", "程序出现异常，请联系管理员");
			return result;
		}

		// 转换为没有重复的数据
		try {
			Calculation calculation = new Calculation(dataAll, FileHelper.converUnique(dataAll));
			int calculateOne = calculation.calculateOne();
			logger.debug("{}", String.format("缺测次数：%s", calculateOne));
			Map<String, Integer> calculateTwo = calculation.calculateTwo();
			Integer total = total(calculateTwo);
			logger.debug("{}", String.format("主班次数：%s, 总上班次数：%s", calculateTwo, total));
			Map<String, Integer> calculateThree = calculation.calculateThree();
			logger.debug("{}", String.format("早测次数：%s", calculateThree));
			Map<String, Integer> calculateFour = calculation.calculateFour();
			logger.debug("{}", String.format("非人为迟测次数：%s", calculateFour));
			Map<String, Map<String, Integer>> calculateFive = calculation.calculateFive();
			Map<String, Integer> timesMap = calculateFive.get("TIMES");
			Map<String, Integer> avgMap = calculateFive.get("AVG");
			logger.debug("{}", "探空高度");
			timesMap.forEach((k,v)->{
				logger.debug("{}", String.format("名字：%s, 平均高度：%s, 次数： %s", k, avgMap.get(k), v));
			});
			
			Map<String, Map<String, Integer>> calculateSix = calculation.calculateSix();
			Map<String, Integer> timesMap2 = calculateSix.get("TIMES");
			Map<String, Integer> avgMap2 = calculateSix.get("AVG");
			logger.debug("{}", "测风高度 综合");
			timesMap2.forEach((k,v)->{
				logger.debug("{}", String.format("名字：%s, 平均高度：%s, 次数： %s", k, avgMap2.get(k), v));
			});
			
			
			Map<String, Map<String, Integer>> calculateSeven = calculation.calculateSeven();
			Map<String, Integer> timesMap3 = calculateSeven.get("TIMES");
			Map<String, Integer> avgMap3 = calculateSeven.get("AVG");
			logger.debug("{}", "测风高度 单测");
			timesMap3.forEach((k,v)->{
				logger.debug("{}", String.format("名字：%s, 平均高度：%s, 次数： %s", k, avgMap3.get(k), v));
			});
			
			Map<String, Integer> calculateEight = calculation.calculateEight();
			logger.debug("{}", String.format("重复次数：%s", calculateEight));
			
			/** 站质量 统计   **/
			logger.debug("{}", String.format("===========站质量 统计========"));
			// 主班次数
			int major = total(calculateTwo);
			logger.debug("{}", String.format("主班次数：%s", major));
			// 非人为缺测
			int miss = calculateOne;
			logger.debug("{}", String.format("非人为缺测次数：%s", miss));
			// 早测次数
			int before = total(calculateThree);
			logger.debug("{}", String.format("早测次数：%s", before));
			// 非人为迟测次数
			int after = total(calculateFour);
			logger.debug("{}", String.format("非人为迟测次数：%s", after));
			// 探空高度平均高度
			int artAvg = avg(avgMap);
			logger.debug("{}", String.format("探空高度平均高度：%s", artAvg));
			// 探空高度次数
			int artTimes = total(timesMap);
			logger.debug("{}", String.format("探空高度次数：%s", artTimes));
			// 测风高度平均高度   综合
			int artAvg2 = avg(avgMap2);
			logger.debug("{}", String.format("测风高度平均高度   综合：%s", artAvg2));
			// 测风高度次数  综合
			int artTimes2 = total(timesMap2);
			logger.debug("{}", String.format("测风高度次数  综合：%s", artTimes2));
			// 测风高度平均高度  单测
			int artAvg3 = avg(avgMap3);
			logger.debug("{}", String.format("测风高度平均高度  单测：%s", artAvg3));
			// 测风高度次数  单测
			int artTimes3 = total(timesMap3);
			logger.debug("{}", String.format("测风高度次数  单测：%s", artTimes3));
			// 重复次数
			int redo = total(calculateEight);
			logger.debug("{}", String.format("重复次数：%s", redo));
			logger.debug("{}", String.format("===========站质量 统计========"));
			
			// 生成excel
			String fileName = dataAll.get(0).getFileName();
			Date date = DateHelper.getDate(fileName);
			// 读取excel模板 .xls
			InputStream inputStream = Controller.class.getClassLoader().getResourceAsStream("guilin-template.xls");
			HSSFWorkbook wb = new HSSFWorkbook(inputStream);

			// 表头设置
			HSSFSheet sheet = wb.getSheet("Sheet1");
			HSSFCell cell = sheet.getRow(1).getCell(1);
			String dateStr = "        单位：桂林市气象局高空站         观测系统名称：L波段系统综合探测          统计时段：%s              ";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月");
			cell.setCellValue(String.format(dateStr, sdf.format(date)));

			// 其他辅助类
			CreationHelper createHelper = wb.getCreationHelper();
			CellStyle style = wb.createCellStyle();
			CellStyle style2 = wb.createCellStyle();
			CellStyle style3 = wb.createCellStyle();
			CellStyle style4 = wb.createCellStyle();
			
			// 填充数据，以主班姓名为依据
			int rows = 9;
			for (Map.Entry<String, Integer> entry : calculateTwo.entrySet()) {
				int column = 1;
				String name = entry.getKey();
				Integer majorTimes = entry.getValue();
				HSSFRow row = sheet.createRow(rows);
				HSSFCell cell1 = row.createCell(column++);
				setRowStyle(wb, cell1, style);
				cell1.setCellValue(createHelper.createRichTextString(name));
				
				HSSFCell cell2 = row.createCell(column++);
				setRowStyleNumber(wb, cell2, style2);
				cell2.setCellValue(majorTimes);
				
				// 后三列，定值为0
				for (int i = 0; i < 3; i++) {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(0);
				}
				
				// 后三列为空
				for (int i = 0; i < 3; i++) {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyle(wb, cell3, style2);
					cell3.setCellValue("");
				}
				
				// 早测列
				Integer beforeTimes = calculateThree.get(name);
				if (beforeTimes == null || beforeTimes == 0) {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyle(wb, cell3, style2);
					cell3.setCellValue("");
				} else {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(beforeTimes);
				}
				
				{
					HSSFCell cell3 = row.createCell(column++);
					setRowStyle(wb, cell3, style2);
					cell3.setCellValue("");
				}
				// 非人为迟测
				Integer afterTimes = calculateFour.get(name);
				if (afterTimes == null || afterTimes == 0) {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyle(wb, cell3, style2);
					cell3.setCellValue("");
				} else {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(afterTimes);
				}
				
				// 后6列为空
				for (int i = 0; i < 6; i++) {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyle(wb, cell3, style2);
					cell3.setCellValue("");
				}
				
				{
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(avgMap.get(name));
				}
				{
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(timesMap.get(name));
				}
				{
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(avgMap2.get(name));
				}
				{
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(timesMap2.get(name));
				}
				{
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(avgMap3.get(name));
				}
				{
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(timesMap3.get(name));
				}
				
				{
					HSSFCell cell3 = row.createCell(column++);
					setRowStyle(wb, cell3, style2);
					cell3.setCellValue("");
				}
				
				// 重复次数
				Integer redoTimes = calculateEight.get(name);
				if (redoTimes == null || redoTimes == 0) {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyle(wb, cell3, style2);
					cell3.setCellValue("");
				} else {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(redoTimes);
				}
				
				// 后6列为空
				for (int i = 0; i < 6; i++) {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyle(wb, cell3, style2);
					cell3.setCellValue("");
				}
				
				rows++;
			}
			
			// 统计到站 一行
			{
				int column = 1;
				HSSFRow row = sheet.createRow(rows);
				HSSFCell cell1 = row.createCell(column++);
				setRowStyle(wb, cell1, style);
				cell1.setCellValue(createHelper.createRichTextString("统计到站"));
				// 后6列为空
				for (int i = 0; i < 6; i++) {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyle(wb, cell3, style2);
					cell3.setCellValue("");
				}
				if (miss == 0) {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyle(wb, cell3, style2);
					cell3.setCellValue("");
				} else {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(miss);
				}
				// 后9列为空
				for (int i = 0; i < 9; i++) {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyle(wb, cell3, style2);
					cell3.setCellValue("");
				}
				{
					HSSFCell cell3 = row.createCell(column++);
					Integer value = avgMap.get("统计到站");
					if (value == null || value == 0) {
						setRowStyle(wb, cell3, style2);
						cell3.setCellValue("");
					} else {
						setRowStyleNumber(wb, cell3, style2);
						cell3.setCellValue(value);
					};
				}
				{
					HSSFCell cell3 = row.createCell(column++);
					Integer value = timesMap.get("统计到站");
					if (value == null || value == 0) {
						setRowStyle(wb, cell3, style2);
						cell3.setCellValue("");
					} else {
						setRowStyleNumber(wb, cell3, style2);
						cell3.setCellValue(value);
					};
				}
				{
					HSSFCell cell3 = row.createCell(column++);
					Integer value = avgMap2.get("统计到站");
					if (value == null || value == 0) {
						setRowStyle(wb, cell3, style2);
						cell3.setCellValue("");
					} else {
						setRowStyleNumber(wb, cell3, style2);
						cell3.setCellValue(value);
					};
				}
				{
					HSSFCell cell3 = row.createCell(column++);
					Integer value = timesMap2.get("统计到站");
					if (value == null || value == 0) {
						setRowStyle(wb, cell3, style2);
						cell3.setCellValue("");
					} else {
						setRowStyleNumber(wb, cell3, style2);
						cell3.setCellValue(value);
					};
				}
				{
					HSSFCell cell3 = row.createCell(column++);
					Integer value = avgMap3.get("统计到站");
					if (value == null || value == 0) {
						setRowStyle(wb, cell3, style2);
						cell3.setCellValue("");
					} else {
						setRowStyleNumber(wb, cell3, style2);
						cell3.setCellValue(value);
					};
				}
				{
					HSSFCell cell3 = row.createCell(column++);
					Integer value = timesMap3.get("统计到站");
					if (value == null || value == 0) {
						setRowStyle(wb, cell3, style2);
						cell3.setCellValue("");
					} else {
						setRowStyleNumber(wb, cell3, style2);
						cell3.setCellValue(value);
					};
				}
				// 后8列为空
				for (int i = 0; i < 8; i++) {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyle(wb, cell3, style2);
					cell3.setCellValue("");
				}
				rows++;
			}
			// 站质量 一行
			{
				int column = 1;
				HSSFRow row = sheet.createRow(rows);
				HSSFCell cell1 = row.createCell(column++);
				setRowStyle(wb, cell1, style);
				cell1.setCellValue(createHelper.createRichTextString("站质量"));
				
				HSSFCell cell2 = row.createCell(column++);
				setRowStyleNumber(wb, cell2, style2);
				cell2.setCellValue(major);
				
				// 后三列，定值为0
				for (int i = 0; i < 3; i++) {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(0);
				}
				
				// 后三列为空
				for (int i = 0; i < 2; i++) {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyle(wb, cell3, style2);
					cell3.setCellValue("");
				}
				
				// 非人为缺测
				if (miss == 0) {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyle(wb, cell3, style2);
					cell3.setCellValue("");
				} else {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(miss);
				}
				
				// 早测列
				if (before == 0) {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyle(wb, cell3, style2);
					cell3.setCellValue("");
				} else {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(before);
				}
				
				{
					HSSFCell cell3 = row.createCell(column++);
					setRowStyle(wb, cell3, style2);
					cell3.setCellValue("");
				}
				// 非人为迟测
				if (after == 0) {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyle(wb, cell3, style2);
					cell3.setCellValue("");
				} else {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(after);
				}
				
				// 后6列为空
				for (int i = 0; i < 6; i++) {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyle(wb, cell3, style2);
					cell3.setCellValue("");
				}
				
				{
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(artAvg);
				}
				{
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(artTimes);
				}
				{
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(artAvg2);
				}
				{
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(artTimes2);
				}
				{
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(artAvg3);
				}
				{
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(artTimes3);
				}
				
				{
					HSSFCell cell3 = row.createCell(column++);
					setRowStyle(wb, cell3, style2);
					cell3.setCellValue("");
				}
				
				// 重复次数
				if (redo == 0) {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyle(wb, cell3, style2);
					cell3.setCellValue("");
				} else {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(redo);
				}
				// 后6列为空
				for (int i = 0; i < 6; i++) {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyle(wb, cell3, style2);
					cell3.setCellValue("");
				}
				
				rows++;
			}
			
			int begin = rows;
			// 备注行
			{
				for (int i = 0; i < 6; i++) {
					HSSFRow row = sheet.createRow(rows);
					for (int j = 1; j < 32; j++) {
						HSSFCell cell1 = row.createCell(j);
						setRowStyle2(wb, cell1, style3);
						cell1.setCellValue("备注：");
					}
					rows++;
				}

			}
			int end = rows - 1;
			sheet.addMergedRegion(new CellRangeAddress(
					begin, //first row (0-based)
					end, //last row  (0-based)
					1, //first column (0-based)
					31  //last column  (0-based)
					));
			
			String endTip = "                                                        填报者：        校对者：         日期：%s";
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy年MM月dd日");
			endTip = String.format(endTip, sdf2.format(new Date()));
			
			begin = rows;
			// 结束语
			{
				for (int i = 0; i < 2; i++) {
					HSSFRow row = sheet.createRow(rows);
					for (int j = 1; j < 32; j++) {
						HSSFCell cell1 = row.createCell(j);
						setRowStyle3(wb, cell1, style4);
						cell1.setCellValue(endTip);
					}
					rows++;
				}

			}
			end = rows - 1;
			sheet.addMergedRegion(new CellRangeAddress(
					begin, //first row (0-based)
					end, //last row  (0-based)
					1, //first column (0-based)
					31  //last column  (0-based)
					));
			
			
			try (OutputStream fileOut = new FileOutputStream(newFilePath)) {
				wb.write(fileOut);
			}

			wb.close();

		} catch (Exception e) {
			logger.error("异常：", e);
			logger.error(e.getMessage());
			result.put("flag", "1");
			result.put("message", "程序出现异常，请联系管理员");
			return result;
		}

		result.put("message", "转换成功");
		if (Objects.isNull(destPath) || "".equals(destPath)) {
			result.put("flag", "0");
			result.put("message", "转换成功，当前未设置生成目录，默认生成在源文件同目录下");
		}
		return result;
	}

	private void setRowStyle(HSSFWorkbook wb, HSSFCell cell, CellStyle style) {
		 // Style the cell with borders all around.
	    style.setBorderBottom(BorderStyle.THIN);
	    style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
	    style.setBorderLeft(BorderStyle.THIN);
	    style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
	    style.setBorderRight(BorderStyle.THIN);
	    style.setRightBorderColor(IndexedColors.BLACK.getIndex());
	    style.setBorderTop(BorderStyle.THIN);
	    style.setTopBorderColor(IndexedColors.BLACK.getIndex());
	    cell.setCellStyle(style);
	}
	
	private void setRowStyleNumber(HSSFWorkbook wb, HSSFCell cell, CellStyle style) {
		 // Style the cell with borders all around.
	    style.setBorderBottom(BorderStyle.THIN);
	    style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
	    style.setBorderLeft(BorderStyle.THIN);
	    style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
	    style.setBorderRight(BorderStyle.THIN);
	    style.setRightBorderColor(IndexedColors.BLACK.getIndex());
	    style.setBorderTop(BorderStyle.THIN);
	    style.setTopBorderColor(IndexedColors.BLACK.getIndex());
	    
	    style.setAlignment(HorizontalAlignment.CENTER);
	    style.setVerticalAlignment(VerticalAlignment.CENTER);
	    cell.setCellStyle(style);
	}
	
	private void setRowStyle2(HSSFWorkbook wb, HSSFCell cell, CellStyle style) {
		// Style the cell with borders all around.
		style.setBorderBottom(BorderStyle.THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(BorderStyle.THIN);
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderRight(BorderStyle.THIN);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderTop(BorderStyle.THIN);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setVerticalAlignment(VerticalAlignment.TOP);
		cell.setCellStyle(style);
	}
	
	private void setRowStyle3(HSSFWorkbook wb, HSSFCell cell, CellStyle style) {
		// Style the cell with borders all around.
		style.setBorderBottom(BorderStyle.THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(BorderStyle.THIN);
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderRight(BorderStyle.THIN);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderTop(BorderStyle.THIN);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		
		style.setAlignment(HorizontalAlignment.LEFT);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		cell.setCellStyle(style);
	}

	private Integer total(Map<String, Integer> calculateTwo) {
		Integer tempTotal = 0;
		for (Map.Entry<String, Integer> entry : calculateTwo.entrySet()) {
			Integer value = entry.getValue();
			tempTotal += value;
			
		}
		return tempTotal;
	}
	
	private Integer avg(Map<String, Integer> calculateTwo) {
		Integer tempTotal = 0;
		for (Map.Entry<String, Integer> entry : calculateTwo.entrySet()) {
			Integer value = entry.getValue();
			tempTotal += value;
			
		}
		return (int) Math.round(tempTotal * 1.0 / calculateTwo.size());
	}
	
}
