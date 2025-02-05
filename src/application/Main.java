package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Cargamos el FXML
        Parent root = FXMLLoader.load(getClass().getResource("/view/VideoPlayer.fxml"));
        
        // Obtenemos las dimensiones de la pantalla principal
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        
        // Creamos la escena con el tamaño completo de la pantalla
        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
        primaryStage.setScene(scene);
        
        // Para evitar que el usuario salga del modo pantalla completa usando ESC:
        primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        
        // Mostrar el Stage primero
        primaryStage.show();
        
        // Luego, forzamos el cambio a pantalla completa.
        Platform.runLater(() -> {
            primaryStage.setFullScreen(true);
        });
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}