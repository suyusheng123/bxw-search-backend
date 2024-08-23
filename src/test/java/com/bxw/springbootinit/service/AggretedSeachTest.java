package com.bxw.springbootinit.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONUtil;
import com.bxw.springbootinit.model.dto.query.AggregatedSearchEsDTO;
import com.bxw.springbootinit.model.dto.query.SearchQueryEsRequest;
import com.bxw.springbootinit.model.entity.AggregatedSearch;
import com.bxw.springbootinit.model.entity.Picture;
import com.bxw.springbootinit.model.entity.Video;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ClassName: AggretedSeachTest
 * Description:
 * 测试类
 *
 * @Author 坤坤学🐸
 * @Create 2024/8/19 14:11
 * @Version 1.0
 */

@SpringBootTest
@Slf4j
public class AggretedSeachTest {
	@Resource
	private ElasticsearchRestTemplate elasticsearchRestTemplate;

	@Resource
	private AggregatedSearchService aggregatedSearchService;

	@Test
	public void testSearchAll() {
		NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().build();
		SearchHits<AggregatedSearchEsDTO> search = elasticsearchRestTemplate.search(searchQuery, AggregatedSearchEsDTO.class);
		log.info("search: {}", search.getSearchHits());
	}

	@Test
	public void testFetchVideo() throws IOException {
		String url = "https://cn.bing.com/videos/search?q=世界旅游胜地&first=1";
		Document doc = Jsoup.connect(url).get();
		Elements elements = doc.select(".mc_fgvc_u");

		List<Video> videoList = new ArrayList<>();
		for (Element e : elements) {
			Video video = new Video();
			// 获取图片地址
			String m = e.select(".vrhdata").get(0).attr("vrhm");
			Map<String, Object> map = JSONUtil.toBean(m, Map.class);

			String title = (String) map.get("vt");
			String videoUrl = (String) map.get("pgurl");

			Elements select = e.select(".meta_pd_content");


			String time = e.select(".vtbc_rc").get(0).child(0).child(0).text();

			String cover = e.select(".cico").get(0).child(0).attr("src");
			String s = cover + "&dpr=1.3";
			video.setTitle(title);
			video.setUrl(videoUrl);
			video.setCover(s);
			videoList.add(video);
		}
		System.out.println(videoList);
		// json转对象
//		Map<String, Object> map = JSONUtil.toBean(result, Map.class);
	}

	@Test
	public void testFetchArticles(){
	    String text = "猴子";
		long current = 1L;
		aggregatedSearchService.fetchArticles(text,current);
	}

	@Test
	public void testFetchPictures(){
		String text = "小黑子";
		long first = 40;
		aggregatedSearchService.fetchPictures(text,40);
	}

	@Test
	public void testFetchVideos(){
		String text = "ssm";
		long first = 2;
		aggregatedSearchService.fetchVideos(text,first);
	}

	@Test
	public void testAggregatedSearch(){
	    SearchQueryEsRequest searchQueryEsRequest = new SearchQueryEsRequest();
	    searchQueryEsRequest.setSearchText("elasticsearch");
	    searchQueryEsRequest.setType(1);
	    searchQueryEsRequest.setSortField("updateTime");
	    searchQueryEsRequest.setSortOrder("desc");

		aggregatedSearchService.aggregatedSearchEs(searchQueryEsRequest);

	}

}
