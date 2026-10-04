# Ji AFK Cinematic — NeoForge 1.21.1 port

This is a source reconstruction and compatibility port of the supplied
`ji-afk-cinematic-1.21.2-1.21.3-2.3.1.jar`, originally created by jiory_.
The original MIT license is included in the JAR and source tree.

## Installation

Use Minecraft **1.21.1**, **NeoForge 21.1.252**, and Java **21**.
Put `ji-afk-cinematic-1.21.1-2.3.1-neoforge.1.jar` in the instance's `mods`
folder. Remove the original Fabric version from that instance.
This port is client-only and uses native NeoForge APIs.
It does not require Fabric API, Connector, or Mod Menu.

The configuration is available from NeoForge's Mods screen and through the
original shortcuts:

- F7, then H: configuration
- Ctrl + H: toggle the mod
- F7, then I: start or stop a cinematic immediately

The first key must remain held while pressing the second key; the sequence
timeout is 1.5 seconds. These shortcuts can be rebound in the configuration.
The default idle threshold is 30 seconds.

The configuration remains `config/ji-afk-cinematic.json`.
Local OGG music remains in `config/ji-afk-cinematic/music/`.

## Changes

- Replaced Fabric initialization, tick, shutdown, HUD, reload, and loader APIs
  with NeoForge equivalents.
- Registered the config screen with NeoForge's Mods screen.
- Adapted Minecraft 1.21.1 input, toast, and name-rendering APIs.
- Rebuilt all 12 mixins for Minecraft 1.21.1's Mojang names and signatures.
- Preserved camera presets, shot mixing, collisions, roll, AFK behavior,
  persistent modes, HUD transitions, chat visibility, music, and translations.
- Detect Sodium and Embeddium during early mixin preparation and skip the
  vanilla occlusion-culling redirect when either is present.
- Retained optional runtime probes, extended for isolated smoke testing.

The original namespace is preserved for assets and configuration. NeoForge's
registered mod ID is `ji_afk_cinematic` because NeoForge mod IDs use underscores.

## Building

Set `JAVA_HOME` to a Java 21 JDK and run:

```powershell
.\gradlew.bat build
```

The resulting JAR is in `build/libs/`. Gradle downloads the dependencies on the
first build. No original JAR or decompiler is needed to rebuild this source.

## Runtime validation

```powershell
.\gradlew.bat runClient -PsmokeTest
```

This launches an isolated client in `run-smoke`, creates a disposable flat world,
and checks cinematic activation, every camera preset, chat, the F7 + H shortcut,
the config screen, persistent input locking, music initialization, and camera/HUD
restoration. Success prints `JI_RUNTIME_TEST_PASS` and closes the client.

To also validate the packaged JAR and local music, place exactly one `.ogg` file
in `run-smoke/config/ji-afk-cinematic/music/` and run:

```powershell
.\gradlew.bat runPackagedSmoke
```

This loads the actual built JAR from `run-smoke/mods`, rebuilds and reloads the
local music resource pack, and checks that its music event is discovered.

Testing covers the clean NeoForge target. Compatibility with a particular
modpack, shaders, Sodium, Embeddium, or Smooth F5 requires testing that combination.
