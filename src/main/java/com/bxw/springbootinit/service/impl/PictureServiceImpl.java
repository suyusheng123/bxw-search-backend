package com.bxw.springbootinit.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxw.springbootinit.MainApplication;
import com.bxw.springbootinit.common.ErrorCode;
import com.bxw.springbootinit.exception.BusinessException;
import com.bxw.springbootinit.model.entity.Picture;
import com.bxw.springbootinit.service.PictureService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * ClassName:PictureServiceImpl
 * Package:com.bxw.springbootinit.service.impl
 * Description:
 * 图片服务实现类
 *
 * @Author 卜翔威
 * @Create 2024/8/7 17:08
 * @Version 1.0
 */

@Service
@Slf4j
public class PictureServiceImpl implements PictureService {

	@Override
	public Page<Picture> searchPicture(String searchText, long page, long pageSize) {
		long currentSize = (page - 1) * pageSize + 1;
		String url = String.format("https://cn.bing.com/images/search?first=%s", currentSize);
		if (searchText != null && !searchText.trim().isEmpty()) {
			url = String.format("https://cn.bing.com/images/search?q=%sfirst=%s", searchText, currentSize);
		}
		Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			log.info("异常为{}", e);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, ErrorCode.SYSTEM_ERROR.getMessage());
		}
		Elements select = doc.select(".iuscp.isv");
		List<Picture> pictures = new ArrayList<>();
		for (Element e : select) {
			if (pictures.size() >= pageSize) break;
			Picture pic = new Picture();
			String m = e.select(".iusc").get(0).attr("m");
			Map<String, Object> map = JSONUtil.toBean(m, Map.class);
			String turl = (String) map.get("turl");
			String purl = (String) map.get("purl");
			// 获取图片地址
			pic.setTurl(turl);
			// 获取链接地址
			pic.setPurl(purl);

			// 获取标题
			String title = e.select(".inflnk").get(0).attr("aria-label");
			pic.setTitle(title);
			pictures.add(pic);
		}
		log.info("图片总共{}张", pictures.size());
		Page<Picture> picturePage = new Page<>(page, pageSize);
		picturePage.setRecords(pictures);

		return picturePage;
	}
}
