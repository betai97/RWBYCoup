# Changelog

## [Unreleased]
~Changed ambassador function, click card to ambassador or click button ambassador both
-Make an updater python script to download new exe each time
-Start a new game without restarting the program  --> PROBLEM -- have to reset opponent cards to blank
ones at start of new game, as well as coins, decknum, etc!!!!!!!!!!!!!!!!
-all rwby powers implemented, along with crossed rwby icons when used
-all characters have rwby icons shown, and displayed to everyone when used in same manner as coup cards
-improved challenge function, allowing player to claim having a certain card for further challenge
-no longer dies if initial connection to host fails
-displays error message if host tries to start a 1 player game
-version compatibility checking
-ruby runner game in loading screen
-resizeable game window
-neater code, fully commented
-save chat logs option
--include descrip of game, all rwby powers and rules, etc

## [1.0] - 2017-8-18
### Added
-core coup game logic to run on host
-chat box
-passing moves by button click
-distribution of rwby characters
-support for 2-7 players
-images for all rwby and coup characters, as well as move-indicating lights
-coup cards crossed out after elimination
-basic challenge function (lacking ability to claim card without actually challenging or specify
which card to challenge with)

### Fixed
-super-contessa bug
-steal from ambassador bug
-SocketTimeoutException bug


# Program description
--> Each program packaged with server and client code, with 1 player acting as host, and the rest
connecting as clients; host uses port forwarding if playing over internet
--> Host runs actual game code, clients send moves when approved by host to do so and receive updates on
game situation from host
--> Program distributed as jar or setup.exe, made with launch4j for exe and inno setup compiler
for ensuring exe has bundled jre access