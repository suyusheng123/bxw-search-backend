package com.bxw.springbootinit.adapter.datasource;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxw.springbootinit.datasource.DataSource;
import com.bxw.springbootinit.model.dto.user.UserQueryRequest;
import com.bxw.springbootinit.model.entity.Picture;
import com.bxw.springbootinit.model.vo.UserVO;
import com.bxw.springbootinit.service.PictureService;
import com.bxw.springbootinit.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * ClassName:DataSourceAdapter
 * Package:com.bxw.springbootinit.adapter.datasource
 * Description:
 * 图片数据源适配器
 * @Author 卜翔威
 * @Create 2024/8/14 10:37
 * @Version 1.0
 */

@Component
public class PictureDataSourceAdapter implements DataSource<Picture> {

	@Resource
	private PictureService pictureService;

	@Override
	public Page<Picture> doSearch(String searchText, long current, long pageSize) {
		return pictureService.searchPicture(searchText, (int) current, (int) pageSize);
	}
}
