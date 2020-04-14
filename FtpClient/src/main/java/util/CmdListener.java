package util;

import ftp.LoggerListener;

/**
 * @author JerryLee
 * @date 2020/4/14
 */
public class CmdListener implements LoggerListener {
    @Override
    public void logMsg(String msg, String type) {
        if(type.equals("cmd")){
            System.out.println("cmd---" + msg);
        }
    }

    @Override
    public void exceptionMsg(String msg, String type) {
        if(type.equals("cmd")){
            System.out.println("cmd---" + "Exception:" + msg);
        }
    }

}
