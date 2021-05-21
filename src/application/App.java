package application;
	
import controlador.SampleController;
import javafx.application.Application;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;


public class App extends Application {
	@Override
	public void start(Stage primaryStage) throws Exception {
		
			FXMLLoader loader = new FXMLLoader();
			Parent root = loader.load(getClass().getResourceAsStream("/interfaz/Sample.fxml"));
			
			SampleController sampleController = loader.getController();

			Scene scene = new Scene(root);
		//	scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			Screen screen = Screen.getPrimary();
			Rectangle2D bounds = screen.getVisualBounds();
		
			//primaryStage.setX(bounds.getMinX());
		//	primaryStage.setY(bounds.getMinY());
			primaryStage.setMaxHeight(740);
			primaryStage.setMaxWidth(1220);
		//	primaryStage.centerOnScreen();
			primaryStage.setScene(scene);
			primaryStage.setTitle("Plataforma De Administracion De Ingreso & Salida");
			primaryStage.resizableProperty().setValue(false);
			
			 primaryStage.setOnCloseRequest(evt->
			 {
				 	System.out.println("hola mundo");
				 	sampleController.cerrarCamara();
		     });    
			
			primaryStage.show();
			
		
	}
	
	public static void main(String[] args) {
		launch();
	}
}
