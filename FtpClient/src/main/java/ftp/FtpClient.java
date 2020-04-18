package ftp;


import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static ftp.FtpFile.parseFile;

/**
 * cmd模拟器
 * @author JerryLee
 * @date 2020/3/3
 */
public class FtpClient{

    private String host = null;

    private String user = null;

    private String pass = null;

    private int port = -1;

    private Socket socket = null;

    private Socket dataSocket = null;

    private boolean isPortOrPasv = false;

    private boolean isConnected = false;

    /**
     * 输入流
     */
    private BufferedReader reader;

    /**
     * 输出流
     */
    private BufferedWriter writer;

    /**
     * 是否与Socket管道连接
     */

    /**
     * 日志打印接口
     */
    private List<LoggerListener> loggerListeners = new ArrayList<>();


    private String currentDir;

    public FtpClient(){}

    public FtpClient(String host,int port,String user,String pass){
        this.host = host;
        this.port = port;
        this.user = user;
        this.pass = pass;
    }

    public FtpClient(FtpClient ftpClient){
        this.host = ftpClient.host;
        this.port = ftpClient.port;
        this.user = ftpClient.user;
        this.pass = ftpClient.pass;
    }


    /**
     * 是否与Socket管道连接
     * @return whether or not
     */
    public boolean getConnected() {
        return isConnected;
    }

