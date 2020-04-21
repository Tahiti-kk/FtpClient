package test;

import ftp.FtpClient;
import ftp.UploadTask;
import javafx.scene.control.TextArea;
import util.CmdListener;
import util.InfoListener;

import java.awt.*;
import java.io.File;
import java.util.Scanner;

/**
 * @author JerryLee
 * @date 2020/4/12
 */
public class test {
    public static void main(String[] args) {
        try{
            TextArea textArea = new TextArea();
            CmdListener cmdListener = new CmdListener(textArea);
            InfoListener infoListener = new InfoListener(textArea);

            FtpClient ftpClient = new FtpClient("116.62.170.221",21,"ftp511","admin123456");

            ftpClient.addListener(cmdListener);
            ftpClient.addListener(infoListener);

            ftpClient.login();

            File file = new File("E:\\note\\大三下\\网络工程\\计算机网络7e.pdf");
            UploadTask uploadTask=new UploadTask(ftpClient,file,0,null,0);
            Thread thread = new Thread(uploadTask);
            thread.start();
            //uploadTask.run();

            Thread.sleep(2000);
            uploadTask.pauseUpload();
            System.out.println("pause");
            Thread.sleep(2000);
            //thread.start();
            uploadTask.run();
            System.out.println("continue");



//            Scanner scan = new Scanner(System.in);
//            while(true) {
//                String str = scan.next();
//                if(str.equals("quit")) {
//                    break;
//                }
//                ftpClient.sendCommand(str);
//            }

        }
        catch (Exception e) {
            System.out.println(e.getMessage());;
        }
    }
}
