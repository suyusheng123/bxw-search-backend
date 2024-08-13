package com.bxw.springbootinit.model.vo;

import com.bxw.springbootinit.model.entity.Picture;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 聚合返回类
 *
 */
@Data
public class SearchVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<PostVO> postList;

    private List<UserVO> userList;

    private List<Picture> pictureList;

    // 记录总数
    private long total;
}
