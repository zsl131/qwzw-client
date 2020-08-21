package com.zslin.web.controller;

import com.zslin.model.FoodOrderDetail;
import com.zslin.service.IFoodOrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "web/print")
public class WebPrintController {

    @Autowired
    private IFoodOrderDetailService foodOrderDetailService;

    /**
     * 打印
     * @param detailId 订单详情ID
     * @param batchNo 批次号
     * @param orderNo 订单编号
     * @param isBatch 是否批次打印
     * @return
     */
    @PostMapping(value = "orders")
    public String orders(Integer detailId, String batchNo, String orderNo, String isBatch) {
        return null;
    }

    /**
     * 生成需要打印的数据信息
     * @param detailId
     * @param batchNo
     * @param orderNo
     * @param isBatch
     * @return
     */
    private List<FoodOrderDetail> buildList(Integer detailId, String batchNo, String orderNo, String isBatch) {
        List<FoodOrderDetail> res ;
        if(isBatch!=null && "1".equals(isBatch)) { //如果是批次打印
            res = foodOrderDetailService.findByOrderNoAndBatchNo(orderNo, batchNo);
        } else {
            res = new ArrayList<>();
            FoodOrderDetail fod = foodOrderDetailService.findOne(detailId);
            res.add(fod);
        }
        return res;
    }
}
