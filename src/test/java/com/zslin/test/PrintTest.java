package com.zslin.test;

import com.jacob.activeX.ActiveXComponent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import javax.print.*;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import java.io.*;

/**
 * Created by 钟述林 393156105@qq.com on 2017/4/8 23:19.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("zsl")
public class PrintTest {

    @Test
    public void test01() {
        File file = new File("D:/temp123.doc"); // 获取选择的文件
        // 构建打印请求属性集
        HashPrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
        // 设置打印格式，因为未确定类型，所以选择autosense
        DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
        // 定位默认的打印服务
        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
        InputStream fis = null;
        try {
            DocPrintJob job = defaultService.createPrintJob(); // 创建打印作业
            fis = new FileInputStream(file); // 构造待打印的文件流
            DocAttributeSet das = new HashDocAttributeSet();
            Doc doc = new SimpleDoc(fis, flavor, das);
            job.print(doc, pras);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void test02() throws Exception {
        PrintTools.printFile("D:/temp123.docx");
    }

    @Test
    public void test03() {
        PrintTools2.print("D:/temp/temp123.docx");
    }

    @Test
    public void test06() {
        ActiveXComponent word=new ActiveXComponent("KWPS.Application");
        System.out.println(word);
    }

    @Test
    public void test05() throws Exception {
        PrintPdf.printFile("D:/temp/temp123.pdf");
    }

    @Test
    public void test04() {
        try {
            File f = ResourceUtils.getFile("classpath:word-temp/bond-template.doc");
            System.out.println(f.exists());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
