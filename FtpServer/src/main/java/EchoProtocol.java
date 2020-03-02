import org.apache.logging.log4j.Logger;
import util.Constant;

import java.io.*;
import java.net.Socket;
import java.text.DecimalFormat;

/**
 * @author JerryLee
 * @date 2020/3/1
 */
public class EchoProtocol implements Runnable {

    private Socket clientSocket;
    private Logger logger;

    public EchoProtocol(Socket clientSocket, Logger logger) {
        this.clientSocket = clientSocket;
        this.logger = logger;
    }

    @Override
    public void run() {
        handleEchoClient(clientSocket, logger);
    }

    public static void handleEchoClient(Socket clientSocket, Logger logger) {
        try{

            DataInputStream inputStream = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));

            // file name and file length can be read from stream
            String fileName = inputStream.readUTF();
            String filePath = Constant.SERVER_DIR + fileName;
            long fileLen = inputStream.readLong();
            DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filePath)));

            System.out.println("开始接受文件: " + fileName);
            System.out.println("文件长度为: " + fileLen);

            // define buffer size
            int bufferSize = 8192;
            byte[] buf = new byte[bufferSize];
            long passedLen = 0;

            logger.info("开始接受文件: " + fileName);
            logger.info("文件长度为: " + fileLen);
            System.out.println("开始接受文件: " + fileName);
            System.out.println("文件长度为: " + fileLen);

            while (true) {
                int read = 0;
                if (inputStream != null) {
                    read = inputStream.read(buf);
                }
                passedLen += read;
                if (read == -1) {
                    break;
                }
                outputStream.write(buf, 0, read);
                outputStream.flush();
                // process percent
                DecimalFormat df = new DecimalFormat("0.0");
                String percent = df.format((float)passedLen / fileLen * 100);
                System.out.println("已传输: " + percent + "%");
            }
            outputStream.close();

        } catch (Exception e) {
            logger.warn("Exception in echo protocol", e);
        } finally {

            try{
                clientSocket.close();
            } catch (Exception e) {
                logger.warn("Warning in echo protocol", e);
            }

        }
    }

}
