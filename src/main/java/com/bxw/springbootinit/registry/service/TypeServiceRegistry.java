package com.bxw.springbootinit.registry.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bxw.springbootinit.adapter.service.ServiceAdapter;
import com.bxw.springbootinit.adapter.service.impl.ArticleServiceAdapter;
import com.bxw.springbootinit.adapter.service.impl.PictureServiceAdapter;
import com.bxw.springbootinit.adapter.service.impl.VideoServiceAdapter;
import com.bxw.springbootinit.model.enums.SearchTypeEnum;
import com.bxw.springbootinit.service.ArticleService;
import com.bxw.springbootinit.service.PictureService;
import com.bxw.springbootinit.service.VideoService;
import com.google.protobuf.Service;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ClassName: TypeServiceRegistry
 * Description:
 * 数据服务注册器
 * @Author 坤坤学🐸
 * @Create 2024/8/22 10:49
 * @Version 1.0
 */
@Component
public class TypeServiceRegistry {
	@Resource
	private ArticleServiceAdapter articleService;

	@Resource
	private PictureServiceAdapter pictureService;


	@Resource
	private VideoServiceAdapter videoService;

	private final Map<String, ServiceAdapter> serviceMap = new HashMap<>();

	@PostConstruct
	public void doInit() {
		serviceMap.put("article",articleService);
		serviceMap.put("video",videoService);
		serviceMap.put("picture",pictureService);
	}
	public ServiceAdapter getServiceByType(String type) {
		  return serviceMap.get(type);
	}

	public ServiceAdapter getServiceByNum(Integer type){
		String value = Objects.requireNonNull(SearchTypeEnum.getEnumByType(type)).getValue();
		return serviceMap.get(value);
	}
}
