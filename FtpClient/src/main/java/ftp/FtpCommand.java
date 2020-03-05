package ftp;

import util.Constant;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static ftp.FtpFile.parseFile;

/**
 * cmd模拟器
 * @author JerryLee
 * @date 2020/3/3
 */
public class FtpCommand {

    /**
     * 输入流
     */
    private BufferedReader in;

    /**
     * 输出流
     */
    private BufferedWriter out;

    /**
     * 是否与Socket管道连接
     */
    private boolean connected;

    /**
     * 日志打印接口
     */
    private List<LoggerListener> loggerListeners = new ArrayList<>();

    private String currentDir;

    public FtpCommand(BufferedReader in, BufferedWriter out) {
        this.in = in;
        this.out = out;
    }

    /**
     * 设置FTP的输入流
     * 设置完毕后需要清除前几句服务器应答的话
     * @param in InputStream
     * @return success or not
     */
    public boolean setInputStream(InputStream in) {
        try{
            if(this.in != null) {
                this.in.close();
            }
            this.in = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            loggerExceptionMsg(e.getMessage());
            System.out.println(e.getMessage());
            return false;
        }
    }

    /**
     * 设置FTP的输出流
     * @param out OutputStream
     * @return success or not
     */
    public boolean setOutputStream(OutputStream out) {
        try{
            if(this.out != null) {
                this.out.close();
            }
            this.out = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            loggerExceptionMsg(e.getMessage());
            System.out.println(e.getMessage());
            return false;
        }
    }

    /**
     * 是否与Socket管道连接
     * @return whether or not
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * 设置与Socket管道的连接状态
     * @param connected 连接状态
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
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
    private void loggerReceiveMsg(String msg) {
        for(LoggerListener listener : this.loggerListeners) {
            listener.receiveMsg(msg);
        }
    }

    /**
     * 接口打印发送的msg
     * @param msg 发送的msg
     */
    private void loggerSendMsg(String msg) {
        for(LoggerListener listener : this.loggerListeners) {
            listener.sendMsg(msg);
        }
    }

    /**
     * 接口打印异常的msg
     * @param msg 异常的msg
     */
    private void loggerExceptionMsg(String msg) {
        for(LoggerListener listener : this.loggerListeners) {
            listener.exceptionMsg(msg);
        }
    }

    /**
     * 通知接口已连接
     */
    private void loggerConnectedMsg() {
        for(LoggerListener listener : this.loggerListeners) {
            listener.connected();
        }
    }

    /**
     * 通知接口断开连接
     */
    private void loggerDisconnectedMsg() {
        for(LoggerListener listener : this.loggerListeners) {
            listener.disconnected();
        }
    }

    /**
     * 登录
     * @param username username
     * @param password password
     * @return success or not
     */
    public boolean login(String username, String password) {
        try{
            if(!sendCommand("USER " + username).startsWith("331 ")) {
                return false;
            }
            if(!sendCommand("PASS " + password).startsWith("230 ")) {
                return false;
            }
            // 如果已在连接状态，则重新连接
            if(isConnected()) {
                this.connected = false;
                loggerDisconnectedMsg();
            }

            // 连接成功
            this.connected = true;
            loggerConnectedMsg();

        } catch (IOException e) {
            loggerExceptionMsg(e.getMessage());
            return false;
        }
        return true;
    }

    public FileExplorer getFileExplorer() {
        // 得到当前的文件路径
        getCurrentDir();
        if(this.currentDir == null) {
            return null;
        } else {
            return new FileExplorer(this.currentDir, getAllFiles());
        }
    }

    /**
     * 获得当前目录
     */
    private void getCurrentDir() {
        try {
            String str = sendCommand("PWD");
            if(str.startsWith("257 ")) {
                String[] strList = str.split("\"");
                this.currentDir = strList[1];
            } else {
                System.out.println("获取当前目录失败");
            }
        } catch (IOException e) {
            System.out.println("获取当前目录失败");
            e.printStackTrace();
        }
    }

    /**
     * 被动模式
     * @return socket
     */
    private Socket passiveMode() {
        Socket socket = null;
        try {
            String str = sendCommand("PASV");
            if(str.startsWith("227")) {
                String[] tab = str.substring(str.indexOf("(")+1, str.indexOf(")")).split(",");
                // Server IP
                String host = tab[0] + "." + tab[1] + "." + tab[2] + "." + tab[3];
                // port
                int port = (Integer.parseInt(tab[4])<<8) + Integer.parseInt(tab[5]);

                socket = new Socket(host, port);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return socket;
    }

    /**
     * 获得服务器该路径下的全部文件
     * @return 文件列表
     */
    private ArrayList<FtpFile> getAllFiles() {
        ArrayList<FtpFile> list = new ArrayList<>();

        Socket socket = passiveMode();
        // 如果未获得正确socket连接，则返回空列表
        if(socket == null) {
            return list;
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            if(!sendCommand("LIST").startsWith("150")) {
                return list;
            }

            String line = reader.readLine();
            while (line != null) {
                FtpFile file = parseFile(line, this.currentDir);
                list.add(file);
                System.out.println(file.toString());
                line = reader.readLine();
            }

            reader.close();
            socket.close();
            loggerReceiveMsg(this.in.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }




    /**
     * 从服务器上下载文件
     * @param file the file
     * @return InputStream, but return null if the file is not found
     */
    public InputStream downloadFile(FtpFile file){
        InputStream in = null;
        // file为文件夹
        if(file.getType() == 1) {
            return null;
        }
        // 被动模式
        Socket socket = passiveMode();
        if(socket == null) {
            loggerExceptionMsg("连接失败");
            return null;
        }
        try {
            String str = sendCommand("RETR " + file.getFilePath());
            if(str.startsWith("125") || str.startsWith("150") || str.startsWith("350")) {
                in = socket.getInputStream();
            }
        } catch (IOException e) {
            loggerExceptionMsg(e.getMessage());
            e.printStackTrace();
        }
        return in;
    }




    /**
     * Todo 删除文件
     * @param file the file
     * @return success or not
     */
    public boolean deleteFile(FtpFile file){
        return true;
    }

    /**
     * 向FTP服务器发送请求
     * 注意：与cmd交互需要单线程
     * @param cmd command
     * @return reply
     * @throws IOException 抛出异常说明未与socket管道连接上
     */
    public String sendCommand(String cmd) throws IOException {
        // 清空cmd中发送的消息
        while(this.in.ready()) {
            loggerReceiveMsg(this.in.readLine());
        }
        // 向管道中输入指令
        this.out.write(new String(cmd.getBytes(), StandardCharsets.UTF_8) + Constant.NEW_LINE);
        this.out.flush();
        loggerSendMsg(cmd);

        String str = null;
        try{
            str = this.in.readLine();

            // 检测未连接至socket或者未登录
            boolean isDisconnected = isConnected() && (str == null);
            boolean isLogin = isConnected() && (str != null) && str.startsWith("530");
            if(isDisconnected || isLogin) {
                this.connected = false;
                loggerDisconnectedMsg();
            }
            // 成功获得返回指令
            loggerReceiveMsg(str);
        } catch (IOException e) {
            loggerExceptionMsg(e.getMessage());
            // 如果显示是连接状态，则通知连接已断开
            if(isConnected()) {
                this.connected = false;
                loggerDisconnectedMsg();
            }
            throw e;
        }
        return str==null ? "" : str;
    }


}
