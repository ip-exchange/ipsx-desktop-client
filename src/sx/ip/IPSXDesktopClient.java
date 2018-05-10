package sx.ip;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import sx.ip.factories.HostServicesControllerFactory;

/**
 *
 *
 *
 * @author Renan
 *
 */
public class IPSXDesktopClient extends Application {

    //define your offsets here
    private double xOffset = 0;

    private double yOffset = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLManualProxy.fxml"));

        loader.setControllerFactory(new HostServicesControllerFactory(getHostServices()));

        Parent root = loader.load();

        //grab your root here
        root.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }

        });

        //move around here
        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }

        });

        Scene scene = new Scene(root);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("imgs/icon.png")));
        stage.setScene(scene);
        stage.show();
    }

    /**
     *
     * @param args the command line arguments
     *
     */
    public static void main(String[] args) {
        launch(args);
    }

}
