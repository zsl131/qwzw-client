package com.zslin.tools;

import com.zslin.basic.tools.ConfigTools;
import com.zslin.basic.tools.NormalTools;
import com.zslin.qwzw.dto.FoodDataDto;
import com.zslin.qwzw.tools.FoodDataTools;
import org.docx4j.jaxb.Context;
import org.docx4j.model.properties.table.tr.TrHeight;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * 打印模板处理工具类
 */
@Component
public class PrintTemplateTools {

    @Autowired
    private ConfigTools configTools;

    @Autowired
    private FoodDataTools foodDataTools;

    public File getTemplateFile(String fileName) {
//            File f = ResourceUtils.getFile("classpath:word-temp/"+fileName);
        return new File(configTools.getUploadPath("word-temp/")+fileName);
//        return f;
    }

    /** 点餐模板 */
    private File getFoodTemplate() {
        return getTemplateFile("food-template.docx");
    }
    private File getCookTemplate() {
        return getTemplateFile("cook-template.docx");
    }

    private File getOrderTemplate() {
        return getTemplateFile("pay-template-80.docx");
    }

    /** 预结单 */
    private File getPreSettleTemplate() {
        return getTemplateFile("pre-settle-template.docx");
    }

    /** 结算单 */
    private File getSettleTemplate() {
        return getTemplateFile("settle-template-80.docx");
    }

