package com.bxw.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bxw.springbootinit.model.entity.Picture;
import com.bxw.springbootinit.model.entity.Video;
import com.bxw.springbootinit.model.vo.VideoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * ClassName: ArticleMapper
 * Description:
 * 视频数据库类
 *
 * @Author 坤坤学🐸
 * @Create 2024/8/21 9:03
 * @Version 1.0
 */
@Mapper
public interface VideoMapper extends BaseMapper<Video> {
	/**
	 * 批量保存搜索数据（如果标题(title)重复则会更新内容(content)）
	 *
	 * @param videos 数据列表
	 * @return boolean
	 */
	boolean saveVideo(@Param("searchList") List<Video> videos);

	/**
	 * 根据es提供的id进行查询
	 */
	@Select({
			"<script>",
			"SELECT id,url,cover,title,updateTime FROM video WHERE id IN",
			"<foreach item='item' index='index' collection='id' open='(' separator=',' close=')'>",
			"#{item}",
			"</foreach>",
			"ORDER BY id DESC",
			"</script>"
	})
	List<VideoVO> searchListFromEs(@Param("id") List<Long> id);

	/**
	 * 根据标题查询
	 */
	@Select({
			"<script>",
			"SELECT id,url,cover,title,date_format(updateTime,'%Y-%m-%d %H:%i:%s') as updateTime FROM video",
			"<where>",
			"<if test=\"title != null and title != ''\">",
			"AND title LIKE CONCAT('%', #{title}, '%')",
			"</if>",
			"</where>",
			"ORDER BY id DESC",
			"LIMIT 10 OFFSET #{current}",
			"</script>"
	})
	List<VideoVO> searchList(@Param("title") String title,@Param("current") Long offset);

	/**
	 * 根据标题查询记录总数
	 */
	@Select({
			"<script>",
			"SELECT count(*) FROM video",
			"<where>",
			"<if test=\"title != null and title != ''\">",
			"AND title LIKE CONCAT('%', #{title}, '%')",
			"</if>",
			"</where>",
			"</script>"
	})
	Long searchListCount(@Param("title") String title);
}
