import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import util.Constant;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author JerryLee
 * @date 2020/3/1
 */
public class FtpServer {

    public static void main(String[] args) {

        Logger logger = LogManager.getLogger("FtpServerLogger");

        try {
            ServerSocket serverSocket = new ServerSocket(Constant.SOCKET_PORT);

            while (true) {
                // block waiting for connection
                Socket clientSocket = serverSocket.accept();

                // spawn thread to handle new connection
                Thread thread = new Thread(new EchoProtocol(clientSocket, logger));

                thread.start();
                logger.info("Created and started a new thread " + thread.getName() + " for a new connection.");
            }

        } catch (Exception e) {
            logger.error("Exception in ftp server", e);
        }
    }

}
