package ftp;
/**
 * 戢启瑞：下载命令的实现，实现文件、文件夹的下载、下载的起止以及下载进度的获取
 * 李军邑：补充断点续下载的功能
 */

import java.io.*;
import java.util.ArrayList;

/**
 * @author : Byron0648
 * @date : 2020-03-16 13:53
 * @description: 下载任务
 */
public class DownloadTask implements Runnable, Serializable {

    private  FtpFile ftpFile;

    private String localDir;

    private boolean begin=false;//判断何时开始
    private boolean exit=true;//判断何时退出

    // 不序列化ftpClient
    private transient FtpClient ftpClient;

    // 文件大小
    private long fileSize;

    // 整个文件夹的下载大小
    private long alreadyDownSize;

    // 暂停时当前文件路径
    private String curFilePath;

    // 暂停时当前文件下载大小
    private long curDownSize;

    public DownloadTask() {
    }

    public DownloadTask(FtpFile ftpFile, FtpClient ftpClient, long alreadyDownSize, String curFilePath, long curDownSize, String localDir) throws Exception {
        this.ftpFile = ftpFile;
        this.ftpClient = new FtpClient(ftpClient);
        this.ftpClient.login();
        this.fileSize = calcFtpFileSize(ftpFile);
        this.alreadyDownSize = alreadyDownSize;
        this.curFilePath = curFilePath;
        this.curDownSize = curDownSize;
        this.localDir = localDir;
        if(alreadyDownSize==0){
            setExit(false);
        }
    }

    public DownloadTask(FtpFile ftpFile, long alreadyDownSize, String curFilePath, long curDownSize, String localDir) throws Exception {
        this.ftpFile = ftpFile;
        this.fileSize = calcFtpFileSize(ftpFile);
        this.alreadyDownSize = alreadyDownSize;
        this.curFilePath = curFilePath;
        this.curDownSize = curDownSize;
        this.localDir = localDir;
        if(alreadyDownSize==0){
            setExit(false);
        }
    }


    public boolean isBegin() {
        return begin;
    }

    public void setBegin(boolean begin) {
        this.begin = begin;
    }

    public boolean isExit() {
        return exit;
    }

    public void setExit(boolean exit) {
        this.exit = exit;
    }

    public long getAlreadyDownSize() {
        return alreadyDownSize;
    }

    public void setAlreadyDownSize(long alreadyDownSize) {
        this.alreadyDownSize = alreadyDownSize;
    }

