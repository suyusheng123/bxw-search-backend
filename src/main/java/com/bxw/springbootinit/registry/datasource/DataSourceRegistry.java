package com.bxw.springbootinit.registry.datasource;

import com.bxw.springbootinit.adapter.datasource.impl.PictureDataSourceAdapter;
import com.bxw.springbootinit.adapter.datasource.impl.ArticleDataSourceAdapter;
import com.bxw.springbootinit.adapter.datasource.DataSource;
import com.bxw.springbootinit.adapter.datasource.impl.VideoDataSourceAdapter;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassName:DataSourceRegistry
 * Package:com.bxw.springbootinit.registry.datasource
 * Description:
 * 数据源注册器模式
 * @Author 卜翔威
 * @Create 2024/8/14 21:31
 * @Version 1.0
 */
@Component
public class DataSourceRegistry {

	@Resource
	private ArticleDataSourceAdapter articleDataSourceAdapter;

	@Resource
	private PictureDataSourceAdapter pictureDataSourceAdapter;

	@Resource
	private VideoDataSourceAdapter videoDataSourceAdapter;

	private final Map<String,DataSource> dataSources = new HashMap<>();

	@PostConstruct
	public void doInit() {
		dataSources.put("article",articleDataSourceAdapter);
		dataSources.put("picture",pictureDataSourceAdapter);
		dataSources.put("video",videoDataSourceAdapter);
	}
	public DataSource getDataSourceByType(String type) {
		return dataSources.get(type);
	}
}
