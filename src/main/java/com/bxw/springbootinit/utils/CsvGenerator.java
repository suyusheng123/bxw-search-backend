package com.bxw.springbootinit.utils;

import java.io.FileWriter;
import java.io.IOException;

/**
 * ClassName: CsvGenerator
 * Description:
 * 生成1000个不同搜索内容的csv文件，用来做压测
 * @Author 坤坤学🐸
 * @Create 2024/8/27 9:37
 * @Version 1.0
 */
public class CsvGenerator {
	public static void main(String[] args) {
		String fileName = "C:\\Users\\Lenovo\\Desktop\\search_data.csv";

		try (FileWriter writer = new FileWriter(fileName)) {
			// 写入CSV文件的标题行
			writer.append("current,pageSize,searchText,sortField,sortOrder,type\n");

			// 生成CSV文件的内容
			int[] pageSizes = {10, 10, 12};
			String[] searchTexts = {
					"红帽", "蓝帽", "绿帽", "黄帽", "紫帽", "黑帽", "白帽", "灰帽", "橙帽", "粉帽",
					"棕帽", "金帽", "银帽", "铜帽", "青帽", "蓝绿帽", "深蓝帽", "浅蓝帽", "深红帽", "浅红帽"
			};
			String[] types = {"article", "picture", "video"};

			for (int i = 0; i < 200; i++) {
				int typeIndex = i % 3; // 轮换使用types数组中的值
				int searchTextIndex = i % 20; // 轮换使用searchTexts数组中的值
				writer.append((i + 1) + ",")
						.append(pageSizes[typeIndex] + ",")
						.append(searchTexts[searchTextIndex] + ",")
						.append("id,")
						.append("descend,")
						.append(types[typeIndex] + "\n");
			}

			System.out.println("CSV 文件已成功生成: " + fileName);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
