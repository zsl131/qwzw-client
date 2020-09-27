package com.zslin.service;

import com.zslin.basic.repository.BaseRepository;
import com.zslin.model.FoodOrderDetail;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IFoodOrderDetailService extends BaseRepository<FoodOrderDetail, Integer>, JpaSpecificationExecutor<FoodOrderDetail> {

    List<FoodOrderDetail> findByOrderId(Integer orderId);

    List<FoodOrderDetail> findByOrderNo(String orderNo, Sort sort);

    List<FoodOrderDetail> findByOrderNoAndFoodId(String orderNo, Integer foodId);

    List<FoodOrderDetail> findByOrderNoAndBatchNo(String orderNo, String batchNo);

    @Query("FROM FoodOrderDetail f WHERE f.createDay=?1 ")
    List<FoodOrderDetail> findByCreateDay(String createDay, Sort sort);
}
