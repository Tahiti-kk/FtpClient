package controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GetFiles {
    //获得本地所有磁盘目录
    public static List<String> getRootFiles(){
        List<String> rootFiles = new ArrayList<>();
        File[] root = File.listRoots();
        if(root != null) {
            for (File file : root) {
                rootFiles.add(file.getPath());
            }
        }
        return rootFiles;
    }

    //获得路径下所有文件名
    public static List<String> getFiles(String path){
        File file = new File(path);
        List<String> fileNames = new ArrayList<>();
        if(file.isDirectory() || file.isAbsolute()){
            File[] files = file.listFiles();
            if(files != null) {
                for (File f : files) {
                    fileNames.add(f.getName());
                }
            }
            return fileNames;
        }else{
            return null;
        }
    }

    //判断是否为文件夹
    public static boolean isFileDirectory(String path){
        File file = new File(path);
        return file.isDirectory();
    }

    //判断是否为系统盘目录
    public static  boolean isRoot(String path){
        File[] root = File.listRoots();
        for (File file:root) {
            if(path.equals(file.getPath())){
                return true;
            }
        }
        return false;
    }

    //获得上层目录
    public static String getParentPath(String path){
        return ( (new File(path)).getParent() );
    }

}
