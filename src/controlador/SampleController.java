package controlador;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.Rect;
import org.bytedeco.javacpp.opencv_core.RectVector;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacpp.opencv_objdetect.CascadeClassifier;
import org.bytedeco.javacpp.opencv_videoio;
import org.bytedeco.javacpp.opencv_videoio.VideoCapture;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

import com.github.sarxos.webcam.Webcam;

import Entity.webCamInfo;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mundo.Detect;

public class SampleController implements Initializable {

	@FXML
	private Pane root;

	@FXML
	private VBox vBox;
	@FXML
	private VBox vBox2;

	@FXML
	private ImageView face1;
	@FXML
	private ImageView face2;

	@FXML
	private Button register;

	@FXML
	private Button puerta;

	private Detect detect = new Detect();

	private int a = 0;

	private BufferedImage bufferImagen;
	private BufferedImage bufferImagen2;

	private opencv_videoio.VideoCapture videoCam;
	private VideoCapture videoCam2;

	// Imagen Camara 1
	Mat imagenCamara = new Mat();
	Mat imagenGray = new Mat();
	Mat imagenEqlz = new Mat();

	// Imagen Camara 2
	Mat imagenCamara2 = new Mat();
	Mat imagenGray2 = new opencv_core.Mat();
	Mat imagenEqlz2 = new opencv_core.Mat();

	private int indiceCamaraEntrada = -2;
	private int indiceCamaraSalida = -2;
	private String camaraSalida;
	private String camaraEntrada;
	private int escala = 4;
	private int idEdificio;
	private String host;
	private String puerto;
	private String hostApi;
	Java2DFrameConverter paintConverter = new Java2DFrameConverter();
	OpenCVFrameConverter.ToMat toMatConverter = new OpenCVFrameConverter.ToMat();
	Java2DFrameConverter paintConverter1 = new Java2DFrameConverter();
	OpenCVFrameConverter.ToMat toMatConverter1 = new OpenCVFrameConverter.ToMat();

	CascadeClassifier face_cascade = new CascadeClassifier(
			"C:\\.propiedades\\cascade\\haarcascade_frontalface_alt2.xml");
	RectVector rv = new RectVector();
	RectVector rv2 = new RectVector();

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		boolean check = true;

		Properties p = new Properties();
		try {
			p.load(new FileReader("C://.propiedades//properties.properties"));
		} catch (FileNotFoundException e) {
			check = false;
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("ERROR");
			alert.setHeaderText("Ha ocurrido un error");
			alert.setContentText(
					"Por favor verifique que el archivo de configuracion se encuentre en la carpeta correspondiente");
			alert.showAndWait();
			register.setDisable(true);
			puerta.setDisable(true);
		} catch (IOException e) {
			check = false;
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("ERROR");
			alert.setHeaderText("Ha ocurrido un error");
			alert.setContentText(
					"Por favor verifique que el archivo de configuracion se encuentre en la carpeta correspondiente");
			alert.showAndWait();
			register.setDisable(true);
			puerta.setDisable(true);
		}

		try {
			idEdificio = Integer.parseInt(p.getProperty("idEdificio"));
		} catch (NumberFormatException e) {
			check = false;
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("ERROR");
			alert.setHeaderText("Ha ocurrido un error");
			alert.setContentText("Por favor verifique el id del Edificio en el archivo de configuracion");
			alert.showAndWait();
			register.setDisable(true);
			puerta.setDisable(true);

		}

		host = verificar(p.getProperty("Host"), "Host");
		puerto = verificar(p.getProperty("Puerto"), "Puerto");
		hostApi = verificar(p.getProperty("HostApi"), "HostApi");

		if (host == null || puerto == null || hostApi == null) {
			check = false;
		}

		if (check == true) {

			int index = 0;

			for (Webcam webCamInfo : Webcam.getWebcams()) {
				webCamInfo web = new webCamInfo();

				String cam = webCamInfo.getName().toString().substring(0, webCamInfo.getName().toString().length() - 2);

				if (cam.equals(p.getProperty("CamaraSalida"))) {
					indiceCamaraSalida = index;
				}
				if (cam.equals(p.getProperty("CamaraEntrada"))) {
					indiceCamaraEntrada = index;
				}
				index++;
			}

			if (indiceCamaraEntrada != -2) {
				abrirCamara(indiceCamaraEntrada);

			} else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("ERROR");
				alert.setHeaderText("Ha ocurrido un error");
				alert.setContentText("Error al detectar la camara ubicada en la entrada del conjunto residencial");
				alert.showAndWait();

			}

