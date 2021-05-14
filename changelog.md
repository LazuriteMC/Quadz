# Quadz 1.0.0 - At long last

### Overview
The coveted new release of Quadz (formerly FPV Racing Mod) is here! If you're wondering what's changed, the answer is 
everything!!

Well, not quite everything. *Internally* the mod has been almost completely rewritten due to its constant revision over
the past five and a half months. A few new features of note are:
* FPV Racing was renamed to Quadz
* Physics were broken out into a separate lib called [Rayon](https://github.com/LazuriteMC/Rayon)
* Chunk loading was broken out into a separate lib called [Lattice](https://github.com/LazuriteMC/Lattice)
* A config screen was added (using [Cloth](https://www.curseforge.com/minecraft/mc-mods/cloth-config) and [Fiber](https://github.com/FabLabsMC/fiber))
* Quadcopters have actual models now and are animated by [Gecko Lib](https://www.curseforge.com/minecraft/mc-mods/geckolib-fabric)
* Additional quadcopters can be loaded by users
* Configurable quadcopter settings (on right-click)
* A new goggles head model when worn
* Keyboard support
* Angle mode
* LOS (line of sight) camera following
* An OSD (on screen display)
* Toast popups showing controller connections/disconnections

### Rayon
For those who don't know, Quadz uses the bullet physics engine to simulate realistic flight characteristics as well as
collision handling. Until this release, Quadz included bullet integration into Minecraft as a part of its code. Now,
that part has been broken out into a separate library called [Rayon](https://github.com/LazuriteMC/Rayon) which can be
applied to other mods besides Quadz. A couple examples would be [Dropz](https://github.com/LazuriteMC/Dropz) and
[Thinking With Portatos](https://github.com/Fusion-Flux/Thinking-With-Portatos). More information on how to use the API
for other mods can be found [here](https://docs.lazurite.dev/rayon/getting-started).

### Lattice
Quadz features another new subsystem called [Lattice](https://github.com/LazuriteMC/Lattice) which allows chunks to load
not only around each player but also around each player's camera. The effect isn't noticeable unless the player and it's
camera are in two separate locations. For Quadz, this means that quadcopters flown in first-person-view are capable of
generating the world independently of the player. This was a feature of Quadz in the past (FPV Racing back then), but it
was achieved by moving the player along with the quadcopter which was :concern:.

### Quadcopter Templates
Data driven quadcopters are now possible in a similar way to how Minecraft's resource or data packs work. It allows
users to pack quadcopter information into a zip file or a folder and load from their `.minecraft/quadz` directory.
Additionally, any quadcopter template loaded into the game is transferable to other clients and servers. When a player
joins a server, the server will send all of its known templates to that client and also receive all of the client's
templates as well. This basically allows players to see templates created by other users without having to download it
themselves! There are instructions, and an example on how to create a quadcopter template
[here](https://github.com/LazuriteMC/Quadz-Template).