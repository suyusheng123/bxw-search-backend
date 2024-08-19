package com.bxw.springbootinit.esdao;

import com.bxw.springbootinit.model.dto.post.PostEsDTO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * ClassName:PostEsDao
 * Package:com.bxw.springbootinit.esdao
 * Description:
 * es测试类
 * @Author 卜翔威
 * @Create 2024/8/16 10:12
 * @Version 1.0
 */
@SpringBootTest
public class PostEsDaoTest {

	@Resource
	private PostEsDao postEsDao;

	@Test
	void testAdd() {
		PostEsDTO postEsDTO = new PostEsDTO();
		postEsDTO.setId(1L);
		postEsDTO.setTitle("鱼皮是狗");
		postEsDTO.setContent("鱼皮的知识星球：https://yupi.icu，直播带大家做项目");
		List<String> tags = new ArrayList<>();
		tags.add("java");
		tags.add("python");
		postEsDTO.setTags(tags);
		postEsDTO.setUserId(1L);
		postEsDTO.setCreateTime(new Date());
		postEsDTO.setUpdateTime(new Date());
		postEsDTO.setIsDelete(0);
		postEsDao.save(postEsDTO);
		System.out.println(postEsDTO.getId());
	}
	@Test
	void testSelect() {
		System.out.println(postEsDao.count());
		Page<PostEsDTO> PostPage = postEsDao.findAll(
				PageRequest.of(0, 5, Sort.by("createTime"))); //分页查询
		List<PostEsDTO> postList = PostPage.getContent();
		System.out.println(postList);
		Optional<PostEsDTO> byId = postEsDao.findById(1L); // 根据 Id 查询
		System.out.println(byId);
	}
}
