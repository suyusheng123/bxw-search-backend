package com.bxw.springbootinit.registry.datasource;

import com.bxw.springbootinit.adapter.datasource.PictureDataSourceAdapter;
import com.bxw.springbootinit.adapter.datasource.PostDataSourceAdapter;
import com.bxw.springbootinit.adapter.datasource.UserDataSourceAdapter;
import com.bxw.springbootinit.datasource.DataSource;
import com.bxw.springbootinit.model.enums.SearchEnum;
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
	private PostDataSourceAdapter postDataSourceAdapter;

	@Resource
	private UserDataSourceAdapter userDataSourceAdapter;

	@Resource
	private PictureDataSourceAdapter pictureDataSourceAdapter;

	private final Map<SearchEnum,DataSource<?>> dataSources = new HashMap<>();

	@PostConstruct
	public void doInit() {
		dataSources.put(SearchEnum.POST,postDataSourceAdapter);
		dataSources.put(SearchEnum.USER,userDataSourceAdapter);
		dataSources.put(SearchEnum.PICTURE,pictureDataSourceAdapter);
	}
	public DataSource<?> getDataSource(SearchEnum searchEnum) {
		return dataSources.get(searchEnum);
	}
}
