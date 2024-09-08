package com.bxw.springbootinit.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

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
					"vue", "react", "java"
			};
			String[] types = {"article", "picture", "video"};
			for (int i = 0; i < 1000; i++) {
				// 生成1到3的随机数
				Random random = new Random();
				int index = random.nextInt(3);
                int searchIndex = random.nextInt(3);
				int page = i % 10 + 1;
				writer.append(page + ",")
						.append(pageSizes[index] + ",")
						.append(searchTexts[searchIndex] + ",")
						.append("id,")
						.append("descend,")
						.append(types[index] + "\n");
			}

			System.out.println("CSV 文件已成功生成: " + fileName);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
