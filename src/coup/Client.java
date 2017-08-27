package coup;

/**
 *
 * @author Ben
 */
import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Client implements Runnable {

    // The client socket
    private static Socket clientSocket = null;
    // The output stream
    private static PrintStream os = null;
    // The input stream
    private static DataInputStream is = null;

    //references to game scene elements
    static volatile TextArea area, dialogue;
    static Label decknum, playerCoins[], playerNames[];
    static ImageView rwbyId, light, cards[];
    static BooleanHold turn, respond, choice, challenge, claim;
    static Button[] claimButtons;
    static Label[] claimLabels;
    
    
    
    public static Scene2Controller scene2Ref;
    public static Scene3Controller scene3Ref;
    public static boolean sc2T;

    public static void main(String host, String nickname, Scene2Controller sc2, Scene3Controller sc3) throws IOException {

        // The default port.
        int portNumber = 2222;

        //take in scene and stage for transitioning on start signal receival
        if (sc2 != null) {
            scene2Ref = sc2;
            scene3Ref = null;
            sc2T = true;
        } else {
            scene3Ref = sc3;
            scene2Ref = null;
            sc2T = false;
        }

        /*
     * Open a socket on a given host and port. Open input and output streams.
         */
        try {
            clientSocket = new Socket(host, portNumber);
            //inputLine = new BufferedReader(new InputStreamReader(System.in));
            os = new PrintStream(clientSocket.getOutputStream());
            is = new DataInputStream(clientSocket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + host);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to the host "
                    + host);
            System.exit(1);
        }

        if (clientSocket == null || os == null || is == null) {
            close("An error has occurred connecting to host.\nThe program will now close.");
        }

        /*
     * If everything has been initialized then we want to write some data to the
     * socket we have opened a connection to on the port portNumber.
         */
        if (clientSocket != null && os != null && is != null) {
            try {
                /* Create a thread to read from the server. */
                new Thread(new Client()).start();

                os.println(nickname);//send nickname to server

            } catch (Exception e) {
                System.err.println("Exception:  " + e);
                System.exit(1);
            }
        }

    }

    public static synchronized void writeToOS(String chatText) {
        os.println(chatText);
    }
    
    public static void setGameElems(TextArea area, TextArea dialogue, ImageView[] cards, ImageView rwbyId, ImageView light, Label decknum, Label[] playerCoins, Label[] playerNames, 
            BooleanHold turn, BooleanHold respond, BooleanHold choice, BooleanHold challenge, BooleanHold claim, Label[] claimLabels, Button[] claimButtons) {
        Client.area = area;
        Client.dialogue = dialogue;
        Client.decknum = decknum;
        Client.playerCoins = playerCoins;
        Client.playerNames = playerNames;
        Client.rwbyId = rwbyId;
        Client.cards = cards;
        Client.light = light;
        Client.turn = turn;
        Client.respond = respond;
        Client.choice = choice;
        Client.challenge = challenge;
        Client.claim = claim;
        Client.claimLabels = claimLabels;
        Client.claimButtons = claimButtons;
    }

    public static void close(String message) throws IOException {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Stage stage = new Stage();
                FXMLLoader loader;
                try {
                    loader = new FXMLLoader(getClass().getResource("GameOverPopup.fxml"));
                    AnchorPane anchorPane = loader.load();
                    GameOverPopupController controller = loader.getController();
                    controller.setMessage(message);
                    stage.setScene(new Scene(anchorPane));
                } catch (IOException ex) {
                    Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                }

                stage.setTitle("RWBY Coup");
                stage.setResizable(true);
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.initOwner(area.getScene().getWindow());
                stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                    @Override
                    public void handle(WindowEvent t) {
                        try {
                            Client.getOS().close();
                            Client.getIS().close();
                            Client.getClientSocket().close();
                            Platform.exit();
                            System.exit(0);
                        } catch (IOException e) {
                            System.err.println("IOException: " + e);
                            Platform.exit();
                            System.exit(1);
                        }
                    }
                });
                stage.showAndWait();
            }
        });
    }

    public static void terminate() {
        try {
            Client.getOS().close();
            Client.getIS().close();
            Client.getClientSocket().close();
            Platform.exit();
            System.exit(0);
        } catch (IOException e) {
            System.err.println("IOException: " + e);
            Platform.exit();
            System.exit(1);
        } catch (Exception e) {
            System.err.println("IOException: " + e);
            Platform.exit();
            System.exit(1);
        }
    }

    public static void setClaimViewable(double d) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                for (Button b : claimButtons) {
                    //System.out.println(b.toString());
                    b.setOpacity(d);
                }
                for (Label l : claimLabels) {
                    l.setOpacity(d);
                }
            }
        });
    }
    
    @Override
    public void run() {

        String responseLine;
        

        //wait for receival of start signal, then switch scenes of caller to game scene w/ proper # of players
        try {
            while ((responseLine = is.readLine()) != null) {
                
                if (responseLine.contains("$startgame")) {
                    while ((responseLine = is.readLine()) != null) {
                        if (responseLine.contains("$players ")) {
                            String s = responseLine;
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    if (sc2T) {
                                        try {
                                            switch (Integer.parseInt(s.substring(s.length() - 1))) {
                                                case 2:
                                                    scene2Ref.root = FXMLLoader.load(getClass().getResource("GameScene2.fxml"));
                                                    break;
                                                case 3:
                                                    scene2Ref.root = FXMLLoader.load(getClass().getResource("GameScene3.fxml"));
                                                    break;
                                                case 4:
                                                    scene2Ref.root = FXMLLoader.load(getClass().getResource("GameScene4.fxml"));
                                                    break;
                                                case 5:
                                                    scene2Ref.root = FXMLLoader.load(getClass().getResource("GameScene5.fxml"));
                                                    break;
                                                case 6:
                                                    scene2Ref.root = FXMLLoader.load(getClass().getResource("GameScene6.fxml"));
                                                    break;
                                                case 7:
                                                    scene2Ref.root = FXMLLoader.load(getClass().getResource("GameScene7.fxml"));
                                                    break;
                                                default:
                                                    System.err.println("\n---Fatal Error- Invalid Number of Players---\n");
                                                    System.exit(1);
                                            }
                                        } catch (IOException ex) {
                                            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                                        }

                                        scene2Ref.scene = new Scene(scene2Ref.root);
                                        scene2Ref.stage.setTitle("RWBY Coup");
                                        scene2Ref.stage.setResizable(false);
                                        scene2Ref.stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
                                        scene2Ref.stage.setTitle("RWBY Coup");
                                        scene2Ref.stage.setScene(scene2Ref.scene);
                                        scene2Ref.stage.show();
                                        scene2Ref.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                                            @Override
                                            public void handle(WindowEvent t) {
                                                Client.writeToOS("/quit");
                                            }
                                        });
                                    } else {
                                        try {
                                            switch (Integer.parseInt(s.substring(s.length() - 1))) {
                                                case 2:
                                                    scene3Ref.root = FXMLLoader.load(getClass().getResource("GameScene2.fxml"));
                                                    break;
                                                case 3:
                                                    scene3Ref.root = FXMLLoader.load(getClass().getResource("GameScene3.fxml"));
                                                    break;
                                                case 4:
                                                    scene3Ref.root = FXMLLoader.load(getClass().getResource("GameScene4.fxml"));
                                                    break;
                                                case 5:
                                                    scene3Ref.root = FXMLLoader.load(getClass().getResource("GameScene5.fxml"));
                                                    break;
                                                case 6:
                                                    scene3Ref.root = FXMLLoader.load(getClass().getResource("GameScene6.fxml"));
                                                    break;
                                                case 7:
                                                    scene3Ref.root = FXMLLoader.load(getClass().getResource("GameScene7.fxml"));
                                                    break;
                                                default:
                                                    System.err.println("\n---Fatal Error- Invalid Number of Players---\n");
                                                    System.exit(1);
                                            }
                                        } catch (IOException ex) {
                                            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
                                        }

                                        scene3Ref.scene = new Scene(scene3Ref.root);
                                        scene3Ref.stage.setTitle("RWBY Coup");
                                        scene3Ref.stage.setResizable(false);
                                        scene3Ref.stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
                                        scene3Ref.stage.setTitle("RWBY Coup");
                                        scene3Ref.stage.setScene(scene3Ref.scene);
                                        scene3Ref.stage.show();
                                        scene3Ref.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                                            @Override
                                            public void handle(WindowEvent t) {
                                                Client.writeToOS("/quit");
                                            }
                                        });
                                    }
                                }
                            });
                            break;
                        }
                    }
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("IOException:  " + e);
            terminate();
        }

        //pause to allow gui elements to be assigned (from fx scene switch thread)
        while(Client.area == null || Client.dialogue == null || Client.decknum == null || Client.playerCoins == null  || Client.playerNames == null  || 
                Client.rwbyId == null  || Client.cards == null  || Client.light == null  || Client.turn == null  || Client.respond == null  || Client.choice == null || Client.challenge == null
                || Client.claim == null || Client.claimLabels == null || Client.claimButtons == null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //process different communications from host server, ie chat text, game communications, etc, and end game when Bye message is sent
        try {
            while ((responseLine = is.readLine()) != null) {
                System.out.println(responseLine);
                if (responseLine.startsWith("*** Bye")) {
                    break;
                }
                final String s = responseLine;
                switch (responseLine.charAt(0)) {
                    //game command
                    case '$':
                        if (responseLine.equals("$name_error")) {
                            close("Someone has chosen the same \nname as someone else!");
                        }
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                    if (s.substring(1, 7).equals("player")) {
                                        if (s.substring(9, 13).equals("name")) {
                                            if (s.charAt(7) == 'u') {
                                                playerNames[0].setText(s.substring(14));
                                            } else {
                                                playerNames[Integer.parseInt(s.substring(7, 8))].setText(s.substring(14));
                                            }
                                        } else if (s.substring(9, 14).equals("card1")) {
                                            if (s.charAt(7) == 'u') {
                                                //System.out.println("..\\"+s.substring(15)+".png");
                                                cards[0].setImage(new Image(getClass().getResource("/" + s.substring(15) + ".png").toString()));
                                            } else {
                                                cards[Integer.parseInt(s.substring(7, 8)) * 2].setImage(new Image(getClass().getResource("/" + s.substring(15) + ".png").toString()));
                                            }
                                        } else if (s.substring(9, 14).equals("card2")) {
                                            if (s.charAt(7) == 'u') {
                                                //System.out.println("..\\"+s.substring(15)+".png");
                                                cards[1].setImage(new Image(getClass().getResource("/" + s.substring(15) + ".png").toString()));
                                            } else {
                                                cards[Integer.parseInt(s.substring(7, 8)) * 2 + 1].setImage(new Image(getClass().getResource("/" + s.substring(15) + ".png").toString()));
                                            }
                                        } else if (s.substring(9, 15).equals("rwbyid")) {
                                            //System.out.println("..\\"+s.substring(16)+".png");
                                            rwbyId.setImage(new Image(getClass().getResource("/" + s.substring(16) + ".png").toString()));
                                        } else if (s.substring(9, 14).equals("coins")) {
                                            if (s.charAt(7) == 'u') {
                                                //System.out.println("..\\"+s.substring(15)+".png");
                                                playerCoins[0].setText("x" + s.substring(15));
                                            } else {
                                                playerCoins[Integer.parseInt(s.substring(7, 8))].setText("x" + s.substring(15));
                                            }
                                        }
                                        
                                    } else if (s.substring(1, 8).equals("decknum")) {
                                        decknum.setText("x" + s.substring(9));
                                    }
                                    else if(s.substring(1,6).equals("light")) {
                                        light.setImage(new Image(getClass().getResource("/" + s.substring(7) + ".png").toString()));
                                        switch (s.substring(7)) {
                                            case "star_light":
                                                respond.value = true;
                                                break;
                                            case "green_light_choice":
                                                choice.value = true;
                                                break;
                                            case "green_light":
                                                turn.value = true;
                                                break;
                                            case "yellow_light":
                                                challenge.value = true;
                                                break;
                                            case "blue_light":
                                                claim.value = true;
                                                Platform.runLater(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        setClaimViewable(1.0);
                                                    }
                                                });
                                                break;
                                            case "red_light":
                                                turn.value = false;
                                                choice.value = false;
                                                respond.value = false;
                                                challenge.value = false;
                                                claim.value = false;
                                                Platform.runLater(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        setClaimViewable(0.0);
                                                    }
                                                });
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                            }
                        });
                        break;
                    //narration text
                    case '&':
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                dialogue.appendText(s.substring(1) + "\n");
                            }
                        });
                        break;
                    default:
                        //chat text
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                area.appendText(s + "\n");
                                String textColor = "black";
                                int textSize = 14;
                                if (s.contains("> !help")) {
                                    area.appendText("Commands (omit quotes):\n!size \"text_size\" (changes text size)\n!color \"text_color\" (changes text color)\n!clear (clears the chat)\n");
                                } else if (s.contains("> !color")) {
                                    if (s.contains("blue")) {
                                        textColor = "blue";
                                    } else if (s.contains("black")) {
                                        textColor = "black";
                                    } else if (s.contains("yellow")) {
                                        textColor = "yellow";
                                    } else if (s.contains("red")) {
                                        textColor = "red";
                                    } else if (s.contains("green")) {
                                        textColor = "green";
                                    } else if (s.contains("orange")) {
                                        textColor = "orange";
                                    } else if (s.contains("purple")) {
                                        textColor = "purple";
                                    } else if (s.contains("pink")) {
                                        textColor = "pink";
                                    } else if (s.contains("list")) {
                                        area.appendText("System colors: black, blue, yellow, red, green, orange, purple, pink.\n");
                                    } else {
                                        area.appendText("Invalid or unknown color entered. Enter \'!color \"list\"' for a list of all recognized colors (omit quotes).\n");
                                        return;
                                    }
                                    area.setStyle("-fx-background-color: #ffffff; -fx-text-fill: " + textColor + "; -fx-font-style: normal;-fx-font-size: " + Integer.toString(textSize) + ";");
                                } else if (s.contains("> !size")) {
                                    try {
                                        textSize = new Scanner(s).useDelimiter("\\D+").nextInt();
                                        if (textSize > 25 || textSize < 10) {
                                            area.appendText("Please enter a valid integer size between 10 and 25.\n");
                                            return;
                                        }
                                        area.setStyle("-fx-background-color: #ffffff; -fx-text-fill: " + textColor + "; -fx-font-style: normal;-fx-font-size: " + Integer.toString(textSize) + ";");
                                    } catch (Exception e) {
                                        area.appendText("Invalid number or no number entered!\n");
                                    }

                                } else if (s.contains("> !clear")) {
                                    area.setText(s.substring(1, s.indexOf(">")) + " has cleared the chat.\n");
                                }
                            }
                        });
                        break;
                }
            }
            close("Someone has disconnected. \nThe game will now close.");
        } catch (IOException e) {
            System.err.println("IOException :  " + e);
            terminate();
        }
    }

    public static DataInputStream getIS()//gets ref to input stream
    {
        return is;
    }

    public static PrintStream getOS() {
        return os;
    }

    public static Socket getClientSocket() {
        return clientSocket;
    }

}
