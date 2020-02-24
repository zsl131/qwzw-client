package com.zslin.test;

import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.JobName;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by zsl on 2019/4/19.
 */
public class PrintPdf {

    public static void printFile(String path) throws Exception {
//        File file = new File(path);
//        File[] fies = file.listFiles();
        File f = new File(path);
//        for (File f : fies) {
        System.out.println("file " + f.getName());
//            String fileExt = f.getName().substring(f.getName().indexOf(".") + 1, f.getName().length());
//            if ("pdf".equalsIgnoreCase(fileExt)) {
//                String filepath = path + File.separator + f.getName();
        File pdfFile = new File(path);
        // 构建打印请求属性集
        PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        pras.add(new JobName(f.getName(), null));

        HashAttributeSet has = new HashAttributeSet();
//                has.add(new PrinterName("Officejet J5500 series", null)); // 添加打印机名称

        // 设置打印格式，因为未确定文件类型，这里选择AUTOSENSE
        DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;

        // 查找所有的可用打印服务
        PrintService printService[] = PrintServiceLookup.lookupPrintServices(flavor, has);

        PrintService print = null;

        for(PrintService ps : printService) {
            System.out.println("==============="+ps.getName());
            if("Thermal Printer(3)".equals(ps.getName())) {print = ps;}
        }

        if (print != null) {
            System.out.println("==========name:::"+print.getName());
            //获得打印服务的文档打印作业
            DocPrintJob job = print.createPrintJob(); // 创建打印任务

            DocAttributeSet das=new HashDocAttributeSet();

            InputStream fis = new FileInputStream(pdfFile); // 构造待打印的文件流
            Doc doc = new SimpleDoc(fis, flavor, das); // 建立打印文件格式
            job.print(doc, pras); // 进行文件的打印
        }
    }
}
