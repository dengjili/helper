package cn.gov.cma.guilin.workhelper.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.gov.cma.guilin.workhelper.model.Work;
import cn.gov.cma.guilin.workhelper.util.DateHelper;

/**
 * Created by it 
 * Created in 2019年4月27日 
 * Description: 所有计算
 * 
 * <pre>
 * 1.统计每月天数是否正常，如果缺少，需要统计缺少次数（一天正常是三条）
 * 2.如果文件名重复，后面覆盖前面的
 * 3.7点和19点是主班次数，对应计算者
 * 4.01点是主班次数，对应校验者
 * 5.统计到站记录到非人为缺测，对应缺少次数
 * 6.少于XX时15为早测，记录到主班对应的个人身上【早测】
 * 7.大于XX时21为非人为迟测，记录到主班对应的个人身上【非人为迟测】
 * 8.07/19 球炸  name探空高度和测风高度给校对者
 * 9.07/19 信号突失/仪器故障  name探空高度和测风高度给计算着
 * 10.07/19 干扰/雷达故障  name探空高度和测风高度 给统计到站
 * 11.07/19 测试高度为平均高度，01为单侧
 * 12.01 球炸  name探空高度和测风高度给计算着
 * 13.01 信号突失/仪器故障  name探空高度和测风高度给校对者
 * 14.01 干扰/雷达故障  name探空高度和测风高度 给统计到站
 * 15. 重放球次数计算到非人为次数（白天计算者、晚上校验者）
 * C:\Users\it\Desktop\高表21\57957站2019年12月值班日志.txt
 * </pre>
 */
public class Calculation {

	// 所有数据
	private List<Work> dataAll;
	// 2.如果文件名重复，后面覆盖前面的
	// 过滤重复数据
	private List<Work> uniqueDatas;
	// 记录当月天数
	private int days = 0;

	public Calculation(List<Work> dataAll, List<Work> uniqueDatas) throws Exception {
		this.dataAll = dataAll;
		this.uniqueDatas = uniqueDatas;
		String fileName = dataAll.get(0).getFileName();
		days = DateHelper.getDays(fileName);
	}
	
	// 1.统计每月天数是否正常，如果缺少，需要统计缺少次数（一天正常是三条）
	// 5.统计到站记录到非人为缺测，对应缺少次数
	// 缺测次数
	public int calculateOne() {
		// 应有次数， 一天三次
		int times = days * 3;
		
		IncrementMap map = new IncrementMap();
		for (Work work : uniqueDatas) {
			String fileName = work.getFileName();
			String date = fileName.substring(6, 14);
			map.put(date);
		}
		int total = 0;
		for (Integer value : map.values()) {
			if (value >= 3) {
				total += 3;
			} else {
				total += value;
			}
		}
		int result = times - total;
		
		return result;
	}

	// 3.7点和19点是主班次数，对应计算者
	// 4.01点是主班次数，对应校验者
	// 计算主班次数,对应名字与次数关系
	public Map<String, Integer> calculateTwo() {
		IncrementMap result = new IncrementMap();
		uniqueDatas.forEach((work) -> {
			if (work.isNomalMajor()) {
				result.put(work.getCalculator());
			} else {
				result.put(work.getChecktor());
			}
		});
		return result;
	}

	// 正常放球时间段【7.15-7.21】 【19.15-19.21】 】1.15-1.21】
	// 粗略计算早测时间段(4.00 - 7.15)(13.00 - 19.15) (0.00 - 1.15)
	// 6.少于XX时15为早测，记录到主班对应的个人身上【早测】
	public Map<String, Integer> calculateThree() throws Exception {
		IncrementMap result = new IncrementMap();

		for (Work work : uniqueDatas) {
			boolean isBefore = false;
			String beginTime = work.getBeginTime();

			// 满足任意区间都为早测
			boolean result1 = DateHelper.isBetween(beginTime, "04.00.00", "07.15.00");
			if (result1) {
				isBefore = true;
			}
			boolean result2 = DateHelper.isBetween(beginTime, "13.00.00", "19.15.00");
			if (result2) {
				isBefore = true;
			}
			boolean result3 = DateHelper.isBetween(beginTime, "00.00.00", "01.15.00");
			if (result3) {
				isBefore = true;
			}

			if (isBefore) {
				if (work.isNomalMajor()) {
					result.put(work.getCalculator());
				} else {
					result.put(work.getChecktor());
				}
			}
		}
		return result;
	}
	
