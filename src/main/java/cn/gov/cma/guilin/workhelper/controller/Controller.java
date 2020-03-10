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

import cn.gov.cma.guilin.workhelper.controller.Calculation.SumMapFloat;
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
			
			logger.debug("{}", String.format("早测满分10分。"));
			
			Map<String, Float> calculateThreeScore = new HashMap<String, Float>();
			calculateThree.forEach((k,v)->{
				calculateThreeScore.put(k, (float) (5.0 * calculateThree.get(k)));
			});
			calculateThreeScore.forEach((k,v)->{
				logger.debug("{}", String.format("名字：%s, 扣分：%.2f", k, calculateThreeScore.get(k)));
			});
			
			Map<String, Map<String, Integer>> calculateFour = calculation.calculateFour();
			Map<String, Integer> levelOne = calculateFour.get("levelOne");
			Map<String, Integer> levelTwo = calculateFour.get("levelTwo");
			Map<String, Integer> levelThree = calculateFour.get("levelThree");
			logger.debug("{}", String.format("非人为迟测次数（时间区间1）：%s", levelOne));
			logger.debug("{}", String.format("非人为迟测次数（时间区间2）：%s", levelTwo));
			logger.debug("{}", String.format("非人为迟测次数（时间区间3）：%s", levelThree));
	
			logger.debug("{}", String.format("非人为迟测满分10分。"));
			SumMapFloat scoreMapFour = new SumMapFloat();
			levelOne.forEach((k,v)->{
				scoreMapFour.putSumFloat(k, (float) (0.5 * levelOne.get(k)));
			});
			levelTwo.forEach((k,v)->{
				scoreMapFour.putSumFloat(k, (float) (1.0 * levelTwo.get(k)));
			});
			levelThree.forEach((k,v)->{
				scoreMapFour.putSumFloat(k, (float) (1.5 * levelThree.get(k)));
			});
			scoreMapFour.forEach((k,v)->{
				logger.debug("{}", String.format("名字：%s, 扣分： %.2f", k, scoreMapFour.get(k)));
			});
			
			SumMapFloat scoreMapTotal = new SumMapFloat();
			calculateThreeScore.forEach((k,v)->{
				scoreMapTotal.putSumFloat(k, v);
			});
			scoreMapFour.forEach((k,v)->{
				scoreMapTotal.putSumFloat(k, v);
			});
			logger.debug("{}", String.format("早测、迟测、任意终止观测得分30分。"));
			scoreMapTotal.forEach((k,v)->{
				logger.debug("{}", String.format("名字：%s, 总分： %.2f", k, 30.0 - scoreMapTotal.get(k)));
			});
			
			Map<String, Map<String, ?>> calculateFive = calculation.calculateFive();
			Map<String, Integer> timesMap = (Map<String, Integer>) calculateFive.get("TIMES");
			Map<String, Integer> avgMap = (Map<String, Integer>) calculateFive.get("AVG");
			Map<String, Float> scoreMap = (Map<String, Float>) calculateFive.get("SCORE");
			logger.debug("{}", "探空高度");
			timesMap.forEach((k,v)->{
				logger.debug("{}", String.format("名字：%s, 平均高度：%s, 得分： %.2f", k, avgMap.get(k), scoreMap.get(k)));
			});
			
			Map<String, Map<String, ?>> calculateSix = calculation.calculateSix();
			Map<String, Integer> timesMap2 = (Map<String, Integer>) calculateSix.get("TIMES");
			Map<String, Integer> avgMap2 = (Map<String, Integer>) calculateSix.get("AVG");
			Map<String, Float> scoreMap2 = (Map<String, Float>) calculateSix.get("SCORE");
			logger.debug("{}", "测风高度综合");
			timesMap2.forEach((k,v)->{
				logger.debug("{}", String.format("名字：%s, 平均高度：%s, 得分：  %.2f", k, avgMap2.get(k), scoreMap2.get(k)));
			});
			
			
			Map<String, Map<String, ?>> calculateSeven = calculation.calculateSeven();
			Map<String, Integer> timesMap3 = (Map<String, Integer>) calculateSeven.get("TIMES");
			Map<String, Integer> avgMap3 = (Map<String, Integer>) calculateSeven.get("AVG");
			Map<String, Float> scoreMap3 = (Map<String, Float>) calculateSeven.get("SCORE");
			logger.debug("{}", "测风高度 单测");
			timesMap3.forEach((k,v)->{
				logger.debug("{}", String.format("名字：%s, 平均高度：%s, 得分： %.2f", k, avgMap3.get(k), scoreMap3.get(k)));
			});
			logger.debug("{}", "测风高度得分");
			timesMap3.forEach((k,v)->{
				logger.debug("{}", String.format("名字：%s, 得分： %.2f", k,  scoreMap2.get(k) + scoreMap3.get(k)));
			});
			
			Map<String, Integer> calculateEight = calculation.calculateEight();
			logger.debug("{}", String.format("重复次数：%s", calculateEight));
			
			
			Map<String, Float> calculateEightScore = new HashMap<>();
			calculateEight.forEach((k, v) -> {
				if (v <= 6) {
					calculateEightScore.put(k, (float) (17 - 0.5 * v));
				} else if (v > 6) {
					int c = v - 6;
					calculateEightScore.put(k, (float) (17 - 3 - 1 * c));
				}
			});
			logger.debug("{}", String.format("重放球得分：%s", calculateEightScore));
			
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
			int afterOne = total(levelOne);
			int afterTwo = total(levelTwo);
			int afterThree = total(levelThree);
			logger.debug("{}", String.format("非人为迟测次数（区间1）：%s", afterOne));
			logger.debug("{}", String.format("非人为迟测次数（区间2）：%s", afterTwo));
			logger.debug("{}", String.format("非人为迟测次数（区间3）：%s", afterThree));
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
			InputStream inputStream = Controller.class.getClassLoader().getResourceAsStream("guilin-template2.xls");
			HSSFWorkbook wb = new HSSFWorkbook(inputStream);

			// 表头设置
			HSSFSheet sheet = wb.getSheet("分站统计");
			HSSFCell cell = sheet.getRow(0).getCell(0);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy年 MM月");
			cell.setCellValue(cell.getStringCellValue()+sdf.format(date));

			// 其他辅助类
			CreationHelper createHelper = wb.getCreationHelper();
			CellStyle style = wb.createCellStyle();
			CellStyle style2 = wb.createCellStyle();
			CellStyle style3 = wb.createCellStyle();
			
			// 填充数据，以主班姓名为依据
			int rows = 6;
			for (Map.Entry<String, Integer> entry : calculateTwo.entrySet()) {
				int column = 0;
				String name = entry.getKey();
				HSSFRow row = sheet.createRow(rows);
				row.setHeightInPoints(30);
				HSSFCell ncell1 = row.createCell(column++);
				setRowStyle(wb, ncell1, style);
				ncell1.setCellValue(createHelper.createRichTextString(name));
				
				// 探空平均高度
				HSSFCell ncell2 = row.createCell(column++);
				setRowStyleNumber(wb, ncell2, style2);
				ncell2.setCellValue(avgMap.get(name));
				
				HSSFCell ncell3 = row.createCell(column++);
				setRowStyleNumber(wb, ncell3, style2);
				ncell3.setCellValue(Double.parseDouble(String.format("%.2f", scoreMap.get(name))));
				
				// 综合测风平均高度
				HSSFCell ncell4 = row.createCell(column++);
				setRowStyleNumber(wb, ncell4, style2);
				ncell4.setCellValue(avgMap2.get(name));
				
				HSSFCell ncell5 = row.createCell(column++);
				setRowStyleNumber(wb, ncell5, style2);
				ncell5.setCellValue(Double.parseDouble(String.format("%.2f", scoreMap2.get(name))));
				// 02时单独测风高度
				HSSFCell ncell6 = row.createCell(column++);
				setRowStyleNumber(wb, ncell6, style2);
				ncell6.setCellValue(avgMap3.get(name));
				
				HSSFCell ncell7 = row.createCell(column++);
				setRowStyleNumber(wb, ncell7, style2);
				ncell7.setCellValue(Double.parseDouble(String.format("%.2f", scoreMap3.get(name))));
				// 测风高度得分
				HSSFCell ncell8 = row.createCell(column++);
				setRowStyleNumber(wb, ncell8, style2);
				ncell8.setCellValue(Double.parseDouble(String.format("%.2f", scoreMap2.get(name) + scoreMap3.get(name))));
				// 报文资料质量 固定100
				HSSFCell ncell9 = row.createCell(column++);
				setRowStyleNumber(wb, ncell9, style2);
				ncell9.setCellValue(100);
				// 报文质量得分 固定15
				HSSFCell ncell10 = row.createCell(column++);
				setRowStyleNumber(wb, ncell10, style2);
				ncell10.setCellValue(100);
				
				// 重放球
				{
					HSSFCell cell1 = row.createCell(column++);
					setRowStyle(wb, cell1, style2);
					cell1.setCellValue(toSafe(calculateEight.get(name)));
					HSSFCell cell2 = row.createCell(column++);
					setRowStyleNumber(wb, cell2, style2);
					cell2.setCellValue(0);
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(0);
					HSSFCell cell4 = row.createCell(column++);
					setRowStyleNumber(wb, cell4, style2);
					Float value = calculateEightScore.get(name);
					if (value == null) {
						cell4.setCellValue(17);
					} else {
						cell4.setCellValue(value);
					}
				}
				
				// 施放不合格仪器次数
				{
					HSSFCell cell1 = row.createCell(column++);
					setRowStyleNumber(wb, cell1, style2);
					cell1.setCellValue(0);
					HSSFCell cell2 = row.createCell(column++);
					setRowStyleNumber(wb, cell2, style2);
					cell2.setCellValue(8);
				}
				
				// 早测次数
				HSSFCell ncell11 = row.createCell(column++);
				setRowStyleNumber(wb, ncell11, style2);
				ncell11.setCellValue(toSafe(calculateThree.get(name)));
				// 早测扣分
				HSSFCell ncell12 = row.createCell(column++);
				setRowStyleNumber(wb, ncell12, style2);
				ncell12.setCellValue(Float.parseFloat(String.format("%.2f", FileHelper.safeGetFloat(calculateThreeScore.get(name)))));
				
				// 人为迟测
				for (int i = 0; i < 3; i++) {
					HSSFCell cell3 = row.createCell(column++);
					setRowStyle(wb, cell3, style2);
					cell3.setCellValue(0);
				}
				
				//非人为迟测
				{
					HSSFCell cell1 = row.createCell(column++);
					setRowStyleNumber(wb, cell1, style2);
					cell1.setCellValue(toSafe(levelOne.get(name)));
					HSSFCell cell2 = row.createCell(column++);
					setRowStyleNumber(wb, cell2, style2);
					cell2.setCellValue(toSafe(levelTwo.get(name)));
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(toSafe(levelThree.get(name)));
					HSSFCell cell4 = row.createCell(column++);
					setRowStyleNumber(wb, cell4, style2);
					cell4.setCellValue(Float.parseFloat(String.format("%.2f", FileHelper.safeGetFloat(scoreMapFour.get(name)))));
				}
				// 扣分 5项
				{
					for (int i = 0; i < 5; i++) {
						HSSFCell cell3 = row.createCell(column++);
						setRowStyle(wb, cell3, style2);
						cell3.setCellValue(0);
					}
				}
				// 早测、迟测、任意终止观测得分
				{
					HSSFCell cell4 = row.createCell(column++);
					setRowStyleNumber(wb, cell4, style2);
					cell4.setCellValue(Float.parseFloat(String.format("%.2f", 30.0 - toSafe(scoreMapTotal.get(name)))));
				}
				// 业务过程得分
				{
					HSSFCell cell3 = row.createCell(column++);
					setRowStyle(wb, cell3, style2);
					cell3.setCellValue("");
				}
				
				rows++;
			}
			
			// 桂林 一行
			{
				int column = 0;
				HSSFRow row = sheet.createRow(rows);
				row.setHeightInPoints(30);
				HSSFCell cell1 = row.createCell(column++);
				setRowStyle(wb, cell1, style);
				cell1.setCellValue(createHelper.createRichTextString("桂林"));
				
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
				{
					HSSFCell cell3 = row.createCell(column++);
					setRowStyleNumber(wb, cell3, style2);
					cell3.setCellValue(0);
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
				for (int i = 0; i < 12; i++) {
					HSSFRow row = sheet.createRow(rows);
					for (int j = 0; j < 32; j++) {
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
					0, //first column (0-based)
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
	
	private Integer toSafe(Integer num) {
		return num == null ? 0 : num;
	}
	private Float toSafe(Float num) {
		return num == null ? 0f : num;
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
	    style.setAlignment(HorizontalAlignment.CENTER);
	    style.setVerticalAlignment(VerticalAlignment.CENTER);
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
