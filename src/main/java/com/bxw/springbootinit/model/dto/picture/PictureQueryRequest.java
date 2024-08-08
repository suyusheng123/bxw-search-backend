package com.bxw.springbootinit.model.dto.picture;

import com.bxw.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 查询请求
 *
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PictureQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String searchText;
}
