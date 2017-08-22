package coup;

/**
 *
 * @author Ben
*/
import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.TextArea;


public class Server {
    
  // The server socket.
  private static ServerSocket serverSocket = null;
  // The client socket
  private static Socket clientSocket = null;
  
  public static int usersNum = 0;
  
  public static TextArea arearef;//reference to passed textarea for writing
  
  //for processing moves
  public static volatile Boolean moveMade;
  public static volatile String move;

  // This chat server can accept up to MAXCLIENTSCOUNT clients' connections.
  private static final int MAXCLIENTSCOUNT = 7;
  private static final clientThread[] threads = new clientThread[MAXCLIENTSCOUNT];
  
  static boolean startNewGame;
  
  /*
  public static clientThread[] getThreads() {
      ArrayList<clientThread> list = new ArrayList();
      for(int i=0;i<MAXCLIENTSCOUNT;i++) {
          if(threads[i] == null) 
              break;
          list.add(threads[i]);
      }
      return (clientThread[]) list.toArray();
  }*/
  
  public static void main(TextArea area)  {

    //port number for connection to occur on
    int portNumber = 2222;
    
    //set reference to text are to be later manipulated
    arearef=area;

        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.out.println(e);
        }

      /*
     * Create a client socket for each connection and pass it to a new client
     * thread. 
       */
      //Execute the code that does this in its own thread so it doesnt halt gui
      NoWait nowait = new NoWait(serverSocket, clientSocket, MAXCLIENTSCOUNT, threads);
      Thread t = new Thread(nowait);
      t.start();
      
      
      startNewGame = false;
      StartGame startGame = new StartGame(t, threads);
      Thread sg = new Thread(startGame);
      sg.start();
    }

  public static void stopWaiting() {
      try {
          serverSocket.close();
      } catch (IOException ex) {
          Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
      } catch (Exception i) {
          Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, i);
      }
  }
  
}





class StartGame implements Runnable {

    Thread t;
    clientThread[] threads;
    
    public StartGame(Thread t, clientThread[] threads) {
        this.t = t;
        this.threads = threads;
    }
    
    @Override
    public void run() {
        Game game;
        Thread g;
        //wait for all clients to connect, then start game
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(StartGame.class.getName()).log(Level.SEVERE, null, ex);
            }
          if (!t.isAlive()) {
              try {
                  Thread.sleep(500);
              } catch (InterruptedException ex) {
                  Logger.getLogger(NoWait.class.getName()).log(Level.SEVERE, null, ex);
              }

              game = new Game(threads);
              g = new Thread(game);
              g.start();
              break;
            }
        }
        
        //wait for game to be over, then start a new game, ad infinitum (user can exit by quitting)
        while (true) {
            if (Server.startNewGame) {
                game = new Game(threads);
                g = new Thread(game);
                g.start();
                Server.startNewGame = false;
            }
            try {
                Thread.sleep(30000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}


//creates a client socket for connections and puts them in a client thread, to be run in sep thread
class NoWait implements Runnable {
    
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private final int MAXCLIENTSCOUNT;
    private final clientThread[] threads;
    
    public NoWait(ServerSocket serverSocket, Socket clientSocket, int MAXCLIENTSCOUNT, clientThread[] threads)
    {
        this.serverSocket=serverSocket;
        this.clientSocket=clientSocket;
        this.MAXCLIENTSCOUNT=MAXCLIENTSCOUNT;
        this.threads = threads;
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                clientSocket = serverSocket.accept();
                int i;
                for (i = 0; i < MAXCLIENTSCOUNT; i++) {
                    if (threads[i] == null) {
                        (threads[i] = new clientThread(clientSocket, threads)).start();
                        break;
                    }
                }
                if (i == MAXCLIENTSCOUNT) 
                    clientSocket.close();
            } catch (IOException e) {
                System.out.println(e);
                Thread.currentThread().stop();
            }
        }
    }
    
    
}

/*
 * The chat client thread. This client thread opens the input and the output
 * streams for a particular client, ask the client's name, informs all the
 * clients connected to the server about the fact that a new client has joined
 * the chat room, and as long as it receive data, echos that data back to all
 * other clients. The thread broadcast the incoming messages to all clients and
 * routes the private message to the particular client. When a client leaves the
 * chat room this thread informs also all the clients about that and terminates.
 */
class clientThread extends Thread {

    private String clientName = null;
    private DataInputStream is = null;
    private PrintStream os = null;
    private Socket clientSocket = null;
    private final clientThread[] threads;
    private int MAXCLIENTSCOUNT;

    public clientThread(Socket clientSocket, clientThread[] threads) {
        this.clientSocket = clientSocket;
        this.threads = threads;
        MAXCLIENTSCOUNT = threads.length;
    }
    
