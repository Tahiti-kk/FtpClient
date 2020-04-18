package util;

import ftp.LoggerListener;
import javafx.scene.control.TextArea;

/**
 * @author JerryLee
 * @date 2020/4/14
 */
public class CmdListener implements LoggerListener {

    private TextArea textArea;

    public CmdListener(TextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void logMsg(String msg, String type) {
        if(type.equals("cmd")){
            textArea.appendText(msg+"\n");
            System.out.println(msg);
        }
    }

    @Override
    public void exceptionMsg(String msg, String type) {
        if(type.equals("cmd")){
            textArea.appendText("cmd---" + "Exception:" + msg+"\n");
            System.out.println("cmd---" + "Exception:" + msg);
        }
    }

}
