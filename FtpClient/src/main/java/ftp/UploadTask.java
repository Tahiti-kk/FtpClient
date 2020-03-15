package ftp;

import java.io.*;


/**
 * @author : Byron0648
 * @date : 2020-03-12 20:39
 * @description: TODO
 */
//TODO 判断上传的文件是不是同一个
public class UploadTask implements Runnable {

    private File uploadFile;

    private boolean begin=false;//判断何时开始
    private boolean exit=true;//判断何时退出

    private long fileSize;
    private long alreadyUpSize;

    private String curFilePath;
    private long curUpSize;

    private FtpClient ftpClient;

    //计算文件大小，单位为byte
    public long calcFileSize(File file){
        if(file.isDirectory()){
            long fileSize=0;
            File[] fileList = file.listFiles();
            for(File f:fileList){
                fileSize+=calcFileSize(f);
            }
            return fileSize;
        }else{
            return file.length();
        }
    }

    UploadTask(FtpClient ftpClient,File file,long alreadyUpSize,String curFilePath,long curUpSize){
       fileSize = calcFileSize(file);
       this.alreadyUpSize = alreadyUpSize;
       uploadFile = file;
       this.ftpClient = ftpClient;
       this.curFilePath = curFilePath;
       this.curUpSize = curUpSize;
       if(alreadyUpSize==0){
           setExit(false);
       }
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getAlreadyUpSize() {
        return alreadyUpSize;
    }

    public void setAlreadyUpSize(long alreadyUpSize) {
        this.alreadyUpSize = alreadyUpSize;
    }

    public boolean isExit() {
        return exit;
    }

    public void setExit(boolean stop) {
        this.exit = getAlreadyUpSize()==getFileSize()||stop;
    }

    public boolean isBegin() {
        return begin;
    }

    public void setBegin(boolean begin) {
        this.begin = begin;
    }

    public long getCurUpSize() {
        return curUpSize;
    }

    public void setCurUpSize(long curUpSize) {
        this.curUpSize = curUpSize;
    }

    public void setCurFilePath(String curFilePath) {
        this.curFilePath = curFilePath;
    }

    //上传文件
    public void upload(File file) throws Exception{
        if (file.isDirectory()) {
            uploadDir(file);
        }else {
            if(file.getAbsolutePath()==curFilePath){
                setBegin(true);
            }
            if(!isExit()||begin) {
                uploadFile(new FileInputStream(file), file);
            }
        }

    }

    //上传一个文件
    public boolean uploadFile(InputStream inputStream,File file) throws Exception {
        ftpClient.dataConnect();
        long curSize=0;
        try{
            String response = ftpClient.sendCommand("STOR "+ file.getName());
            if(!response.startsWith("150")){
                throw new Exception("not allowed to send the file" + file.getName());
            }
            BufferedInputStream input = new BufferedInputStream(inputStream);
            BufferedOutputStream output = new BufferedOutputStream(ftpClient.getDataSocket().getOutputStream());
            byte[] buffer = new byte[1024];
            int bytesRead = 0;
            //若为开始位置，则跳过之前的进度，设置begin为假，exit为假
            if(begin){
                input.skip(getCurUpSize());
                setBegin(false);
                setExit(false);
            }

            while((bytesRead=input.read(buffer))!=-1&&!isExit()){//判断何时停止
                output.write(buffer,0,bytesRead);
                curSize+=bytesRead;
                setAlreadyUpSize(alreadyUpSize+bytesRead);//更新已上传进度
            }
            output.flush();
            output.close();
            input.close();
            ftpClient.getDataSocket().close();
            ftpClient.readLine();
        }catch(Exception e){
            e.printStackTrace();
        }
        //如果在这停止 保存当前文件路径 和已下载字节数
        if(isExit()){
            setCurUpSize(curSize);
            setCurFilePath(file.getAbsolutePath());
            //保存 filePath 和 curSize， 上面还要加filePath参数
        }
        return file.length()==curSize;
    }

    //上传文件夹
    public void uploadDir(File file) throws Exception{
        ftpClient.makeDirectory(ftpClient.getCurrentDir()+"/"+file.getName());
        ftpClient.cwd(ftpClient.getCurrentDir()+"/"+file.getName());
        File[] fileList = file.listFiles();
        for (File f:fileList){
            //找到开始位置
            if(f.getAbsolutePath()==curFilePath){
                setBegin(true);
            }
            if(begin||!exit){
                upload(f);
            }
        }
        ftpClient.cwd("..");
    }

    //TODO
    @Override
    public void run(){
        try{
            upload(uploadFile);
            if(getAlreadyUpSize()!=getFileSize()){
                //TODO 保存列表
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }



}