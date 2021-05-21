package controlador;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import org.bytedeco.javacpp.opencv_videoio;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_videoio.VideoCapture;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

import com.github.sarxos.webcam.Webcam;
import com.sun.prism.paint.Color;

import Entity.Visitante;
import Entity.webCamInfo;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import mundo.Detect;

public class registroController implements Initializable {
	@FXML
	private TextField nombre;
	@FXML
	private TextField apellido;
	@FXML
	private TextField numeroCelular;
	@FXML
	private TextField numeroApartamento;
	@FXML
	private TextField descripcion;

	@FXML
	private TextField numeroDocumento;

	@FXML
	private Pane pane;

	@FXML
	private Pane pane1;

	@FXML
	private Button captura;

	@FXML
	private Button recargar;

	@FXML
	private Button registrar;

	@FXML
	private ImageView foto;

	@FXML
	private Label vNombre;
	@FXML
	private Label vApellido;
	@FXML
	private Label vCelular;
	@FXML
	private Label vApartamento;
	@FXML
	private Label vDocumento;
	@FXML
	private Label vDescripcion;
	@FXML
	private Label vImage;
	@FXML
	private Label vTipoVisitante;

	@FXML
	private ComboBox<String> tipoVisitante;

	private Image image = null;

	private Thread th;
	private Thread th2;

	private String nombre1;
	private String apellido1;
	private String numeroCelular1;
	private String numeroApartamento1;
	private String descripcion1;
	private String numeroDocumento1;
	private String tipoVisitante1;
	private BufferedImage imageVisitante;
	private Detect detect = new Detect();
	private String id;
	private String hostApi;
	private int camaraFoto = -2;
	private boolean verify;
	private int response;

