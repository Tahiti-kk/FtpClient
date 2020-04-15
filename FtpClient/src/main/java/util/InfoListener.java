package util;

import ftp.LoggerListener;
import javafx.scene.control.TextArea;

/**
 * @author JerryLee
 * @date 2020/4/14
 */
public class InfoListener implements LoggerListener {

    private TextArea textArea;

    public InfoListener(TextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void logMsg(String msg, String type) {
        if(type.equals("info")){
            textArea.appendText(msg+"\n");
            System.out.println("info---" + msg);
        }
    }

    @Override
    public void exceptionMsg(String msg, String type) {
        if(type.equals("info")){
            textArea.appendText("info---" + "Exception:" + msg+"\n");
            System.out.println("info---" + "Exception:" + msg);
        }
    }
}
