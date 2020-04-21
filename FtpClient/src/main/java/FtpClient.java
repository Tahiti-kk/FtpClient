import controller.Controller;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import service.TaskService;

import java.net.URL;

/**
 * @author JerryLee
 * @date 2020/3/1
 */
public class FtpClient extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception{
        URL resourse = getClass().getResource("FTP.fxml");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FTP.fxml"));
        Parent root = loader.load();
        Controller controller = loader.getController();
        primaryStage.setTitle("FTP Client");
        Scene scene = new Scene(root, 1200, 600);
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                try {
                    TaskService taskService = controller.getTaskService();
                    if(taskService.getDownloadTaskList().size() > 0 || taskService.getUploadTaskList().size() > 0) {
                        String fileName = controller.getFtpClientIp() + ".dat";
                        TaskService.SerializeTaskService(taskService, fileName);
                    }
                }catch (Exception e){
                    System.out.println(e);
                }
            }
        });

        primaryStage.getIcons().add(new Image("file:FTP.png"));
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Todo JavaFX页面的启动
        launch(args);
    }
}
