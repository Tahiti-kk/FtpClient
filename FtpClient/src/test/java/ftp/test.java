package ftp;

import controller.MainPage;
import org.junit.Test;
import util.Constant;

import java.net.Socket;

import static ftp.FtpFile.parseFile;

/**
 * @author JerryLee
 * @date 2020/3/4
 */
public class test {

    @Test
    public void testMainPage() {
        MainPage mainPage = new MainPage();
        mainPage.init();
    }
}
