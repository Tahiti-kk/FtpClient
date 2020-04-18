import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

/**
 * @author JerryLee
 * @date 2020/3/1
 */
public class FtpClient extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception{
        URL resourse = getClass().getResource("sample.fxml");
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        // Todo JavaFX页面的启动
        launch(args);
    }
}