	// 正常放球时间段【7.15-7.21】 【19.15-19.21】 】1.15-1.21】
	// （07. 21.00 - 07.45.00）（07.46.00 - 08.15.00）（08.16.00 - 08.30.00）
	//  (19 21.00 - 19.45.00)   (19.46.00 - 20.15.00) ( 20.16.00 -  20.30.00)
	//  (01. 21.00 - 01.45.00)  (01.46.00 - 02.15.00) ( 02.16.00 -  02.30.00)
	// 7.大于XX时21为非人为迟测，记录到主班对应的个人身上【非人为迟测】
	public Map<String, Map<String, Integer>> calculateFour() throws Exception {
		Map<String, Map<String, Integer>> all = new HashMap<String, Map<String,Integer>>();
		
		
		IncrementMap one = new IncrementMap();
		IncrementMap two = new IncrementMap();
		IncrementMap three = new IncrementMap();

		for (Work work : uniqueDatas) {
			boolean manyTimes = work.isManyTimes();
			if (manyTimes) {
				continue;
			}
			
			boolean nomalMajor = work.isNomalMajor();
			if (!nomalMajor) {
				continue;
			}
			
			String beginTime = work.getBeginTime();

			boolean isAfterOne = false;
			// 满足任意区间都为迟测
			boolean resultOne1 = DateHelper.isBetween(beginTime, "07.21.00", "07.45.59");
			if (resultOne1) {
				isAfterOne = true;
			}
			boolean resultOne2 = DateHelper.isBetween(beginTime, "19.21.00", "19.45.59");
			if (resultOne2) {
				isAfterOne = true;
			}
			boolean resultOne3 = DateHelper.isBetween(beginTime, "01.21.00", "01.45.59");
			if (resultOne3) {
				isAfterOne = true;
			}

			if (isAfterOne) {
				if (nomalMajor) {
					one.put(work.getCalculator());
				} else {
					one.put(work.getChecktor());
				}
			}
			
			boolean isAfterTwo = false;
	
			boolean resultTwo1 = DateHelper.isBetween(beginTime, "07.46.00", "08.15.59");
			if (resultTwo1) {
				isAfterTwo = true;
			}
			boolean resultTwo2 = DateHelper.isBetween(beginTime, "19.46.00", "20.15.59");
			if (resultTwo2) {
				isAfterTwo = true;
			}
			boolean resultTwo3 = DateHelper.isBetween(beginTime, "01.46.00", "02.15.59");
			if (resultTwo3) {
				isAfterTwo = true;
			}

			if (isAfterTwo) {
				if (nomalMajor) {
					two.put(work.getCalculator());
				} else {
					two.put(work.getChecktor());
				}
			}
			
			boolean isAfterThree = false;
	
			boolean resultThree1 = DateHelper.isBetween(beginTime, "08.16.00", "08.30.59");
			if (resultThree1) {
				isAfterThree = true;
			}
			boolean resultThree2 = DateHelper.isBetween(beginTime, "20.16.00", "20.30.59");
			if (resultThree2) {
				isAfterThree = true;
			}
			boolean resultThree3 = DateHelper.isBetween(beginTime, "02.16.00", "02.30.59");
			if (resultThree3) {
				isAfterThree = true;
			}

			if (isAfterThree) {
				if (nomalMajor) {
					three.put(work.getCalculator());
				} else {
					three.put(work.getChecktor());
				}
			}
		}
		all.put("levelOne", one);
		all.put("levelTwo", two);
		all.put("levelThree", three);
		
		return all;
	}
	
