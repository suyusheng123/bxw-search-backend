package com.bxw.springbootinit;

import cn.hutool.core.lang.Console;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.bxw.springbootinit.model.entity.Picture;
import com.bxw.springbootinit.model.entity.Post;
import com.bxw.springbootinit.service.PostService;
import javafx.geometry.Pos;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ClassName:CrawlerTest
 * Package:com.bxw.springbootinit
 * Description:
 * 数据抓取测试类
 *
 * @Author 卜翔威
 * @Create 2024/7/28 16:11
 * @Version 1.0
 */

@SpringBootTest
public class CrawlerTest {

	@Resource
	PostService postService;

	@Test
	public void testFetchPicture() throws IOException {
		Integer current = 1;
		String url = "https://cn.bing.com/images/search?qs=n&form=QBIR&sp=-1&lq=0&sc=10-10&cvid=14BF03DE047E48B0A5B2665F15404967&ghsh=0&ghacc=0&first=" + current;
		Document doc = Jsoup.connect(url).get();
		Elements select = doc.select(".iuscp.isv");
		List<Picture> pictures = new ArrayList<>();
		for (Element e : select) {
			Picture pic = new Picture();
			// 获取图片地址
			String m  = e.select(".iusc").get(0).attr("m");
			Map<String,Object> map = JSONUtil.toBean(m,Map.class);
			String murl = (String) map.get("murl");
			pic.setUrl(murl);

			// 获取标题
			String title  = e.select(".inflnk").get(0).attr("aria-label");
			pic.setTitle(title);
			pictures.add(pic);
		}
		System.out.println(pictures);
	}

	@Test
	public void testFetchPassage() {
		String json = "{\"pageSize\":12,\"sortOrder\":\"descend\",\"sortField\":\"createTime\",\"tags\":[\"笔记\"],\"current\":1,\"reviewStatus\":1,\"category\":\"文章\",\"hiddenContent\":true}";
		String url = "http://api.code-nav.cn/api/post/list/page/vo";
		String result = HttpRequest.post(url)
				.body(json)
				.execute().body();

		// json转对象
		Map<String, Object> map = JSONUtil.toBean(result, Map.class);
		// 获取抓取网站的数据
		JSONObject data = (JSONObject) map.get("data");
		if (data == null) return;
		JSONArray records = (JSONArray) data.get("records");
		if (records == null) return;
		// 将获取到的数据进行转化,转化成自己的Post对象
		List<Post> postList = new ArrayList<>();
		for (Object record : records) {
			if (record != null) {
				JSONObject temp = (JSONObject) record;
				Post post = new Post();
				post.setTitle(temp.getStr("title"));
				post.setContent(temp.getStr("content"));
				// tags从数组转化成字符串类型
				JSONArray tags = (JSONArray) temp.get("tags");
				List<String> list = tags.toList(String.class);
				post.setTags(JSONUtil.toJsonStr(list));
				post.setThumbNum(temp.getInt("thumbNum"));
				post.setFavourNum(temp.getInt("favourNum"));
				post.setUserId(1L);
				postList.add(post);
			}
		}
		System.out.println(postList);
		boolean b = postService.saveBatch(postList);
		// 断言
		Assertions.assertTrue(b);
	}
}
