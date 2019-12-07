package com.atguigu.gmall.manage;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageWebApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Test
    public void textFileUpload() throws IOException, MyException {
        //配置fdfs的全局链接地址
        String file = this.getClass().getResource("/tracker.conf").getFile();
        ClientGlobal.init(file);
        TrackerClient trackerClient=new TrackerClient();
        //获得一个trackerServer实例
        TrackerServer trackerServer=trackerClient.getConnection();
        //通过tracker获得一个storage的链接客户端
        StorageClient storageClient=new StorageClient(trackerServer,null);
        //上传图片
        String orginalFilename="C:\\Users\\Apollos\\Desktop\\文史研究\\timg.jpg";
        String[] upload_file = storageClient.upload_file(orginalFilename, "jpg", null);
        //打印返回信息
        for (int i = 0; i < upload_file.length; i++) {
            String s = upload_file[i];
            System.out.println("s = " + s);
        }
        /**
         * 返回信息样例：
         * s = group1
         * s = M00/00/00/wKjigV3ojVyAPo5mAABD6EUxEew062.jpg
         * 那么图片url应该为：
         * ip地址/group1/M00/00/00/wKjigV3ojVyAPo5mAABD6EUxEew062.jpg
         */
    }
}
