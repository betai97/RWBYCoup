/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coup;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * FXML Controller class
 *
 * @author Ben
 */
public class Scene1Controller implements Initializable {

    @FXML
    private Button host;
    @FXML
    private Button connect;
    
    
    

    @FXML
    private void handleButtonAction(ActionEvent event) throws IOException {
        Stage stage; 
        Parent root;
        if (event.getSource() == host) {
            stage = (Stage) host.getScene().getWindow();
            root = FXMLLoader.load(getClass().getResource("Scene3.fxml"));
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent t) {
                    Server.stopWaiting();
                    Client.terminate();
                }
            });
        } else {
            stage = (Stage) connect.getScene().getWindow();
            root = FXMLLoader.load(getClass().getResource("Scene2.fxml"));
        }
        Scene scene = new Scene(root);

        stage.setTitle("RWBY Coup");
        stage.setResizable(false);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
    }

}
