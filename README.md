# STS battle AI

This project originally contained all components necessary to run the battle AI.

Parts of the projects that are easily usable by others have been broken off into their
own projects and are available.

Note that while many mods have Github repositories, these may not be up to date with the latest
version on the steam workshop. The precompiled JAR files can be gotten from the steam workshop
links by using https://steamworkshopdownloader.io/.

## Common Requirements

Mod the Spire (https://steamcommunity.com/sharedfiles/filedetails/?id=1605060445)

BaseMod (https://steamcommunity.com/workshop/filedetails/?id=1605833019)

## AI Requirements

Communication Mod (https://steamcommunity.com/workshop/filedetails/?id=2131373661)

STS State Saver Mod (https://github.com/boardengineer/STSStateSaver)

Ludicrous Speed Mod (https://github.com/boardengineer/LudicrousSpeed)

### Running Instructions

1) Download/Build all needed mods and place them in your mods folder.

2) Make two empty folders in your root SlayTheSpire directory, called `startstates` and
`savestates`

3) Start the game 'server' by running ModTheSpire.jar with the `-DisServer=true` flag, the screen
should open and go black. ***NOTE:*** When running `java -jar ...` the `-DisServer` flag must come
*before* the `-jar` flag, or it won't work.

4) Start another copy of the game without the server flag and start playing normally.  Once in a
fight with Ironclad,Silent, or Defect, press the start AI button and it should complete the AI 
should complete the fight.