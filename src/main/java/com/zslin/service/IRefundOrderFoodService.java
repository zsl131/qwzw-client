package com.zslin.service;

import com.zslin.basic.repository.BaseRepository;
import com.zslin.model.RefundOrderFood;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface IRefundOrderFoodService extends BaseRepository<RefundOrderFood, Integer>, JpaSpecificationExecutor<RefundOrderFood> {

    RefundOrderFood findByOrderNoAndFoodId(String orderNo, Integer foodId);

    List<RefundOrderFood> findByOrderNo(String orderNo);

    List<RefundOrderFood> findByCreateDay(String createDay);
}
