package mundo;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.ConnectException;
import java.net.Socket;
import java.net.URISyntaxException;

import java.util.UUID;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import Entity.Visitante;

public class Detect {

	public void reconocerRostro(BufferedImage image, int id, String accion, String host, String puerto, String hostApi)
			throws URISyntaxException, IOException {
		HttpClient httpclient = HttpClients.createDefault();
		HttpPost post = new HttpPost(hostApi + "/process/residentes/verify");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "png", baos);
		baos.flush();
		byte[] bytes = baos.toByteArray();
		HttpResponse response;

		MultipartEntityBuilder multiPart = MultipartEntityBuilder.create();
		multiPart.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		multiPart.addBinaryBody("archivo", bytes, ContentType.DEFAULT_BINARY, "Registro" + UUID.randomUUID());
		multiPart.addTextBody("id", id + "", ContentType.DEFAULT_BINARY);
		multiPart.addTextBody("accion", accion, ContentType.DEFAULT_BINARY);

		HttpEntity entity = multiPart.build();

		post.setEntity(entity);

		try {
			response = httpclient.execute(post);

			HttpEntity entitys = response.getEntity();

			if (entitys != null) {
				String retSrc = EntityUtils.toString(entitys);

				JSONObject result = new JSONObject(retSrc); // Convert String to JSON Object


				if (result.get("accion").equals("true")) {
					abrirPuerta(host, puerto, result.get("code") + "", image, accion);
					Thread.sleep(5000);
				}
			}

		} catch (HttpHostConnectException | InterruptedException e)
		{
			Platform.runLater(() ->{
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("ERROR");
				alert.setHeaderText("Ha ocurrido un error");
				alert.setContentText("Error al conectar con el SERVIDOR API");
				alert.showAndWait();

			});

		}

	}

	public int registrarVisitante(Visitante visitante, String hostApi) throws IOException {
		HttpClient httpclient = HttpClients.createDefault();
		HttpPost post = new HttpPost(hostApi + "/process/residentes/registrar");
		HttpResponse response;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(visitante.getFoto(), "png", baos);
		baos.flush();
		byte[] bytes = baos.toByteArray();

		MultipartEntityBuilder multiPart = MultipartEntityBuilder.create();
		multiPart.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
		multiPart.addBinaryBody("archivo", bytes, ContentType.DEFAULT_BINARY, "visitante" + "_" + UUID.randomUUID());

		multiPart.addTextBody("nombre", visitante.getNombre(), ContentType.DEFAULT_BINARY);

		multiPart.addTextBody("apellido", visitante.getApellido(), ContentType.DEFAULT_BINARY);

		multiPart.addTextBody("celular", visitante.getNumeroCelular(), ContentType.DEFAULT_BINARY);

		multiPart.addTextBody("apartamento", visitante.getNumeroApartamento(), ContentType.DEFAULT_BINARY);

		multiPart.addTextBody("identificacion", visitante.getNumeroDocumento(), ContentType.DEFAULT_BINARY);

		multiPart.addTextBody("descripcion", visitante.getDescripcion(), ContentType.DEFAULT_BINARY);

		multiPart.addTextBody("id", visitante.getId(), ContentType.DEFAULT_BINARY);

		multiPart.addTextBody("tipo", visitante.getTipoVisitante(), ContentType.DEFAULT_BINARY);

		HttpEntity entity = multiPart.build();

		post.setEntity(entity);

		try {
			response = httpclient.execute(post);

		} catch (HttpHostConnectException a) {

			Platform.runLater(() ->{
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("ERROR");
				alert.setHeaderText("Ha ocurrido un error");
				alert.setContentText("Error al conectar con el SERVIDOR API");
				alert.showAndWait();

			});

			return 1;
		}

		return response.getStatusLine().getStatusCode();

	}

	public void abrirPuertaButton(String host, String puerto, int mensaje) {

		try {
			Socket cliente = new Socket(host, Integer.parseInt(puerto));
			DataOutputStream dos = new DataOutputStream(cliente.getOutputStream());

			dos.writeUTF(mensaje + "");

			cliente.close();
			dos.close();
		} catch (NumberFormatException | IOException e1) {

		}

	}

	public void abrirPuerta(String host, String puerto, String code, BufferedImage image, String accion) {
		try {
			Socket cliente = new Socket(host, Integer.parseInt(puerto));

			DataOutputStream dos = new DataOutputStream(cliente.getOutputStream());

			dos.writeUTF(code);
			dos.writeUTF(accion);

			BufferedOutputStream bos = new BufferedOutputStream(cliente.getOutputStream());

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos);
			baos.flush();
			byte[] bytes = baos.toByteArray();

			dos.writeInt(bytes.length);

			for (int i = 0; i < bytes.length; i++) {
				bos.write(bytes[i]);
			}

			bos.close();
			cliente.close();

		}
		catch (ConnectException e)
		{
			e.printStackTrace();
			Platform.runLater(() ->{
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("ERROR");
				alert.setHeaderText("Ha ocurrido un error");
				alert.setContentText("Error al conectar con el SERVIDOR DE LA PUERTA");
				alert.showAndWait();

			});

		}
		catch (NumberFormatException | IOException e1) {

			e1.printStackTrace();
			Platform.runLater(() ->{
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("ERROR");
				alert.setHeaderText("Ha ocurrido un error");
				alert.setContentText("Error al conectar con el SERVIDOR DE LA PUERTA");
				alert.showAndWait();

			});
		}

	}

}
