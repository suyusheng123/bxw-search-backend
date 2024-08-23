package com.bxw.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bxw.springbootinit.model.entity.Article;
import com.bxw.springbootinit.model.entity.Picture;
import com.bxw.springbootinit.model.vo.PictureVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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

	/**
	 * 根据es提供的id进行查询
	 */
	@Select({
			"<script>",
			"SELECT id,url FROM picture WHERE id IN",
			"<foreach item='item' index='index' collection='id' open='(' separator=',' close=')'>",
			"#{item}",
			"</foreach>",
			"</script>"
	})
	List<PictureVO> searchListFromEs(@Param("id") List<Long> id);

	/**
	 * 根据标题查询
	 */
	@Select({
			"<script>",
			"SELECT id,url,title FROM picture",
			"<where>",
			"<if test=\"title != null and title != ''\">",
			"AND title LIKE CONCAT('%', #{title}, '%')",
			"</if>",
			"</where>",
			"ORDER BY updateTime DESC",
			"LIMIT 15",
			"</script>"
	})
	List<PictureVO> searchList(@Param("title") String title);
}
