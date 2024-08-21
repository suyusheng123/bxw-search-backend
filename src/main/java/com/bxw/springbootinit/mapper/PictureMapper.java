package com.bxw.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bxw.springbootinit.model.entity.Article;
import com.bxw.springbootinit.model.entity.Picture;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ClassName: ArticleMapper
 * Description:
 * 图片数据库类
 * @Author 坤坤学🐸
 * @Create 2024/8/21 9:03
 * @Version 1.0
 */
@Mapper
public interface PictureMapper extends BaseMapper<Picture> {
	/**
	 * 批量保存搜索数据（如果标题(title)重复则会更新内容(content)）
	 *
	 * @param pictures 数据列表
	 * @return boolean
	 */
	boolean savePicture(@Param("searchList") List<Picture> pictures);
}
