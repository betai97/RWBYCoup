A description of rules will be added here at some point



A new windows exe file will become available after each new major release. A new jar will become available after
each update of code.


Program design:
--> Each program is packaged with server and client code, with 1 player acting as host, and the rest
connecting as clients; host uses port forwarding if playing over internet
--> The host runs actual game code, clients send moves when approved by host to do so and receive updates on
game situation from host
--> Program distributed as jar or setup.exe, made with launch4j for exe and inno setup compiler
for ensuring exe has bundled jre access

To run:
Windows:
You probably want to download and run the setup.exe file, it is bundled with the correct java version, and 
has a game icon. You can still use the Coup.jar file, but you will have to make sure that you have java 8
installed on your computer.

Mac/Linux:
Make sure java 8 is installed, and run the Coup.jar file.

Hosting the game:
If you are hosting the game, you need to set up port forwarding to port 2222. You can either log into your router and do this,
or use the UPnP Port mapper program to do it, following the instructions.txt file.
If you are connecting to someone else's game, no need to do anything.