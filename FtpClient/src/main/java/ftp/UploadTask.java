package ftp;

import java.io.File;

/**
 * @author : Byron0648
 * @date : 2020-03-12 20:39
 * @description: TODO
 */
public class UploadTask implements Runnable {

    private File uploadFile;


    private long fileSize;
    private long alreadyUpSize;

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

    UploadTask(File file){
       fileSize = calcFileSize(file);
       alreadyUpSize = 0;
       uploadFile = file;
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

    @Override
    public void run() {

    }


}