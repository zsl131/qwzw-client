package com.zslin.service;

import com.zslin.basic.repository.BaseRepository;
import com.zslin.model.OrderBagDetail;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IOrderBagDetailService extends BaseRepository<OrderBagDetail, Integer>, JpaSpecificationExecutor<OrderBagDetail> {

    List<OrderBagDetail> findByOrderNo(String orderNo);

    @Modifying
    @Transactional
    void deleteByOrderNo(String orderNo);

    OrderBagDetail findByOrderNoAndFoodId(String orderNo, Integer foodId);

    /** 获取总优惠金额 */
    @Query("SELECT SUM(o.money) FROM OrderBagDetail o WHERE o.orderNo=?1")
    Float sumDiscountMoney(String orderNo);
}
