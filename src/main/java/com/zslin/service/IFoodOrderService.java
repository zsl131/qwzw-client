package com.zslin.service;

import com.zslin.basic.repository.BaseRepository;
import com.zslin.model.FoodOrder;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface IFoodOrderService extends BaseRepository<FoodOrder, Integer>, JpaSpecificationExecutor<FoodOrder> {

    FoodOrder findByNo(String no);

    /** 获取status状态下的餐桌ID */
    @Query("SELECT f.tableId FROM FoodOrder f WHERE f.status=?1")
    List<Integer> findTableIds(String status);

    /** 获取正在就餐中的订单 */
    @Query("FROM FoodOrder f WHERE f.status='0'")
    List<FoodOrder> findByMealing();

    /** 获取在就餐的订单 */
    FoodOrder findByTableIdAndStatus(Integer tableId, String status);

    /** 只有用餐中的订单才能修改 */
    @Query("UPDATE FoodOrder f SET f.amount=?1 WHERE f.no=?2 AND f.status='0'")
    @Transactional
    @Modifying
    Integer updatePeopleAmount(Integer amount, String orderNo);

    /** 修改总份数 */
    @Query("UPDATE FoodOrder f SET f.unitCount=?1 WHERE f.no=?2")
    @Modifying
    @Transactional
    void updateUnitCount(Integer unitCount, String orderNo);
}
