package controller;
import ftp.DownloadTask;
import ftp.FtpClient;
import ftp.FtpFile;
import ftp.UploadTask;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import util.CmdListener;
import util.InfoListener;


public class Controller implements Initializable {

    private String currentFilePath = "";

    @FXML
    private ListView<String> fileList;

    @FXML
    private  ListView<String> severList;

    @FXML
    private TextField fxIP;
    @FXML
    private TextField fxPort;
    @FXML
    private TextField fxAccount;
    @FXML
    private TextField fxPassword;

    @FXML
    private Label fx_localPath;
    @FXML
    private Label fx_severPath;

//    @FXML
//    private MenuButton fx_downloadMenu;
//    @FXML
//    private MenuButton fx_uploadMenu;

    //cmd和info窗口
    @FXML
    private TextArea fx_cmdText;
    @FXML
    private TextArea fx_infoText;

    @FXML
    private HBox fx_fileHbox;
    @FXML
    private Accordion fx_progressAcc;

    @FXML
    private VBox fx_downloadVbox;
    @FXML
    private VBox fx_uploadVbox;


    private FtpClient ftpClient = null;

    //界面展现初始化
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> items = FXCollections.observableArrayList(GetFiles.getRootFiles());
        fileList.setItems(items);
    }

    //双击本地文件列表事件
    public void local_ClickTwo(MouseEvent event){
        if(event.getClickCount()==2 && event.getButton().name().equals("PRIMARY")){
            if(currentFilePath.equals("")){
                currentFilePath = fileList.getSelectionModel().getSelectedItem();
                fileList.setItems(FXCollections.observableArrayList(GetFiles.getFiles(currentFilePath)));
            }
            else if(GetFiles.isFileDirectory(currentFilePath)){
                currentFilePath += fileList.getSelectionModel().getSelectedItem();
                currentFilePath += "\\";
                fileList.setItems(FXCollections.observableArrayList(GetFiles.getFiles(currentFilePath)));
            }
            fx_localPath.setText(currentFilePath);
        }
    }

    //双击服务器文件列表事件
    public void sever_ClickTwo(MouseEvent event){
        if(event.getClickCount()==2 && event.getButton().name().equals("PRIMARY")){
            if(ftpClient != null){
                try {
                    ftpClient.cwd(severList.getSelectionModel().getSelectedItem());
                    ArrayList<String> ftpFilesName = new ArrayList<>();
                    ArrayList<FtpFile> ftpFiles = ftpClient.getAllFiles();
                    for (FtpFile f : ftpFiles) {
                        ftpFilesName.add(f.getFileName());
                    }
                    ObservableList<String> items = FXCollections.observableArrayList(ftpFilesName);
                    severList.setItems(items);
                    fx_severPath.setText(ftpClient.getCurrentDir());
                }catch (Exception ex){
                    Stage stage = new Stage();
                    Label l = new Label(ex.getMessage());
                    Scene s = new Scene(l,200,100);
                    stage.setScene(s);
                    stage.show();
                }
            }
        }
    }

    //本地文件back按键点击事件
    public void back_btn_Click(MouseEvent event){
        if(GetFiles.isRoot(currentFilePath)){
            ObservableList<String> items = FXCollections.observableArrayList(GetFiles.getRootFiles());
            fileList.setItems(items);
            currentFilePath = "";
        }else if(currentFilePath.equals("")){
        }else {
            String path = GetFiles.getParentPath(currentFilePath);
            fileList.setItems(FXCollections.observableArrayList(GetFiles.getFiles(path)));
            currentFilePath = path;
        }
        fx_localPath.setText(currentFilePath);
    }

    //服务器文件back按键点击事件
    public void sever_Back(MouseEvent event){
        try {
            ftpClient.cwd("..");
            ArrayList<String> ftpFileNames = getFtpFileNames(ftpClient.getAllFiles());
            ObservableList<String> items = FXCollections.observableArrayList(ftpFileNames);
            severList.setItems(items);
            fx_severPath.setText(ftpClient.getCurrentDir());
        }catch (Exception ex){
            Stage stage = new Stage();
            Label l = new Label(ex.getMessage());
            Scene s = new Scene(l,200,100);
            stage.setScene(s);
            stage.show();
        }
    }

    //连接按钮
    public void Connect_Btn_Click(MouseEvent event)throws Exception {
        String ip = fxIP.getText();
        String port = fxPort.getText();
        String account = fxAccount.getText();
        String password = fxPassword.getText();
        if (ip.equals("") || port.equals("") || account.equals("") || password.equals("")) {
            Stage stage = new Stage();
            Label l = new Label("请输入完整信息");
            Scene s = new Scene(l, 200, 100);
            stage.setScene(s);
            stage.show();
        } else {
            try {
                ftpClient = new FtpClient(ip, Integer.parseInt(port), account, password);

                // 添加日志信息接口
                CmdListener cmdListener = new CmdListener(fx_cmdText);
                InfoListener infoListener = new InfoListener(fx_infoText);
                ftpClient.addListener(cmdListener);
                ftpClient.addListener(infoListener);

                ftpClient.login();
                fx_severPath.setText(ftpClient.getCurrentDir());
                ArrayList<FtpFile> ftpFiles = ftpClient.getAllFiles();
                ArrayList<String> ftpFilesName = getFtpFileNames(ftpFiles);
                ObservableList<String> items = FXCollections.observableArrayList(ftpFilesName);
                severList.setItems(items);
            } catch (Exception ex) {
                Stage stage = new Stage();
                Label l = new Label("连接出现问题！" + ex.getMessage());
                Scene s = new Scene(l, 200, 100);
                stage.setScene(s);
                stage.show();
            }
        }
    }

    //上传文件
    public void upload(){
        if(fileList.getSelectionModel().getSelectedItems().size() != 0){
            try {
                for (String s : fileList.getSelectionModel().getSelectedItems()) {
                    if (ftpClient != null) {
                        File file = new File(currentFilePath + s);
                        UploadTask uploadTask=new UploadTask(ftpClient,file,0,null,0);
                        ProgressBar progressBar = new ProgressBar();
                        fx_uploadVbox.getChildren().add(progressBar);
                        Thread t_upload = new Thread(uploadTask);
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (uploadTask.getAlreadyUpSize() < uploadTask.getFileSize()) {
                                    progressBar.setProgress(((double) (uploadTask.getAlreadyUpSize())) / (uploadTask.getFileSize()));
                                    System.out.println("进度：" + uploadTask.getAlreadyUpSize());
                                }
                            }
                        });
                        t.start();
                        t_upload.start();
                    }
                }
                refreshSeverList();
            }catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        }
    }

    //刷新服务器文件列表文件
    private void refreshSeverList(){
        try {
            if(ftpClient != null) {
                ArrayList<FtpFile> ftpFiles = ftpClient.getAllFiles();
                ArrayList<String> ftpFilesName = getFtpFileNames(ftpFiles);
                ObservableList<String> items = FXCollections.observableArrayList(ftpFilesName);
                severList.setItems(items);
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    //下载文件
    public void download(){
        if(currentFilePath.equals("")){
            System.out.println("无法下载到磁盘目录下");
            return;
        }
        if(severList.getSelectionModel().getSelectedItems().size() != 0){
            if(ftpClient != null) {
                try {
                    ArrayList<FtpFile> ftpFiles = ftpClient.getAllFiles();
                    for (String s : severList.getSelectionModel().getSelectedItems()) {
                        for(FtpFile f:ftpFiles){
                            if(s.equals(f.getFileName())) {
                                FtpClient ftpClient1 = new FtpClient("116.62.170.221",21,"ftp511","admin123456");
                                ftpClient1.getCurrentDir();
                                DownloadTask downloadTask = new DownloadTask(f, ftpClient1, 0, null, 0, currentFilePath);
                                ProgressBar progressBar = new ProgressBar();
                                fx_downloadVbox.getChildren().add(progressBar);
                                Thread t_download = new Thread(downloadTask);
                                Thread t = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        while (downloadTask.getAlreadyDownSize() < downloadTask.getFileSize()) {
                                            progressBar.setProgress(((double) (downloadTask.getAlreadyDownSize())) / (downloadTask.getFileSize()));
                                            System.out.println("进度：" + downloadTask.getAlreadyDownSize());
                                        }
                                    }
                                });
                                t.start();
                                t_download.start();
                            }
                        }
                    }
                    refreshLocalList();
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    //cmd窗口信息
    public void Click_cmd(MouseEvent me){
        fx_infoText.setVisible(false);
        fx_cmdText.setVisible(true);
        //对cmdText文本进行设置
        fx_cmdText.setText("这是cmd窗口");
    }

    //info窗口信息
    public void Click_info(MouseEvent me){
        fx_cmdText.setVisible(false);
        fx_infoText.setVisible(true);
        //对infoText文本进行设置
        fx_infoText.setText("这是info窗口");
    }

    //切换文件列表
    public void Click_fileHbox(MouseEvent me){
        fx_progressAcc.setVisible(false);
        fx_fileHbox.setVisible(true);
    }

    //切换进程显示
    public void Click_processVbox(MouseEvent me){
        fx_fileHbox.setVisible(false);
        fx_progressAcc.setVisible(true);
    }

    //刷新本地文件列表
    private void refreshLocalList(){
        if(currentFilePath.equals("")){
            ObservableList<String> items = FXCollections.observableArrayList(GetFiles.getRootFiles());
            fileList.setItems(items);
        }else{
            fileList.setItems(FXCollections.observableArrayList(GetFiles.getFiles(currentFilePath)));
        }
    }

    private ArrayList<String> getFtpFileNames(ArrayList<FtpFile> ftpFiles){
        ArrayList<String> ftpFilesName = new ArrayList<>();
        for (FtpFile f : ftpFiles) {
            ftpFilesName.add(f.getFileName());
        }
        return ftpFilesName;
    }
}
