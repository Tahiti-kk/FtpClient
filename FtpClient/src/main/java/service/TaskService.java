package service;

import ftp.DownloadTask;
import ftp.FtpClient;
import ftp.UploadTask;

import java.io.*;
import java.util.ArrayList;

//主要用于保存下载列表
public class TaskService implements Serializable {
    private ArrayList<DownloadTask> downloadTaskList = new ArrayList<>();
    private ArrayList<UploadTask> uploadTaskList = new ArrayList<>();

    public TaskService() {
    }

    public TaskService(ArrayList<DownloadTask> downloadTaskList, ArrayList<UploadTask> uploadTaskList) {
        this.downloadTaskList = downloadTaskList;
        this.uploadTaskList = uploadTaskList;
    }

    public ArrayList<DownloadTask> getDownloadTaskList() {
        return downloadTaskList;
    }

    public ArrayList<UploadTask> getUploadTaskList() {
        return uploadTaskList;
    }

    public void addDownloadTask(DownloadTask dt) throws Exception {
        System.out.println("添加下载任务");
        downloadTaskList.add(dt);
        //logger
    }

    public void delDownloadTask(DownloadTask dt){
        System.out.println("删除下载任务");
        downloadTaskList.remove(dt);
    }

    public void startDownloadTask(DownloadTask dt){
        dt.setExit(false);
        System.out.println("开始下载任务");
        Thread t = new Thread(dt);
        t.start();
    }

    public void stopDownloadTask(DownloadTask dt){
        System.out.println("暂停下载任务");
        dt.pauseDownload();
    }

    public void addUploadTask(UploadTask ut){
        System.out.println("添加上传任务");
        uploadTaskList.add(ut);
    }

    public void delUploadTask(UploadTask ut){
        System.out.println("删除上传任务");
        uploadTaskList.remove(ut);
    }

    public void startUploadTask(UploadTask ut){
        ut.setExit(false);
        System.out.println("开始上传任务");
        Thread t = new Thread(ut);
        t.start();
    }

    public void stopUploadTask(UploadTask ut){
        System.out.println("暂停上传任务");
        ut.pauseUpload();
    }

    //序列化
    public static void SerializeTaskService(TaskService ts,String filePath) throws IOException{
        ObjectOutputStream oo = new ObjectOutputStream(new FileOutputStream(new File(filePath)));
        oo.writeObject(ts);
        oo.close();
    }

    //反序列化
    public static TaskService DeSerializeTaskService(String filePath) throws IOException {
        TaskService taskService = null;
        try{
            ObjectInputStream oin = new ObjectInputStream(new FileInputStream(new File(filePath)));
            taskService = (TaskService)oin.readObject();
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
        return taskService;
    }

    public void show(){
        System.out.println("show");
        for (var d:downloadTaskList){
            System.out.println(d.toString());
        }
        for (var u:uploadTaskList){
            System.out.println(u.toString());
        }
    }

    //给Task注入ftpClient成员
    public void setFtpClientInTasks(FtpClient ftpClient) throws Exception {
        for (var dt:downloadTaskList){
            dt.setFtpClient(ftpClient);
        }
        for (var ut:uploadTaskList){
            ut.setFtpClient(ftpClient);
        }
    }
}