    public void setCurFilePath(String curFilePath) {
        this.curFilePath = curFilePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setCurDownSize(long curDownSize){ this.curDownSize = curDownSize; }

    public long getCurDownSize(){ return curDownSize;}

    public String getCurFilePath(){
        return curFilePath;
    }

    public String getDownFileName(){
        return ftpFile.getFileName();
    }
    //设置FtpClient
    public void setFtpClient(FtpClient ftpClient) throws Exception {
        this.ftpClient = new FtpClient(ftpClient);
        this.ftpClient.login();
    }

    //计算文件大小，单位为byte
    public long calcFtpFileSize(FtpFile file) throws Exception {
        if(file.isDirectory()){
            ftpClient.cwd(ftpClient.getCurrentDir()+"/"+file.getFileName());
            long fileSize=0;
            ArrayList<FtpFile> fileList = ftpClient.getAllFiles();
            for(FtpFile f:fileList){
                fileSize+=calcFtpFileSize(f);
            }
            ftpClient.cwd("..");
            return fileSize;
        }else{
            return file.getFileSize();
        }
    }

    //下载单个文件
    public void downloadFile(String fileName,String localPath) throws Exception {
        ftpClient.dataConnect();
        setCurFilePath(ftpClient.getCurrentDir()+"/"+fileName);
        long curSize=0;
        try{
            // 如果为下载中文件，则开始断点续传
            if(isBegin()) {
                // 使用BINARY模式传送文件；
                if(!ftpClient.sendCommand("TYPE I").startsWith("200")) {
                    throw new Exception("使用二进制模式失败!");
                }
                String response = ftpClient.sendCommand("REST "+getCurDownSize());
                if(!response.startsWith("350")){
                    throw new Exception("file "+fileName+" continue download fail!");
                }

                setBegin(false);
                setExit(false);
            }

            String response = ftpClient.sendCommand("RETR "+fileName);
            if(!response.startsWith("150")){
                throw new Exception("file "+fileName+" download fail!");
            }
            //新建文件
            File file = new File(localPath + "\\" + fileName);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    return;
                }
            }
            //这里要使用append方式不然断点下载会出错
            FileOutputStream fos = new FileOutputStream(file,true);
            BufferedOutputStream out = new BufferedOutputStream(fos);
            BufferedInputStream input = new BufferedInputStream(ftpClient.getDataSocket().getInputStream());


            byte[] b = new byte[1024];
            int bytesRead = 0;
            //读操作以及更新进度
            while ((bytesRead = input.read(b, 0, b.length))!=-1&&!isExit()) {
                out.write(b, 0, bytesRead);
                curSize+=bytesRead;
                setAlreadyDownSize(alreadyDownSize+bytesRead);
                System.out.println(Thread.currentThread().getName()+ ftpFile.getFileName()+" 进度百分比：" + (double)alreadyDownSize/(double)fileSize);
            }
            out.flush();
            out.close();
            input.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        ftpClient.getDataSocket().close();
        ftpClient.readLine();
        //如果在这停止 保存当前文件路径 和已下载字节数
        if(isExit()){
            setCurDownSize(curSize);
            System.out.println("停止位置"+curFilePath);
        }
    }

    // 下载文件夹
    public void downloadDir(FtpFile file,String localPath) throws Exception{
        String dirPath = localPath+"\\"+file.getFileName();
        System.out.println("下载文件夹到："+dirPath);
        File dir = new File(dirPath);
        //新建文件夹
        if(!dir.exists()){
            System.out.println("new dir");
            if(!dir.mkdir()){
                return;
            }
        }
        ftpClient.cwd(ftpClient.getCurrentDir()+"/"+file.getFileName());
        ArrayList<FtpFile> fileList = ftpClient.getAllFiles();
        for (FtpFile f:fileList){
            //找起始点 ---
            String path = ftpClient.getCurrentDir()+"/"+f.getFileName();
            System.out.println("ftp path="+path);
            System.out.println("当前路径"+curFilePath);
            if((ftpClient.getCurrentDir()+"/"+f.getFileName()).equals(curFilePath)){
                System.out.println("下载开始");
                setBegin(true);
            }
            if(isBegin()||!isExit()){
                download(f,dirPath);
            }
        }
        ftpClient.cwd("..");
    }

    //下载函数，localDir为目标地址
    public void download(FtpFile file,String localDir) throws Exception {
        if(file.isDirectory()){
            downloadDir(file,localDir);
        }else{
            if((ftpClient.getCurrentDir()+"/"+file.getFileName()).equals(curFilePath)){
                setBegin(true);
            }
            if(!isExit()||begin) {
                downloadFile(file.getFileName(),localDir);
            }
        }
    }

    @Override
    public void run() {
        try{
            File dir = new File(localDir);
            if(!dir.exists()){
                dir.mkdir();
            }
            download(ftpFile,localDir);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void pauseDownload() {
        setExit(true);
    }

    @Override
    public String toString() {
        return "DownloadTask{" +
                "ftpFile=" + ftpFile +
                ", localDir='" + localDir + '\'' +
                ", begin=" + begin +
                ", exit=" + exit +
                ", ftpClient=" + ftpClient +
                ", fileSize=" + fileSize +
                ", alreadyDownSize=" + alreadyDownSize +
                ", curFilePath='" + curFilePath + '\'' +
                ", curDownSize=" + curDownSize +
                '}';
    }
}