package coup;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Ben
 */
public class Scene2Controller implements Initializable {

    
    
    private boolean flag = true;
    
    public Stage stage;
    public Parent root;
    public Scene scene;
    
    
    @FXML 
    TextField nickname;
    @FXML
    TextField ipaddress;
    @FXML
    ProgressBar loading;
    @FXML
    Label wait;
    
    
    public void connect() throws InterruptedException, IOException
    {
        if(flag)
        {
            flag = false;
            stage = (Stage) nickname.getScene().getWindow();
            
            Client.main(ipaddress.getText(), nickname.getText(), this, null);  // check if connection success

            loading.setOpacity(1);
            loading.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            wait.setOpacity(1);
            // now we need to wait for confirmation from the server that the game has begun, when it has, client will transition scenes for us

        }
    }
    
    
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
