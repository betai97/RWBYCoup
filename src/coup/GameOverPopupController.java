/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coup;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

/**
 * FXML Controller class
 *
 * @author Ben
 */




public class GameOverPopupController implements Initializable {
    
    @FXML
    Label message;
    
    public void setMessage(String s) {
        message.setText(s);
    }
    
    public void close() {
        
      try {
          /*
         * Close the output stream, close the input stream, close the socket.
         */
        Client.getOS().close();
        Client.getIS().close();
        Client.getClientSocket().close();
        Platform.exit();
        System.exit(0);
      }
      catch(IOException e){
          System.err.println("IOException: " + e);
      }
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