    public File buildSettleFile(FoodDataDto fdd) {
        File targetFile = new File(configTools.getUploadPath("tickets/")+"settle-"+(UUID.randomUUID().toString())+".docx");
        try {
            File f = getSettleTemplate();
            // 载入模板文件
            WordprocessingMLPackage wPackage = WordprocessingMLPackage.load(f);
            // 提取正文
            MainDocumentPart mainDocumentPart = wPackage.getMainDocumentPart();
            ObjectFactory factory = Context.getWmlObjectFactory();
            HashMap<String, String> datas = new HashMap<>();
            datas.put("shopName", fdd.getShopName());
            datas.put("deskName", fdd.getDeskName());
            datas.put("peopleCount", fdd.getPeopleCount()+"");
            datas.put("orderNo", fdd.getOrderNo());
            datas.put("date", NormalTools.curDate());
            mainDocumentPart.variableReplace(datas);

            createNormalTable(wPackage, mainDocumentPart, factory, fdd);
            createParagraph(mainDocumentPart, factory, "", JcEnumeration.RIGHT);

            Float discountMoney = fdd.getDiscountMoney();
            if(discountMoney!=null && discountMoney>0) {
                createParagraph(mainDocumentPart, factory, "抵扣："+discountMoney+" 元", JcEnumeration.RIGHT, "000000", "22",
                        true, true, false, false);
            }

            createParagraph(mainDocumentPart, factory, "需支付："+(fdd.getTotalMoney()-(discountMoney==null?0:discountMoney))+" 元", JcEnumeration.RIGHT, "000000", "28",
                    true, true, false, false);

            createParagraph(mainDocumentPart, factory, "吾悦广场5楼", JcEnumeration.CENTER);
            createParagraph(mainDocumentPart, factory, "0870-2399488", JcEnumeration.CENTER, "000000", "20",
                    true, false, false, false);

//            byte[] barcode = qrGenerateTools.getBarcode(orderNo);
//            ImageAdd.replaceImage(wPackage, "barcode", barcode, "test1", "haha1");
            wPackage.save(targetFile);
        } catch (Docx4JException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return targetFile;
    }

    public File buildCookFile(String deskName, String foodName) {
        File targetFile = new File(configTools.getUploadPath("tickets/")+"food-"+(UUID.randomUUID().toString())+".docx");
        try {
            File f = getCookTemplate();
            // 载入模板文件
            WordprocessingMLPackage wPackage = WordprocessingMLPackage.load(f);
            // 提取正文
            MainDocumentPart mainDocumentPart = wPackage.getMainDocumentPart();
            ObjectFactory factory = Context.getWmlObjectFactory();
            HashMap<String, String> datas = new HashMap<>();
            datas.put("deskName", deskName);
            datas.put("foodName", foodName);
            datas.put("date", NormalTools.curDate());
            mainDocumentPart.variableReplace(datas);

            wPackage.save(targetFile);
        } catch (Docx4JException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return targetFile;
    }

    public File buildFoodFile(FoodDataDto fdd) {
        File targetFile = new File(configTools.getUploadPath("tickets/")+"food-"+(UUID.randomUUID().toString())+".docx");
        try {
            File f = getFoodTemplate();
            // 载入模板文件
            WordprocessingMLPackage wPackage = WordprocessingMLPackage.load(f);
            // 提取正文
            MainDocumentPart mainDocumentPart = wPackage.getMainDocumentPart();
            ObjectFactory factory = Context.getWmlObjectFactory();
            HashMap<String, String> datas = new HashMap<>();
            datas.put("shopName", fdd.getShopName());
            datas.put("pos", FoodDataTools.buildPos(fdd.getPos())); //位置
            datas.put("deskName", fdd.getDeskName());
            datas.put("peopleCount", fdd.getPeopleCount()+"");
            datas.put("orderNo", fdd.getOrderNo());
            datas.put("batchNo", fdd.getBatchNo());
            datas.put("date", NormalTools.curDate());
            mainDocumentPart.variableReplace(datas);

            createNormalTable(wPackage, mainDocumentPart, factory, fdd);
            createParagraph(mainDocumentPart, factory, "", JcEnumeration.RIGHT);

            if(!"1".equals(fdd.getIsFirst())) {
                createParagraph(mainDocumentPart, factory, "注意：非首次打印", JcEnumeration.CENTER, "000000", "24",
                        true, true, false, false);
            }

            if(fdd.getPos().equals(FoodDataDto.POS_CASH)) {
                createParagraph(mainDocumentPart, factory, "吾悦广场5楼", JcEnumeration.CENTER);
                createParagraph(mainDocumentPart, factory, "0870-2399488", JcEnumeration.CENTER, "000000", "20",
                     true, false, false, false);
            }

//            byte[] barcode = qrGenerateTools.getBarcode(orderNo);
//            ImageAdd.replaceImage(wPackage, "barcode", barcode, "test1", "haha1");
            wPackage.save(targetFile);
        } catch (Docx4JException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return targetFile;
    }

    public File buildOrderFile(FoodDataDto fdd) {
        File targetFile = new File(configTools.getUploadPath("tickets/")+"order-"+(UUID.randomUUID().toString())+".docx");
        try {
            File f = getOrderTemplate();
            // 载入模板文件
            WordprocessingMLPackage wPackage = WordprocessingMLPackage.load(f);
            // 提取正文
            MainDocumentPart mainDocumentPart = wPackage.getMainDocumentPart();
            ObjectFactory factory = Context.getWmlObjectFactory();
            HashMap<String, String> datas = new HashMap<>();
            datas.put("shopName", fdd.getShopName());
            datas.put("pos", FoodDataTools.buildPos(fdd.getPos())); //位置
            datas.put("deskName", fdd.getDeskName());
            datas.put("peopleCount", fdd.getPeopleCount()+"");
            datas.put("orderNo", fdd.getOrderNo());
            datas.put("batchNo", fdd.getBatchNo());
            datas.put("date", NormalTools.curDate());
            mainDocumentPart.variableReplace(datas);

            createNormalTable(wPackage, mainDocumentPart, factory, fdd);
            createParagraph(mainDocumentPart, factory, "", JcEnumeration.RIGHT);

            Float discountMoney = fdd.getDiscountMoney();
            if(discountMoney!=null && discountMoney>0) {
                createParagraph(mainDocumentPart, factory, "抵扣："+discountMoney+" 元", JcEnumeration.RIGHT, "000000", "22",
                        true, true, false, false);
            }

            createParagraph(mainDocumentPart, factory, "实收："+(fdd.getTotalMoney()-(discountMoney==null?0:discountMoney))+" 元", JcEnumeration.RIGHT, "000000", "28",
                    true, true, false, false);

            createParagraph(mainDocumentPart, factory, "吾悦广场5楼", JcEnumeration.CENTER);
            createParagraph(mainDocumentPart, factory, "0870-2399488", JcEnumeration.CENTER, "000000", "20",
                    true, false, false, false);

//            byte[] barcode = qrGenerateTools.getBarcode(orderNo);
//            ImageAdd.replaceImage(wPackage, "barcode", barcode, "test1", "haha1");
            wPackage.save(targetFile);
        } catch (Docx4JException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return targetFile;
    }

    /*public File buildFoodFile(String shopName, String pos, String deskName, Integer peopleCount, String orderNo,
                              String batchNo, String details) {
        File targetFile = new File(configTools.getUploadPath("tickets/")+"food-"+(UUID.randomUUID().toString())+".docx");
        try {
            File f = getFoodTemplate();
            // 载入模板文件
            WordprocessingMLPackage wPackage = WordprocessingMLPackage.load(f);
            // 提取正文
            MainDocumentPart mainDocumentPart = wPackage.getMainDocumentPart();
            ObjectFactory factory = Context.getWmlObjectFactory();
            HashMap<String, String> datas = new HashMap<>();
            datas.put("shopName", shopName);
            datas.put("pos", pos); //位置
            datas.put("deskName", deskName);
            datas.put("peopleCount", peopleCount+"");
            datas.put("orderNo", orderNo);
            datas.put("batchNo", batchNo);
            datas.put("date", NormalTools.curDate());
            datas.put("details", details);
            mainDocumentPart.variableReplace(datas);

            FoodDataDto fdd = foodDataTools.buildData2Cook(orderNo, batchNo);
            createNormalTable(wPackage, mainDocumentPart, factory, fdd);
            createParagraph(mainDocumentPart, factory, "", JcEnumeration.RIGHT);
            //createParagraph(mainDocumentPart, factory, "吾悦广场5楼", JcEnumeration.CENTER);
            //createParagraph(mainDocumentPart, factory, "0870-2399488", JcEnumeration.CENTER, "000000", "20",
               //     true, false, false, false);

//            byte[] barcode = qrGenerateTools.getBarcode(orderNo);
//            ImageAdd.replaceImage(wPackage, "barcode", barcode, "test1", "haha1");
            wPackage.save(targetFile);
        } catch (Docx4JException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return targetFile;
    }*/

    private void createNormalTable(WordprocessingMLPackage wordMLPackage,
                                   MainDocumentPart t, ObjectFactory factory, FoodDataDto foodDataDto) throws Exception {
        RPr titleRpr = getRPr(factory, "宋体", "000000", "22", STHint.EAST_ASIA,
                true, false, false, false);
        RPr contentRpr = getRPr(factory, "宋体", "000000", "22",
                STHint.EAST_ASIA, false, false, false, false);
        Tbl table = factory.createTbl();
        CTBorder topBorder = new CTBorder();
        topBorder.setColor("80C687");
        topBorder.setVal(STBorder.SINGLE);
        topBorder.setSz(new BigInteger("2"));
        CTBorder leftBorder = new CTBorder();
        leftBorder.setVal(STBorder.NONE);
        leftBorder.setSz(new BigInteger("0"));

        CTBorder hBorder = new CTBorder();
        hBorder.setVal(STBorder.SINGLE);
        hBorder.setSz(new BigInteger("1"));

        addBorders(table, topBorder, topBorder, leftBorder, leftBorder, hBorder, null);

//        double[] colWidthPercent = new double[] { 30, 20, 20, 30 };// 百分比
        double[] colWidthPercent = foodDataDto.getColWidths();// 百分比
        setTableGridCol(wordMLPackage, factory, table, 100, colWidthPercent);

        Tr titleRow = factory.createTr();
        setTableTrHeight(factory, titleRow, "500");

        for(String name : foodDataDto.getColNames()) {
            addTableCell(factory, wordMLPackage, titleRow, name, titleRpr,
                    JcEnumeration.CENTER, true, "C6D9F1");
        }
        table.getContent().add(titleRow);

        /*addTableCell(factory, wordMLPackage, titleRow, "名谁", titleRpr,
                JcEnumeration.CENTER, true, "C6D9F1");
        addTableCell(factory, wordMLPackage, titleRow, "籍贯", titleRpr,
                JcEnumeration.CENTER, true, "C6D9F1");
        addTableCell(factory, wordMLPackage, titleRow, "营生", titleRpr,
                JcEnumeration.CENTER, true, "C6D9F1");
        table.getContent().add(titleRow);*/

        for(String data : foodDataDto.getData()) {
            Tr contentRow = factory.createTr();
            List<String> contents = getContents(data);
            for(String con : contents) {
                addTableCell(factory, wordMLPackage, contentRow, con, contentRpr,
                        JcEnumeration.CENTER, false, null);
            }
            table.getContent().add(contentRow);
        }
        /*for (int i = 0; i < 5; i++) {
            Tr contentRow = factory.createTr();
            addTableCell(factory, wordMLPackage, contentRow, "无名氏", contentRpr,
                    JcEnumeration.CENTER, false, null);
            addTableCell(factory, wordMLPackage, contentRow, "佚名", contentRpr,
                    JcEnumeration.CENTER, false, null);
            addTableCell(factory, wordMLPackage, contentRow, "武林", contentRpr,
                    JcEnumeration.CENTER, false, null);
            addTableCell(factory, wordMLPackage, contentRow, "吟诗赋曲",
                    contentRpr, JcEnumeration.CENTER, false, null);
            table.getContent().add(contentRow);
        }*/
        setTableAlign(factory, table, JcEnumeration.CENTER);
        t.addObject(table);
    }

    private List<String> getContents(String con) {
        String [] array = con.split(FoodDataDto.DATA_SEP);
        List<String> res = new ArrayList<>();
        for(String c : array) {
            if(c!=null && !"".equals(c.trim())) {
                res.add(c);
            }
        }
        return res;
    }

    /**
     * 创建段落
     * @param t
     * @param factory
     * @param text 文本内容
     * @param alignment 对齐方式
     * @param colorVal 字体颜色
     * @param fontSize 字号大小
     * @param isBold 是否加粗
     * @param isUnderLine 是否下划线
     * @param isItalic 是否倾斜
     * @param isStrike 就否删除线
     */
    private void createParagraph(MainDocumentPart t, ObjectFactory factory, String text, JcEnumeration alignment,
                                 String colorVal, String fontSize, boolean isBold,
                                 boolean isUnderLine, boolean isItalic, boolean isStrike) {
        RPr titleRpr = getRPr(factory, "宋体", colorVal, fontSize, STHint.EAST_ASIA,
                isBold, isUnderLine, isItalic, isStrike);
        P paragraph=factory.createP();
        setParagraphSpacing(factory, paragraph, alignment, true,
                "0", "0", null, null, true, "240", STLineSpacingRule.AUTO);
        CTBorder topBorder=new CTBorder() ;
        topBorder.setSpace(new BigInteger("1"));
        topBorder.setSz(new BigInteger("2"));
        topBorder.setVal(STBorder.WAVE);
        Text txt = factory.createText();
        txt.setValue(text);

        R run = factory.createR();
        run.getContent().add(txt);
        run.setRPr(titleRpr);
        paragraph.getContent().add(run);
        //t.createParagraghLine(wordMLPackage, mp, factory, paragraph, topBorder, topBorder, topBorder, topBorder);
        t.addObject(paragraph);
    }

    private void createParagraph(MainDocumentPart t, ObjectFactory factory, String text, JcEnumeration alignment) {
        createParagraph(t, factory, text, alignment, "000000", "20",
                false, false, false, false);
    }
    private void createParagraph(MainDocumentPart t, ObjectFactory factory, String text) {
        createParagraph(t, factory, text, JcEnumeration.LEFT);
    }

    // 表格水平对齐方式
    private void setTableAlign(ObjectFactory factory, Tbl table,
                              JcEnumeration jcEnumeration) {
        TblPr tablePr = table.getTblPr();
        if (tablePr == null) {
            tablePr = factory.createTblPr();
        }
        Jc jc = tablePr.getJc();
        if (jc == null) {
            jc = new Jc();
        }
        jc.setVal(jcEnumeration);
        tablePr.setJc(jc);
        table.setTblPr(tablePr);
    }

    // 新增单元格
    private void addTableCell(ObjectFactory factory,
                             WordprocessingMLPackage wordMLPackage, Tr tableRow, String content,
                             RPr rpr, JcEnumeration jcEnumeration, boolean hasBgColor,
                             String backgroudColor) {
        Tc tableCell = factory.createTc();
        P p = factory.createP();
        setParagraphSpacing(factory, p, jcEnumeration, true, "0", "0", null,
                null, true, "240", STLineSpacingRule.AUTO);
        Text t = factory.createText();
        t.setValue(content);
        R run = factory.createR();
        // 设置表格内容字体样式
        run.setRPr(rpr);

        TcPr tcPr = tableCell.getTcPr();
        if (tcPr == null) {
            tcPr = factory.createTcPr();
        }

        CTVerticalJc valign = factory.createCTVerticalJc();
        valign.setVal(STVerticalJc.CENTER);
        tcPr.setVAlign(valign);

        run.getContent().add(t);
        p.getContent().add(run);
        tableCell.getContent().add(p);
        if (hasBgColor) {
            CTShd shd = tcPr.getShd();
            if (shd == null) {
                shd = factory.createCTShd();
            }
            shd.setColor("auto");
            shd.setFill(backgroudColor);
            tcPr.setShd(shd);
        }
        tableCell.setTcPr(tcPr);
        tableRow.getContent().add(tableCell);
    }

    // 设置tr高度
    private void setTableTrHeight(ObjectFactory factory, Tr tr, String height) {
        TrPr trPr = tr.getTrPr();
        if (trPr == null) {
            trPr = factory.createTrPr();
        }
        CTHeight ctHeight = new CTHeight();
        ctHeight.setVal(new BigInteger(height));
        TrHeight trHeight = new TrHeight(ctHeight);
        trHeight.set(trPr);
        tr.setTrPr(trPr);
    }

    // 设置段间距-->行距 段前段后距离
    // 段前段后可以设置行和磅 行距只有磅
    // 段前磅值和行值同时设置，只有行值起作用
    // TODO 1磅=20 1行=100 单倍行距=240 为什么是这个值不知道
    /**
     * @param jcEnumeration
     *            对齐方式
     * @param isSpace
     *            是否设置段前段后值
     * @param before
     *            段前磅数
     * @param after
     *            段后磅数
     * @param beforeLines
     *            段前行数
     * @param afterLines
     *            段后行数
     * @param isLine
     *            是否设置行距
     * @param lineValue
     *            行距值
     * @param sTLineSpacingRule
     *            自动auto 固定exact 最小 atLeast
     */
    private void setParagraphSpacing(ObjectFactory factory, P p,
                                    JcEnumeration jcEnumeration, boolean isSpace, String before,
                                    String after, String beforeLines, String afterLines,
                                    boolean isLine, String lineValue,
                                    STLineSpacingRule sTLineSpacingRule) {
        PPr pPr = p.getPPr();
        if (pPr == null) {
            pPr = factory.createPPr();
        }
        Jc jc = pPr.getJc();
        if (jc == null) {
            jc = new Jc();
        }
        jc.setVal(jcEnumeration);
        pPr.setJc(jc);

        PPrBase.Spacing spacing = new PPrBase.Spacing();
        if (isSpace) {
            if (before != null) {
                // 段前磅数
                spacing.setBefore(new BigInteger(before));
            }
            if (after != null) {
                // 段后磅数
                spacing.setAfter(new BigInteger(after));
            }
            if (beforeLines != null) {
                // 段前行数
                spacing.setBeforeLines(new BigInteger(beforeLines));
            }
            if (afterLines != null) {
                // 段后行数
                spacing.setAfterLines(new BigInteger(afterLines));
            }
        }
        if (isLine) {
            if (lineValue != null) {
                spacing.setLine(new BigInteger(lineValue));
            }
            spacing.setLineRule(sTLineSpacingRule);
        }
        pPr.setSpacing(spacing);
        p.setPPr(pPr);
    }

    // 表格增加边框 可以设置上下左右四个边框样式以及横竖水平线样式
    private void addBorders(Tbl table, CTBorder topBorder,
                           CTBorder bottomBorder, CTBorder leftBorder, CTBorder rightBorder,
                           CTBorder hBorder, CTBorder vBorder) {
        table.setTblPr(new TblPr());
        TblBorders borders = new TblBorders();
        borders.setBottom(bottomBorder);
        borders.setLeft(leftBorder);
        borders.setRight(rightBorder);
        borders.setTop(bottomBorder);
        borders.setInsideH(hBorder);
        borders.setInsideV(vBorder);
        table.getTblPr().setTblBorders(borders);
    }

    // 设置整列宽度
    /**
     * @param tableWidthPercent
     *            表格占页面宽度百分比
     * @param widthPercent
     *            各列百分比
     */
    private void setTableGridCol(WordprocessingMLPackage wordPackage,
                                ObjectFactory factory, Tbl table, double tableWidthPercent,
                                double[] widthPercent) throws Exception {
        int width = getWritableWidth(wordPackage);
        int tableWidth = (int) (width * tableWidthPercent / 100);
        TblGrid tblGrid = factory.createTblGrid();
        for (int i = 0; i < widthPercent.length; i++) {
            TblGridCol gridCol = factory.createTblGridCol();
            gridCol.setW(BigInteger.valueOf((long) (tableWidth
                    * widthPercent[i] / 100)));
            tblGrid.getGridCol().add(gridCol);
        }
        table.setTblGrid(tblGrid);

        TblPr tblPr = table.getTblPr();
        if (tblPr == null) {
            tblPr = factory.createTblPr();
        }
        TblWidth tblWidth = new TblWidth();
        tblWidth.setType("dxa");// 这一行是必须的,不自己设置宽度默认是auto
        tblWidth.setW(new BigInteger(tableWidth + ""));
        tblPr.setTblW(tblWidth);
        table.setTblPr(tblPr);
    }

    // 得到页面宽度
    private int getWritableWidth(WordprocessingMLPackage wordPackage)
            throws Exception {
        return wordPackage.getDocumentModel().getSections().get(0)
                .getPageDimensions().getWritableWidthTwips();
    }

    /**
     * 创建字体
     *
     * @param isBlod
     *            粗体
     * @param isUnderLine
     *            下划线
     * @param isItalic
     *            斜体
     * @param isStrike
     *            删除线
     */
    private RPr getRPr(ObjectFactory factory, String fontFamily,
                      String colorVal, String fontSize, STHint sTHint, boolean isBlod,
                      boolean isUnderLine, boolean isItalic, boolean isStrike) {
        RPr rPr = factory.createRPr();
        RFonts rf = new RFonts();
        rf.setHint(sTHint);
        rf.setAscii(fontFamily);
        rf.setHAnsi(fontFamily);
        rPr.setRFonts(rf);

        BooleanDefaultTrue bdt = factory.createBooleanDefaultTrue();
        rPr.setBCs(bdt);
        if (isBlod) {
            rPr.setB(bdt);
        }
        if (isItalic) {
            rPr.setI(bdt);
        }
        if (isStrike) {
            rPr.setStrike(bdt);
        }
        if (isUnderLine) {
            U underline = new U();
            underline.setVal(UnderlineEnumeration.SINGLE);
            rPr.setU(underline);
        }

        Color color = new Color();
        color.setVal(colorVal);
        rPr.setColor(color);

        HpsMeasure sz = new HpsMeasure();
        sz.setVal(new BigInteger(fontSize));
        rPr.setSz(sz);
        rPr.setSzCs(sz);
        return rPr;
    }
}
