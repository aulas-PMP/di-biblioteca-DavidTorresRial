package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Carga el FXML (asegúrate de que la ruta sea correcta)
        Parent root = FXMLLoader.load(getClass().getResource("/view/VideoPlayer.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Biblioteca de Videos");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}