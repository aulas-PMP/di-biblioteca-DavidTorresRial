import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargar el FXML principal
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Reproductor.fxml"));
        Parent root = loader.load();

        // Crear la escena
        Scene scene = new Scene(root, 1000, 600);
        
        // Añadir hoja de estilos CSS (colócala junto al FXML o en carpeta 'resources')
        scene.getStylesheets().add(getClass().getResource("estilos.css").toExternalForm());

        // Configurar stage
        primaryStage.setTitle("Biblioteca Multimedia");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}