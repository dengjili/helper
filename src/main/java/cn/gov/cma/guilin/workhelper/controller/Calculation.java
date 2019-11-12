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
 * 15. 重复次数计算到非人为次数（白天计算值、晚上校验者）
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
	// 粗略计算迟测时间段(7.21 - 13.00)(19.21 - 0.00) (1.21 - 4.00)
	// 7.大于XX时21为非人为迟测，记录到主班对应的个人身上【非人为迟测】
	public Map<String, Integer> calculateFour() throws Exception {
		IncrementMap result = new IncrementMap();

		for (Work work : uniqueDatas) {
			boolean isBefore = false;
			String beginTime = work.getBeginTime();

			// 满足任意区间都为早测
			boolean result1 = DateHelper.isBetween(beginTime, "07.21.00", "13.00.00");
			if (result1) {
				isBefore = true;
			}
			boolean result2 = DateHelper.isBetween(beginTime, "19.21.00", "00.00.00");
			if (result2) {
				isBefore = true;
			}
			boolean result3 = DateHelper.isBetween(beginTime, "01.21.00", "04.00.00");
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
	
	// 8.07/19 球炸  name探空高度和测风高度给校对者
	// 9.07/19 信号突失/仪器故障 name探空高度和测风高度给计算着
	// 10.07/19 干扰/雷达故障 name探空高度和测风高度 给统计到站
	// 探空高度 计算
	public Map<String, Map<String, Integer>> calculateFive() throws Exception {
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
			case "仪器故障":
				incrementAndAvg.put(work.getCalculator(), Long.parseLong(work.getAirStopHight()));
				break;
			case "干扰":
				incrementAndAvg.put("统计到站", Long.parseLong(work.getAirStopHight()));
				break;
			case "雷达故障":
				incrementAndAvg.put("统计到站", Long.parseLong(work.getAirStopHight()));
				break;
			default:
				break;
			}
		}
		
		Map<String, Integer> timesMap = incrementAndAvg.getTimesMap();
		Map<String, Long> sumMap = incrementAndAvg.getSumMap();
		Map<String, Integer> avgMap = new HashMap<>();
		sumMap.forEach((k,v)->{
			Integer times = timesMap.get(k);
			Integer avg = (int) Math.round((v * 1.0 / times));
			avgMap.put(k, avg);
		});
		
		Map<String, Map<String, Integer>> result = new HashMap<>();
		result.put("TIMES", timesMap);
		result.put("AVG", avgMap);
		return result;
	}
	
	// 8.07/19 球炸 name探空高度和测风高度给校对者
	// 9.07/19 信号突失/仪器故障 name探空高度和测风高度给计算着
	// 10.07/19 干扰/雷达故障 name探空高度和测风高度 给统计到站
	// 测风高度 综合计算
	public Map<String, Map<String, Integer>> calculateSix() throws Exception {
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
			case "仪器故障":
				incrementAndAvg.put(work.getCalculator(), Long.parseLong(work.getWindStopHight()));
				break;
			case "干扰":
				incrementAndAvg.put("统计到站", Long.parseLong(work.getWindStopHight()));
				break;
			case "雷达故障":
				incrementAndAvg.put("统计到站", Long.parseLong(work.getWindStopHight()));
				break;
			default:
				break;
			}
		}

		Map<String, Integer> timesMap = incrementAndAvg.getTimesMap();
		Map<String, Long> sumMap = incrementAndAvg.getSumMap();
		Map<String, Integer> avgMap = new HashMap<>();
		sumMap.forEach((k, v) -> {
			Integer times = timesMap.get(k);
			Integer avg = (int) Math.round((v * 1.0 / times));
			avgMap.put(k, avg);
		});

		Map<String, Map<String, Integer>> result = new HashMap<>();
		result.put("TIMES", timesMap);
		result.put("AVG", avgMap);
		return result;
	}
	
	// 12.01 球炸  name探空高度和测风高度给计算着
	// 13.01 信号突失/仪器故障  name探空高度和测风高度给校对者
	// 14.01 干扰/雷达故障  name探空高度和测风高度 给统计到站
	// 测风高度 单测计算
	public Map<String, Map<String, Integer>> calculateSeven() throws Exception {
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
			case "仪器故障":
				incrementAndAvg.put(work.getChecktor(), Long.parseLong(work.getWindStopHight()));
				break;
			case "干扰":
				incrementAndAvg.put("统计到站", Long.parseLong(work.getWindStopHight()));
				break;
			case "雷达故障":
				incrementAndAvg.put("统计到站", Long.parseLong(work.getWindStopHight()));
				break;
			default:
				break;
			}
		}
		
		Map<String, Integer> timesMap = incrementAndAvg.getTimesMap();
		Map<String, Long> sumMap = incrementAndAvg.getSumMap();
		Map<String, Integer> avgMap = new HashMap<>();
		sumMap.forEach((k, v) -> {
			Integer times = timesMap.get(k);
			Integer avg = (int) Math.round((v * 1.0 / times));
			avgMap.put(k, avg);
		});
		
		Map<String, Map<String, Integer>> result = new HashMap<>();
		result.put("TIMES", timesMap);
		result.put("AVG", avgMap);
		return result;
	}
	
	// 15. 重复次数计算到非人为次数（白天计算值、晚上校验者）
	public Map<String, Integer> calculateEight() throws Exception {
		IncrementMap result = new IncrementMap();
		Set<String> fileNameSet = new HashSet<>(dataAll.size() * 2);
		for (Work work : dataAll) {
			String fileName = work.getFileName();
			if (fileNameSet.contains(fileName)) {
				if (work.isNomalMajor()) {
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
