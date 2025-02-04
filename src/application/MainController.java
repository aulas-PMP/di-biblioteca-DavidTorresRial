package application;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;

public class MainController {

    @FXML
    private BorderPane borderPane; // Contenedor raíz

    @FXML
    private MediaView mediaView;

    @FXML
    private Label fileTitle;

    @FXML
    private TableView<FileInfo> tableLibrary;

    @FXML
    private TableColumn<FileInfo, String> colNombre;

    @FXML
    private TableColumn<FileInfo, String> colFormato;

    @FXML
    private TableColumn<FileInfo, String> colDuracion;

    @FXML
    private Slider speedSlider;
    
    // Nueva declaración para el control de volumen
    @FXML
    private Slider volumeSlider;

    // Barra de progreso para la reproducción
    @FXML
    private Slider progressSlider;

    // Referencias a los paneles laterales
    @FXML
    private TitledPane leftPane;   // Panel de Edición (izquierda)
    @FXML
    private TitledPane rightPane;  // Panel de Biblioteca (derecha)

    private MediaPlayer mediaPlayer;
    private double defaultWidth = 640;
    private double defaultHeight = 360;
    
    // Lista observable para la biblioteca
    private ObservableList<FileInfo> libraryFiles = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Configurar las columnas de la tabla usando propiedades de FileInfo
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colFormato.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormato()));
        colDuracion.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDuracion()));

        // Cargar la biblioteca multimedia (directorio "media")
        loadLibrary();

        // Listener: al seleccionar un archivo en la tabla se reproduce
        tableLibrary.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                openFile(newSelection.getFile());
            }
        });
    }

    /**
     * Carga los archivos multimedia del directorio "media" en la tabla.
     */
    private void loadLibrary() {
        libraryFiles.clear();
        File dir = new File("media");
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) ->
                    name.toLowerCase().endsWith(".mp4") || name.toLowerCase().endsWith(".mp3"));
            if (files != null) {
                for (File f : files) {
                    // Para simplificar, la duración se muestra como "Desconocido"
                    String nombre = f.getName();
                    String formato = nombre.substring(nombre.lastIndexOf(".") + 1);
                    String duracion = "Desconocido";
                    libraryFiles.add(new FileInfo(nombre, formato, duracion, f));
                }
            }
        }
        tableLibrary.setItems(libraryFiles);
    }

    /**
     * Abre un archivo usando un FileChooser.
     */
    @FXML
    private void handleAbrir(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Abrir Archivo Multimedia");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Multimedia", "*.mp4", "*.mp3")
        );
        Stage stage = (Stage) mediaView.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            openFile(file);
            // Muestra un diálogo no modal con información del archivo
            Alert infoAlert = new Alert(AlertType.INFORMATION);
            infoAlert.setTitle("Archivo Abierto");
            infoAlert.setHeaderText(null);
            infoAlert.setContentText("Archivo: " + file.getName());
            infoAlert.initOwner(stage);
            infoAlert.show();
        }
    }

    /**
     * Abre y reproduce el archivo multimedia.
     */
    private void openFile(File file) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(true);
            mediaView.setMediaPlayer(mediaPlayer);
            fileTitle.setText(file.getName());
            // Ajusta la velocidad según el slider
            mediaPlayer.setRate(speedSlider.getValue());

            // Configurar la barra de progreso cuando el media esté listo
            mediaPlayer.setOnReady(() -> {
                Duration total = mediaPlayer.getTotalDuration();
                progressSlider.setMax(total.toSeconds());
            });

            // Actualiza la barra de progreso conforme avanza el video
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!progressSlider.isValueChanging()) {
                    progressSlider.setValue(newTime.toSeconds());
                }
            });

            // Permite mover la barra para cambiar la posición del video
            progressSlider.setOnMouseReleased((MouseEvent event) -> {
                if (mediaPlayer != null) {
                    mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
                }
            });

        } catch (Exception e) {
            showError("Error al abrir el archivo", e.getMessage());
        }
    }

    /**
     * Cierra la aplicación.
     */
    @FXML
    private void handleSalir(ActionEvent event) {
        Platform.exit();
    }

    /**
     * Muestra la información "Acerca de" en un diálogo modal.
     */
    @FXML
    private void handleAcercaDe(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Acerca de");
        alert.setHeaderText("Biblioteca Multimedia");
        alert.setContentText("Autor: Pablo Martínez Pavón\nAlumno: [Tu Nombre]");
        alert.showAndWait();
    }

    /**
     * Refresca la biblioteca recargando los archivos del directorio "media".
     */
    @FXML
    private void handleRefrescarBiblioteca(ActionEvent event) {
        loadLibrary();
    }

    /**
     * Alterna el modo de pantalla completa.
     */
    @FXML
    private void handlePantallaCompleta(ActionEvent event) {
        Stage stage = (Stage) mediaView.getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
    }

    /**
     * Inicia la reproducción.
     */
    @FXML
    private void handlePlay(ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    /**
     * Pausa la reproducción.
     */
    @FXML
    private void handlePause(ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    /**
     * Detiene la reproducción.
     */
    @FXML
    private void handleStop(ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    /**
     * Cambia el tamaño del reproductor (alternando entre un tamaño predeterminado y uno mayor).
     */
    @FXML
    private void handleCambiarTamano(ActionEvent event) {
        if (mediaView.getFitWidth() == defaultWidth) {
            // Ajusta a un tamaño mayor (por ejemplo, restando el ancho de los paneles laterales)
            mediaView.setFitWidth(mediaView.getScene().getWidth() - 250);
            mediaView.setFitHeight(mediaView.getScene().getHeight() - 150);
        } else {
            mediaView.setFitWidth(defaultWidth);
            mediaView.setFitHeight(defaultHeight);
        }
    }

    /**
     * Cambia la velocidad de reproducción según el valor del slider.
     */
    @FXML
    private void handleCambiarVelocidad(MouseEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.setRate(speedSlider.getValue());
        }
    }

    /**
     * Alterna la visibilidad del panel de Edición (izquierdo).
     * Este método se invoca desde la opción del menú "Ocultar/Mostrar Edición".
     */
    @FXML
    private void handleToggleLeft(ActionEvent event) {
        if (borderPane.getLeft() != null) {
            borderPane.setLeft(null);
        } else {
            borderPane.setLeft(leftPane);
        }
    }

    /**
     * Alterna la visibilidad del panel de Biblioteca (derecho).
     * Este método se invoca desde la opción del menú "Ocultar/Mostrar Biblioteca".
     */
    @FXML
    private void handleToggleRight(ActionEvent event) {
        if (borderPane.getRight() != null) {
            borderPane.setRight(null);
        } else {
            borderPane.setRight(rightPane);
        }
    }

    /**
     * Muestra un diálogo de error.
     */
    private void showError(String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Cambia el volumen de reproducción según el valor del slider.
     */
    @FXML
    public void handleCambiarVolumen(MouseEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volumeSlider.getValue());
        }
    }

    /**
     * Clase interna que modela la información de cada archivo multimedia en la biblioteca.
     */
    public static class FileInfo {
        private String nombre;
        private String formato;
        private String duracion;
        private File file;

        public FileInfo(String nombre, String formato, String duracion, File file) {
            this.nombre = nombre;
            this.formato = formato;
            this.duracion = duracion;
            this.file = file;
        }

        public String getNombre() {
            return nombre;
        }

        public String getFormato() {
            return formato;
        }

        public String getDuracion() {
            return duracion;
        }

        public File getFile() {
            return file;
        }
    }
}