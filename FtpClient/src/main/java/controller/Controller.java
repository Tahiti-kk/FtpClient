
package controller;
import ftp.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import service.TaskService;
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
    private Button fx_btnConnect;

    @FXML
    private Label fx_localPath;
    @FXML
    private Label fx_severPath;

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

    //新建文件夹时利用变量记录文件夹名称
    private String newFileName = "";

    //与服务器端连接
    private FtpClient ftpClient = null;

    //传输服务
    private TaskService taskService = new TaskService();

    public TaskService getTaskService() {
        return taskService;
    }

    public String getFtpClientIp(){
        return ftpClient.gethost();
    }

    //界面展现初始化
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> items = FXCollections.observableArrayList(LocalFiles.getRootFiles());
        fileList.setItems(items);

    }

    //双击本地文件列表事件
    public void local_ClickTwo(MouseEvent event){
        if(event.getClickCount()==2 && event.getButton().name().equals("PRIMARY")){
            if(currentFilePath.equals("")){
                currentFilePath = fileList.getSelectionModel().getSelectedItem();
                fileList.setItems(FXCollections.observableArrayList(LocalFiles.getFiles(currentFilePath)));
            }
            else if(LocalFiles.isFileDirectory(currentFilePath)){
                currentFilePath += fileList.getSelectionModel().getSelectedItem();
                currentFilePath += "\\";
                fileList.setItems(FXCollections.observableArrayList(LocalFiles.getFiles(currentFilePath)));
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
        if(LocalFiles.isRoot(currentFilePath)){
            ObservableList<String> items = FXCollections.observableArrayList(LocalFiles.getRootFiles());
            fileList.setItems(items);
            currentFilePath = "";
        }else if(currentFilePath.equals("")){
        }else {
            String path = LocalFiles.getParentPath(currentFilePath);
            fileList.setItems(FXCollections.observableArrayList(LocalFiles.getFiles(path)));
            if(LocalFiles.isRoot(path)){
                currentFilePath = path;
            }else {
                currentFilePath = path + "\\";
            }
        }
        fx_localPath.setText(currentFilePath);
    }

    //服务器文件back按键点击事件
    public void sever_Back(MouseEvent event){
        if(ftpClient != null) {
            try {
                ftpClient.cwd("..");
                ArrayList<String> ftpFileNames = getFtpFileNames(ftpClient.getAllFiles());
                ObservableList<String> items = FXCollections.observableArrayList(ftpFileNames);
                severList.setItems(items);
                fx_severPath.setText(ftpClient.getCurrentDir());
            } catch (Exception ex) {
                Stage stage = new Stage();
                Label l = new Label(ex.getMessage());
                Scene s = new Scene(l, 200, 100);
                stage.setScene(s);
                stage.show();
            }
        }
    }

    //连接
    public void Connect_Btn_Click(MouseEvent event)throws Exception {
        if(fx_btnConnect.getText().equals("连接")) {
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
                    String fileName = ip + ".dat";
                    File file = new File(fileName);
                    if (file.exists()) {
                        taskService = TaskService.DeSerializeTaskService(fileName);
                        createProcess();
                    }
                    taskService.setFtpClientInTasks(ftpClient);

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
                    fx_btnConnect.setText("断开连接");
                } catch (Exception ex) {
                    Stage stage = new Stage();
                    Label l = new Label("连接出现问题！" + ex.getMessage());
                    Scene s = new Scene(l, 200, 100);
                    stage.setScene(s);
                    stage.show();
                }
            }
        }else if(fx_btnConnect.getText().equals("断开连接")){
            fx_btnConnect.setText("连接");
            try {
                if(taskService.getDownloadTaskList().size() > 0 || taskService.getUploadTaskList().size() > 0) {
                    String fileName = ftpClient.gethost() + ".dat";
                    TaskService.SerializeTaskService(taskService, fileName);
                }
                ftpClient.quit();
                severList.setItems(null);
                ftpClient = null;
            }catch (Exception e){
                System.out.println(e);
            }
        }
    }

    //上传文件
    public void upload(){
        if(ftpClient == null){
            return;
        }
        if(fileList.getSelectionModel().getSelectedItems().size() != 0){
            try {
                for (String s : fileList.getSelectionModel().getSelectedItems()) {
                    if (ftpClient != null) {
                        File file = new File(currentFilePath + s);
                        UploadTask uploadTask=new UploadTask(ftpClient,file,0,null,0);
                        taskService.addUploadTask(uploadTask);

                        //向进程列表中添加进度条和暂停继续按钮
                        HBox hbox = new HBox();
                        Label upFileName = new Label(currentFilePath + s);
                        ProgressBar progressBar = new ProgressBar();
                        progressBar.setPrefWidth(200);
                        Button button = new Button("暂停");
                        //为button添加匿名按键事件
                        button.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                setUploadPasuseAction(button,uploadTask,progressBar,upFileName);
//                                if(button.getText().equals("暂停")){
//                                    button.setText("继续");
//                                    taskService.stopUploadTask(uploadTask);
//                                }else if(button.getText().equals("继续")){
//                                    button.setText("暂停");
//                                    taskService.startUploadTask(uploadTask);
//                                    Task<Void> task = new Task<Void>() {
//                                        @Override
//                                        protected Void call() throws Exception {
//                                            while (uploadTask.getAlreadyUpSize() < uploadTask.getFileSize() && !uploadTask.isExit()) {
//                                                System.out.println("进度：" + uploadTask.getAlreadyUpSize());
//                                                progressBar.setProgress(((double) (uploadTask.getAlreadyUpSize())) / (uploadTask.getFileSize()));
//                                            }
//                                            if(uploadTask.getAlreadyUpSize() >= uploadTask.getFileSize()){
//                                                refreshLocalList();
//                                            }
//                                            return null;
//                                        }
//                                    };
//                                    progressBar.progressProperty().bind(task.progressProperty());
//                                    Thread t = new Thread(task);
//                                    t.start();
//                                }
                            }
                        });
                        hbox.getChildren().addAll(progressBar,button);
                        fx_uploadVbox.getChildren().add(upFileName);
                        fx_uploadVbox.getChildren().add(hbox);

                        //更新进度条
                        Task<Void> task = new Task<Void>() {
                            @Override
                            protected Void call() throws Exception {
                                while (uploadTask.getAlreadyUpSize() < uploadTask.getFileSize() && !uploadTask.isExit()) {
                                    updateProgress(uploadTask.getAlreadyUpSize(),uploadTask.getFileSize());
                                    System.out.println("进度：" + uploadTask.getAlreadyUpSize());
                                }
                                if(uploadTask.getAlreadyUpSize() >= uploadTask.getFileSize()){
                                    updateProgress(1,1);
                                    refreshSeverList();
                                }
                                return null;
                            }
                        };
                        progressBar.progressProperty().bind(task.progressProperty());
                        Thread t = new Thread(task);
                        t.start();
                        taskService.startUploadTask(uploadTask);
                    }
                }
            }catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        }
    }

    //下载文件
    public void download(){
        if(ftpClient == null){
            return;
        }
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
                                DownloadTask downloadTask = new DownloadTask(f, ftpClient, 0, null, 0, currentFilePath);
                                taskService.addDownloadTask(downloadTask);

                                //向进程列表中添加进度条和暂停继续按钮
                                HBox hbox = new HBox();
                                Label downFileName = new Label(f.getFileName());
                                ProgressBar progressBar = new ProgressBar();
                                progressBar.setPrefWidth(200);
                                Button button = new Button("暂停");
                                //为button添加匿名按键事件
                                button.setOnAction(new EventHandler<ActionEvent>() {
                                    @Override
                                    public void handle(ActionEvent actionEvent) {
                                        setDownloadPauseAction(button,downloadTask,progressBar,downFileName);
//                                        if(button.getText().equals("暂停")){
//                                            button.setText("继续");
//                                            taskService.stopDownloadTask(downloadTask);
//                                        }else if(button.getText().equals("继续")){
//                                            button.setText("暂停");
//                                            taskService.startDownloadTask(downloadTask);
//                                            Task<Void> task = new Task<Void>() {
//                                                @Override
//                                                protected Void call() throws Exception {
//                                                    while (downloadTask.getAlreadyDownSize() < downloadTask.getFileSize() && !downloadTask.isExit()) {
//                                                        updateProgress(downloadTask.getAlreadyDownSize(),downloadTask.getFileSize());
//                                                        System.out.println("进度：" + downloadTask.getAlreadyDownSize());
//                                                    }
//                                                    if(downloadTask.getAlreadyDownSize() >= downloadTask.getFileSize()){
//                                                        refreshLocalList();
//                                                    }
//                                                    return null;
//                                                }
//                                            };
//                                            progressBar.progressProperty().bind(task.progressProperty());
//                                            Thread t = new Thread(task);
//                                            t.start();
//                                        }
                                    }
                                });
                                hbox.getChildren().addAll(progressBar,button);
                                fx_downloadVbox.getChildren().add(downFileName);
                                fx_downloadVbox.getChildren().add(hbox);

                                //创建进度显示的线程
                                //Thread t_download = new Thread(downloadTask);
//                                Thread t = new Thread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        while (downloadTask.getAlreadyDownSize() < downloadTask.getFileSize() && !downloadTask.isExit()) {
//                                            progressBar.setProgress(((double) (downloadTask.getAlreadyDownSize())) / (downloadTask.getFileSize()));
//                                            System.out.println("进度：" + downloadTask.getAlreadyDownSize());
//                                        }
//                                        if(downloadTask.getAlreadyDownSize() >= downloadTask.getFileSize()){
//                                            refreshLocalList();
//                                        }
//                                    }
//                                });

                                //javafx不允许非javafx主线程更改组件信息，以下为解决方法
                                Task<Void> task = new Task<Void>() {
                                    @Override
                                    protected Void call() throws Exception {
                                        while (downloadTask.getAlreadyDownSize() < downloadTask.getFileSize() && !downloadTask.isExit()) {
                                            System.out.println("进度：" + downloadTask.getAlreadyDownSize());
                                            updateProgress(downloadTask.getAlreadyDownSize(),downloadTask.getFileSize());
                                        }
                                        if(downloadTask.getAlreadyDownSize() >= downloadTask.getFileSize()){
                                            updateProgress(1,1);
                                            refreshLocalList();
                                        }
                                        return null;
                                    }
                                };
                                progressBar.progressProperty().bind(task.progressProperty());
                                Thread t = new Thread(task);
                                t.start();
                                //t_download.start();
                                taskService.startDownloadTask(downloadTask);
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
    }

    //info窗口信息
    public void Click_info(MouseEvent me){
        fx_cmdText.setVisible(false);
        fx_infoText.setVisible(true);
    }

