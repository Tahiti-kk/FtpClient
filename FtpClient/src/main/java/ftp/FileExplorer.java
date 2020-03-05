package ftp;

import java.util.ArrayList;

/**
 * @author JerryLee
 * @date 2020/3/5
 */
public class FileExplorer {
    private String currentDir;
    private ArrayList<FtpFile> fileList;


    FileExplorer(String currentDir, ArrayList<FtpFile> fileList) {
        this.currentDir = currentDir;
        this.fileList = fileList;
    }

}
