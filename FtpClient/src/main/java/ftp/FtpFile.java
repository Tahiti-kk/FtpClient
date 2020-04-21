package ftp;
/**
 * 李军邑：实现
 */
import util.Constant;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

/**
 * @author JerryLee
 * @date 2020/3/4
 */
public class FtpFile implements Serializable {

    private String line;

    /**
     * 文件类型：0为文件，1为文件夹
     */
    private int type;
    private long fileSize;
    private String fileName;
    private String filePath;

    public static FtpFile parseFile(String line, String currentDir) {
        FtpFile file=new FtpFile();
        file.line = line;

        // 解析字符串
        String[] strList = line.split(" ");
        Vector<String> infoList = new Vector<>();
        for (String str : strList) {
            if(!str.equals("")) {
                infoList.add(str);
            }
        }
        String type = line.substring(0,1);
        if(type.equals("d"))
        {
            // 设置为文件夹
            file.type = 1;
            file.filePath = currentDir;
        } else {
            // 设置为文件
            file.type = 0;
            file.filePath = currentDir + Constant.FILE_SEPARATOR + infoList.get(8);
        }
        file.fileSize = Long.parseLong(infoList.get(4));
        file.fileName = infoList.get(8);
        return file;
    }

    @Override
    public String toString() {
        return line;
    }

    public boolean isDirectory(){
        return type == 1;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

}