    /**
     * 设置与Socket管道的连接状态
     * @param isConnected 连接状态
     */
    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }


    public Socket getDataSocket() {
        return dataSocket;
    }

    /**
     * 登录
     * @return success or not
     */
    public boolean login() throws Exception {
        if(socket != null){
            socket.close();
            if(dataSocket != null){
                dataSocket.close();
            }
        }
        socket = new Socket(host,port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        String response = readLine();
        if(!response.startsWith("220")){
            throw new Exception("error exists after connect");
        }
        sendLine("USER " + user);
        response = readLine();
        if(!response.startsWith("331")){
            throw new Exception("error exists after send user");
        }
        sendLine("PASS " + pass);
        response = readLine();
        if(!response.startsWith("230")){
            throw new Exception("error exists after send pass");
        }
        loggerMsg("connect successfully!", "info");
        setConnected(true);
        return response.startsWith("230");



//        try{
//            if(!sendCommand("USER " + username).startsWith("331")) {
//                return false;
//            }
//            if(!sendCommand("PASS " + password).startsWith("230")) {
//                return false;
//            }
//            // 如果已在连接状态，则重新连接
//            if(getConnected()) {
//                this.isConnected = false;
//                loggerDisconnectedMsg();
//            }
//
//            // 连接成功
//            this.isConnected = true;
//            loggerConnectedMsg();
//
//        } catch (IOException e) {
//            loggerExceptionMsg(e.getMessage());
//            return false;
//        }
//        return true;
    }

    /**
     * 获得当前目录
     */
    public String getCurrentDir() throws Exception{
        String response = sendCommand("PWD");
        if(response.startsWith("257")){
            currentDir = response.substring(response.indexOf('\"') + 1, response.lastIndexOf('\"'));
        }else{
            throw new Exception("获取当前目录失败");
            //TODO log?
        }
        return currentDir;
    }

    //更改工作目录
    public void cwd(String dir) throws Exception{
        String response =  sendCommand("CWD " + dir);
        if(!response.startsWith("250")){
            throw new Exception("No such file or factory");
        }
        if (dir.equals("..") && !getCurrentDir().equals('/')) {
            currentDir = currentDir.substring(0, currentDir.lastIndexOf('/'));
        } else if (!dir.equals("..")) {
            currentDir += "/" + dir;
        }
    }

    // 文件重命名
    public boolean rename(String srcFile,String destFile) throws Exception{
        String response = sendCommand("REFR "+srcFile);
        if(!response.startsWith("350")){
            return false;
        }
        response = sendCommand("RNFO "+destFile);
        return response.startsWith("250");
    }

    //新建目录
    public boolean makeDirectory(String dirName) throws Exception {
        String response = sendCommand("MKD "+ dirName);
        return response.startsWith("257");
    }

    //删除目录（空）TODO
    public boolean delDirectory(String dirName) throws Exception{
        String response = sendCommand("RMD "+dirName);
        return response.startsWith("250");
    }

    /**
     * Todo 删除文件
     * @return success or not
     */
    public boolean deleteFile(String fileName) throws Exception {
        String response = sendCommand("DELE " + fileName);
        return response.startsWith("250");
    }

    // 删除目录或文件
    public boolean delete(FtpFile file) throws Exception{
        //目录
        if(file.isDirectory()){
            cwd(getCurrentDir() + "/" + file.getFileName());
            ArrayList<FtpFile> fileList = getAllFiles();
            for (FtpFile ftpFile:fileList) {
                delete(ftpFile);
            }
            cwd("..");
            delDirectory(getCurrentDir()+"/"+file.getFileName());
        }
        //文件
        else {
            deleteFile(getCurrentDir()+"/"+file.getFileName());
        }
        return true;//TODO
    }

    //退出连接
    public boolean quit() throws Exception{
        String response = sendCommand("QUIT");
        return response.startsWith("221");
    }

    //数据连接
    public void dataConnect() throws Exception{
        if(isPortOrPasv){
            connectPORT();
        }else{
            connectPASV();
        }
    }

    //主动模式 TODO
    public void connectPORT() throws Exception{

    }

    //被动模式
    public void connectPASV() throws Exception{
        String response = sendCommand("PASV");
        if(!response.startsWith("227")){
            loggerExceptionMsg("Exception: error exists after send pasv","info");
            throw new Exception("error exists after send pasv");
        }
        String message = response.substring(response.indexOf('(')+1,response.indexOf(')'));
        String[] split = message.split(",");
        String ip = split[0]+"."+split[1]+"."+split[2]+"."+split[3];
        int port = (Integer.parseInt(split[4])<<8)+Integer.parseInt(split[5]);
        loggerMsg("ip:" + ip, "info");
        loggerMsg("port:" + port, "info");
        dataSocket = new Socket(ip,port);
    }

    /**
     * 获得服务器该路径下的全部文件
     * @return 文件列表
     */
    public ArrayList<FtpFile> getAllFiles() throws Exception {
        ArrayList<FtpFile> list = new ArrayList<>();

        dataConnect();
        // 如果未获得正确socket连接，则返回空列表
        if(dataSocket == null) {
            loggerExceptionMsg("Exception: 数据连接错误","info");
            throw new Exception("数据连接错误");
            //TODO
        }
        try {
            BufferedReader bReader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
            if(!sendCommand("LIST").startsWith("150")) {
                return list;
            }

            String line = bReader.readLine();
            while (line != null) {
                loggerMsg(line, "cmd");
                FtpFile file = parseFile(line, this.currentDir);
                list.add(file);
                line = bReader.readLine();
            }

            bReader.close();
            dataSocket.close();
            readLine();
            //loggerReceiveMsg(this.in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }


    /**
     * 从服务器上下载文件
     * @return InputStream, but return null if the file is not found
     */
//    public boolean downloadFile(String fileName,String localPath) throws Exception {
//        dataConnect();
//
//        String response = sendCommand("RETR "+fileName);
//        if(!response.startsWith("150")){
//            loggerExceptionMsg("Exception: file "+fileName+" download fail!","info");
//            throw new Exception("file "+fileName+" download fail!");
//        }
//
//        byte[] b = new byte[1024];
//        int len = -1;
//        // Here we may overwrite existing file.
//        File file = new File(localPath + "/" + fileName);
//        if (!file.exists()) {// This file does not exist
//            if (!file.createNewFile()) {// And create it unsuccessfully
//                // means this file can't be downloaded here
//                return false;
//            }
//        }
//        FileOutputStream fos = new FileOutputStream(file);
//        BufferedOutputStream out = new BufferedOutputStream(fos);
//        BufferedInputStream input = new BufferedInputStream(dataSocket.getInputStream());
//        while (-1 != (len = input.read(b, 0, b.length))) {
//            out.write(b, 0, len);
//        }
//        out.flush();
//        out.close();
//        input.close();
//        dataSocket.close();
//        response = readLine();
//        return response.startsWith("226");
//    }

//    public void downloadDir(FtpFile file,String localPath,String fileName) throws Exception{
//        String dirPath = localPath+"/"+fileName;
//        File dir = new File(dirPath);
//        if(!dir.exists()){
//            if(!dir.mkdir()){
//                return;
//            }
//        }
//        cwd(getCurrentDir()+"/"+file.getFileName());
//        ArrayList<FtpFile> fileList = getAllFiles();
//        for (FtpFile f:fileList){
//            downloadFile(f.getFileName(),dirPath);
//        }
//    }

//    public void download(FtpFile file,String localDir) throws Exception {
//        if(file.isDirectory()){
//            downloadDir(file,localDir,file.getFileName());
//        }else{
//            downloadFile(file.getFileName(),localDir);
//        }
//    }

//    //TODO
//    public InputStream downloadFile(FtpFile file){
//        InputStream in = null;
//        // file为文件夹
//        if(file.getType() == 1) {
//            return null;
//        }
//        // 被动模式
//        Socket socket = passiveMode();
//        if(socket == null) {
//            loggerExceptionMsg("连接失败");
//            return null;
//        }
//        try {
//            String str = sendCommand("RETR " + file.getFilePath());
//            if(str.startsWith("125") || str.startsWith("150") || str.startsWith("350")) {
//                in = socket.getInputStream();
//            }
//        } catch (IOException e) {
//            loggerExceptionMsg(e.getMessage());
//            e.printStackTrace();
//        }
//        return in;
//    }

    public String readLine() throws IOException {
        String line = reader.readLine();
        System.out.println(line);
        loggerMsg(line, "cmd");
        return line;
    }

    public void sendLine(String line) throws IOException{
        loggerMsg("cmd> "+line, "cmd");
        System.out.println(line);
        writer.write(line + "\r\n");
        writer.flush();
    }

    /**
     * 向FTP服务器发送请求
     * 注意：与cmd交互需要单线程
     * @param cmd command
     * @return reply
     * @throws IOException 抛出异常说明未与socket管道连接上
     */
    public String sendCommand(String cmd) throws Exception {
        if(!isConnected){
            loggerExceptionMsg("Exception: not connect to the server!","info");
            throw new Exception("not connect to the server!");
        }
        // 向管道中输入指令
        sendLine(cmd);
        String response = readLine();
        return response;
    }

    /**
     * 增加打印日志接口
     * @param loggerListener 日志接口
     */
    public void addListener(LoggerListener loggerListener) {
        this.loggerListeners.add(loggerListener);
    }

    /**
     * 删除打印日志接口
     * @param loggerListener 日志接口
     * @return find and delete or not
     */
    public boolean removeListener(LoggerListener loggerListener){
        return this.loggerListeners.remove(loggerListener);
    }

    /**
     * 接口打印接收的msg
     * @param msg 接收的msg
     */
    private void loggerMsg(String msg, String type) {
        for(LoggerListener listener : this.loggerListeners) {
            listener.logMsg(msg, type);
        }
    }

    /**
     * 接口打印异常的msg
     * @param msg 异常的msg
     */
    private void loggerExceptionMsg(String msg, String type) {
        for(LoggerListener listener : this.loggerListeners) {
            listener.exceptionMsg(msg, type);
        }
    }


}
