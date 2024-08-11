package com.bxw.springbootinit.utils;

import java.nio.charset.StandardCharsets;

/**
 * ClassName:Base64
 * Package:com.bxw.springbootinit.utils
 * Description:
 * Base64工具类
 *
 * @Author 卜翔威
 * @Create 2024/8/10 17:34
 * @Version 1.0
 */
public class Base64 {
	public static void main(String[] args) {
		String proxy = "aHR0cHM6Ly93d3cuZGF0YWZpcnN0LmNvbS9ibG9nL2VudmlzaW9uZWQtZHJlYW1zLWN1c3RvbWVyLXN1Y2Nlc3Mtc3Rvcmllcy11bnZlaWxlZC9hNmUyNzM0ZDBhM2IxMjIwMzUwOWY3NWUzNDc5MjJhZQ==";
		String url = new String(java.util.Base64.getDecoder().decode(proxy), StandardCharsets.UTF_8);
		System.out.println(url);
	}
}
