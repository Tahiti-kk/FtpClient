package ftp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * @author : Byron0648
 * @date : 2020-03-16 13:53
 * @description: TODO
 */
public class DownloadTask implements Runnable {

    private FtpFile ftpFile;

    private String localDir;

    private boolean begin=false;//判断何时开始
    private boolean exit=true;//判断何时退出

    private FtpClient ftpClient;

    private long fileSize;

    private long alreadyDownSize;

    private String curFilePath;
    private long curDownSize;

    public DownloadTask(FtpFile ftpFile, FtpClient ftpClient, long alreadyDownSize, String curFilePath, long curDownSize, String localDir) throws Exception {
        this.ftpFile = ftpFile;
        this.ftpClient = ftpClient;
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

    //计算文件大小，单位为byte
    public long calcFtpFileSize(FtpFile file) throws Exception {
        if(file.isDirectory()){
            long fileSize=0;
            ArrayList<FtpFile> fileList = ftpClient.getAllFiles();
            for(FtpFile f:fileList){
                fileSize+=calcFtpFileSize(f);
            }
            return fileSize;
        }else{
            return file.getFileSize();
        }
    }

    //下载单个文件
    public void downloadFile(String fileName,String localPath) throws Exception {
        ftpClient.dataConnect();
        long curSize=0;
        try{
            String response = ftpClient.sendCommand("RETR "+fileName);
            if(!response.startsWith("150")){
                throw new Exception("file "+fileName+" download fail!");
            }
            //新建文件 TODO看后面考不考虑文件替换
            File file = new File(localPath + "/" + fileName);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    return;
                }
            }
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream out = new BufferedOutputStream(fos);
            BufferedInputStream input = new BufferedInputStream(ftpClient.getDataSocket().getInputStream());

            byte[] b = new byte[1024];
            int bytesRead = 0;
            //若为开始位置，则跳过之前的进度，设置begin为假，exit为假
            if(isBegin()){
                input.skip(getCurDownSize());
                setBegin(false);
                setExit(false);
            }

            while ((bytesRead = input.read(b, 0, b.length))!=-1&&!isExit()) {
                out.write(b, 0, bytesRead);
                curSize+=bytesRead;
                setAlreadyDownSize(alreadyDownSize+bytesRead);
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
            //保存 filePath 和 curSize， 上面还要加filePath参数
        }
    }

    //下载文件夹
    public void downloadDir(FtpFile file,String localPath) throws Exception{
        String dirPath = localPath+"/"+file.getFileName();
        File dir = new File(dirPath);
        //新建文件夹
        if(!dir.exists()){
            if(!dir.mkdir()){
                return;
            }
        }
        ftpClient.cwd(ftpClient.getCurrentDir()+"/"+file.getFileName());
        ArrayList<FtpFile> fileList = ftpClient.getAllFiles();
        for (FtpFile f:fileList){
            //找起始点
            if(ftpClient.getCurrentDir()+"/"+f.getFileName()==curFilePath){
                setBegin(true);
            }
            if(isBegin()||!isExit()){
                download(f,dirPath);
            }
        }
    }

    //下载函数，localDir为目标地址
    public void download(FtpFile file,String localDir) throws Exception {
        if(file.isDirectory()){
            downloadDir(file,localDir);
        }else{
            if(ftpClient.getCurrentDir()+"/"+file.getFileName()==curFilePath){
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
            download(ftpFile,localDir);
            if(getAlreadyDownSize()!=getFileSize()){
                //TODO 保存列表
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}