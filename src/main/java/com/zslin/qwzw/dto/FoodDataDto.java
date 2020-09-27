package com.zslin.qwzw.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 食品打印DTO对象
 */
@Data
public class FoodDataDto {

    /** 数据分隔符 */
    public static final String DATA_SEP = ",";

    /** 厨房位置 */
    public static final String POS_COOK = "2";

    /** 吧台位置 */
    public static final String POS_CASH = "1";

    /** 甜品 */
    public static final String POS_SWEET = "3";

    private String orderNo;

    private String batchNo;

    private String shopName;
    /**
     * 1-吧台；2-厨房
     */
    private String pos;
    private String deskName;
    private Integer peopleCount;

    private double [] colWidths;

    private String [] colNames;

    /** 长度 */
    private Integer length;

    /** data格式：
     * 根据上面长度以“,”分隔
     */
    private List<String> data;

    /** 是否为首次打印，0-不是；1-是 */
    private String isFirst;

    /** 合计金额 */
    private Float totalMoney;

    public static FoodDataDto getInstance() {return new FoodDataDto();}
//    public static FoodDataDto getInstance(int length) {return new FoodDataDto(length);}

    public FoodDataDto setColWidths(double ...widths) {
//        FoodDataDto res = getInstance(widths.length);
        this.colWidths = widths;
        return this;
    }

    public FoodDataDto setColNames(String ...colNames) {
        this.colNames = colNames;
        return this;
    }

    /*private FoodDataDto(int length) {
        this.length = length;
        this.colWidths = new double[length];
        this.colNames = new String[length];
        data = new ArrayList<>();
    }*/
}
