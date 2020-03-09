package controller;

import ftp.FtpClient;
import util.Constant;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author JerryLee
 * @date 2020/3/5
 */
public class MainPage {

    private FtpClient ftpClient;


    public boolean init() throws Exception {
        try{
            ftpClient = new FtpClient("116.62.170.221",21,"ftp511","admin123456");
            ftpClient.login();
            String pwd = ftpClient.getCurrentDir();
            //ftpClient.makeDirectory(pwd+"/zqr");
            //ftpClient.delDirectory(pwd+"/zqr");
            //File file = new File("/Users/byron/Desktop/aaab.txt");
            //ftpClient.upload(file);
            ftpClient.quit();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        return true;
    }
}
