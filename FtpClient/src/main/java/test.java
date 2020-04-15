import ftp.FtpClient;
import util.CmdListener;
import util.InfoListener;

import java.util.Scanner;

/**
 * @author JerryLee
 * @date 2020/4/12
 */
public class test {
    public static void main(String[] args) {
        try{
            CmdListener cmdListener = new CmdListener();
            InfoListener infoListener = new InfoListener();

            FtpClient ftpClient = new FtpClient("116.62.170.221",21,"ftp511","admin123456");

            ftpClient.addListener(cmdListener);
            ftpClient.addListener(infoListener);

            ftpClient.login();
            Scanner scan = new Scanner(System.in);
            while(true) {
                String str = scan.next();
                if(str.equals("quit")) {
                    break;
                }
                ftpClient.sendCommand(str);
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());;
        }
    }
}
