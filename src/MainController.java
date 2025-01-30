import java.io.File;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.media.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;

public class MainController {

    @FXML
    private Label labelTituloArchivo;
    @FXML
    private MediaView mediaView;
    @FXML
    private TableView<?> tableBiblioteca;
    @FXML
    private TableColumn<?, ?> colNombre;
    @FXML
    private TableColumn<?, ?> colFormato;
    @FXML
    private TableColumn<?, ?> colDuracion;

    private MediaPlayer mediaPlayer;

    // Variable para guardar último momento de reproducción (extra)
    private Duration ultimoTiempo = Duration.ZERO;

    // 1. Menú Archivo -> "Abrir..."
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
            cargarMedia(file);
        }
    }

    // 2. Menú Archivo -> Salir
    @FXML
    private void onSalir(ActionEvent event) {
        System.exit(0);
    }

    // 3. Menú Biblioteca -> Actualizar
    @FXML
    private void onRefrescarBiblioteca(ActionEvent event) {
        // Aquí podrías listar archivos del directorio y mostrarlos en la TableView
        // Ejemplo sencillo (sin filtrar):
        /*
        File directorio = new File("media");
        File[] archivos = directorio.listFiles();
        if (archivos != null) {
            for (File f : archivos) {
                // Convertir a un objeto "ArchivoMultimedia" con nombre, formato, etc.
                // Agregarlo a la TableView
            }
        }
        */
    }

    // 4. Menú Ver -> Pantalla completa
    @FXML
    private void onPantallaCompleta(ActionEvent event) {
        Stage stage = (Stage) labelTituloArchivo.getScene().getWindow();
        stage.setFullScreen(true);
    }

    // 5. Menú Acerca de
    @FXML
    private void onAcercaDe(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Acerca de esta App");
        alert.setHeaderText("Biblioteca Multimedia");
        alert.setContentText("Autor: [Tu Nombre]\nVersión: 1.0\nProyecto JavaFX con FXML y CSS.");
        alert.showAndWait();
    }

    // 6. Panel Edición: Cambiar Tamaño (manteniendo ratio)
    @FXML
    private void onCambiarTamaño(ActionEvent event) {
        if (mediaView != null) {
            mediaView.setPreserveRatio(true);
            mediaView.setFitWidth(800);
            mediaView.setFitHeight(600);
        }
    }

    // 7. Panel Edición: Cambiar Velocidad
    @FXML
    private void onCambiarVelocidad(ActionEvent event) {
        if (mediaPlayer != null) {
            // Duplicar velocidad
            mediaPlayer.setRate(2.0);
        }
    }

    // 8. Botones de reproducción
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

    // *** MÉTODO AUXILIAR para cargar un archivo de audio/vídeo ***
    private void cargarMedia(File file) {
        try {
            // Si había otro MediaPlayer, lo liberamos
            if (mediaPlayer != null) {
                mediaPlayer.dispose();
            }

            // Creación de Media y MediaPlayer
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);

            // Mostrar nombre de archivo en la etiqueta
            labelTituloArchivo.setText(file.getName());

            // Manejo de errores
            mediaPlayer.setOnError(() -> {
                Alert err = new Alert(Alert.AlertType.ERROR, 
                                      "Error al reproducir el archivo:\n" 
                                      + mediaPlayer.getError().getMessage());
                err.show();
            });

            // Extra: Escucha del tiempo actual para guardarlo
            mediaPlayer.currentTimeProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    ultimoTiempo = mediaPlayer.getCurrentTime();
                }
            });

            // Reproducir
            mediaPlayer.play();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, 
                "No se pudo cargar el archivo:\n" + e.getMessage());
            alert.showAndWait();
        }
    }
}