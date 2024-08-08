package com.bxw.springbootinit.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bxw.springbootinit.common.ErrorCode;
import com.bxw.springbootinit.exception.BusinessException;
import com.bxw.springbootinit.model.entity.Picture;
import com.bxw.springbootinit.service.PictureService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ClassName:PictureServiceImpl
 * Package:com.bxw.springbootinit.service.impl
 * Description:
 * 图片服务实现类
 * @Author 卜翔威
 * @Create 2024/8/7 17:08
 * @Version 1.0
 */

public class PictureServiceImpl implements PictureService {

	@Override
	public Page<Picture> searchPicture(String searchText, long page, long pageSize) {
		long currentSize  = (page - 1) * pageSize;
		String url = String.format("https://cn.bing.com/images/search?q=%sfirst=%s",searchText,currentSize);
		Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR,ErrorCode.SYSTEM_ERROR.getMessage());
		}
		Elements select = doc.select(".iuscp.isv");
		List<Picture> pictures = new ArrayList<>();
		for (Element e : select) {
			Picture pic = new Picture();
			// 获取图片地址
			String m  = e.select(".iusc").get(0).attr("m");
			Map<String,Object> map = JSONUtil.toBean(m,Map.class);
			String murl = (String) map.get("murl");
			pic.setUrl(murl);

			// 获取标题
			String title  = e.select(".inflnk").get(0).attr("aria-label");
			pic.setTitle(title);
			pictures.add(pic);
			if(pictures.size() > 20) break;
		}
		Page<Picture> picturePage = new Page<>(page,pageSize);
		picturePage.setRecords(pictures);
		return picturePage;
	}
}
