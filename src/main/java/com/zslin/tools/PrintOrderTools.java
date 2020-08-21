package com.zslin.tools;

import com.zslin.model.FoodOrderDetail;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 订单打印工具类
 */
@Component
public class PrintOrderTools {

    /**
     * 处理打印
     * @param detailList 菜品详情列表
     */
    public void handle(List<FoodOrderDetail> detailList) {

    }

    /** 生成字符串 */
    public String buildDetailsStr() {
        return null;
    }
}
