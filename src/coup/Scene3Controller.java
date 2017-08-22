package coup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Ben
 */
public class Scene3Controller implements Initializable {

    static boolean flag = false;//ensure server only launched once
    static boolean flag2 = false;//ensure user hosting server only connects once
    
    
    public Stage stage;
    public Parent root;
    public Scene scene;
    
    
    
    @FXML
    TextField nickname;
    @FXML
    Button startbutton;
    @FXML
    private TextArea connected;
    @FXML 
    Label ip;
    
    
    
    //TD:
    //ip addr on init   -- we're gonna want outbound ip address, set up port forwarding on your router ,, etc
    //make next stage on init to pass textfield to client
    //handle users exiting - make server send bye message to all clients
    
    
    @FXML
    public void nicknameconnect() throws IOException {
        if(!flag2 && flag) {//if server has been started and this code hasn't been executed before
            stage = (Stage) startbutton.getScene().getWindow();
            
            Client.main("localhost", nickname.getText(), null, this);
            flag2=true;
        }
    }
    
    
    @FXML
    public void startgame() throws IOException, InterruptedException {
        if (flag2) {
            //stop waiting for clients to connect
            Server.stopWaiting();

            //send start message to clients
            Client.writeToOS("$startgame");

            Client.writeToOS("$players " + Integer.toString(Server.usersNum));
        }

    }

    @FXML
    public void host() {
        if (!flag) {
            connected.appendText("---Beginning to host server---\n");

            Server.main(connected);

            flag = true;
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //get external ip and present to user for them to share
        try {
            URL whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            String ipaddr = in.readLine();
            ip.setText("Your IP Address: " + ipaddr);
        } catch (Exception e) {
            System.err.println("Exception: "+e);
        }
        
    }    
    
}
