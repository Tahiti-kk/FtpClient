package ftp;

import java.io.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;


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

    public UploadTask(FtpClient ftpClient,File file,long alreadyUpSize,String curFilePath,long curUpSize){
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
            BufferedInputStream input = new BufferedInputStream(inputStream);
            BufferedOutputStream output = new BufferedOutputStream(ftpClient.getDataSocket().getOutputStream());
            byte[] buffer = new byte[1024];
            int bytesRead = 0;

            // 如果为上传中中文件，则开始断点续传
            if(isBegin()) {
                String response = ftpClient.sendCommand("APPE "+file.getName());
                if(!response.startsWith("150")){
                    throw new Exception("file "+file.getName()+" continue upload fail!");
                }
                input.skip(getCurUpSize());
                setBegin(false);
                setExit(false);
            } else {
                String response = ftpClient.sendCommand("STOR "+ file.getName());
                if(!response.startsWith("150")){
                    throw new Exception("not allowed to send the file" + file.getName());
                }
            }

//            DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
//            // 可以设置精确几位小数
//            df.setMaximumFractionDigits(2);
//            //模式 例如四舍五入
//            df.setRoundingMode(RoundingMode.HALF_UP);

            // 新建线程，每隔0.5秒更新进度条
//            new Thread() {
//                @Override
//                public void run() {
//                    // 程序未退出并且未上传完成
//                    while(!isExit()&&(alreadyUpSize!=fileSize)) {
//                        try {
//                            Thread.sleep(500);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        System.out.println("进度百分比：" + df.format(1.0*alreadyUpSize/fileSize*100) + "%");
//                    }
//                }
//            }.start();

            while((bytesRead=input.read(buffer))!=-1&&!isExit()){//判断何时停止
                output.write(buffer,0,bytesRead);
                curSize+=bytesRead;
                setAlreadyUpSize(alreadyUpSize+bytesRead);//更新已上传进度
            }
            output.flush();
            output.close();
            input.close();

        }catch(Exception e){
            e.printStackTrace();
        }
        ftpClient.getDataSocket().close();
        ftpClient.readLine();
        //如果在这停止 保存当前文件路径 和已下载字节数
        if(isExit()){
            setCurUpSize(curSize);
            setCurFilePath(file.getAbsolutePath());
            //保存 filePath 和 curSize， 上面还要加filePath参数
        }
        return file.length()==curSize;
    }

    // 上传文件夹
    public void uploadDir(File file) throws Exception{
        ftpClient.makeDirectory(ftpClient.getCurrentDir()+"/"+file.getName());
        ftpClient.cwd(ftpClient.getCurrentDir()+"/"+file.getName());
        File[] fileList = file.listFiles();
        for (File f:fileList){
            //找到开始位置
            if(f.getAbsolutePath()==curFilePath){
                setBegin(true);
            }
            if(isBegin()||!isExit()){
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

    public void pauseUpload() {
        setExit(true);
    }

}