package com.zslin.model;

import javax.persistence.*;

/**
 * Created by 钟述林 393156105@qq.com on 2017/1/23 11:35.
 */
@Entity
@Table(name = "t_food")
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "cate_id")
    private Integer cateId;

    @Column(name = "cate_name")
    private String cateName;

    /** 序号 */
    @Column(name = "order_no")
    private Integer orderNo;

    /** 点评数量 */
    @Column(name = "comment_count")
    private Integer commentCount=0;

    /** 点赞次数 */
    @Column(name = "good_count")
    private Integer goodCount=0;

    /** 图片地址 */
    @Column(name = "pic_path")
    private String picPath;

    /** 状态，1-可就餐；0-已下餐 */
    private String status;

    /** 名称 */
    private String name;

    @Column(name = "store_id")
    private Integer storeId;

    @Column(name = "store_name")
    private String storeName;

    @Column(name = "store_sn")
    private String storeSn;

    /** 备注 */
    private String remark;

    /** 单位名称 */
    @Column(name = "unit_name")
    private String unitName;

    @Column(name = "data_id")
    private Integer dataId;

    private Float price;

    /** 售卖类型，1-堂食；2-外卖；3-均可 */
    @Column(name = "sale_mode")
    private String saleMode;

    /** 编号 */
    private String sn;

    /** 名称字母 */
    private String nameLetter;

    @Column(name = "sn_no")
    private Integer snNo;

    public Integer getSnNo() {
        return snNo;
    }

    public void setSnNo(Integer snNo) {
        this.snNo = snNo;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getNameLetter() {
        return nameLetter;
    }

    public void setNameLetter(String nameLetter) {
        this.nameLetter = nameLetter;
    }

    public String getSaleMode() {
        return saleMode;
    }

    public void setSaleMode(String saleMode) {
        this.saleMode = saleMode;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

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

    public Integer getCateId() {
        return cateId;
    }

    public void setCateId(Integer cateId) {
        this.cateId = cateId;
    }

    public String getCateName() {
        return cateName;
    }

    public void setCateName(String cateName) {
        this.cateName = cateName;
    }

    public Integer getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getGoodCount() {
        return goodCount;
    }

    public void setGoodCount(Integer goodCount) {
        this.goodCount = goodCount;
    }

    public String getPicPath() {
        return picPath;
    }

    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getStoreSn() {
        return storeSn;
    }

    public void setStoreSn(String storeSn) {
        this.storeSn = storeSn;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }
}
