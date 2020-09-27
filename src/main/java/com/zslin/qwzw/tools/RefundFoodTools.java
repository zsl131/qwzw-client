package com.zslin.qwzw.tools;

import com.zslin.basic.tools.NormalTools;
import com.zslin.model.FoodOrderDetail;
import com.zslin.model.RefundOrderFood;
import com.zslin.service.IFoodOrderDetailService;
import com.zslin.service.IFoodOrderService;
import com.zslin.service.IRefundOrderFoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RefundFoodTools {

    @Autowired
    private IFoodOrderDetailService foodOrderDetailService;

    @Autowired
    private IFoodOrderService foodOrderService;

    @Autowired
    private IRefundOrderFoodService refundOrderFoodService;

    /**
     * 退菜处理
     * @param orderNo 订单编号
     * @param foodId 菜品ID
     * @param refundAmount 退菜数量
     */
    public void refund(String orderNo, Integer foodId, Integer refundAmount) {
        List<FoodOrderDetail> list = foodOrderDetailService.findByOrderNoAndFoodId(orderNo, foodId);
        int surplus = refundAmount;
        for(FoodOrderDetail fod : list) {
            if(surplus<=0) {break;}
            Integer amount = fod.getAmount();
            if(amount>surplus) { //如果数量够退菜数量
                fod.setAmount(amount-surplus);
                foodOrderDetailService.save(fod);
                addRefundRecord(fod, surplus);
            } else if(amount<=surplus) {
                foodOrderDetailService.delete(fod);
                addRefundRecord(fod, amount);
            } /*else if(amount<surplus) {
                foodOrderDetailService.delete(fod);
                addRefundRecord(fod, amount);
            }*/
            surplus -= amount;
        }
    }

    private void addRefundRecord(FoodOrderDetail fod, Integer amount) {
        RefundOrderFood rof = refundOrderFoodService.findByOrderNoAndFoodId(fod.getOrderNo(), fod.getFoodId());
        if(rof==null) {
            rof = new RefundOrderFood();
            rof.setAmount(amount);
            rof.setBatchNo(fod.getBatchNo());
            rof.setCateId(fod.getCateId());
            rof.setCateName(fod.getCateName());
            rof.setCreateDay(NormalTools.curDate("yyyy-MM-dd"));
            rof.setCreateLong(System.currentTimeMillis());
            rof.setCreateTime(NormalTools.curDate());
            rof.setFoodId(fod.getFoodId());
            rof.setFoodName(fod.getFoodName());
            rof.setFoodNameLetter(fod.getFoodNameLetter());
            rof.setOrderId(fod.getOrderId());
            rof.setOrderNo(fod.getOrderNo());
            rof.setPrice(fod.getPrice());
            rof.setPrintCount(fod.getPrintCount());
            rof.setPrintFlag(fod.getPrintFlag());
            rof.setPrintStatus(fod.getPrintStatus());
            rof.setSubtotal(fod.getSubtotal());
            refundOrderFoodService.save(rof);
        } else {
            rof.setAmount(rof.getAmount()+amount);
            refundOrderFoodService.save(rof);
        }
    }
}