	private opencv_videoio.VideoCapture videoCam;
	Mat imagenCamara = new Mat();
	private BufferedImage bufferImagen;
	Java2DFrameConverter paintConverter = new Java2DFrameConverter();
	OpenCVFrameConverter.ToMat toMatConverter = new OpenCVFrameConverter.ToMat();

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		Properties p = new Properties();
		boolean check = true;
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
		} catch (IOException e) {
			check = false;
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("ERROR");
			alert.setHeaderText("Ha ocurrido un error");
			alert.setContentText(
					"Por favor verifique que el archivo de configuracion se encuentre en la carpeta correspondiente");
			alert.showAndWait();
		}

		hostApi = p.getProperty("HostApi");
		if (hostApi == null) {
			check = false;
		}

		System.err.println(check);

		if (check == true) {

			id = p.getProperty("idEdificio");

			tipoVisitante.getItems().addAll("Visitante", "Domiciliario", "Servicios");
			int index = 0;

			for (Webcam webCamInfo : Webcam.getWebcams()) {
				webCamInfo web = new webCamInfo();

				String cam = webCamInfo.getName().toString().substring(0, webCamInfo.getName().toString().length() - 2);

				if (cam.equals(p.getProperty("CamaraFoto"))) {
					camaraFoto = index;
				}

				index++;

			}

			System.out.println("la camara para la foto esta en el index :" + camaraFoto);
			if (!p.getProperty("CamaraFoto").isEmpty() && p.getProperty("CamaraFoto").length() > 0
					&& camaraFoto != -2) {
				abrirCamara(camaraFoto);
			} else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("ERROR");
				alert.setHeaderText("Ha ocurrido un error");
				alert.setContentText("Error al detectar la camara destinada a realizar la toma de fotos de visitantes");
				alert.showAndWait();

			}

		}

	}

	public void abrirCamara(int indice) {
		Task<Void> webCamInitializer2 = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				videoCam = new VideoCapture();
				videoCam.open(indice);

				leerImagen();

				return null;
			}

		};
		new Thread(webCamInitializer2).start();

	}

	public void leerImagen()

	{
		Task<Void> webCamInitializer2 = new Task<Void>() {
			@Override
			protected Void call() throws Exception {

				while (true) {
					if (videoCam.read(imagenCamara)) {
						bufferImagen = paintConverter.getBufferedImage(toMatConverter.convert(imagenCamara));

						Platform.runLater(() -> {
							final Image imagen = SwingFXUtils.toFXImage(bufferImagen, null);

							foto.setImage(imagen);

						});
						bufferImagen.flush();
					}
				}
			}

		};
		th = new Thread(webCamInitializer2);
		th.setDaemon(true);
		th.start();
	}

	public void prueba() {
		runTask();

	}

	public void runTask() {
		boolean verify = verify();
		Image image1 = new Image("file:///C:/.propiedades/images/carga.gif");
		ImageView imageView1 = new ImageView(image1);
		Text cargando = new Text("Registrando Visitante...");
		if (verify == true)
		{

			imageView1.relocate(372, 423);
			imageView1.setFitHeight(110);
			imageView1.setFitWidth(140);
			cargando.relocate(430, 500);
			cargando.setFont(Font.font("Verdana", 25));
			pane1.getChildren().add(imageView1);
			pane1.getChildren().add(cargando);
		}


		Task<Void> longTask = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				if(verify==true)
				{
					registrar(verify);
				}
				return null;
			}
		};

		longTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent t) {

				if(verify==true) {
					pane1.getChildren().remove(imageView1);
					pane1.getChildren().remove(cargando);
					verificar();
				}
			}
		});

		th2 = new Thread(longTask);
		th2.setDaemon(true);
		th2.start();

	}

	public void verificar() {

		if (response == 1) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("ERROR");
			alert.setHeaderText("Error al conectar con el Servidor De Aplicacion");
			alert.setContentText("Por favor contacte con el proveedor");
			alert.showAndWait();
		}
		if (response == 500) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("ERROR");
			alert.setHeaderText("El visitante ya se encuentra registrado en el sistema");
			alert.setContentText("Por favor verifique el numero de identificacion del visitante");
			alert.showAndWait();
			vDocumento.setText("Por favor verifique el numero de identificacion del visistante");
			vDocumento.setVisible(true);
			// verify = false;
			this.numeroDocumento.setStyle(
					"-fx-border-color: #ba2727;-fx-border-radius: 4px;-fx-border-width: 1px;-fx-border-style: solid");

		}
		if (response == 409) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("ERROR");
			alert.setHeaderText("La captura realizada no contiene ningun rostro humano");
			alert.setContentText("Por favor realice la captura del rostro del visitante nuevamente");
			alert.showAndWait();
			this.recargar();
		}
		if (response == 201) {
			Stage stage = (Stage) this.registrar.getScene().getWindow();
			stage.close();

			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Exito");
			alert.setHeaderText(null);
			alert.setContentText("!El visitante se ha registrado con exito¡");
			alert.showAndWait();
		}
		if (response == 208) {
			Stage stage = (Stage) this.registrar.getScene().getWindow();
			stage.close();

			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText(null);
			alert.setContentText("El rostro del visitante ya se encuentra registrado en el sistema");
			alert.showAndWait();
		}
		salir();
	}

	@FXML
	public void registrar(Boolean verify) {

		System.out.println("todo esta :" + verify);

		if (verify == true) {
			System.out.println("entre al servicio");
			Visitante visitante = new Visitante();
			visitante.setNombre(nombre1);
			visitante.setApellido(apellido1);
			visitante.setNumeroApartamento(numeroApartamento1);
			visitante.setNumeroCelular(numeroCelular1);
			visitante.setNumeroDocumento(numeroDocumento1);
			visitante.setDescripcion(descripcion1);
			visitante.setFoto(imageVisitante);
			visitante.setId(id);
			visitante.setTipoVisitante(tipoVisitante1);

			try {

				response = detect.registrarVisitante(visitante, hostApi);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Termine todo ya");

	}

	public boolean verify() {
		nombre1 = this.nombre.getText();
		apellido1 = this.apellido.getText();
		numeroCelular1 = this.numeroCelular.getText();
		numeroApartamento1 = this.numeroApartamento.getText();
		descripcion1 = this.descripcion.getText();
		numeroDocumento1 = this.numeroDocumento.getText();
		tipoVisitante1 = this.tipoVisitante.getValue();

		verify = true;
		String mensaje = checkText(nombre1.replaceAll("\\s", ""), "Nombre");

		if (tipoVisitante1 == null) {
			vTipoVisitante.setText("Por favor seleccione el tipo de visitante");
			vTipoVisitante.setVisible(true);
			verify = false;

			this.tipoVisitante.setStyle(
					"-fx-border-color: #ba2727;-fx-border-radius: 4px;-fx-border-width: 1px;-fx-border-style: solid");
		} else {
			this.tipoVisitante.setStyle(
					"-fx-border-color: #008CBA;-fx-border-radius: 4px;-fx-border-width: 1px;-fx-border-style: solid");
			vTipoVisitante.setVisible(false);
		}

		if (!mensaje.equals("true")) {
			vNombre.setText(mensaje);
			vNombre.setVisible(true);
			verify = false;
			this.nombre.setStyle(
					"-fx-border-color: #ba2727;-fx-border-radius: 4px;-fx-border-width: 1px;-fx-border-style: solid");

		} else {
			this.nombre.setStyle(
					"-fx-border-color: #008CBA;-fx-border-radius: 4px;-fx-border-width: 1px;-fx-border-style: solid");
			vNombre.setVisible(false);
		}

		mensaje = checkText(apellido1.replaceAll("\\s", ""), "Apellido");
		if (!mensaje.equals("true")) {
			vApellido.setText(mensaje);
			vApellido.setVisible(true);
			verify = false;
			this.apellido.setStyle(
					"-fx-border-color: #ba2727;-fx-border-radius: 4px;-fx-border-width: 1px;-fx-border-style: solid");

		} else {
			this.apellido.setStyle(
					"-fx-border-color: #008CBA;-fx-border-radius: 4px;-fx-border-width: 1px;-fx-border-style: solid");
			vApellido.setVisible(false);
		}

		mensaje = checkNumber("Numero Celular", numeroCelular1.replaceAll("\\s", ""), 10, 10);
		if (!mensaje.equals("true")) {
			vCelular.setText(mensaje);
			vCelular.setVisible(true);
			verify = false;
			this.numeroCelular.setStyle(
					"-fx-border-color: #ba2727;-fx-border-radius: 4px;-fx-border-width: 1px;-fx-border-style: solid");

		} else {
			this.numeroCelular.setStyle(
					"-fx-border-color: #008CBA;-fx-border-radius: 4px;-fx-border-width: 1px;-fx-border-style: solid");
			vCelular.setVisible(false);
		}

		mensaje = checkNumber("Numero Apartamento", numeroApartamento1.replaceAll("\\s", ""), 6, 4);
		if (!mensaje.equals("true")) {
			vApartamento.setText(mensaje);
			vApartamento.setVisible(true);
			verify = false;
			this.numeroApartamento.setStyle(
					"-fx-border-color: #ba2727;-fx-border-radius: 4px;-fx-border-width: 1px;-fx-border-style: solid");

		} else {
			this.numeroApartamento.setStyle(
					"-fx-border-color: #008CBA;-fx-border-radius: 4px;-fx-border-width: 1px;-fx-border-style: solid");

			vApartamento.setVisible(false);
		}

		mensaje = checkNumber("Numero Documento", numeroDocumento1.replaceAll("\\s", ""), 12, 8);
		if (!mensaje.equals("true")) {
			vDocumento.setText(mensaje);
			vDocumento.setVisible(true);
			verify = false;
			this.numeroDocumento.setStyle(
					"-fx-border-color: #ba2727;-fx-border-radius: 4px;-fx-border-width: 1px;-fx-border-style: solid");
		} else {
			this.numeroDocumento.setStyle(
					"-fx-border-color:#008CBA;-fx-border-radius: 4px;-fx-border-width: 1px;-fx-border-style: solid");
			vDocumento.setVisible(false);
		}

		if (descripcion1.replaceAll("\\s", "").length() == 0) {
			vDescripcion.setText("El campo Descripción es obligatorio");
			vDescripcion.setVisible(true);
			verify = false;
			this.descripcion.setStyle(
					"-fx-border-color: #ba2727;-fx-border-radius: 4px;-fx-border-width: 1px;-fx-border-style: solid");

		} else {
			this.descripcion.setStyle(
					"-fx-border-color: #008CBA;-fx-border-radius: 4px;-fx-border-width: 1px;-fx-border-style: solid");
			vDescripcion.setVisible(false);
		}

		if (image == null) {

			vImage.setText("Por favor realice la captura del rostrol del visitante");
			vImage.setVisible(true);
			verify = false;
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Advertencia");
			alert.setHeaderText("No se ha detectado la captura del rostro del visitante");
			alert.setContentText("Por favor realice la captura del rostro del visitante");
			alert.showAndWait();

		} else
		{
			vImage.setVisible(false);
		}

		return verify;

	}

	@FXML
	public void salir() {
		if (camaraFoto != -2) {
			if (videoCam.isOpened()) {
				videoCam.release();
				th.interrupt();
				image = null;

			}
		}

	}

	@FXML
	private void recargar() {
		System.out.println(image);
		if (this.image != null) {
			System.out.println("si se puede reiniciar");
			image = null;
			pane.setStyle("-fx-background-color: #ba2727");
			captura.setStyle("-fx-border-color: #ba2727");
			captura.setText("Realizar Captura");
			initialize(null, null);

		} else {
			System.out.println("no se puede recargar");
		}

	}

	@FXML
	public void tomarFoto() {
		if (camaraFoto != -2) {
			image = foto.getImage();
			imageVisitante = bufferImagen;
			if (videoCam.isOpened()) {
				videoCam.release();
				th.interrupt();

				foto.setImage(image);

				pane.setStyle("-fx-background-color: #008CBA");
				captura.setStyle("-fx-border-color: #008CBA");
				captura.setText("Captura Realizada");

				vImage.setVisible(false);
			}
		} else {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("ERROR");
			alert.setHeaderText("Ha ocurrido un error");
			alert.setContentText("Error al detectar la camara destinada a realizar la toma de fotos de visitantes");
			alert.showAndWait();
		}

	}

	public boolean isTexto(String cadena) {

		for (int x = 0; x < cadena.length(); x++) {
			char c = cadena.charAt(x);
			if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == ' ')) {
				return false;
			}
		}
		return true;
	}

	public String checkText(String cadena, String dato) {
		if (!isTexto(cadena)) {
			return "El campo " + dato + " Solo acepta Caracteres alfabeticos";
		} else if (cadena == null || cadena.equals("null")) {
			return "El campo " + dato + " debe ser diferente de null";
		} else if (cadena.length() == 0) {
			return "El campo " + dato + " es obligatorio";
		}

		return "true";

	}

	public boolean isNumber(String dato) {
		try {
			Long number = Long.parseLong(dato);
			return true;

		} catch (NumberFormatException e) {
			return false;
		}
	}

	public String checkNumber(String mensaje, String dato, int max, int min) {

		if (dato == null || dato.equals("null")) {
			return "El campo " + dato + " debe ser diferente de null";
		} else if (dato.length() == 0) {
			return "El campo " + dato + " es obligatorio";
		} else if (!isNumber(dato)) {
			return "El campo " + mensaje + " Solo acepta Caracteres numericos";
		} else if (dato.length() < min) {
			return "El campo " + mensaje + " debe contenera al menos " + min + " digitos";
		} else if (dato.length() > max) {
			return "El campo " + mensaje + " debe contenera  maximo " + max + " digitos";
		}

		return "true";

	}
}
