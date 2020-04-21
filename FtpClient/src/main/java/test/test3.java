package test;

import ftp.DownloadTask;
import ftp.FtpClient;
import ftp.FtpFile;
import service.TaskService;

import java.util.ArrayList;

public class test3 {
    public static void main(String[] args) throws Exception {
        FtpClient ftpClient = new FtpClient("116.62.170.221", 21, "ftp511", "admin123456");
        ftpClient.login();
        ArrayList<FtpFile> ftpFiles = ftpClient.getAllFiles();
        //调整get的值下不同东西
        DownloadTask downloadTask = new DownloadTask(ftpFiles.get(4),ftpClient,0,null,0,"/Users/byron/Desktop/FtpDownload/");
        DownloadTask downloadTask2 = new DownloadTask(ftpFiles.get(5),ftpClient,0,null, 0,"/Users/byron/Desktop/FtpDownload/");
        TaskService ts = new TaskService();

        ts.addDownloadTask(downloadTask);
        ts.addDownloadTask(downloadTask2);

//        ts.startDownloadTask(downloadTask);
//        ts.startDownloadTask(downloadTask2);

        //ts.show();
        TaskService.SerializeTaskService(ts,"/Users/byron/Desktop/testSer.txt");
        TaskService ts2 = TaskService.DeSerializeTaskService("/Users/byron/Desktop/testSer.txt");
        ts2.show();

//        ts2.setFtpClientInTasks(ftpClient);
//        ts2.startDownloadTask(downloadTask);
//        ts2.startDownloadTask(downloadTask2);

        DownloadTask downloadTask3 = new DownloadTask(ftpFiles.get(5),ftpClient,0,null, 0,"/Users/byron/Desktop/FtpDownload/");
        ts.addDownloadTask(downloadTask2);

        TaskService.SerializeTaskService(ts,"/Users/byron/Desktop/testSer.txt");
        TaskService ts3 = TaskService.DeSerializeTaskService("/Users/byron/Desktop/testSer.txt");
        ts3.show();
    }
}
