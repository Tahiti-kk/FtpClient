package ftp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.Constant;

import java.io.*;
import java.net.Socket;

/**
 * 该类只关注于文件传输过程中过程中流的操作
 * 可以更方便的进行多线程和断点传输的扩展
 * 在使用前需建立socket连接并定义输入流和输出流
 * 使用DataInputStream进行传输
 * 可定义完之后加入线程池
 * Todo 日志打印
 * @author JerryLee
 * @date 2020/3/5
 */
public class TransformTask implements Runnable {

    private DataInputStream is;
    private DataOutputStream os;
    private FtpFile ftpFile;

    public TransformTask(InputStream in, OutputStream out, FtpFile ftpFile) {
        this.is = new DataInputStream(in);
        this.os  = new DataOutputStream(out);
        this.ftpFile = ftpFile;
    }

    @Override
    public void run(){
        try {
            // 将文件参数传入输出流
            os.writeUTF(ftpFile.getFileName());
            os.flush();
            os.writeLong((long) ftpFile.getFileSize());
            os.flush();

            // define buffer size
            int bufferSize = Constant.BUFFER_SIZE;
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
            // 提示传输文件已完成
            // socket.close();
            // logger.info("文件传输完成");
            // System.out.println("文件传输完成");
        }catch (Exception e) {
            // logger.error("文件传输错误", e);
            System.out.println(e.getMessage());
        }
    }
}
