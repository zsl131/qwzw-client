package com.zslin.service;

import com.zslin.basic.repository.BaseRepository;
import com.zslin.model.FoodOrder;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Column;
import java.util.List;

public interface IFoodOrderService extends BaseRepository<FoodOrder, Integer>, JpaSpecificationExecutor<FoodOrder> {

    FoodOrder findByNo(String no);

    /** 获取status状态下的餐桌ID */
    @Query("SELECT f.tableId FROM FoodOrder f WHERE f.status=?1")
    List<Integer> findTableIds(String status);

    @Query("UPDATE FoodOrder f SET f.type=?1 WHERE f.no=?2")
    @Modifying
    @Transactional
    void updateTypeByOrderNo(String type, String orderNo);

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

    /**  */
    @Query("SELECT COUNT(f.id) FROM FoodOrder f WHERE f.createDay=?1 AND f.status!='-1'")
    Integer deskCount(String createDay);

    @Query("SELECT COUNT(f.id) FROM FoodOrder f WHERE f.createDay=?1 AND f.status!=?2")
    Integer deskCount(String createDay, String status);

    @Query("SELECT SUM(f.amount) FROM FoodOrder f WHERE f.createDay=?1  AND f.status!='-1'")
    Integer peopleCount(String createDay);

    /** 抹零的数量 */
    @Query("SELECT COUNT(f.id) FROM FoodOrder f WHERE f.createDay=?1 AND f.removeDot='1'")
    Integer dotCount(String createDay);

    /** 抹零金额 */
    @Query("SELECT SUM(f.dotMoney) FROM FoodOrder f WHERE f.createDay=?1 AND f.dotMoney>0")
    Float dotMoney(String createDay);

    /** 未结束的订单数 */
    @Query("SELECT COUNT(f.id) FROM FoodOrder f WHERE f.createDay=?1 AND f.status='0'")
    Integer unEndCount(String createDay);

    /** 总金额，含抹零金额 */
    @Query("SELECT SUM(f.totalMoney) FROM FoodOrder f WHERE f.createDay=?1 AND f.status!='-1'")
    Float totalMoney(String day);

    /** 不同付款方式的金额 */
    @Query("SELECT SUM(f.totalMoney2) FROM FoodOrder f WHERE f.createDay=?1 AND f.status!='-1' AND f.payType=?2 ")
    Float payMoney(String day, String payType);

    @Query("SELECT SUM(f.discountMoney) FROM FoodOrder f WHERE f.createDay=?1 AND f.status!='-1'")
    Float discountMoney(String day);
}