	// 8.07/19 球炸  name探空高度和测风高度给校对者
	// 9.07/19 信号突失/仪器故障/信号不清  name探空高度和测风高度给计算着
	// 10.07/19 干扰/雷达故障 name探空高度和测风高度 给统计到站
	// 探空高度 计算
	public Map<String, Map<String, ?>> calculateFive() throws Exception {
		IncrementAndAvg incrementAndAvg = new IncrementAndAvg();
		for (Work work : uniqueDatas) {
			
			// 凌晨1点不计算，无高度
			if (!work.isNomalMajor()) {
				continue;
			}
			
			String airStopReason = work.getAirStopReason();
			switch (airStopReason) {
			case "球炸":
				incrementAndAvg.put(work.getChecktor(), Long.parseLong(work.getAirStopHight()));
				break;
			case "信号突失":
				incrementAndAvg.put(work.getCalculator(), Long.parseLong(work.getAirStopHight()));
				break;
			case "信号不清":
				incrementAndAvg.put(work.getCalculator(), Long.parseLong(work.getAirStopHight()));
				break;
			case "仪器故障":
				incrementAndAvg.put(work.getCalculator(), Long.parseLong(work.getAirStopHight()));
				break;
			case "干扰":
			//	incrementAndAvg.put("统计到站", Long.parseLong(work.getAirStopHight()));
				break;
			case "雷达故障":
				//incrementAndAvg.put("统计到站", Long.parseLong(work.getAirStopHight()));
				break;
			default:
				break;
			}
		}
		
		Map<String, Integer> timesMap = incrementAndAvg.getTimesMap();
		Map<String, Long> sumMap = incrementAndAvg.getSumMap();
		Map<String, Integer> avgMap = new HashMap<>();
		Map<String, Float> scoreMap = new HashMap<>();
		sumMap.forEach((k,v)->{
			Integer times = timesMap.get(k);
			Integer avg = (int) Math.round((v * 1.0 / times));
			avgMap.put(k, avg);
			if(avg >= 28600) {				
				scoreMap.put(k, 15f);
			}
			else if(18600 < avg && avg < 28600) {				
				scoreMap.put(k, (float) ((avg-18600) * 0.0015));
			}
			else if( avg <= 18600 ) {				
				scoreMap.put(k, 0f);
			}
		});
		
		Map<String, Map<String, ?>> result = new HashMap<>();
		result.put("TIMES", timesMap);
		result.put("AVG", avgMap);
		result.put("SCORE", scoreMap);
		return result;
	}
	
	// 8.07/19 球炸 name探空高度和测风高度给校对者
	// 9.07/19 信号突失/仪器故障/信号不清 name探空高度和测风高度给计算着
	// 10.07/19 干扰/雷达故障 name探空高度和测风高度 给统计到站
	// 测风高度 综合计算
	public Map<String, Map<String, ?>> calculateSix() throws Exception {
		IncrementAndAvg incrementAndAvg = new IncrementAndAvg();
		for (Work work : uniqueDatas) {

			// 凌晨1点不计算，只计算综合
			if (!work.isNomalMajor()) {
				continue;
			}

			String windStopReason = work.getWindStopReason();
			switch (windStopReason) {
			case "球炸":
				incrementAndAvg.put(work.getChecktor(), Long.parseLong(work.getWindStopHight()));
				break;
			case "信号突失":
				incrementAndAvg.put(work.getCalculator(), Long.parseLong(work.getWindStopHight()));
				break;
			case "信号不清":
				incrementAndAvg.put(work.getCalculator(), Long.parseLong(work.getWindStopHight()));
				break;
			case "仪器故障":
				incrementAndAvg.put(work.getCalculator(), Long.parseLong(work.getWindStopHight()));
				break;
			case "干扰":
			//	incrementAndAvg.put("统计到站", Long.parseLong(work.getWindStopHight()));
				break;
			case "雷达故障":
			//	incrementAndAvg.put("统计到站", Long.parseLong(work.getWindStopHight()));
				break;
			default:
				break;
			}
		}

		Map<String, Integer> timesMap = incrementAndAvg.getTimesMap();
		Map<String, Long> sumMap = incrementAndAvg.getSumMap();
		Map<String, Integer> avgMap = new HashMap<>();
		Map<String, Float> scoreMap = new HashMap<>();
		sumMap.forEach((k, v) -> {
			Integer times = timesMap.get(k);
			Integer avg = (int) Math.round((v * 1.0 / times));
			avgMap.put(k, avg);
			if(avg >= 27000) {				
				scoreMap.put(k, 12f);
			}
			else if(17000 < avg && avg < 27000) {				
				scoreMap.put(k, (float) ((avg-17000) * 0.0012));
			}
			else if( avg <= 17000 ) {				
				scoreMap.put(k, 0f);
			}
		});

		Map<String, Map<String, ?>> result = new HashMap<>();
		result.put("TIMES", timesMap);
		result.put("AVG", avgMap);
		result.put("SCORE", scoreMap);
		return result;
	}
	
