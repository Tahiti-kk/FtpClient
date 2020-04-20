package ftp;

import java.io.*;
import java.util.ArrayList;

/**
 * @author : Byron0648
 * @date : 2020-03-16 13:53
 * @description: TODO
 */
public class DownloadTask implements Runnable, Serializable {

    private  FtpFile ftpFile;

    private String localDir;

    private boolean begin=false;//判断何时开始
    private boolean exit=true;//判断何时退出

    //不序列化ftpClient
    private transient FtpClient ftpClient;

    private long fileSize;

    // 整个文件夹的下载大小
    private long alreadyDownSize;

    // 暂停时当前文件路径
    private String curFilePath;

    // 暂停时当前文件下载大小
    private long curDownSize;

    public DownloadTask() {
    }

    //这个好像没用了，用下面那个构造函数，因为ftpclient不能序列化，要用set注入
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
        long curSize=0;
        boolean append=false;
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
                //???
                //append=true;
                setBegin(false);
                setExit(false);
            }

            String response = ftpClient.sendCommand("RETR "+fileName);
            if(!response.startsWith("150")){
                throw new Exception("file "+fileName+" download fail!");
            }
            //新建文件 TODO看后面考不考虑文件替换
            File file = new File(localPath + "\\" + fileName);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    return;
                }
            }
            FileOutputStream fos = new FileOutputStream(file,true);
            BufferedOutputStream out = new BufferedOutputStream(fos);
            BufferedInputStream input = new BufferedInputStream(ftpClient.getDataSocket().getInputStream());

//            if(append=true){
//
//            }
            byte[] b = new byte[1024];
            int bytesRead = 0;

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
            setCurFilePath(ftpClient.getCurrentDir()+"/"+fileName);
            System.out.println("停止位置"+curFilePath);
            //保存 filePath 和 curSize， 上面还要加filePath参数
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
            System.out.println("???");
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
            // 如果没有下载完，保存列表
            if(getAlreadyDownSize()!=getFileSize()){
                //TODO 保存列表
            }
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