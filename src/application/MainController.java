package application;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
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

    // Nuevo: StackPane donde va centrado el video
    @FXML
    private StackPane centerPane;

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

    @FXML
    private Slider volumeSlider;

    @FXML
    private Slider progressSlider;

    // Paneles laterales izquierdo y derecho
    @FXML
    private TitledPane leftPane;
    @FXML
    private TitledPane rightPane;

    // Reproductor
    private MediaPlayer mediaPlayer;

    // Tamaño por defecto para el botón "Cambiar Tamaño"
    private double defaultWidth = 640;
    private double defaultHeight = 360;

    // Lista observable para la biblioteca
    private ObservableList<FileInfo> libraryFiles = FXCollections.observableArrayList();

    // Flag para un "modo maximizado" (forzar a ocupar todo el espacio)
    private boolean maximized = false;

    @FXML
    public void initialize() {
        // Configurar columnas de la tabla
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colFormato.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormato()));
        colDuracion.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDuracion()));

        // Cargar biblioteca desde el directorio "media"
        loadLibrary();

        // Reproducir al seleccionar un archivo en la tabla
        tableLibrary.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                openFile(newSel.getFile());
            }
        });

        // Ajustes iniciales de la MediaView
        mediaView.setPreserveRatio(true);
        mediaView.setSmooth(true);

        // Escuchamos cambios de tamaño en el StackPane "centerPane" para ajustar el video
        centerPane.widthProperty().addListener((obs, oldVal, newVal) -> adjustVideoSize());
        centerPane.heightProperty().addListener((obs, oldVal, newVal) -> adjustVideoSize());

        mediaView.minWidth(640);
        mediaView.minHeight(360);
        
        // Configurar el Label para que se ajuste al ancho y centre su contenido
        fileTitle.setMaxWidth(Double.MAX_VALUE);
        fileTitle.setAlignment(Pos.CENTER);
        
        // Configuraciones existentes:
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colFormato.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFormato()));
        colDuracion.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDuracion()));
        loadLibrary();
    }

    /**
     * Método que ajusta el tamaño del video según:
     *  - si cabe a tamaño original, NO se escala,
     *  - si no cabe, se reduce para ocupar el máximo,
     *  - si está en modo "maximized", siempre se ocupa todo el espacio.
     */
    private void adjustVideoSize() {
        if (mediaPlayer == null) return; // Si no hay video cargado, no hacemos nada

        // Dimensiones originales del video
        double videoWidth = mediaPlayer.getMedia().getWidth();
        double videoHeight = mediaPlayer.getMedia().getHeight();

        // Espacio disponible en el StackPane
        double paneWidth = centerPane.getWidth();
        double paneHeight = centerPane.getHeight();

        if (maximized) {
            // Si está en modo "maximizado", siempre llena el contenedor
            mediaView.setFitWidth(paneWidth);
            mediaView.setFitHeight(paneHeight);
        } else {
            // Si NO está maximizado, solo escalar si el video no cabe
            boolean cabeEnAncho = (videoWidth <= paneWidth);
            boolean cabeEnAlto = (videoHeight <= paneHeight);

            if (cabeEnAncho && cabeEnAlto) {
                // Muestra a su tamaño original
                mediaView.setFitWidth(videoWidth);
                mediaView.setFitHeight(videoHeight);
            } else {
                // Escala para ocupar el máximo (manteniendo aspecto por preserveRatio)
                mediaView.setFitWidth(paneWidth);
                mediaView.setFitHeight(paneHeight);
            }
        }
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
                    // Simplificamos: la duración se muestra "Desconocido"
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
     * Abre un archivo con FileChooser.
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
            // Muestra info del archivo
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

            // Ajusta la velocidad
            mediaPlayer.setRate(speedSlider.getValue());

            // Cuando el media está listo, configuramos la barra de progreso y ajustamos tamaño
            mediaPlayer.setOnReady(() -> {
                Duration total = mediaPlayer.getTotalDuration();
                progressSlider.setMax(total.toSeconds());
                adjustVideoSize(); // Ajustamos el tamaño la primera vez
            });

            // Sincroniza el slider de progreso
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!progressSlider.isValueChanging()) {
                    progressSlider.setValue(newTime.toSeconds());
                }
            });

            // Permite saltar en la reproducción
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
     * Muestra información "Acerca de".
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
     * Refresca la biblioteca (directorios).
     */
    @FXML
    private void handleRefrescarBiblioteca(ActionEvent event) {
        loadLibrary();
    }

    /**
     * Alterna pantalla completa.
     */
    @FXML
    private void handlePantallaCompleta(ActionEvent event) {
        Stage stage = (Stage) mediaView.getScene().getWindow();
        // Usamos Platform.runLater para asegurarnos de que el cambio se realice correctamente
        Platform.runLater(() -> {
            if (!stage.isFullScreen()) {
                stage.setFullScreen(true);
            } else {
                stage.setFullScreen(false);
                // Opcionalmente, se puede maximizar la ventana en modo "normal"
                stage.setMaximized(true);
            }
        });
    }
    

    /**
     * Play.
     */
    @FXML
    private void handlePlay(ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    /**
     * Pausa.
     */
    @FXML
    private void handlePause(ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    /**
     * Stop.
     */
    @FXML
    private void handleStop(ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    /**
     * Botón "Cambiar Tamaño": en este ejemplo, alterna un tamaño fijo (640x360)
     * con un "modo maximizado" que ocupa todo el StackPane.
     */
    @FXML
    private void handleCambiarTamano(ActionEvent event) {
        // Si ya está en modo maximizado, vuelve a 640x360
        if (maximized) {
            maximized = false;
            mediaView.setFitWidth(defaultWidth);
            mediaView.setFitHeight(defaultHeight);
        } else {
            // Activa modo maximizado
            maximized = true;
        }
        // Reajusta con la lógica general
        adjustVideoSize();
    }

    /**
     * Cambia la velocidad de reproducción según el slider.
     */
    @FXML
    private void handleCambiarVelocidad(MouseEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.setRate(speedSlider.getValue());
        }
    }

    /**
     * Alterna la visibilidad del panel izquierdo (Edición).
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
     * Alterna la visibilidad del panel derecho (Biblioteca).
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
     * Cambia el volumen según el slider.
     */
    @FXML
    public void handleCambiarVolumen(MouseEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volumeSlider.getValue());
        }
    }

    /**
     * Clase interna para modelar la información de cada archivo en la biblioteca.
     */
    public static class FileInfo {
        private final String nombre;
        private final String formato;
        private final String duracion;
        private final File file;

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