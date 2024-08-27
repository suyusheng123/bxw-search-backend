package com.bxw.springbootinit.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.internal.StringUtil;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ClassName: GetSuggestUtils
 * Description:
 * 第三方搜索建议接口测试
 * @Author 坤坤学🐸
 * @Create 2024/8/25 21:18
 * @Version 1.0
 */

@Slf4j
public class GetSuggestUtils {
	public static void main(String[] args) {
		String searchText = "java";
		String url = String.format("https://kaifa.baidu.com/rest/v1/recommend/suggests?wd=%s", searchText);
		String res = HttpRequest.get(url).execute().body();
		System.out.println(res);
		Map<String,Object> suggestRes= JSONUtil.toBean(res, Map.class);
		String status = String.valueOf(suggestRes.get("status"));
		if(!Objects.equals(status,"OK")){
			log.error("第三方网站搜索接口异常,搜索内容为{}",searchText);
			return;
		}
		JSONArray suggestWords = (JSONArray)suggestRes.get("data");
		if(ObjectUtil.isEmpty(suggestWords)){
			log.error("第三方网站搜索接口返回数据为空，关键字为{}",searchText);
			return;
		}
		List<String> words = JSONUtil.toList(suggestWords, String.class);
		System.out.println(words);
	}
}