			if (indiceCamaraSalida != -2) {
				abrirCamara2(indiceCamaraSalida);
			} else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("ERROR");
				alert.setHeaderText("Ha ocurrido un error");
				alert.setContentText("Error al detectar la camara ubicada en la salida del conjunto residencial");
				alert.showAndWait();
			}

		}

	}

	public String verificar(String verify, String name) {

		if (verify != null && verify.length() > 0) {
			return verify;
		} else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("ERROR");
			alert.setHeaderText("Ha ocurrido un error");
			alert.setContentText("Por favor verifique el " + name + " en el archivo de configuracion");
			alert.showAndWait();
			register.setDisable(true);
			puerta.setDisable(true);

			return null;
		}

	}

	public void abrirCamara(int indice) {
		Task<Void> webCamInitializer = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				videoCam = new VideoCapture();
				videoCam.open(indice);

				leerImagen();

				return null;
			}

		};
		new Thread(webCamInitializer).start();

	}

	public void leerImagen()

	{
		Task<Void> webCamInitializer = new Task<Void>() {

			@Override
			protected Void call() throws Exception {

				while (true) {
					if (videoCam.read(imagenCamara)) {
						opencv_imgproc.cvtColor(imagenCamara, imagenGray, opencv_imgproc.CV_BGR2GRAY);
						opencv_imgproc.equalizeHist(imagenGray, imagenEqlz);
						opencv_imgproc.resize(imagenEqlz, imagenEqlz,
								new opencv_core.Size(imagenCamara.cols() / 4, imagenCamara.rows() / 4), 0, 0,
								opencv_imgproc.INTER_LINEAR);

						face_cascade.detectMultiScale(imagenEqlz, rv, 1.15, 3,
								opencv_objdetect.CASCADE_DO_CANNY_PRUNING, new Size(0, 0), new Size(400, 400));
						imagenCamara.data();

						Rect r = new Rect(20, 20, 220, 220);
						if (rv.empty()) {
						} else {

							detect.reconocerRostro(bufferImagen, idEdificio, "Ingreso", host, puerto, hostApi);

						}

						bufferImagen = paintConverter.getBufferedImage(toMatConverter.convert(imagenCamara));

						Platform.runLater(() -> {
							final Image imagen = SwingFXUtils.toFXImage(bufferImagen, null);

							face1.setImage(imagen);
						});
						bufferImagen.flush();
					}
				}
			}

		};

		Thread th = new Thread(webCamInitializer);
		th.setDaemon(true);
		th.start();

	}

	public void abrirCamara2(int indice) {
		Task<Void> webCamInitializer = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				videoCam2 = new VideoCapture();
				videoCam2.open(indice);

				leerImagen2();

				return null;
			}

		};
		Thread th = new Thread(webCamInitializer);
		th.setDaemon(true);
		th.start();

	}

	public void leerImagen2() {
		Task<Void> webCamInitializer = new Task<Void>() {

			@Override
			protected Void call() throws Exception {

				while (true) {
					if (videoCam2.read(imagenCamara2)) {
						opencv_imgproc.cvtColor(imagenCamara2, imagenGray2, opencv_imgproc.CV_BGR2GRAY);
						opencv_imgproc.equalizeHist(imagenGray2, imagenEqlz2);
						opencv_imgproc.resize(imagenEqlz2, imagenEqlz2,
								new opencv_core.Size(imagenCamara2.cols() / 4, imagenCamara2.rows() / 4), 0, 0,
								opencv_imgproc.INTER_LINEAR);

						face_cascade.detectMultiScale(imagenEqlz2, rv2, 1.15, 3,
								opencv_objdetect.CASCADE_DO_CANNY_PRUNING, new Size(0, 0), new Size(120, 120));

						if (rv2.empty()) {
						} else {
							System.out.println("si veo algo 2");
							detect.reconocerRostro(bufferImagen2, idEdificio, "Salida", host, puerto, hostApi);

						}

						bufferImagen2 = paintConverter1.getBufferedImage(toMatConverter1.convert(imagenCamara2));
						Platform.runLater(() -> {
							final Image imagen = SwingFXUtils.toFXImage(bufferImagen2, null);

							face2.setImage(imagen);
						});
						bufferImagen2.flush();
					}
				}
			}

		};
		Thread th = new Thread(webCamInitializer);
		th.setDaemon(true);
		th.start();
	}

	public void cerrarCamara() {

		if (indiceCamaraEntrada != -2) {
			if (videoCam.isOpened()) {
				videoCam.release();
			}
		}
		if (indiceCamaraSalida != -2) {
			if (videoCam2.isOpened()) {
				videoCam2.release();
			}
		}

	}

	@FXML
	private void registrar() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/interfaz/Registro.fxml"));

			Parent root = loader.load();

			registroController controlador = loader.getController();
			Scene scene = new Scene(root);
			Stage stage = new Stage();
			stage.setTitle("Registro De Visitantes");
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setMaxHeight(680);
			stage.setMaxWidth(850);
			stage.resizableProperty().setValue(false);
			stage.setScene(scene);
			stage.setOnCloseRequest(evt -> {
				controlador.salir();

			});
			stage.showAndWait();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void abrirPuerta() {
		detect.abrirPuertaButton(host, puerto, 3);
	}
}
