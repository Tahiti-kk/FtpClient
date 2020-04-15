package ftp;

/**
 * 日志打印的接口
 * @author JerryLee
 * @date 2020/3/3
 */
public interface LoggerListener {

    /**
     * 收到msg
     * @param msg message
     */
    void logMsg(String msg, String type);

    /**
     * 异常msg
     * @param msg message
     */
    void exceptionMsg(String msg, String type);

}
