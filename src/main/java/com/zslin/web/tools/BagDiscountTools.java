package com.zslin.web.tools;

import com.zslin.basic.repository.SimpleSortBuilder;
import com.zslin.model.FoodOrder;
import com.zslin.model.FoodOrderDetail;
import com.zslin.model.OrderBagDetail;
import com.zslin.qwzw.model.FoodBag;
import com.zslin.qwzw.model.FoodBagDetail;
import com.zslin.qwzw.service.IFoodBagDetailService;
import com.zslin.qwzw.service.IFoodBagService;
import com.zslin.service.IFoodOrderDetailService;
import com.zslin.service.IFoodOrderService;
import com.zslin.service.IOrderBagDetailService;
import com.zslin.web.dto.BagDiscountDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 套餐抵价工具类
 */
@Component
public class BagDiscountTools {

    @Autowired
    private IFoodOrderDetailService foodOrderDetailService;

    @Autowired
    private IFoodOrderService foodOrderService;

    @Autowired
    private IFoodBagService foodBagService;

    @Autowired
    private IFoodBagDetailService foodBagDetailService;

    @Autowired
    private IOrderBagDetailService orderBagDetailService;

    private static final String SEP = "-";

    public BagDiscountDto onDiscount(String orderNo, Integer bagId) {
        //FoodOrder order = foodOrderService.findByNo(orderNo);
        //FoodBag bag = foodBagService.findOne(bagId);

        //1. 先删除已经设置抵价详情
        orderBagDetailService.deleteByOrderNo(orderNo);

        List<FoodOrderDetail> orderDetailList = foodOrderDetailService.findByOrderNo(orderNo, SimpleSortBuilder.generateSort("id"));
        List<FoodBagDetail> bagDetailList = foodBagDetailService.findByBagId(bagId);

        for(FoodBagDetail fbd : bagDetailList) {
            Integer amount = fbd.getAmount();
            String ids = fbd.getFoodIds();
            //Integer [] id_array = buildIds(ids);
            for(FoodOrderDetail fod : orderDetailList) {
                if(amount>0 && needDiscount(fbd.getFoodIds(), fod.getFoodId())) { //如果需要抵扣
                    Integer minusAmount = amount>fod.getAmount()?fod.getAmount():amount;
                    saveDetail(orderNo, fod, fbd, minusAmount);
                    amount -= minusAmount;
                    orderDetailList = rebuildList(orderDetailList, fod.getId(), minusAmount);
                }
            }
        }

        List<OrderBagDetail> detailList = orderBagDetailService.findByOrderNo(orderNo);
        Float discountMoney = 0f;
        for(OrderBagDetail obd : detailList) {discountMoney += obd.getMoney();}
        BagDiscountDto dto = new BagDiscountDto();
        dto.setDetailList(detailList);
        dto.setDiscountMoney(discountMoney);
        return dto;
    }

    private List<FoodOrderDetail> rebuildList(List<FoodOrderDetail> detailList, Integer detailId, Integer amount) {
        List<FoodOrderDetail> result = new ArrayList<>();
        for(FoodOrderDetail fod : detailList) {
            if(fod.getId().equals(detailId)) {
                if(fod.getAmount()>amount) {fod.setAmount(fod.getAmount()-amount);result.add(fod);}
            } else {result.add(fod);}
        }
        return result;
    }

    /*private Integer [] buildIds(String ids) {
        String [] array = ids.split(SEP);
        List<Integer> idList = new ArrayList<>();
        for(String str : array) {
            if(str!=null && !"".equals(str.trim())) {
                try {
                    idList.add(Integer.parseInt(str));
                } catch (Exception e) {
                }
            }
        }
        return idList.toArray(new Integer[idList.size()]);
    }*/

    private void saveDetail(String orderNo, FoodOrderDetail fod, FoodBagDetail fbd, Integer amount) {
        Integer foodId = fod.getFoodId();
        OrderBagDetail obd = orderBagDetailService.findByOrderNoAndFoodId(orderNo, foodId);
        if(obd==null) {
            obd = new OrderBagDetail();
            obd.setAmount(amount);
            obd.setBagId(fbd.getBagId());
            obd.setFoodId(foodId);
            obd.setFoodName(fod.getFoodName());
            obd.setOrderNo(orderNo);
            obd.setPrice(fod.getPrice());
            obd.setMoney(fod.getPrice()*amount);
        } else {
            obd.setAmount(obd.getAmount()+amount);
            obd.setMoney(obd.getPrice()*obd.getAmount());
        }
        orderBagDetailService.save(obd);
    }

    /** 判断是否需要进行抵扣 */
    private boolean needDiscount(String ids, Integer foodId) {
        String [] array = ids.split(SEP);
        boolean res = false;
        for(String str : array) {
            if(str!=null && !"".equals(str) && str.equals(foodId+"")) {res = true; break; }
        }
        return res;
    }
}
