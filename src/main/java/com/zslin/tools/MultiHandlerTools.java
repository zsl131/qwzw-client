package com.zslin.tools;

import com.alibaba.fastjson.JSON;
import com.zslin.basic.tools.MyBeanUtils;
import com.zslin.model.Category;
import com.zslin.model.DiningTable;
import com.zslin.model.Food;
import com.zslin.service.ICategoryService;
import com.zslin.service.IDiningTableService;
import com.zslin.service.IFoodService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 多店铺处理工具类
 */
@Component
public class MultiHandlerTools {

    @Autowired
    private ICategoryService categoryService;

    @Autowired
    private IFoodService foodService;

    @Autowired
    private IDiningTableService diningTableService;

    @Autowired
    private PictureTools pictureTools;

    /** 处理菜品分类 */
    public void handlerCategory(Integer dataId, JSONObject jsonObj, String action) {
        Category category = JSON.toJavaObject(JSON.parseObject(jsonObj.toString()), Category.class);
        if("save".equalsIgnoreCase(action)) {
            Category obj = categoryService.findByDataId(dataId);
            if(obj==null) {
                category.setPicPath(pictureTools.downloadImage("category", category.getPicPath()));
                category.setDataId(dataId);
                categoryService.save(category);
            } else {
                MyBeanUtils.copyProperties(category, obj, "id", "dataId", "picPath");
                obj.setPicPath(pictureTools.downloadImage("category", category.getPicPath()));
                categoryService.save(obj);
            }
        } else if("delete".equalsIgnoreCase(action)) {
            categoryService.deleteByDataId(dataId);
        }
    }

    /** 处理菜品 */
    public void handlerFood(Integer dataId, JSONObject jsonObj, String action) {
        Food food = JSON.toJavaObject(JSON.parseObject(jsonObj.toString()), Food.class);
        if("save".equalsIgnoreCase(action)) {
            Food obj = foodService.findByDataId(dataId);
            if(obj==null) {
                food.setPicPath(pictureTools.downloadImage("food", food.getPicPath()));
                food.setDataId(dataId);
                foodService.save(food);
            } else {
                MyBeanUtils.copyProperties(food, obj, "id", "dataId", "picPath");
                obj.setPicPath(pictureTools.downloadImage("food", food.getPicPath()));
                foodService.save(obj);
            }
        } else if("delete".equalsIgnoreCase(action)) {
            foodService.deleteByDataId(dataId);
        }
    }

    /** 处理餐桌 */
    public void handlerDiningTable(Integer dataId, JSONObject jsonObj, String action) {
        DiningTable table = JSON.toJavaObject(JSON.parseObject(jsonObj.toString()), DiningTable.class);
        if("save".equalsIgnoreCase(action)) {
            DiningTable obj = diningTableService.findByDataId(dataId);
            if(obj==null) {
                table.setDataId(dataId);
                diningTableService.save(table);
            } else {
                MyBeanUtils.copyProperties(table, obj, "id", "dataId");
                diningTableService.save(obj);
            }
        } else if("delete".equalsIgnoreCase(action)) {
            diningTableService.deleteByDataId(dataId);
        }
    }
}
