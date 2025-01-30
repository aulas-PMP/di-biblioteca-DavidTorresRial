import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneBuilderApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Carga el FXML que está en la MISMA carpeta que esta clase.
        Parent root = FXMLLoader.load(getClass().getResource("Reproductor.fxml"));

        Scene scene = new Scene(root, 600, 400);
        primaryStage.setTitle("Mi App JavaFX");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}