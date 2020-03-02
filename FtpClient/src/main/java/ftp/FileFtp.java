package ftp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.Constant;

import java.io.*;
import java.net.Socket;

/**
 * @author JerryLee
 * @date 2020/3/2
 */
public class FileFtp {

    private Socket socket;
    private Logger logger;

    public FileFtp(){
        socket = null;
        logger = LogManager.getLogger("FtpClientLogger");
    }

    /**
     * 获取socket
     * @return socket实例
     */
    public Socket getSocket(){
        if(socket == null) {
            if(connect()){
                return socket;
            } else{
                logger.error("获取socket失败");
                return null;
            }
        }
        return socket;
    }

    /**
     * 连接服务器
     * @return 是否连接成功
     */
    private boolean connect() {
        try{
            // create socket for client
            socket = new Socket(Constant.LOCAL_IP, Constant.SOCKET_PORT);
            return true;
        } catch (Exception e) {
            logger.error("创建socket失败", e);
            return false;
        }
    }

    /**
     * 上传文件
     * @param filePath 传输文件路径
     */
    public void uploadFile(String filePath){
        try {

            if(!connect()) {
                return;
            }

            File file = new File(filePath);
            logger.info("文件名为: " + file.getName() + ",文件路径为: " + filePath);
            System.out.println("文件名为: " + file.getName() + ",文件路径为: " + filePath);

            // create IO stream
            DataInputStream is = new DataInputStream(new FileInputStream(filePath));
            DataOutputStream os  = new DataOutputStream(socket.getOutputStream());

            // 将文件名和文件长度传入输出流
            os.writeUTF(file.getName());
            os.flush();
            os.writeLong((long) file.length());
            os.flush();

            // define buffer size
            int bufferSize = 8192;
            byte[] buf = new byte[bufferSize];

            while(true) {
                int read = 0;
                if (is != null) {
                    read = is.read(buf);
                }

                if(read == -1) {
                    break;
                }
                os.write(buf,0,read);
            }
            os.flush();
            is.close();
            os.close();
            // 关闭socket，不然客户端会等待server的数据过来，直到socket超时，导致数据不完整
            socket.close();
            logger.info("文件传输完成");
            System.out.println("文件传输完成");
        }catch (Exception e) {
            logger.error("文件传输错误", e);
            System.out.println(e.getMessage());
        }

    }
}