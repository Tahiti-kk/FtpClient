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
    void receiveMsg(String msg);

    /**
     * 发送msg
     * @param msg message
     */
    void sendMsg(String msg);

    /**
     * 异常msg
     * @param msg message
     */
    void exceptionMsg(String msg);

    /**
     * 打印已连接
     */
    void connected();

    /**
     * 打印未连接
     */
    void disconnected();
}