    public PrintStream getOS() {
        return os;
    }
    
    public String getClientName() {
        return clientName;
    }

    @Override
    public void run() {
        int MAXCLIENTSCOUNT = this.MAXCLIENTSCOUNT;
        clientThread[] threads = this.threads;

        try {
            /*
             * Create input and output streams for this client.
             */
            Server.usersNum++;
            
            is = new DataInputStream(clientSocket.getInputStream());
            os = new PrintStream(clientSocket.getOutputStream());
            String name;

            //display connected user in gui
            name = is.readLine().trim();
            Server.arearef.appendText("<" + name + "> has connected\n");

            synchronized (this) {
                for (int i = 0; i < MAXCLIENTSCOUNT; i++) {
                    if (threads[i] != null && threads[i] == this) {
                        clientName = name;
                        break;
                    }
                }

                

                /* Start the conversation. */
                String line = "";
                while (true) {
                    try {
                        line = is.readLine();
                    } catch (SocketTimeoutException sTE) {
                        System.err.println("Timeout occurred!");
                        System.err.println("Error: " + sTE);
                        continue;
                    }
                    if (line.startsWith("/quit")) {
                        break;
                    }
                    //if the messsage is a command, forward the command    &&&& CHECK That its this person's turn!!!!!
                    if (line.startsWith("$") || line.startsWith("&")) {
                        synchronized (this) {
                            for (int i = 0; i < MAXCLIENTSCOUNT; i++) {
                                if (threads[i] != null && threads[i].clientName != null) {
                                    threads[i].os.println(line);
                                }
                            }
                        }
                    } 
                    else if (line.startsWith("#")) {
                        synchronized (this) {
                            Server.moveMade = true;
                            Server.move = line;
                            System.out.println("movemade");
                        }
                    }
                    else {//otherwise broadcast it to the chat with user name
                        synchronized (this) {
                            for (int i = 0; i < MAXCLIENTSCOUNT; i++) {
                                if (threads[i] != null && threads[i].clientName != null) {
                                    threads[i].os.println("<" + name + "> " + line.substring(1));
                                }
                            }
                        }
                    }
                }

                //end program for everyone
                synchronized (this) {
                    for (int i = 0; i < MAXCLIENTSCOUNT; i++) {
                        if (threads[i] != null && threads[i].clientName != null) {
                            threads[i].os.println("*** Bye ");
                        }
                    }
                }

                /*
       * Clean up. Set the current thread variable to null so that a new client
       * could be accepted by the server.
                 */
                synchronized (this) {
                    for (int i = 0; i < MAXCLIENTSCOUNT; i++) {
                        if (threads[i] == this) {
                            threads[i] = null;
                        }
                    }
                }
                /*
       * Close the output stream, close the input stream, close the socket.
                 */
                is.close();
                os.close();
                clientSocket.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(clientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}


class Game implements Runnable {

    volatile int playerNum;
    clientThread[] threads;
    Player players[];
    volatile ArrayList<String> deck;

    public Game(clientThread[] passedThreads) {
        //get number of players and initialize players with name and coin #
        int threadNum = 0;
        for (clientThread ct: passedThreads) {
            if(ct == null)
                break;
            threadNum++;
        }
        threads = new clientThread[threadNum];
        
        for(int i=0;i<threadNum;i++)
            threads[i] = passedThreads[i];
        
        
        playerNum = threads.length;
        players = new Player[playerNum];
        for(int i=0;i<playerNum;i++)
            players[i] = new Player(2, i, threads[i].getClientName());
    }
    
    private void initialize() {
        //make sure no one has the same name
        ArrayList<String> playerList = new ArrayList();
        for(Player p: players) {
            if(playerList.contains(p.name)) {
                Client.writeToOS("$name_error");
                Thread.currentThread().stop();
            }
            playerList.add(p.name);
        }
        
        
        
        //make and shuffle deck
        final String[] CARDS = {"duke", "captain", "assassin", "ambassador", "contessa"};
        deck = new ArrayList();
        if (playerNum <= 5)
            for (int i=0;i<5;i++)
                for(int j=0;j<3;j++)
                    deck.add(CARDS[i]);
        else
            for(int i=0;i<5;i++)
                for(int j=0;j<4;j++)
                    deck.add(CARDS[i]);
        Collections.shuffle(deck);
        
        //make and shuffle rwby ids
        final String[] RWBYIDS = {"blake","cinder","emerald","jaune","mercury","neo","nora","penny","pyrrha","ren","roman","ruby","salem","tyrion","velvet","weiss","yang"};
        ArrayList<String> rwbyids = new ArrayList(Arrays.asList(RWBYIDS));
        Collections.shuffle(rwbyids);
        
        System.out.println(deck);
        System.out.println(threads.length);
        System.out.println(Arrays.toString(players));
        System.out.println(playerNum);
        
        //assign rwby ids and cards to each player
        for(Player p: players) {
            p.card1 = deck.remove(deck.size()-1);
            p.card2 = deck.remove(deck.size()-1);
            p.rwbyid = rwbyids.remove(rwbyids.size()-1);
        }
        
        //send initial values (names, cards, rwbyids, coins) to each client for their gui
        ArrayList<Player> tempPlayers = new ArrayList(Arrays.asList(players));
        for (int i = 0; i < playerNum; i++) {
            threads[i].getOS().println("$playeru name " + players[i].name);
            threads[i].getOS().println("$playeru card1 " + players[i].card1);
            threads[i].getOS().println("$playeru card2 " + players[i].card2);
            threads[i].getOS().println("$playeru rwbyid " + players[i].rwbyid);

            for (int j = 1; j < playerNum; j++) {
                threads[i].getOS().println("$player" + j + " name " + tempPlayers.get(j).name);
            }
            Collections.rotate(tempPlayers, -1);
        }
        Client.writeToOS("$decknum " + deck.size());

        Server.moveMade = false;
        Server.move = "";
    }

    @Override
    public void run() {
        //set up game
        initialize();
        sleep(1);
        Client.writeToOS("&Welcome to RWBY Coup!");
        sleep(2);
        if(playerNum>5)
            Client.writeToOS("&Because there are more than 5 players, there will be 4 of each card!");
        else
            Client.writeToOS("&Because there are 5 or fewer players, there will be 3 of each card!");
        sleep(3);
        Client.writeToOS("&Each player will have two minutes to make their move, before defaulting to either income or couping the person directly to their left.");
        sleep(3);
        Client.writeToOS("&The stoplight will turn green if it is your turn, turn yellow for challenging, and turn red otherwise.");
        sleep(2);
        Client.writeToOS("&There will be a 20 second period of time to challenge after each move is made.");
        sleep(2);
        Client.writeToOS("&This period can be extended up to 5 times by 1 minute each time by clicking the \'extend challenge\' button.");
        sleep(4);
        Client.writeToOS("&-------------------------------------------------------------------------------------------------------------------");
        sleep(5);
        
        //enter main game loop
        double elapsedTime;
        Random rand = new Random();
        int offset = 1, ind = rand.nextInt(playerNum);
        
        while (!gameIsOver()) {

            if (!players[ind].playing) {
                ind += offset;
                if (ind > players.length - 1) {
                    ind = 0;
                } else if (ind < 0) {
                    ind = players.length - 1;
                }
                continue;
            }
            
            Client.writeToOS("&It is now " + players[ind].name + "'s turn!");
            sleep(2);
            
            threads[ind].getOS().println("$light green_light");
            elapsedTime = makeMove(ind, 120.0);
            if (elapsedTime >= 120.0) {  //process if move timed out
                if (players[ind].coins < 10) {
                    Client.writeToOS("&" + players[ind].name + " has defaulted to income!");
                    income(ind);
                } else {
                    try {
                        Client.writeToOS("&" + players[ind].name + " has defaulted to coup!");
                        coup(ind);
                    } catch (IOException ex) {
                        Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                threads[ind].getOS().println("$light red_light");
            } else {
                if(players[ind].coins >= 10) 
                    try {
                        coup(ind);
                    } catch (IOException ex) {
                        Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                    }
                else {
                    switch(Server.move) {
                    case "#income":
                        income(ind);
                        break;
                        case "#foreignaid":
                            try {
                                foreignAid(ind);
                            } catch (IOException ex) {
                                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            break;
                    case "#duke":
                        try {
                            duke(ind);
                        } catch (IOException ex) {
                            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "#coup":
                        if (players[ind].coins >= 7)
                            try {
                                coup(ind);
                            } catch (IOException ex) {
                                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        else {
                            Client.writeToOS("&" + players[ind].name + " has tried to coup without enought coins!");
                            Client.writeToOS("&" + players[ind].name + " has defaulted to income!");
                            income(ind);
                        }
                        break;
                    case "#assassinate":
                        if (players[ind].coins >= 3)
                            try {
                                assassinate(ind);
                            } catch (IOException ex) {
                                Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        else {
                            Client.writeToOS("&" + players[ind].name + " has tried to assassinate without enought coins!");
                            Client.writeToOS("&" + players[ind].name + " has defaulted to income!");
                            income(ind);
                        }
                        break;
                    case "#ambassador1":
                        try {
                            ambassador1(ind);
                        } catch (IOException ex) {
                            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "#ambassador2":
                        try {
                            ambassador2(ind);
                        } catch (IOException ex) {
                            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "#ambassadorboth":
                        try {
                            ambassadorBoth(ind);
                        } catch (IOException ex) {
                            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    case "#steal":
                        try {
                            steal(ind);
                        } catch (IOException ex) {
                            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        break;
                    default:
                        System.out.println(Server.move);
                        System.err.println("Unimplemented feature or error encountered!");
                        System.exit(1);
                        break;
                    }
                }
            }

            ind += offset;
            if (ind > players.length - 1) {
                ind = 0;
            } else if (ind < 0) {
                ind = players.length - 1;
            }
        }
        
        Client.writeToOS("&Congratulations, " + getWinner().name + ", you have won!");
        sleep(3);
        Client.writeToOS("&The next game should start within 30 seconds.");
        Server.startNewGame = true;
    }
    
    private void dispCoins(int pNum) {
        int shift = pNum;
        for (int j = 0; j < playerNum; j++) {
            if (shift == 0) {
                threads[j].getOS().println("$playeru coins " + players[pNum].coins);
                shift--;
                continue;
            }
            if (shift < 0) {
                shift = playerNum - 1;
            }
            threads[j].getOS().println("$player" + shift + " coins " + players[pNum].coins);
            shift--;
        }
    }
    
    private void dispDeadCard1(int toCoup) {
        int shifted = toCoup;
        for (int j = 0; j < playerNum; j++) {
            if (shifted == 0) {
                threads[j].getOS().println("$playeru card1 dead_" + players[toCoup].card1);
                shifted--;
                continue;
            }
            if (shifted < 0) {
                shifted = playerNum - 1;
            }
            threads[j].getOS().println("$player" + shifted + " card1 dead_" + players[toCoup].card1);
            shifted--;
        }
    }
    
    private void dispDeadCard2(int toCoup) {
        int shifted = toCoup;
        for (int j = 0; j < playerNum; j++) {
            if (shifted == 0) {
                threads[j].getOS().println("$playeru card2 dead_" + players[toCoup].card2);
                shifted--;
                continue;
            }
            if (shifted < 0) {
                shifted = playerNum - 1;
            }
            threads[j].getOS().println("$player" + shifted + " card2 dead_" + players[toCoup].card2);
            shifted--;
        }
    }
    
            
            
    private void income(int pNum) {
        Client.writeToOS("&" + players[pNum].name + " has decided to income!");
        Client.writeToOS("&"+ players[pNum].name + " has gained 1 coin!");
        players[pNum].coins++;
        dispCoins(pNum);
    }
    
    private void foreignAid(int pNum) throws IOException {
        Client.writeToOS("&" + players[pNum].name + " has decided to foreign aid!");
        Client.writeToOS("&"+ players[pNum].name + " has gained 2 coins!");
        players[pNum].coins += 2;
        dispCoins(pNum);
        challenge(pNum, -1, "foreignaid");
    }
    
    private void duke(int pNum) throws IOException {
        Client.writeToOS("&" + players[pNum].name + " has decided to duke!");
        Client.writeToOS("&"+ players[pNum].name + " has gained 3 coins!");
        players[pNum].coins += 3;
        dispCoins(pNum);
        challenge(pNum, -1, "duke");
    }
    
    private void ambassador1(int pNum) throws IOException {
        Client.writeToOS("&" + players[pNum].name + " has decided to ambassador!");
        if(players[pNum].card1.length() == 0) {
            Client.writeToOS("&" + players[pNum].name + " has tried to swap his/her card 1 when it is dead!");
            Client.writeToOS("&" + players[pNum].name + " defaults to income");
            income(pNum);
            return;
        }
        if (!challenge(pNum, -1, "ambassador")) {
            Client.writeToOS("&" + players[pNum].name + " has swapped his/her card 1 (leftmost card)!");
            swapCard1(pNum);
        }
    }
    
    private void swapCard1(int pNum) {
        String newCard = deck.remove(deck.size() - 1);
        deck.add(players[pNum].card1);
        Collections.shuffle(deck);
        players[pNum].card1 = newCard;
        threads[pNum].getOS().println("$playeru card1 " + players[pNum].card1);
    }
    
    private void swapCard2(int pNum) {
        String newCard = deck.remove(deck.size() - 1);
        deck.add(players[pNum].card2);
        Collections.shuffle(deck);
        players[pNum].card2 = newCard;
        threads[pNum].getOS().println("$playeru card2 " + players[pNum].card2);
    }
    
    private void swapCardBoth(int pNum) {
        String newCard1 = deck.remove(deck.size() - 1);
        String newCard2 = deck.remove(deck.size() - 1);
        deck.add(players[pNum].card1);
        deck.add(players[pNum].card2);
        Collections.shuffle(deck);
        players[pNum].card1 = newCard1;
        players[pNum].card2 = newCard2;
        threads[pNum].getOS().println("$playeru card1 " + players[pNum].card1);
        threads[pNum].getOS().println("$playeru card2 " + players[pNum].card2);
    }
    
    private void ambassador2(int pNum) throws IOException {
        Client.writeToOS("&" + players[pNum].name + " has decided to ambassador!");
        if(players[pNum].card2.length() == 0) {
            Client.writeToOS("&" + players[pNum].name + " has tried to swap his/her card 2 when it is dead!");
            Client.writeToOS("&" + players[pNum].name + " defaults to income");
            income(pNum);
            return;
        }
        if (!challenge(pNum, -1, "ambassador")) {
            Client.writeToOS("&" + players[pNum].name + " has swapped his/her card 2 (rightmost card)!");
            swapCard2(pNum);
        }
    }

    private void ambassadorBoth(int pNum) throws IOException {
        Client.writeToOS("&" + players[pNum].name + " has decided to ambassador!");
        if(players[pNum].card1.length() == 0 || players[pNum].card2.length() == 0) {
            Client.writeToOS("&" + players[pNum].name + " has tried to swap both his/her cards when one is dead!");
            Client.writeToOS("&" + players[pNum].name + " defaults to income");
            income(pNum);
            return;
        }
        if (!challenge(pNum, -1, "ambassador")) {
            Client.writeToOS("&" + players[pNum].name + " has swapped both cards!");
            swapCardBoth(pNum);
        }
    }
    
    private void assassinate(int pNum) throws IOException {
        int toAssassinate = 0, i;
        Client.writeToOS("&" + players[pNum].name + " has decided to assassinate!");
        sleep(2);
        threads[pNum].getOS().println("&Who would you like to assassinate? Click on their name label, above their card(s).");
        threads[pNum].getOS().println("&You have 1 minute to decide who to assassinate, before auto-assassinating the person directly to your left.");
        threads[pNum].getOS().println("$light green_light_choice");
        double elapsedTime = makeMove(pNum, 60.0);
        if (elapsedTime >= 60.00) {
            i = 1;
            while (true) {   //make sure person to left is still playing
                toAssassinate = pNum + i;
                if (toAssassinate > playerNum - 1) {
                    toAssassinate = 0;
                    i = 0;
                }
                if (players[toAssassinate].playing) {
                    break;
                }
                i++;
            }
            threads[pNum].getOS().println("$light red_light");
        } else {
            toAssassinate = -77;
            for (Player p : players) {
                if (p.name.equals(Server.move.substring(1))) {
                    toAssassinate = p.threadNum;
                    break;
                }
            }
            if (toAssassinate == -77) {
                System.err.println("Error in finding name of person to assassinate.");
                System.exit(1);
            }
        }

        Client.writeToOS("&" + players[pNum].name + " has assassinated " + players[toAssassinate].name + "!");
        sleep(2);
        
        if(!players[toAssassinate].playing) {
            Client.writeToOS("&" + players[toAssassinate].name + " is already out of the game!");
            Client.writeToOS("&" + players[pNum].name + " defaults to income.");
            income(pNum);
            return;
        }
        threads[toAssassinate].getOS().println("&You have been assassinated by " + players[pNum].name + "!");
        
        players[pNum].coins -= 3;
        dispCoins(pNum);
        
        if(!challenge(pNum, toAssassinate, "assassinate")) {
            if(players[toAssassinate].playing)
                loseCard(toAssassinate);
        }
    }
    
    private void steal(int pNum) throws IOException {
        int toSteal = 0, i;
        Client.writeToOS("&" + players[pNum].name + " has decided to steal!");
        sleep(2);
        threads[pNum].getOS().println("&Who would you like to steal from? Click on their name label, above their card(s).");
        threads[pNum].getOS().println("&You have 1 minute to decide who to steal from, before auto-stealing from the person directly to your left.");
        threads[pNum].getOS().println("$light green_light_choice");
        double elapsedTime = makeMove(pNum, 60.0);
        System.out.println("elapsedTime: " + elapsedTime);
        if (elapsedTime >= 60.00) {
            i = 1;
            int k=0;
            while (k != playerNum - 1) {   //make sure person to left has more than 0 coins
                toSteal = pNum + i;
                if (toSteal > playerNum - 1) {
                    toSteal = 0;
                    i = 0;
                }
                if (players[toSteal].coins > 0) {
                    break;
                }
                i++;k++;
            }
            if(k == playerNum - 1) {
                Client.writeToOS("&No one has any coins! " + players[pNum].name + " defaults to income.");
                income(pNum);
                return;
            }
            threads[pNum].getOS().println("$light red_light");
        } else {
            toSteal = -77;
            for (Player p : players) {
                if (p.name.equals(Server.move.substring(1))) {
                    toSteal = p.threadNum;
                    break;
                }
            }
            if (toSteal == -77) {
                System.err.println("Error in finding name of person to steal from.");
                System.exit(1);
            }
        }
        switch (players[toSteal].coins) {
            case 0:
                Client.writeToOS("&" + players[toSteal].name + " has no coins! " + players[pNum].name + " defaults to income.");
                income(pNum);
                return;
            case 1:
                players[toSteal].coins--;
                players[pNum].coins++;
                break;
            default:
                players[toSteal].coins -= 2;
                players[pNum].coins += 2;
                break;
        }
        Client.writeToOS("&" + players[pNum].name + " steals from " + players[toSteal].name + "!");
        dispCoins(pNum);
        dispCoins(toSteal);
        
        challenge(pNum, toSteal, "steal");
    }
    
    private double makeMove(int pNum, double timeOut) {
        long startTime = System.nanoTime();
        double elapsedTime;
        boolean warn30 = false, warn20 = false, warn10 = false;
        Server.move = "";
        while ((elapsedTime = elapsedTime(startTime)) < timeOut) {
            if ((timeOut - elapsedTime) < 30 && !warn30) {
                Client.writeToOS("&" + players[pNum].name + " has 30 seconds to make a move!");
                warn30 = true;
            } else if ((timeOut - elapsedTime) < 20 && !warn20) {
                Client.writeToOS("&" + players[pNum].name + " has 20 seconds to make a move!");
                warn20 = true;
            } else if ((timeOut - elapsedTime) < 10 && !warn10) {
                Client.writeToOS("&" + players[pNum].name + " has 10 seconds to make a move!");
                warn10 = true;
            }

            if (Server.moveMade) {
                Server.moveMade = false;
                break;
            }
            sleep(1);
        }
        
        return elapsedTime;
    }
    
    private void coup(int pNum) throws IOException {
        int toCoup = 0, i;
        Client.writeToOS("&" + players[pNum].name + " has decided to coup!");
        sleep(2);
        threads[pNum].getOS().println("&Who would you like to coup? Click on their name label, above their card(s).");
        threads[pNum].getOS().println("&You have 1 minute to decide who to coup, before auto-couping the person directly to your left.");
        threads[pNum].getOS().println("$light green_light_choice");
        double elapsedTime = makeMove(pNum, 60.0);
        if (elapsedTime >= 60.00) {
            i = 1;
            while (true) {   //make sure person to left is still playing
                toCoup = pNum + i;
                if (toCoup > playerNum - 1) {
                    toCoup = 0;
                    i = 0;
                }
                if (players[toCoup].playing) {
                    break;
                }
                i++;
            }
            threads[pNum].getOS().println("$light red_light");
        } else {
            toCoup = -77;
            for (Player p : players) {
                if (p.name.equals(Server.move.substring(1))) {
                    toCoup = p.threadNum;
                    break;
                }
            }
            if (toCoup == -77) {
                System.err.println("Error in finding name of person to coup.");
                System.exit(1);
            }
        }

        Client.writeToOS("&" + players[pNum].name + " has couped " + players[toCoup].name + "!");
        sleep(2);
        
        if(!players[toCoup].playing) {
            Client.writeToOS("&" + players[toCoup].name + " is already out of the game!");
            Client.writeToOS("&" + players[pNum].name + " defaults to income.");
            income(pNum);
            return;
        }
        
        threads[toCoup].getOS().println("&You have been couped by " + players[pNum].name + "!");
        
        players[pNum].coins -= 7;
        dispCoins(pNum);
        
        if(!challenge(pNum, toCoup, "coup"))
            if(players[toCoup].playing)
                loseCard(toCoup);
    }
    
    private void loseCard(int loser) {
        sleep(3);
        threads[loser].getOS().println("&Click the card you would like to give up.");
        threads[loser].getOS().println("&You have 1 minute to make your decision.");
        threads[loser].getOS().println("$light star_light");
        double elapsedTime = makeMove(loser, 60.0);
        if (elapsedTime >= 60.0) {
            if (players[loser].card1.length() > 0) {
                Client.writeToOS("&" + players[loser].name + " has given up the " + players[loser].card1 + "!");
                dispDeadCard1(loser);
                players[loser].card1 = "";
            } else {
                Client.writeToOS("&" + players[loser].name + " has given up the " + players[loser].card2 + "!");
                dispDeadCard2(loser);
                players[loser].card2 = "";
            }
            threads[loser].getOS().println("$light red_light");
        } else {
            switch (Server.move.charAt(1)) {
                case '1':
                    if (players[loser].card1.length() > 0) {
                        Client.writeToOS("&" + players[loser].name + " has given up the " + players[loser].card1 + "!");
                        dispDeadCard1(loser);
                        players[loser].card1 = "";
                    } else {
                        Client.writeToOS("&" + players[loser].name + " has given up the " + players[loser].card2 + "!");
                        dispDeadCard2(loser);
                        players[loser].card2 = "";
                    }
                    break;
                case '2':
                    if (players[loser].card2.length() > 0) {
                        Client.writeToOS("&" + players[loser].name + " has given up the " + players[loser].card2 + "!");
                        dispDeadCard2(loser);
                        players[loser].card2 = "";
                    } else {
                        Client.writeToOS("&" + players[loser].name + " has given up the " + players[loser].card1 + "!");
                        dispDeadCard1(loser);
                        players[loser].card1 = "";
                    }
                    break;
                default:
                    System.err.println("Fatal Error!");
                    System.exit(1);
                    break;
            }
        }

        if (players[loser].card1.length() == 0 && players[loser].card2.length() == 0) {
            players[loser].playing = false;
            Client.writeToOS("&" + players[loser].name + " is out of the game!");
        }
    }

    private boolean challenge(int pNum, int pTarget, String move) throws IOException {
        double timeOut = 20.0;
        long startTime = System.nanoTime();
        double elapsedTime;
        int extended = 0;
        boolean warn20 = false, warn10 = false;
        Server.move = "";
        Client.writeToOS("&There will now be 20s to challenge. This time period can be extended by a minute up to 5 times.");
        for(int i=0;i<playerNum;i++)
            if(players[i].playing)
                threads[i].getOS().println("$light yellow_light");
        while ((elapsedTime = elapsedTime(startTime)) < timeOut) {
            if ((timeOut - elapsedTime) < 20 && !warn20) {
                Client.writeToOS("&20 seconds remain to challenge!");
                warn20 = true;
            } else if ((timeOut - elapsedTime) < 10 && !warn10) {
                Client.writeToOS("&10 seconds remain to challenge!");
                warn10 = true;
            }

            if (Server.moveMade) {
                Server.moveMade = false;
                if(Server.move.substring(1,7).equals("extend") && extended < 5) {
                    Client.writeToOS("&Time for challenging extended by 1 minute by " + Server.move.substring(8) + ".");
                    warn20 = false;
                    warn10 = false;
                    timeOut += 60.0;
                    extended++;
                }
                else if(Server.move.substring(1,10).equals("challenge")) {
                    int challengerId = -77;
                    
                    for(Player p: players)
                        if(p.name.equals(Server.move.substring(11))) {
                            challengerId = p.threadNum;
                            break;
                        }
                    
                    if(challengerId == -77) {
                        Client.close("Error in challenge function!");
                        System.exit(1);
                    }
                    
                    
                    if(challengerId == pNum) {
                        Client.writeToOS("&" + players[challengerId].name + " has challenged himself: invalid move!");
                        continue;
                    }
                    
                    Client.writeToOS("$light red_light");
                    
                    Client.writeToOS("&" + players[challengerId].name + " challenges " + players[pNum].name + "!");
                    
                    switch(move) {
                        case "duke":
                            if(players[pNum].card1.equals("duke") || players[pNum].card2.equals("duke")) {
                                Client.writeToOS("&" + players[pNum].name + " has the duke!");
                                Client.writeToOS("&" + players[challengerId].name + " loses a card!");
                                loseCard(challengerId);
                                if(players[pNum].card1.equals("duke"))
                                    swapCard1(pNum);
                                else
                                    swapCard2(pNum);
                            } else {
                                Client.writeToOS("&" + players[pNum].name + " does not have the duke!");
                                Client.writeToOS("&" + players[pNum].name + " loses a card!");
                                players[pNum].coins -= 3;
                                loseCard(pNum);
                                dispCoins(pNum);
                            }
                            return false;
                        case "ambassador":
                            if(players[pNum].card1.equals("ambassador") || players[pNum].card2.equals("ambassador")) {
                                Client.writeToOS("&" + players[pNum].name + " has the ambassador!");
                                Client.writeToOS("&" + players[challengerId].name + " loses a card!");
                                loseCard(challengerId);
                                if(players[pNum].card1.equals("ambassador"))
                                    swapCard1(pNum);
                                else
                                    swapCard2(pNum);
                                return false;
                            } else {
                                Client.writeToOS("&" + players[pNum].name + " does not have the ambassador!");
                                Client.writeToOS("&" + players[pNum].name + " loses a card!");
                                loseCard(pNum);
                                return true;
                            }
                        case "foreignaid":
                            if(players[challengerId].card1.equals("duke") || players[challengerId].card2.equals("duke")) {
                                Client.writeToOS("&" + players[challengerId].name + " has the duke!");
                                Client.writeToOS("&" + players[pNum].name + " loses a card!");
                                loseCard(pNum);
                                players[pNum].coins -= 2;
                                dispCoins(pNum);
                            } else {
                                Client.writeToOS("&" + players[challengerId].name + " does not have the duke!");
                                Client.writeToOS("&" + players[challengerId].name + " loses a card!");
                                loseCard(challengerId);
                            }
                            return false;
                        case "assassinate":
                            if ((players[pTarget].card1.equals("contessa") || players[pTarget].card2.equals("contessa")) && pTarget == challengerId) {
                                Client.writeToOS("&" + players[pTarget].name + " has the contessa!");
                                Client.writeToOS("&" + players[pNum].name + " loses a card!");
                                loseCard(pNum);
                                return true;
                            } else if (players[pNum].card1.equals("assassin") || players[pNum].card2.equals("assassin")) {
                                Client.writeToOS("&" + players[pNum].name + " has the assassin!");
                                Client.writeToOS("&" + players[challengerId].name + " loses a card!");
                                loseCard(challengerId);
                                if (players[pNum].card1.equals("assassin")) {
                                    swapCard1(pNum);
                                } else {
                                    swapCard2(pNum);
                                }
                                return false;
                            } else {
                                Client.writeToOS("&" + players[pNum].name + " does not have the assassin!");
                                Client.writeToOS("&" + players[pNum].name + " loses a card!");
                                loseCard(pNum);
                                return true;
                            }
                        case "steal":
                            if(players[pNum].card1.equals("captain") || players[pNum].card2.equals("captain") || players[pNum].card1.equals("ambassador") || players[pNum].card1.equals("ambassador")) {
                                Client.writeToOS("&" + players[pNum].name + " has the captain or ambassador!");
                                Client.writeToOS("&" + players[challengerId].name + " loses a card!");
                                loseCard(challengerId);
                                if(players[pNum].card1.equals("captain"))
                                    swapCard1(pNum);
                                else
                                    swapCard2(pNum);
                            } else {
                                Client.writeToOS("&" + players[pNum].name + " does not have the captain!");
                                Client.writeToOS("&" + players[pNum].name + " loses a card!");
                                loseCard(pNum);
                                players[pNum].coins -= 2;
                                players[pTarget].coins += 2;
                                dispCoins(pNum);
                                dispCoins(pTarget);
                            }
                            return false;
                        case "coup":
                            if(challengerId != pTarget) {
                                Client.writeToOS("&" + players[challengerId].name + " is not being couped and can't challenge!");
                                continue;
                            }
                            if((players[pTarget].card1.equals("contessa") && players[pTarget].card2.equals("contessa")) && pTarget == challengerId) {
                                Client.writeToOS("&" + players[pTarget].name + " has the super-contessa!");
                                Client.writeToOS("&" + players[pNum].name + " loses a card!");
                                loseCard(pNum);
                                return true;
                            } else {
                                Client.writeToOS("&" + players[pTarget].name + " does not have the super-contessa!");
                                loseCard(pTarget);
                                return false;
                            }
                        default:
                            break;
                    }
                    break;
                }
            }
            sleep(1);
        }
        Client.writeToOS("$light red_light");
        Client.writeToOS("&No one has challenged!");
        sleep(3);
        Server.move = "";
        Server.moveMade = false;
        return false;
    }

    private void rwbyAction(int pNum) {
        
    }

    private double elapsedTime(long startTime) {  // returns elapsed time in seconds
        return (System.nanoTime() - startTime) / 1000000000.0;
    }

    void sleep(double seconds) {  //has thread sleep for specified seconds
        try {
            Thread.sleep((long) (seconds * 1000));
        } catch (InterruptedException ex) {
            Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private boolean gameIsOver() {
        int cnt = 0;
        for (Player p : players) {
            if (p.playing) {
                cnt++;
            }
        }
        return cnt < 2;
    }

    private Player getWinner() {
        //precondition: exactly one player in players has playing == true 
        for (Player p : players) {
            if (p.playing) {
                return p;
            }
        }
        return null;
    }

    private class Player {

        int threadNum;
        int coins;
        boolean playing;
        String name, card1, card2, rwbyid;

        public Player(int coins, int threadNum, String name) {
            this.coins = coins;
            this.name = name;
            this.threadNum = threadNum;
            playing = true;
        }
    }

}
