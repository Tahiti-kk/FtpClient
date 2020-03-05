package controller;

import ftp.FileExplorer;
import ftp.FtpCommand;
import util.Constant;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author JerryLee
 * @date 2020/3/5
 */
public class MainPage {

    private Socket socket;
    private FtpCommand ftpCommand;
    private FileExplorer fileExplorer;


    public boolean init() {
        try {
            // 创建Socket连接
            this.socket = new Socket(Constant.REMOTE_IP, Constant.SOCKET_PORT);

            // 创建与服务器的通讯
            // Todo 目前默认为被动模式
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
            this.ftpCommand = new FtpCommand(reader, writer);

            // 登录
            if(!ftpCommand.login(Constant.USERNAME, Constant.PASSWORD)) {
                System.out.println("登录失败");
                return false;
            }

            // 获取文件目录和文件列表
            this.fileExplorer = ftpCommand.getFileExplorer();
            return true;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