//    //切换文件列表
//    public void Click_fileHbox(MouseEvent me){
//        fx_progressAcc.setVisible(false);
//        fx_fileHbox.setVisible(true);
//    }
//
//    //切换进程显示
//    public void Click_processVbox(MouseEvent me){
//        fx_fileHbox.setVisible(false);
//        fx_progressAcc.setVisible(true);
//    }

    //服务器端新建文件夹
    public void sever_addDir(){
        Stage stage = new Stage();
        Label l = new Label("输入文件夹名称：");
        TextField tx = new TextField();
        Button bt = new Button("确定");
        bt.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                newFileName = tx.getText();
                try {
                    ftpClient.makeDirectory(newFileName);
                    refreshSeverList();
                    Stage stage = (Stage)bt.getScene().getWindow();
                    stage.close();
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }
                newFileName = "";
            }
        });
        VBox vBox = new VBox(l,tx,bt);
        Scene scene = new Scene(vBox, 200, 200);
        stage.setScene(scene);
        stage.show();
    }


    //服务器端重命名文件或文件夹
    public void sever_renameFile(){
        if(severList.getSelectionModel().getSelectedItems().size() == 1 && ftpClient != null){
            try {
                ArrayList<FtpFile> ftpFiles = ftpClient.getAllFiles();
                for(String s:severList.getSelectionModel().getSelectedItems()) {
                    for (FtpFile fx : ftpFiles) {
                        if (fx.getFileName().equals(s)) {
                            Stage stage = new Stage();
                            Label l = new Label("输入新文件名称：");
                            TextField tx = new TextField(fx.getFileName());
                            Button bt = new Button("确定");
                            bt.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    newFileName = tx.getText();
                                    try {
                                        ftpClient.rename(fx.getFileName(),newFileName);
                                        refreshSeverList();
                                        Stage stage = (Stage)bt.getScene().getWindow();
                                        stage.close();
                                    }catch (Exception e){
                                        System.out.println(e.getMessage());
                                    }
                                    newFileName = "";
                                }
                            });
                            VBox vBox = new VBox(l,tx,bt);
                            Scene scene = new Scene(vBox, 200, 200);
                            stage.setScene(scene);
                            stage.show();
                        }
                    }
                }
                refreshSeverList();
            }catch(Exception e){
                System.out.println(e);
            }
        }
    }

    //服务器端删除文件或者文件夹
    public void sever_deleteFile(){
        if(severList.getSelectionModel().getSelectedItems().size() > 0 && ftpClient != null){
            try {
                ArrayList<FtpFile> ftpFiles = ftpClient.getAllFiles();
                for(String s:severList.getSelectionModel().getSelectedItems()) {
                    for (FtpFile fx : ftpFiles) {
                        if (fx.getFileName().equals(s)) {
                            ftpClient.delete(fx);
                            break;
                        }
                    }
                }
                refreshSeverList();
            }catch(Exception e){
                System.out.println(e);
            }
        }
    }

    //本地新建文件夹
    public void local_AddDir(){
        Stage stage = new Stage();
        Label l = new Label("输入文件夹名称：");
        TextField tx = new TextField();
        Button bt = new Button("确定");
        bt.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                newFileName = tx.getText();
                try {
                    if(!LocalFiles.makeLocalDir(currentFilePath + newFileName)){
                        showMessage("新建文件夹失败");
                    }
                    refreshLocalList();
                    Stage stage = (Stage)bt.getScene().getWindow();
                    stage.close();
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }
                newFileName = "";
            }
        });
        VBox vBox = new VBox(l,tx,bt);
        Scene scene = new Scene(vBox, 200, 200);
        stage.setScene(scene);
        stage.show();
    }

    //删除本地文件
    public void local_deleteFiles(){
        if(fileList.getSelectionModel().getSelectedItems().size() > 0){
            for(String s:fileList.getSelectionModel().getSelectedItems()) {
                if(!LocalFiles.deleteFile(currentFilePath + s)){
                    showMessage("文件删除失败");
                }
            }
            refreshLocalList();
        }
    }

    //重命名本地文件
    public void local_renameFile(){
        if(fileList.getSelectionModel().getSelectedItems().size() == 1 ) {
                for (String s : fileList.getSelectionModel().getSelectedItems()) {
                    Stage stage = new Stage();
                    Label l = new Label("输入新文件名称：");
                    TextField tx = new TextField(s);
                    Button bt = new Button("确定");
                    bt.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            newFileName = tx.getText();
                            try {
                                if(!LocalFiles.rename(currentFilePath + s,currentFilePath + newFileName)){
                                    showMessage("文件夹重命名失败");
                                }
                                refreshLocalList();
                                Stage stage = (Stage) bt.getScene().getWindow();
                                stage.close();
                            } catch (Exception e) {
                                System.out.println(e.getMessage());
                            }
                            newFileName = "";
                        }
                    });
                    VBox vBox = new VBox(l, tx, bt);
                    Scene scene = new Scene(vBox, 200, 200);
                    stage.setScene(scene);
                    stage.show();
                }
        }
    }

    //刷新本地文件列表
    public void refreshLocalList(){
        if(currentFilePath.equals("")){
            ObservableList<String> items = FXCollections.observableArrayList(LocalFiles.getRootFiles());
            fileList.setItems(items);
        }else{
            fileList.setItems(FXCollections.observableArrayList(LocalFiles.getFiles(currentFilePath)));
        }
    }

    //刷新服务器文件列表文件
    public void refreshSeverList(){
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

    //设置下载暂停的的按键事件
    private void setDownloadPauseAction(Button button,DownloadTask downloadTask,ProgressBar progressBar,Label label){
        if(button.getText().equals("暂停")){
            button.setText("继续");
            taskService.stopDownloadTask(downloadTask);
        }else if(button.getText().equals("继续")){
            button.setText("暂停");
            taskService.startDownloadTask(downloadTask);
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    updateProgress(downloadTask.getAlreadyDownSize(),downloadTask.getFileSize());
                    while (downloadTask.getAlreadyDownSize() < downloadTask.getFileSize() && !downloadTask.isExit()) {
                        updateProgress(downloadTask.getAlreadyDownSize(),downloadTask.getFileSize());
                        updateMessage(downloadTask.getCurFilePath());
                        System.out.println("进度：" + downloadTask.getAlreadyDownSize());
                    }
                    if(downloadTask.getAlreadyDownSize() >= downloadTask.getFileSize()){
                        updateProgress(1,1);
                        refreshLocalList();
                    }
                    return null;
                }
            };
            progressBar.progressProperty().bind(task.progressProperty());
            label.textProperty().bind(task.messageProperty());
            Thread t = new Thread(task);
            t.start();
        }
    }

    //设置上传暂停的按键事件
    private void setUploadPasuseAction(Button button,UploadTask uploadTask,ProgressBar progressBar,Label label){
        if(button.getText().equals("暂停")){
            button.setText("继续");
            taskService.stopUploadTask(uploadTask);
        }else if(button.getText().equals("继续")) {
            button.setText("暂停");
            taskService.startUploadTask(uploadTask);
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    updateProgress(uploadTask.getAlreadyUpSize(),uploadTask.getFileSize());
                    while (uploadTask.getAlreadyUpSize() < uploadTask.getFileSize() && !uploadTask.isExit()) {
                        System.out.println("进度：" + uploadTask.getAlreadyUpSize());
                        updateProgress(uploadTask.getAlreadyUpSize(),uploadTask.getFileSize());
                        updateMessage(uploadTask.getCurFilePath());
                    }
                    if (uploadTask.getAlreadyUpSize() >= uploadTask.getFileSize()) {
                        updateProgress(1,1);
                        refreshLocalList();
                    }
                    return null;
                }
            };
            progressBar.progressProperty().bind(task.progressProperty());
            label.textProperty().bind(task.messageProperty());
            Thread t = new Thread(task);
            t.start();
        }
    }

    //为上次未完成的进度创建进度条
    private void createProcess(){
        if(taskService.getDownloadTaskList().size() > 0){
            for(DownloadTask dt:taskService.getDownloadTaskList()){
                HBox hbox = new HBox();
                Label downFileName = new Label(dt.getCurFilePath());
                ProgressBar progressBar = new ProgressBar();
                progressBar.setPrefWidth(200);
                progressBar.setProgress(((double)(dt.getAlreadyDownSize()))/dt.getFileSize());
                Button button = new Button("继续");
                //为button添加匿名按键事件
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        setDownloadPauseAction(button,dt, progressBar,downFileName);
                    }
                });
                hbox.getChildren().addAll(progressBar,button);
                fx_downloadVbox.getChildren().addAll(downFileName,hbox);
            }
        }
        if(taskService.getUploadTaskList().size() > 0){
            for(UploadTask ut:taskService.getUploadTaskList()){
                //向进程列表中添加进度条和暂停继续按钮
                HBox hbox = new HBox();
                Label upFileName = new Label(ut.getCurFilePath());
                ProgressBar progressBar = new ProgressBar();
                progressBar.setPrefWidth(200);
                progressBar.setProgress(((double)(ut.getAlreadyUpSize()))/ut.getFileSize());
                Button button = new Button("继续");
                //为button添加匿名按键事件
                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        setUploadPasuseAction(button,ut,progressBar,upFileName);
                    }
                });
                hbox.getChildren().addAll(progressBar,button);
                fx_uploadVbox.getChildren().addAll(upFileName,hbox);
            }
        }
    }

    //获得所有的文件名
    private ArrayList<String> getFtpFileNames(ArrayList<FtpFile> ftpFiles){
        ArrayList<String> ftpFilesName = new ArrayList<>();
        for (FtpFile f : ftpFiles) {
            ftpFilesName.add(f.getFileName());
        }
        return ftpFilesName;
    }

    //新建弹窗显示信息
    public void showMessage(String s) {
        Stage stage = new Stage();
        Label l = new Label(s);
        Scene scene = new Scene(l,200,100);
        stage.setScene(scene);
        stage.show();
    }
}