# Changelog

## [Unreleased]
~Changed ambassador function, click card to ambassador or click button ambassador both
-Updater script to download new exe each time 
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

## [1.1] - 2017-9-4
### Added
-New games start at the end of old ones, without having to restart the program

### Fixed
-Assassination in challenge function now generally works as it should, allowing target to claim a card
or call attacker on assassin 

## Bugs
-Sometimes get an exception in changing opacity of challenging buttons in assassination

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