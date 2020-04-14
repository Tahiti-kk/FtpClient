package util;

import ftp.LoggerListener;

/**
 * @author JerryLee
 * @date 2020/4/14
 */
public class InfoListener implements LoggerListener {
    @Override
    public void logMsg(String msg, String type) {
        if(type.equals("info")){
            System.out.println("info---" + msg);
        }
    }

    @Override
    public void exceptionMsg(String msg, String type) {
        if(type.equals("info")){
            System.out.println("info---" + "Exception:" + msg);
        }
    }
}
