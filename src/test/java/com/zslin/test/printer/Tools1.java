package com.zslin.test.printer;

import javax.print.*;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Tools1 {

    //构建打印请求属性集
    HashPrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
    //设置打印格式，因为未确定类型，所以选择autosense
    DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
//    DocFlavor flavor = DocFlavor.INPUT_STREAM.TEXT_PLAIN_US_ASCII;

    public void list() {
//        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
//        DocFlavor flavor = DocFlavor.BYTE_ARRAY.PNG;
        //可用的打印机列表(字符串数组)
        PrintService printService[] = PrintServiceLookup.lookupPrintServices(flavor, pras);
        for(int i=0;i<printService.length;i++){
            //Printers[i]=printService[i].getName();
            System.out.println("---all --------"+ printService[i].getName());
        }

        PrintService PS = PrintServiceLookup.lookupDefaultPrintService();
        System.out.println("=====default===="+PS.getName());
//        PS.getName();
    }

    private PrintService queryService(String printerName) {
        PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
        PrintService printServices[] = PrintServiceLookup.lookupPrintServices(flavor, pras);
        PrintService res = null;
        for(PrintService ps : printServices) {
            if(printerName.equalsIgnoreCase(ps.getName())) {res = ps; break;}
        }
        System.out.println("----------query-----------"+res);
        //如果没有找到对应名字的打印机，则选择默认打印机
        if(res==null) {res = defaultPrintService;}
        return res;
    }

    public void print(String printName, File file) {
        try {
            PrintService service = queryService(printName);
            DocPrintJob job = service.createPrintJob(); //创建打印作业
            FileInputStream fis = new FileInputStream(file); //构造待打印的文件流
            DocAttributeSet das = new HashDocAttributeSet();
            Doc doc = new SimpleDoc(fis, DocFlavor.INPUT_STREAM.AUTOSENSE, das);
            job.print(doc, pras);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (PrintException e) {
            e.printStackTrace();
        }
    }
}
