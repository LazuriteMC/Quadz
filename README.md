![Project icon](https://raw.githubusercontent.com/lazuritemc/Quadz/development/src/main/resources/assets/quadz/icon.png)

# Quadz

[![GitHub](https://img.shields.io/github/license/LazuriteMC/Quadz?color=A31F34&label=License&labelColor=8A8B8C)](https://github.com/LazuriteMC/Quadz/blob/development/LICENSE)
[![Discord](https://img.shields.io/discord/719662192601071747?color=7289DA&label=Discord&labelColor=2C2F33&logo=Discord)](https://discord.gg/NNPPHN7b3P)
[![Trello](https://img.shields.io/static/v1?label=Trello&message=Board&color=FFFFFF&logo=Trello&labelColor=0052CC)](https://trello.com/b/naSFhSWz/fpv-racing-mod)

Quadz adds flyable FPV drones to Minecraft using the [Fabric Modloader and API](https://fabricmc.net/). It also makes
use of several libraries including [Rayon](https://github.com/lazuritemc/rayon), a rigid body simulation library for minecraft, which was
developed with this mod in mind. Another library it uses is called [Lattice](https://github.com/lazuritemc/lattice) which allows chunks
to be loaded around the player's camera instead of just the player itself.

### Background
This mod began in late 2018 as a fun side project. Originally, it was just [BlueVista](https://github.com/ethanejohnsons) developing it
on his own whenever he had time. The original codebase was written to work with Forge but was later changed to Fabric due to its ease
of use and other benefits. It wasn't until May 2020 during COVID-19 lockdown that this mod began to take shape, and the first release came
the following September.

The old forge repository can be found [here](https://github.com/ethanejohnsons/FPV-Racing-Mod).

### Dependencies
In order to use this mod, you'll have to download a few other things as well:
* [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
* [Geckolib](https://www.curseforge.com/minecraft/mc-mods/geckolib-fabric)
* [Rayon](https://www.curseforge.com/minecraft/mc-mods/rayon)
  
### Optional Dependencies (Highly Recommended)
The following dependencies allow you to configure Quadz using its config screen. If you don't install these, you won't
be able to change your controller or camera settings.
* [Cloth Config API](https://www.curseforge.com/minecraft/mc-mods/cloth-config)
* [Mod Menu](https://www.curseforge.com/minecraft/mc-mods/modmenu)