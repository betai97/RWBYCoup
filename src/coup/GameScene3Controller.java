/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coup;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 *
 * @author Ben
 */
public class GameScene3Controller implements Initializable {
    
    
    BooleanHold turn, respond, choice, challenge, claim;
    
    @FXML 
    private TextArea textarea, dialogue;
    @FXML
    private TextField textfield;
    @FXML
    private ImageView player1card1, player1card2, playercard1, playercard2, player2card1, player2card2, rwbyidentity, light;
    @FXML
    Label player1coins, playercoins, player2coins, decknum, player, player1, player2;
    
    @FXML
    Label assassinationLabel, stealLabel;
    @FXML
    Button blockwContessa, callAssassin, blockwcaptain, blockwambassador, callCaptain;
    
   public void sendmessage(KeyEvent event) {
       if(event.getCode() == KeyCode.ENTER) {
           Client.writeToOS('^' + textfield.getText());    // append with '^' to indicate to the server the message is chat text
           textfield.setText("");
       }
   }
   
   public void rwbyaction() {
       dialogue.appendText("RWBY powers not yet implemented!\n");
   }
   public void challenge() {
       if (challenge.value)
           Client.writeToOS("#challenge " + player.getText());
   }
   public void extendChallenge () {
       if(challenge.value)
           Client.writeToOS("#extend " + player.getText());
   }
   public void duke() throws FileNotFoundException{
       if(turn.value) {
           Client.writeToOS("#duke");
           offTurn();
       }
   }
   public void income() throws FileNotFoundException{
       if(turn.value) {
           Client.writeToOS("#income");
           offTurn();
       }
   }
   public void ambassador1() throws FileNotFoundException{
       if(turn.value) {
           Client.writeToOS("#ambassador1");
           offTurn();
       }
   }
   public void ambassador2() throws FileNotFoundException {
       if(turn.value) {
           Client.writeToOS("#ambassador2");
           offTurn();
       }
   }
   public void ambassadorboth() throws FileNotFoundException {
       if(turn.value) {
           Client.writeToOS("#ambassadorboth");
           offTurn();
       } 
   }
   public void foreignaid() throws FileNotFoundException{
       if(turn.value) {
           Client.writeToOS("#foreignaid");
           offTurn();
       }
   }
   public void assassinate() throws FileNotFoundException{
       if(turn.value) {
           Client.writeToOS("#assassinate");
           offTurn();
       }
   }
   public void steal() throws FileNotFoundException{
       if(turn.value) {
           Client.writeToOS("#steal");
           offTurn();
       }
   }
   public void coup() throws FileNotFoundException{
       if(turn.value) {
           Client.writeToOS("#coup");
           offTurn();
       }
   }
   
   public void loseCard1() throws FileNotFoundException {
       if(respond.value) {
           Client.writeToOS("#1");
           respond.value = false;
           offTurn();
       }
   }
   
   public void loseCard2() throws FileNotFoundException {
       if(respond.value) {
           Client.writeToOS("#2");
           respond.value = false;
           offTurn();
       }
   }
   
    public void coupPerson(MouseEvent event) throws FileNotFoundException {
        if(choice.value) {
            Label l = (Label) event.getSource();
            Client.writeToOS("#" + l.getText());
            choice.value = false;
            offTurn();
        }
    }
    
    public void offTurn() throws FileNotFoundException {
        light.setImage(new Image(getClass().getResource("/red_light.png").toString()));
        turn.value = false;
    }

    public void blockAssassination() throws FileNotFoundException {
        if (claim.value) {
            Client.writeToOS("#blockAssassination");
            claim.value = false;
            Client.setClaimViewable(0.0);
            offTurn();
        }
    }

    public void callOnAssassin() throws FileNotFoundException {
        if (claim.value) {
            Client.writeToOS("#callOnAssassin");
            claim.value = false;
            Client.setClaimViewable(0.0);
            offTurn();
        }
    }

    public void blockStealwAmbassador() throws FileNotFoundException {
        if (claim.value) {
            Client.writeToOS("#blockStealwAmbassador");
            claim.value = false;
            Client.setClaimViewable(0.0);
            offTurn();
        }
    }

    public void blockStealwCaptain() throws FileNotFoundException {
        if (claim.value) {
            Client.writeToOS("#blockStealwCaptain");
            claim.value = false;
            Client.setClaimViewable(0.0);
            offTurn();
        }
    }

    public void callOnCaptain() throws FileNotFoundException {
        if (claim.value) {
            Client.writeToOS("#callOnCaptain");
            claim.value = false;
            Client.setClaimViewable(0.0);
            offTurn();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ImageView[] cards = {playercard1, playercard2, player1card1, player1card2, player2card1, player2card2};
        Label[] coins = {playercoins, player1coins, player2coins};
        Label[] names = {player, player1, player2};
        
        turn = new BooleanHold();
        choice = new BooleanHold();
        respond = new BooleanHold();
        challenge = new BooleanHold();
        claim = new BooleanHold();
        
        turn.value = false;
        respond.value = false;
        choice.value = false;
        challenge.value = false;
        claim.value = false;
        
        Label[] claimLabels = {assassinationLabel, stealLabel};
        Button[] claimButtons = {blockwContessa, callAssassin, blockwcaptain, blockwambassador, callCaptain};
        
        Client.setGameElems(textarea, dialogue, cards, rwbyidentity, light, decknum, coins, names, turn, respond, choice, challenge, claim, claimLabels, claimButtons);
    }    

}