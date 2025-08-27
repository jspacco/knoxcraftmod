# Knoxcraft Terps: 3D Logo with Turtles

![Terp image showing Java code and the pryamid structure it creates](docs/img/knoxcraft-img1.png)

## Students

* Download the Java client code release TODO: create and link release
* Unzip and open in VS Code
    - double-click the `knoxcraft.code-workspace` file!
    - ❌ File => Open folder will NOT work correctly
* Install vanilla Minecraft 1.21.8
* Choose Multiplayer
* Enter the server URL provided by your instructor
* Log into Knoxcraft!


### Knoxcraft Terps commands

`/terp list` <font style="color: blue;"># list Terp programs</font>

`/terp summon` <font style="color: blue;"># summon your Terp to your current position</font>

`/terp forward|back|up|down|left|right|turnleft|turnright` <font style="color: blue;"># move or turn your terp</font> 

`/terp run <program_name>` <font style="color: blue;"># run the program_name </font> 

### Configure Knoxcraft Terps programs:

Each Knoxcraft Terps program, such as [Mauritius.java](https://raw.githubusercontent.com/jspacco/knoxcraftmod/refs/heads/mc/1.21.8-vanilla/src/main/java/edu/knox/knoxcraftmod/client/example/Mauritius.java), begins with a series of variable declarations.

```java
// Your instructor gives you this URL
String serverUrl = "http://euclid.knox.edu:8080";
// Your Minecraft playername
// You need to buy Minecraft
// This fulfills Spacdog's years-long dream:
// telling students they need to buy Minecraft for school
String minecraftPlayername = "spacdog";
// Your instructor gives you username/password
// They may not bother with username/password
// if that's the case just leave it as the empty string
String username = "test";
// THIS IS NOT YOUR COLLEGE EMAIL PASSWORD
// It's an insecure password that only works with Knoxcraft
String password = "foobar123";
// Pick a name for your program
// NO SPACES
String programName = "house";
// Whatever description you want
String description = "Build the classic first Minecraft house: a primitive hovel made of dirt, grass, and random blocks";
```


## Instructors

You will need to run a Minecraft server for your students. This is not as difficult as you are currently thinking it will be.

Requirements:
* A linux server visible to your students where you can run a Minecraft server
    - Talk to your IT staff
    - Yes it's possible to run a server in the cloud, but it's not trivial
* Download Forge [1.21.8-58.0.6](https://maven.minecraftforge.net/net/minecraftforge/forge/1.21.8-58.0.10/forge-1.21.8-58.0.10-installer.jar)
* Download the latest release of knoxcraftmod TODO: link
* Download the [install-server.sh](https://raw.githubusercontent.com/jspacco/knoxcraftmod/refs/heads/mc/1.21.8-vanilla/tools/install-server.sh) script

Create the following directory structure (or something like it)

```
└── 📁Knoxcraft/
    ├── 📁server
    ├── forge-1.21.8-58.0.6-installer.jar
    ├── install-server.sh
    └── knoxcraftmod-0.0.1.jar
```

If the `server` folder does not exist, the script will create it.

Now `cd` into the `Knoxcraft` folder and run this command:

`./install-server.sh forge-1.21.8-58.0.6-installer.jar knoxcraftmod-0.0.1.jar server`

This will install and start up a new Minecraft server modded with Knoxcraftmod in the `server` folder.

## FAQ

* Q: 
    - A:
* Q: Why is this so awesome?
    - A: I'm amazing


## Minecraft / Forge compatibility
| Minecraft | Forge | Release | Major changes |
|-------|-------|-------|-------|
| 1.21.8 | 58.0.6  | 0.0.1 | [Event Bus 7](https://gist.github.com/PaintNinja/ad82c224aecee25efac1ea3e2cf19b91) <br>[Tick phase](https://forums.minecraftforge.net/topic/154394-forge-541-minecraft-1214/?utm_source=chatgpt.com) <br>[Codecs](https://docs.minecraftforge.net/en/latest/datastorage/codecs/)|
| 1.21.5 | 55.0.24 | No release<br>[mc-1.21.5-vanilla](https://github.com/jspacco/knoxcraftmod/tree/mc/1.21.5-vanilla) branch | |
| 1.21.1 | 52.1.3  | No release<br>[mc-1.21.1-vanilla](https://github.com/jspacco/knoxcraftmod/tree/mc/1.21.1-vanilla) branch | |
| 1.21   | 51.0.33 | No release<br>[mc-1.21-vanilla](https://github.com/jspacco/knoxcraftmod/tree/mc/1.21-vanilla) branch | |

