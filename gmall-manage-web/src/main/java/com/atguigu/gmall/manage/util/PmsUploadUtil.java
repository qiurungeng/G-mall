package com.atguigu.gmall.manage.util;

import com.atguigu.gmall.bean.PmsProductInfo;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class PmsUploadUtil {
    public static String uploadImage(MultipartFile multipartFile){
        String imgUrl="http://192.168.226.129";
        //上传图片到存储服务器

        //配置fdfs的全局链接地址
        String file = PmsUploadUtil.class.getResource("/tracker.conf").getFile();
        try {
            ClientGlobal.init(file);
        }catch (Exception e){
            e.printStackTrace();
        }
        TrackerClient trackerClient=new TrackerClient();
        //获得一个trackerServer实例
        TrackerServer trackerServer= null;
        try {
            trackerServer = trackerClient.getConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //通过tracker获得一个storage的链接客户端
        StorageClient storageClient=new StorageClient(trackerServer,null);

        //上传图片
        try {
            byte[] bytes = multipartFile.getBytes();    //获得上传的二进制对象
            String orginalFilename=multipartFile.getOriginalFilename();
            String extName=orginalFilename.substring(orginalFilename.lastIndexOf(".")+1);   //获取扩展文件名
            String[] uploadInfos = storageClient.upload_file(bytes, extName, null);
            for (String uploadInfo : uploadInfos) {
                imgUrl+="/"+uploadInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imgUrl;
    }
}
