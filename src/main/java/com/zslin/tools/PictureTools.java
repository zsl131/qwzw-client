package com.zslin.tools;

import com.zslin.basic.tools.ConfigTools;
import com.zslin.model.Company;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

/**
 * 图片处理工具
 */
@Component
public class PictureTools {

    @Autowired
    private ClientConfigTools clientConfigTools;

    @Autowired
    private ConfigTools configTools;

    public String downloadImage(String contextPath, String picUrl) {
        if(picUrl==null || "".equals(picUrl)) {return null;}
        picUrl = picUrl.replace("\\", "/"); //替换
        contextPath = "download"+File.separator+contextPath;
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        BufferedInputStream bis = null;
        FileOutputStream out = null;

        try {

            String fileName = buildFileName(picUrl);
            //System.out.println("------->"+fileName);
            String file = configTools.getUploadPath(contextPath) + fileName;
            String resultPath = file.replace(configTools.getUploadPath(), "");

            out = new FileOutputStream(file);
            // 建立链接
            URL httpUrl=new URL(rebuildPicPath(picUrl));
            conn=(HttpURLConnection) httpUrl.openConnection();
            //以Post方式提交表单，默认get方式
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            // post方式不能使用缓存
            conn.setUseCaches(false);
            //连接指定的资源
            conn.connect();
            //获取网络输入流
            inputStream=conn.getInputStream();
            bis = new BufferedInputStream(inputStream);
            byte b [] = new byte[1024];
            int len = 0;
            while((len=bis.read(b))!=-1){
                out.write(b, 0, len);
            }
            return rebuildResultPath(resultPath);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(out!=null){
                    out.close();
                }
                if(bis!=null){
                    bis.close();
                }
                if(inputStream!=null){
                    inputStream.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return null;
    }

    private String rebuildResultPath(String resultPath) {
        resultPath = resultPath.replaceAll("\\\\", "/");
        if(!resultPath.startsWith("/")) {resultPath = "/"+resultPath;}
        return resultPath;
    }

    private String rebuildPicPath(String picUrl) {
        if(!picUrl.startsWith("http://") && !picUrl.startsWith("https://")) {
            Company c = clientConfigTools.getCompany();
            if (c != null && c.getId() != null && c.getId() > 0) {
                picUrl = c.getBaseUrl() +":" + c.getBasePort() + picUrl;
            }
        }
       // System.out.println(picUrl);
        return picUrl;
    }

    private String buildFileName(String picUrl) {
        String res = null;
        try {
            String lastName = picUrl.substring(picUrl.lastIndexOf("/")+1);
            if(lastName.contains("\\")) {lastName = lastName.substring(lastName.lastIndexOf("\\")+1);}
            if(lastName.contains(".")) {
                res = lastName;
            } else {
                res = UUID.randomUUID().toString()+".jpg";
            }
        } catch (Exception e) {
            res = UUID.randomUUID().toString()+".jpg";
        }
        return res;
    }
}
