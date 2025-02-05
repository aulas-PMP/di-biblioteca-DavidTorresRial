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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import java.io.InputStream;

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

    private ImageView audioImageView;

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
    
            // Configurar el ImageView para archivos de audio
            InputStream imgStream = getClass().getResourceAsStream("img/musica-removebg-preview.png");
            if (imgStream == null) {
                System.err.println("No se encontró la imagen en 'img/musica-removebg-preview.png'");
            } else {
                ImageView audioImageView = new ImageView(new Image(imgStream));
                audioImageView.setPreserveRatio(true);
                audioImageView.setSmooth(true);
                audioImageView.setVisible(false); // inicialmente no se muestra
                // Añade el ImageView al centro; como centerPane es un StackPane, los nodos se apilan.
                centerPane.getChildren().add(audioImageView);
                // Guarda audioImageView en un atributo de la clase si lo necesitas en otros métodos
                this.audioImageView = audioImageView;
        }

        // Configuraciones existentes (se pueden eliminar duplicados)
        // Estos setCellValueFactory ya se configuraron al inicio, por lo que puedes eliminarlos si son duplicados
        // colNombre.setCellValueFactory(...);
        // colFormato.setCellValueFactory(...);
        // colDuracion.setCellValueFactory(...);
        // loadLibrary();
    }


    private void adjustVideoSize() {
        if (mediaPlayer == null) return; // Si no hay video cargado, no hacemos nada
    
        // Dimensiones originales del video
        double videoWidth = mediaPlayer.getMedia().getWidth();
        double videoHeight = mediaPlayer.getMedia().getHeight();
    
        // Espacio disponible en el StackPane (la zona central del BorderPane)
        double paneWidth = centerPane.getWidth();
        double paneHeight = centerPane.getHeight();
    
        if (maximized) {
            // En modo maximizado, ocupa todo el centro
            mediaView.setFitWidth(paneWidth);
            mediaView.setFitHeight(paneHeight);
        } else {
            // Si el video cabe en la zona central, se muestra a su tamaño original;
            // de lo contrario, se escala para ocupar el máximo (manteniendo preserveRatio)
            boolean cabeEnAncho = (videoWidth <= paneWidth);
            boolean cabeEnAlto = (videoHeight <= paneHeight);
    
            if (cabeEnAncho && cabeEnAlto) {
                mediaView.setFitWidth(videoWidth);
                mediaView.setFitHeight(videoHeight);
            } else {
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
                    String nombre = f.getName();
                    String formato = nombre.substring(nombre.lastIndexOf(".") + 1);
                    // Creamos FileInfo sin duración definida aún
                    FileInfo info = new FileInfo(nombre, formato, f);
                    libraryFiles.add(info);
                    
                    try {
                        Media media = new Media(f.toURI().toString());
                        MediaPlayer tempPlayer = new MediaPlayer(media);
                        
                        // Listener para detectar cuando se conoce la duración
                        tempPlayer.totalDurationProperty().addListener((obs, oldDuration, newDuration) -> {
                            if(newDuration != null && !newDuration.isUnknown() && newDuration.greaterThan(Duration.ZERO)) {
                                Platform.runLater(() -> info.setDuracion(formatDuration(newDuration)));
                                tempPlayer.dispose(); // Liberamos recursos
                            }
                        });
                        
                        tempPlayer.setOnError(() -> {
                            Platform.runLater(() -> info.setDuracion("Error"));
                            tempPlayer.dispose();
                        });
                        
                        // También se puede usar onReady como respaldo
                        tempPlayer.setOnReady(() -> {
                            Duration total = tempPlayer.getTotalDuration();
                            if(total != null && !total.isUnknown() && total.greaterThan(Duration.ZERO)) {
                                Platform.runLater(() -> info.setDuracion(formatDuration(total)));
                            } else {
                                Platform.runLater(() -> info.setDuracion("0:00"));
                            }
                            tempPlayer.dispose();
                        });
                        
                    } catch(Exception ex) {
                        info.setDuracion("Desconocido");
                    }                    
                }
            }
        }
        tableLibrary.setItems(libraryFiles);
    }
    
    private String formatDuration(Duration duration) {
        int seconds = (int) Math.floor(duration.toSeconds());
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
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
    
            // Cuando la media esté lista, configuramos la barra de progreso, verificamos si es audio y ajustamos el tamaño
            mediaPlayer.setOnReady(() -> {
                // Comprobamos si la media tiene dimensiones válidas (video) o no (audio)
                double videoWidth = media.getWidth();
                double videoHeight = media.getHeight();
                if (videoWidth <= 0 || videoHeight <= 0) {
                    // Es audio: ocultamos la MediaView y mostramos el audioImageView
                    mediaView.setVisible(false);
                    if (audioImageView != null) {
                        audioImageView.setVisible(true);
                        // Ajustamos el tamaño de la imagen para que ocupe el espacio del centerPane
                        audioImageView.setFitWidth(centerPane.getWidth());
                        audioImageView.setFitHeight(centerPane.getHeight());
                    }
                } else {
                    // Es video: mostramos la MediaView y ocultamos la imagen de audio
                    mediaView.setVisible(true);
                    if (audioImageView != null) {
                        audioImageView.setVisible(false);
                    }
                    adjustVideoSize();
                }
    
                Duration total = mediaPlayer.getTotalDuration();
                progressSlider.setMax(total.toSeconds());
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
        alert.setHeaderText("DaTo Media");
        alert.setContentText("Profesor: Pablo Martínez Pavón\nAlumno: David Torres Rial");
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

    @FXML
    private void handleToggleLeft(ActionEvent event) {
        boolean actualmenteVisible = leftPane.isVisible();
        // Alternamos visibilidad y gestión en el layout
        leftPane.setVisible(!actualmenteVisible);
        leftPane.setManaged(!actualmenteVisible);
    
        // Forzamos la actualización del layout del BorderPane
        borderPane.requestLayout();
        // Ejecutamos adjustVideoSize() una vez se complete el layout
        Platform.runLater(() -> adjustVideoSize());
    }
    
    @FXML
    private void handleToggleRight(ActionEvent event) {
        boolean actualmenteVisible = rightPane.isVisible();
        rightPane.setVisible(!actualmenteVisible);
        rightPane.setManaged(!actualmenteVisible);
    
        borderPane.requestLayout();
        Platform.runLater(() -> adjustVideoSize());
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
        private final File file;
        private final SimpleStringProperty duracion;
    
        public FileInfo(String nombre, String formato, File file) {
            this.nombre = nombre;
            this.formato = formato;
            this.file = file;
            // Inicialmente mostramos "Cargando..." hasta obtener la duración real.
            this.duracion = new SimpleStringProperty("Cargando...");
        }
    
        public String getNombre() {
            return nombre;
        }
    
        public String getFormato() {
            return formato;
        }
    
        public String getDuracion() {
            return duracion.get();
        }
        
        public void setDuracion(String duracion) {
            this.duracion.set(duracion);
        }
        
        public SimpleStringProperty duracionProperty() {
            return duracion;
        }
    
        public File getFile() {
            return file;
        }
    }    
}