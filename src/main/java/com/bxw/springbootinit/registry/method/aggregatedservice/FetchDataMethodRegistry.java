package com.bxw.springbootinit.registry.method.aggregatedservice;

import com.bxw.springbootinit.adapter.method.aggregatedservice.FetchDataMethodAdapter;
import com.bxw.springbootinit.adapter.method.aggregatedservice.impl.FetchArticlesMethodAdapterImpl;
import com.bxw.springbootinit.adapter.method.aggregatedservice.impl.FetchPicturesMethodAdapterImpl;
import com.bxw.springbootinit.adapter.method.aggregatedservice.impl.FetchVideosMethodAdapterImpl;
import com.bxw.springbootinit.adapter.service.ServiceAdapter;
import com.bxw.springbootinit.adapter.service.impl.ArticleServiceAdapter;
import com.bxw.springbootinit.adapter.service.impl.PictureServiceAdapter;
import com.bxw.springbootinit.adapter.service.impl.VideoServiceAdapter;
import com.bxw.springbootinit.model.enums.SearchTypeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ClassName: aggregatedservice
 * Description:
 *
 * @Author 坤坤学🐸
 * @Create 2024/8/25 17:01
 * @Version 1.0
 */
@Component
public class FetchDataMethodRegistry{
	@Resource
	private FetchArticlesMethodAdapterImpl fetchArticlesMethodAdapter;

	@Resource
	private FetchPicturesMethodAdapterImpl fetchPicturesMethodAdapter;


	@Resource
	private FetchVideosMethodAdapterImpl fetchVideosMethodAdapter;

	private final Map<String, FetchDataMethodAdapter> methodMap = new HashMap<>();

	@PostConstruct
	public void doInit() {
		methodMap.put("article",fetchArticlesMethodAdapter);
		methodMap.put("video",fetchVideosMethodAdapter);
		methodMap.put("picture",fetchPicturesMethodAdapter);
	}
	public FetchDataMethodAdapter getMethodByType(String type) {
		return methodMap.get(type);
	}
}
