package cn.e3mall.fast;

import cn.e3mall.common.utils.FastDFSClient;
import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.junit.Test;

import java.io.IOException;

public class FastDfsTest {
    @Test
    public void testFileUpload() throws IOException, MyException {
        //创建一个配置夜话。文件名任意，内容就是tracker服务器的地址。
        //使用合璧对象加载配置文件
        ClientGlobal.init("D:\\idea\\e3mall\\e3-manager\\e3-manager-web\\src\\main\\resources\\conf\\client.conf");
        //创建一个TrackerClient对象
        TrackerClient trackerClient = new TrackerClient();
        //通过TrackerClient获得一个TrackerServer对象
        TrackerServer trackerServer = trackerClient.getConnection();
        //创建一个StorageServer的引用，可是是null
        StorageServer storageServer = null;
        //创建一个StorageClient对象，参数需要TrackerServer和StorageServer
        StorageClient storageClient = new StorageClient(trackerServer, storageServer);
        //使用StorageClient上传文件
        String[] strings = storageClient.upload_file("E:\\Pictures\\Saved Pictures\\avatar.jpg", "jpg", null);
        for (String string : strings) {
            System.out.println(string);
        }
    }

    @Test
    public void testFastDsfClient() throws Exception {
        //FastDFSClient fastDFSClient = new FastDFSClient("classpath:conf\\client.conf");
        FastDFSClient fastDFSClient = new FastDFSClient("D:\\idea\\e3mall\\e3-manager\\e3-manager-web\\src\\main\\resources\\conf\\client.conf");
        String jpg = fastDFSClient.uploadFile("E:\\Pictures\\唯美\\444H.jpg", "jpg");
        System.out.println(jpg);
    }
}
