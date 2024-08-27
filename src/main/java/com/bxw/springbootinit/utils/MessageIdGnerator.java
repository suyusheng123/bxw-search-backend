package com.bxw.springbootinit.utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

/**
 * ClassName: MessageIdGnerator
 * Description:
 * 根据字符串生成唯一id
 * @Author 坤坤学🐸
 * @Create 2024/8/25 20:08
 * @Version 1.0
 */
public class MessageIdGnerator {
	public static String generateNumericId(String title) {
		try {
			// 获取SHA-256实例
			MessageDigest digest = MessageDigest.getInstance("SHA-256");

			// 将输入标题字符串转换为字节数组
			byte[] hash = digest.digest(title.getBytes(StandardCharsets.UTF_8));

			// 将字节数组转换为正整数
			BigInteger number = new BigInteger(1, hash);

			// 将正整数转换为字符串形式
			String numericId = number.toString(10);

			// 如果需要，可以截取前N位，保证ID长度
			return numericId.length() > 16 ? numericId.substring(0, 16) : numericId;

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	public static void main(String[] args) {
		String title = "红帽linux中文系统下载iso,红帽子9.0版下载-redhat linux 9.0 iso下载 简体中文正式版-IT猫扑网..._程序员必修课的博客-CSDN博客" +
				"这是一个包含 Emoji 的标题 \uD83D\uDE0A";
		String numericId = generateNumericId(title);
		System.out.println("Generated Numeric ID: " + numericId);
	}
}
