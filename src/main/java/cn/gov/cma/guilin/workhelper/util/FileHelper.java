package cn.gov.cma.guilin.workhelper.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.gov.cma.guilin.workhelper.model.Work;

public interface FileHelper {
	public static String codeString(String filePath) {
		File file = new File(filePath);
		try (BufferedInputStream bin = new BufferedInputStream(new FileInputStream(file));) {
			int p = (bin.read() << 8) + bin.read();
			String code = null;
			// 其中的 0xefbb、0xfffe、0xfeff、0x5c75这些都是这个文件的前面两个字节的16进制数
			switch (p) {
			case 0xefbb:
				code = "UTF-8";
				break;
			case 0xfffe:
				code = "Unicode";
				break;
			case 0xfeff:
				code = "UTF-16BE";
				break;
			case 0x5c75:
				code = "ANSI|ASCII";
				break;
			default:
				code = "GB2312";
			}

			return code;
		} catch (Exception e) {
			return  "GB2132";
		}
	}

	/**
	 * 
	 * Created by it
	 * Created in 下午12:38:37
	 * Description: 转换为没有重复的数据,已文件名为唯一标识，默认后面的数据覆盖前面的数据
	 */
	public static List<Work> converUnique(List<Work> dataAll){
		// 利用map的数据特性，key唯一，后面的值会覆盖前面的值
		Map<String, Work> dataMap = new HashMap<>();
		dataAll.forEach((work)->dataMap.put(work.getFileName(), work));
		List<Work> uniquedatas = new ArrayList<>(dataMap.values());
		return uniquedatas;
	}
}
