import ftp.DownloadTask;
import ftp.FtpClient;
import ftp.FtpFile;

import java.util.ArrayList;

public class test2 {
    public static void main(String[] args) throws Exception {
        FtpClient ftpClient = new FtpClient("116.62.170.221", 21, "ftp511", "admin123456");
        ftpClient.login();
        ArrayList<FtpFile> ftpFiles = ftpClient.getAllFiles();
        //调整get的值下不同东西
        DownloadTask downloadTask = new DownloadTask(ftpFiles.get(4),ftpClient,0,null,0,"C:\\Users\\lenovo\\Desktop\\FtpDownload");
        DownloadTask downloadTask2 = new DownloadTask(ftpFiles.get(5),ftpClient,0,null,0,"C:\\Users\\lenovo\\Desktop\\FtpDownload");

        Thread t1 = new Thread(downloadTask);
        Thread t2 = new Thread(downloadTask2);
        t1.start();
        t2.start();
    }
}