import java.io.File;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.media.*;
import javafx.stage.FileChooser;

public class MainController {

    @FXML
    private Label labelTituloArchivo;
    @FXML
    private MediaView mediaView;
    @FXML
    private ListView<String> listaArchivos;

    private MediaPlayer mediaPlayer;

    // 1. Abrir un archivo desde Menú -> "Abrir..."
    @FXML
    private void onAbrirArchivo(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo multimedia");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Archivos de vídeo", "*.mp4", "*.avi", "*.mov"),
            new FileChooser.ExtensionFilter("Archivos de audio", "*.mp3", "*.wav"),
            new FileChooser.ExtensionFilter("Todos", "*.*")
        );
        
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            // Actualizamos título
            labelTituloArchivo.setText(file.getName());
            // Creamos Media y MediaPlayer
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            // Reproducir directamente (o esperar el click en Play)
            mediaPlayer.play();
        }
    }

    // 2. Refrescar Biblioteca
    @FXML
    private void onRefrescarBiblioteca(ActionEvent event) {
        // Escanea un directorio, obtiene lista y la añade a listaArchivos
        // Ejemplo: listaArchivos.getItems().setAll("video1.mp4", "audio1.mp3");
    }

    // 3. Pantalla completa
    @FXML
    private void onPantallaCompleta(ActionEvent event) {
        // Por ejemplo:
        // labelTituloArchivo.getScene().getWindow().setFullScreen(true);
    }

    // 4. Acerca de
    @FXML
    private void onAcercaDe(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de");
        alert.setHeaderText("Información de la aplicación");
        alert.setContentText("Biblioteca multimedia\nAutor: Nombre del Alumno\nVersión: 1.0");
        alert.showAndWait();
    }

    // 5. Play, Pause, Stop
    @FXML
    private void onPlay(ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    @FXML
    private void onPause(ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @FXML
    private void onStop(ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    // 6. Cambiar Tamaño
    @FXML
    private void onCambiarTamaño(ActionEvent event) {
        if (mediaView != null) {
            mediaView.setFitWidth(800);
            mediaView.setFitHeight(600);
        }
    }

    // 7. Cambiar Velocidad
    @FXML
    private void onCambiarVelocidad(ActionEvent event) {
        if (mediaPlayer != null) {
            // Duplicamos velocidad
            mediaPlayer.setRate(2.0);
        }
    }

    // 8. Salir
    @FXML
    private void onSalir(ActionEvent event) {
        System.exit(0);
    }
}