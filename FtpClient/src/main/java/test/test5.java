package test;

import ftp.DownloadTask;
import ftp.FtpClient;
import ftp.FtpFile;
import ftp.UploadTask;

import java.io.File;
import java.util.ArrayList;

public class test5 {
    public static void main(String[] args) throws Exception {
        FtpClient ftpClient = new FtpClient("116.62.170.221", 21, "ftp511", "admin123456");
        ftpClient.login();
        ArrayList<FtpFile> ftpFiles = ftpClient.getAllFiles();
        File file = new File("C:\\Users\\lenovo\\Desktop\\Desert.jpg");
        UploadTask uploadTask = new UploadTask(ftpClient,file,0,null,0);
        Thread t2 = new Thread(uploadTask);
        t2.start();
        Thread.sleep(1000);
        uploadTask.pauseUpload();
        Thread.sleep(600);
        Thread t4 = new Thread(uploadTask);
        t4.start();
    }
}