	// 12.01 球炸  name探空高度和测风高度给计算着
	// 13.01 信号突失/仪器故障 /信号不清 name探空高度和测风高度给校对者
	// 14.01 干扰/雷达故障  name探空高度和测风高度 给统计到站
	// 测风高度 单测计算
	public Map<String, Map<String, ?>> calculateSeven() throws Exception {
		IncrementAndAvg incrementAndAvg = new IncrementAndAvg();
		for (Work work : uniqueDatas) {
			
			// 只计算凌晨1点
			if (work.isNomalMajor()) {
				continue;
			}
			
			String windStopReason = work.getWindStopReason();
			switch (windStopReason) {
			case "球炸":
				incrementAndAvg.put(work.getCalculator(), Long.parseLong(work.getWindStopHight()));
				break;
			case "信号突失":
				incrementAndAvg.put(work.getChecktor(), Long.parseLong(work.getWindStopHight()));
				break;
			case "信号不清":
				incrementAndAvg.put(work.getChecktor(), Long.parseLong(work.getWindStopHight()));
				break;
			case "仪器故障":
				incrementAndAvg.put(work.getChecktor(), Long.parseLong(work.getWindStopHight()));
				break;
			case "干扰":
				//incrementAndAvg.put("统计到站", Long.parseLong(work.getWindStopHight()));
				break;
			case "雷达故障":
				//incrementAndAvg.put("统计到站", Long.parseLong(work.getWindStopHight()));
				break;
			default:
				break;
			}
		}
		
		Map<String, Integer> timesMap = incrementAndAvg.getTimesMap();
		Map<String, Long> sumMap = incrementAndAvg.getSumMap();
		Map<String, Integer> avgMap = new HashMap<>();
		Map<String, Float> scoreMap = new HashMap<>();
		sumMap.forEach((k, v) -> {
			Integer times = timesMap.get(k);
			Integer avg = (int) Math.round((v * 1.0 / times));
			avgMap.put(k, avg);
			if(avg >= 18000) {				
				scoreMap.put(k, 3f);
			}
			else if( avg < 18000 ) {				
				scoreMap.put(k, 0f);
			}
		});
		
		Map<String, Map<String, ?>> result = new HashMap<>();
		result.put("TIMES", timesMap);
		result.put("AVG", avgMap);
		result.put("SCORE", scoreMap);
		return result;
	}
	
	// 15. 重复次数计算到非人为次数（白天计算值、晚上校验者）
	public Map<String, Integer> calculateEight() throws Exception {
		IncrementMap result = new IncrementMap();
		Set<String> fileNameSet = new HashSet<>(dataAll.size() * 2);
		for (Work work : dataAll) {
			boolean nomalMajor = work.isNomalMajor();
			if (!nomalMajor) {
				continue;
			}
			String fileName = work.getFileName();
			if (fileNameSet.contains(fileName)) {
				if (nomalMajor) {
					result.put(work.getCalculator());
				} else {
					result.put(work.getChecktor());
				}
			}
			fileNameSet.add(fileName);
		}
		return result;
	}

	// 统计器类
	private class IncrementMap extends HashMap<String, Integer> {
		private static final long serialVersionUID = 1L;

		public void put(String name) {
			Integer times = get(name);
			if (times == null) {
				put(name, 1);
			} else {
				put(name, ++times);
			}
		}
	}
	
	// 累加类
	private class SumMap extends HashMap<String, Long> {
		private static final long serialVersionUID = 1L;

		public void putSum(String name, Long nums) {
			Long now = get(name);
			if (now == null) {
				put(name, nums);
			} else {
				put(name, now + nums);
			}
		}
	}
	
	// 累加类
	public static class SumMapFloat extends HashMap<String, Float> {
		private static final long serialVersionUID = 1L;

		public void putSumFloat(String name, Float nums) {
			Float now = get(name);
			if (now == null) {
				put(name, nums);
			} else {
				put(name, now + nums);
			}
		}
	}
	
	private class IncrementAndAvg {
		// 统计次数
		IncrementMap timesMap = new IncrementMap();
		SumMap sumMap = new SumMap();
		
		public void put(String name, long num) {
			timesMap.put(name);
			sumMap.putSum(name, num);
		}
		
		public HashMap<String, Long> getSumMap() {
			return sumMap;
		}
		
		public HashMap<String, Integer> getTimesMap() {
			return timesMap;
		}
	}
}
