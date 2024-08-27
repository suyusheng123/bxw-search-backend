package com.bxw.springbootinit.model.dto.mq;

import com.bxw.springbootinit.model.entity.AggregatedSearch;
import lombok.Data;
import org.springframework.data.redis.core.ListOperations;

import java.io.Serializable;
import java.util.List;

/**
 * ClassName: InsertBatchDTO
 * Description:
 * mq批量插入消息
 * @Author 坤坤学🐸
 * @Create 2024/8/23 15:05
 * @Version 1.0
 */
@Data
public class InsertBatchDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	private List<AggregatedSearch> searches;

	private List<?> dataList;

	private String type;

	/**
	 * 消息的id,用来判断是否已经被消费了
	 */
	private String messageKey;
}
