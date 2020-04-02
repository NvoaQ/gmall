package com.chendong.gmall.manage.util;

import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class PmsUploadUtil {


    public static String uploadImage(MultipartFile multipartFile) {

        //服务器的ip地址
        String imgUrl = "http://192.168.182.128";

        //上传图片到服务器
        //配置fastdfs的全局链接地址
        String tracker = PmsUploadUtil.class.getResource("/tracker.conf").getPath();//获得配置文件的路径

        try {
            ClientGlobal.init(tracker);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TrackerClient trackerClient = new TrackerClient();

        //获得一个trackerServer的实例
        TrackerServer trackerServer = null;
        try {
            trackerServer = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //通过tracker获得一个Storage的链接客户端
        StorageClient storageClient = new StorageClient(trackerServer,null);

        try {
            byte[] bytes = multipartFile.getBytes();//获得上传的二进制对象

            //获得文件后缀名
            String originalFilename = multipartFile.getOriginalFilename();//a.jpg
            //打印文件名到控制台
            System.out.println(originalFilename);
            //获取文件名中"."的索引
            int i = originalFilename.lastIndexOf(".");
            //通过切分，获得文件的后缀名
            String extName = originalFilename.substring(i+1);

            String[] uploadInfos = storageClient.upload_file(bytes, extName, null);

            for (String uploadInfo : uploadInfos) {
                imgUrl = imgUrl + "/" + uploadInfo;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return imgUrl;
    }
}
