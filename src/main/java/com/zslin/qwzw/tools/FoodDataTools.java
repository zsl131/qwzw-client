package com.zslin.qwzw.tools;

import com.zslin.basic.repository.SimpleSortBuilder;
import com.zslin.model.Company;
import com.zslin.model.FoodOrder;
import com.zslin.model.FoodOrderDetail;
import com.zslin.qwzw.dto.FoodDataDto;
import com.zslin.qwzw.model.PrintConfig;
import com.zslin.service.IFoodOrderDetailService;
import com.zslin.service.IFoodOrderService;
import com.zslin.tools.PrintTemplateTools;
import com.zslin.tools.PrintTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FoodDataTools {

    @Autowired
    private IFoodOrderDetailService foodOrderDetailService;

    @Autowired
    private IFoodOrderService foodOrderService;

    @Autowired
    private CompanyTools companyTools;

    @Autowired
    private PrintTemplateTools printTemplateTools;

    @Autowired
    private PrintConfigTools printConfigTools;

    /** 调用打印 */
    private void print(String printName, File file) {
        PrintTools.print(file.getAbsolutePath(), printName);
        file.delete();
    }

    /** 打印预结单 */
    public void printFoodSettle(String orderNo) {
        List<FoodOrderDetail> detailList = foodOrderDetailService.findByOrderNo(orderNo, SimpleSortBuilder.generateSort("id"));
        detailList = rebuildFoodDetail(detailList);
        FoodOrder order = foodOrderService.findByNo(orderNo);
        Company company = companyTools.getCompany();

        PrintConfig pc = printConfigTools.getConfig();

        FoodDataDto dto = buildBaseDto(order, "", "", company);
        dto.setColWidths(65, 20, 15);
        dto.setColNames("菜品", "数量", "小计");
        dto.setData(buildData2Cash(detailList));
        dto.setTotalMoney(buildMoney(detailList));

        File file = printTemplateTools.buildSettleFile(dto);

        print(pc.getPrintName1(), file);
    }

    public void printFood(String orderNo, String batchNo, String isFirst) {
        List<FoodOrderDetail> detailList = foodOrderDetailService.findByOrderNoAndBatchNo(orderNo, batchNo);

        FoodOrder order = foodOrderService.findByNo(orderNo);
        Company company = companyTools.getCompany();
        PrintConfig pc = printConfigTools.getConfig();
        if(pc!=null) { //如果打印配置不为空
            List<FoodOrderDetail> cookData = buildData(detailList, "2"); //厨房菜单
            if(cookData!=null && cookData.size()>0) {
                FoodDataDto cookDto = buildBaseDto(order, batchNo, isFirst, company);
                cookDto.setColWidths(75, 25);
                cookDto.setColNames("菜品", "数量");
                cookDto.setData(buildData2Cook(cookData));
                cookDto.setPos(FoodDataDto.POS_COOK);

                File file = printTemplateTools.buildFoodFile(cookDto);

                print(pc.getPrintName2(), file);
            }

            List<FoodOrderDetail> cashData = buildData(detailList, "1"); //吧台菜单
            if(cashData!=null && cashData.size()>0) {
                FoodDataDto cashDto = buildBaseDto(order, batchNo, isFirst, company);
                cashDto.setColWidths(65, 20, 15);
                cashDto.setColNames("菜品", "数量", "小计");
                cashDto.setData(buildData2Cash(cashData));
                cashDto.setPos(FoodDataDto.POS_CASH);

                File file = printTemplateTools.buildFoodFile(cashDto);

                print(pc.getPrintName1(), file);
            }

            List<FoodOrderDetail> sweetData = buildData(detailList, "3"); //甜品菜单
            if(sweetData!=null && sweetData.size()>0) {
                FoodDataDto sweetDto = buildBaseDto(order, batchNo, isFirst, company);
                sweetDto.setColWidths(75, 25);
                sweetDto.setColNames("菜品", "数量");
                sweetDto.setData(buildData2Cook(sweetData));
                sweetDto.setPos(FoodDataDto.POS_SWEET);

                File file = printTemplateTools.buildFoodFile(sweetDto);

                print(pc.getPrintName3(), file);
            }
        }
    }

    public void printFood(String orderNo, String batchNo) {
        printFood(orderNo, batchNo, "1");
    }

    /** 生成位置中文 */
    public static String buildPos(String pos) {
        String res = "";
        if(pos.equals(FoodDataDto.POS_CASH)) {res = "顾客联";}
        else if(pos.equals(FoodDataDto.POS_COOK)) {res = "厨房联";}
        else if(pos.equals(FoodDataDto.POS_SWEET)) {res = "甜品站";}
        return res;
    }

    private FoodDataDto buildBaseDto(FoodOrder order, String batchNo, String isFirst, Company company) {
        FoodDataDto dto = FoodDataDto.getInstance();
        dto.setOrderNo(order.getNo());
        dto.setBatchNo(batchNo);
        dto.setIsFirst(isFirst);
        dto.setDeskName(order.getTableName());
        dto.setPeopleCount(order.getAmount());
        if(company!=null) {
            dto.setShopName(company.getName());
        }
        return dto;
    }

    /** 生成打印菜口数据
     * 1-吧台；2-厨房
     */
    private List<FoodOrderDetail> buildData(List<FoodOrderDetail> detailList, String f) {
        List<FoodOrderDetail> res = new ArrayList<>();
        for(FoodOrderDetail fod : detailList) {
            String flag = fod.getPrintFlag();
            if(f.equals(flag) || "all".equalsIgnoreCase(flag)) {res.add(fod);}
        }
        return res;
    }

    /** 生成厨房出单的数据 */
    private List<String> buildData2Cook(List<FoodOrderDetail> detailList) {
        List<String> res = new ArrayList<>();
        for(FoodOrderDetail fod : detailList) {
            res.add(fod.getFoodName()+FoodDataDto.DATA_SEP+fod.getAmount());
        }
        return res;
    }

    private Float buildMoney(List<FoodOrderDetail> detailList) {
        Float res = 0f;
        for(FoodOrderDetail fod : detailList) {
            res += (fod.getPrice()*fod.getAmount());
        }
        return Float.parseFloat(rebuildMoney(res));
    }

    /** 生成吧台出单的数据 */
    private List<String> buildData2Cash(List<FoodOrderDetail> detailList) {
        List<String> res = new ArrayList<>();
        for(FoodOrderDetail fod : detailList) {
            res.add(fod.getFoodName()+FoodDataDto.DATA_SEP+
                    fod.getAmount()+"*"+fod.getPrice()+ FoodDataDto.DATA_SEP+
                    (rebuildMoney(fod.getAmount()*fod.getPrice())));
        }
        return res;
    }

    private String rebuildMoney(double money) {
        return String.format("%.1f", money);
    }

    public static List<FoodOrderDetail> rebuildFoodDetail(List<FoodOrderDetail> detailList) {
        if(detailList==null) {return new ArrayList<>();}
        Map<Integer, List<FoodOrderDetail>> map = new HashMap<>();
        for(FoodOrderDetail fod : detailList) {
            setData(map, fod.getFoodId(), fod);
        }
        return rebuildFood(map);
    }

    private static void setData(Map<Integer, List<FoodOrderDetail>> map, Integer key, FoodOrderDetail value) {
        if(!map.containsKey(key)) {
            map.put(key, new ArrayList<>());
        }
        map.get(key).add(value);
    }

    private static List<FoodOrderDetail> rebuildFood(Map<Integer, List<FoodOrderDetail>> detailMap) {
        List<FoodOrderDetail> res = new ArrayList<>();
        for (Map.Entry<Integer, List<FoodOrderDetail>> entry : detailMap.entrySet()) {
            List<FoodOrderDetail> list = entry.getValue();
            Integer amount = 0; FoodOrderDetail detail=null;
            for(FoodOrderDetail fod : list) {amount += fod.getAmount(); detail = fod;}
            if(detail!=null) {
                detail.setAmount(amount);
                res.add(detail);
            }
        }
        return res;
    }
}
