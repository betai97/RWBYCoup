A more thorough description of game rules will be added here soon.

Essentially, each player wants to get coins and use them to eliminate other players' cards. There are different ways to do this on each turn.
Full rules for the game can be found at: https://people.rit.edu/~mjo5501/230/project1/index.html
However, in this version of the game, there is the added level of complexity that each user additionally gets a certain one-shot move,
represented by a character from the tv show RWBY, as described below:
blake - 7 coins avoid coup
cinder - 5 coins make player take action
emerald - make players switch cards
jaune - heal a card
mercury - no coin assasinate
neo - reshuffle all cards
nora - 10 coin coup both cards
penny - start w 9 coins
pyrrha - take half coins of person couped
ren - look at two cards of other players
roman - swap turn order
ruby - make two consecutive moves
salem - switch player positions
tyrion - poison coup
velvet - 3 coin copy a rwby power
weiss - resurrect player
yang - two coin coup when a card is lost


A new windows exe file will become available after each new major release. A new jar will become available 
after each update of code.

Program design: 
--> Each program is packaged with server and client code, with 1 player acting as host, and the rest connecting as clients; 
host uses port forwarding if playing over internet 
--> The host runs actual game code, clients send moves when approved by host to do so and receive updates on game situation from host 
--> Program distributed as jar or setup.exe, made with launch4j for exe and inno setup compiler for ensuring exe has bundled jre access

To run: Windows: 
You probably want to download and run the setup.exe file, it is bundled with the correct java version, and has a game icon. 
You can still use the Coup.jar file, but you will have to make sure that you have java 8 installed on your computer.

Mac/Linux: Make sure java 8 is installed, and run the Coup.jar file.

Hosting the game: If you are hosting the game, you need to set up port forwarding to port 2222. 
You can either log into your router and do this, or use the UPnP Port mapper program to do it, following the instructions.txt file. 
If you are connecting to someone else's game, no need to do anything.

No copyright infringment intended towards Rooster Teeth or Coup.