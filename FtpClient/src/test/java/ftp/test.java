package ftp;

import controller.MainPage;
import org.junit.Test;
import util.Constant;

import java.io.File;
import java.net.Socket;
import java.util.ArrayList;

import static ftp.FtpFile.parseFile;

/**
 * @author JerryLee
 * @date 2020/3/4
 */
public class test {

    private FtpClient ftpClient;

    @Test
    public void testMainPage() throws Exception {
        MainPage mainPage = new MainPage();
        try{
            mainPage.init();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

    @Test
    public void testLoginPmd() throws Exception{
        ftpClient = new FtpClient("116.62.170.221",21,"ftp511","admin123456");
        ftpClient.login();
        String pwd = ftpClient.getCurrentDir();
        ftpClient.getAllFiles();
        //ftpClient.makeDirectory(pwd+"/zqr");
        //ftpClient.delDirectory(pwd+"/zqr");
        //File file = new File("/Users/byron/Desktop/aaab.txt");
        //ftpClient.upload(file);
        ftpClient.quit();
        ftpClient = null;
    }

    @Test
    public void test1() throws Exception{
        ftpClient = new FtpClient("116.62.170.221",21,"ftp511","admin123456");
        ftpClient.login();
        String curPath = ftpClient.getCurrentDir();
        ftpClient.makeDirectory(curPath+"/newFolder");
        ftpClient.cwd(curPath+"/newFolder");
        curPath = ftpClient.getCurrentDir();
        ftpClient.cwd("..");
        curPath = ftpClient.getCurrentDir();
        ftpClient.delDirectory(curPath+"/newFolder");
        ftpClient.quit();
        ftpClient = null;

    }

    @Test
    public void test2() throws Exception{
        ftpClient = new FtpClient("116.62.170.221",21,"ftp511","admin123456");
        ftpClient.login();
        ftpClient.getAllFiles();
        ftpClient.quit();
        ftpClient = null;
    }

    @Test
    public void test3() throws Exception{
        ftpClient = new FtpClient("116.62.170.221",21,"ftp511","admin123456");
        ftpClient.login();
        File file = new File("/Users/byron/Desktop/testfile.txt");
        ftpClient.upload(file);
        File dir = new File("/Users/byron/Desktop/folder");
        ftpClient.upload(dir);
        ftpClient.quit();
        ftpClient = null;
    }

    @Test
    public void test4() throws Exception{
        ftpClient = new FtpClient("116.62.170.221",21,"ftp511","admin123456");
        ftpClient.login();
        ArrayList<FtpFile> ftpFiles = ftpClient.getAllFiles();
        ftpClient.download(ftpFiles.get(0),"/Users/byron/Desktop");
        ftpClient.download(ftpFiles.get(8),"/Users/byron/Desktop");
        ftpClient.quit();
        ftpClient = null;
    }

    @Test
    public void test5() throws Exception{
        ftpClient = new FtpClient("116.62.170.221",21,"ftp511","admin123456");
        ftpClient.login();
        ArrayList<FtpFile> ftpFiles = ftpClient.getAllFiles();
        ftpClient.delete(ftpFiles.get(0));
        ftpClient.quit();
        ftpClient = null;
    }
}
