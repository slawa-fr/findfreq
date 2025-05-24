import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("sample.fxml"));
            //System.out.println("root:" + getClass().getResource("sample.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        primaryStage.setTitle("Настройка приёмной спутниковой антенны по азимуту (АЗ) и углу места (УМ) на выбранный космический аппарат (КА)");
        primaryStage.setScene(new Scene(root, 860, 650));
// Чтобы нельзя было изменять размеры окна
        primaryStage.setResizable(false);
        primaryStage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
