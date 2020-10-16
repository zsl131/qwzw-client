package com.zslin.web.dto;

import com.zslin.model.OrderBagDetail;
import lombok.Data;

import java.util.List;

/**
 * 券码抵价DTO
 */
@Data
public class BagDiscountDto {

    private Float discountMoney = 0f;

    private List<OrderBagDetail> detailList;
}
