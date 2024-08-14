package com.bxw.springbootinit.manager;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxw.springbootinit.adapter.datasource.PictureDataSourceAdapter;
import com.bxw.springbootinit.adapter.datasource.PostDataSourceAdapter;
import com.bxw.springbootinit.adapter.datasource.UserDataSourceAdapter;
import com.bxw.springbootinit.common.BaseResponse;
import com.bxw.springbootinit.common.ErrorCode;
import com.bxw.springbootinit.common.ResultUtils;
import com.bxw.springbootinit.datasource.DataSource;
import com.bxw.springbootinit.exception.ThrowUtils;
import com.bxw.springbootinit.model.dto.post.PostQueryRequest;
import com.bxw.springbootinit.model.dto.query.QueryRequest;
import com.bxw.springbootinit.model.dto.user.UserQueryRequest;
import com.bxw.springbootinit.model.entity.Picture;
import com.bxw.springbootinit.model.entity.Post;
import com.bxw.springbootinit.model.enums.SearchEnum;
import com.bxw.springbootinit.model.vo.PostVO;
import com.bxw.springbootinit.model.vo.SearchVO;
import com.bxw.springbootinit.model.vo.UserVO;
import com.bxw.springbootinit.registry.datasource.DataSourceRegistry;
import com.bxw.springbootinit.service.PictureService;
import com.bxw.springbootinit.service.PostService;
import com.bxw.springbootinit.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ClassName:SearchFacade
 * Package:com.bxw.springbootinit.manager
 * Description:
 * 搜索门面模式
 * @Author 卜翔威
 * @Create 2024/8/14 8:43
 * @Version 1.0
 */

@Component
public class SearchFacade {

	// 注册器模式
	@Resource
	private DataSourceRegistry dataSourceRegistry;

	public SearchVO searchAll(@RequestBody QueryRequest queryRequest, HttpServletRequest request) {
		// 门面模式
		String searchText = queryRequest.getSearchText();
		long current = queryRequest.getCurrent();
		long pageSize = queryRequest.getPageSize();
		if (searchText == null) {
			searchText = "";
		}
		// 如果前端传入的type为null或者是空字符串,那么就给他一个默认值
		String type = queryRequest.getType();
		if (type == null || type.isEmpty()) {
			type = SearchEnum.PICTURE.getValue();
		}
		SearchEnum typeEnum = SearchEnum.getEnumByValue(type);
		ThrowUtils.throwIf(Objects.isNull(typeEnum), ErrorCode.PARAMS_ERROR);
		SearchVO searchVO = new SearchVO();
		// 根据不同的枚举类型来进行相应的查询,注册器模式
		DataSource<?> dataSource = dataSourceRegistry.getDataSource(typeEnum);
		Page<?> page = dataSource.doSearch(searchText, current, pageSize);
		searchVO.setDataList(page.getRecords());
		searchVO.setTotal((int)page.getTotal());
		return searchVO;
	}
}
