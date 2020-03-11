package com.zslin.model;

import lombok.Data;

import javax.persistence.*;

/**
 * 餐桌
 */
@Entity
@Table(name = "t_dining_table")
@Data
public class DiningTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "store_id")
    private Integer storeId;

    @Column(name = "store_name")
    private String storeName;

    @Column(name = "store_sn")
    private String storeSn;

    private String name;

    @Column(name = "order_no")
    private Integer orderNo;

    private String remark;

    @Column(name = "data_id")
    private Integer dataId;

    public Integer getDataId() {
        return dataId;
    }

    public void setDataId(Integer dataId) {
        this.dataId = dataId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
