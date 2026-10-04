# Aki AFK Cam Reforged

**NeoForge port and maintenance by [Achilleus (Achilleus-1)](https://github.com/Achilleus-1).**
An independently maintained port of Ji AFK Cinematic by jiory_ (Jiory).

Starts a cinematic camera while you are AFK, with camera presets, HUD transitions, music, and configurable shortcuts.

## Installation

Requires **Minecraft 1.21.1**, **NeoForge 21.1.255** (dependency range: 21.1.255 to below 21.2), and **Java 21**.

Download `aki-afk-cam-reforged-1.21.1-2.3.1-neoforge.2.jar` from [Releases](https://github.com/Achilleus-1/Akis-AFKCam-Reforged/releases) and place it in your instance's `mods` folder. Remove older/original copies of Ji AFK Cinematic before installing this port.

This is a client-only mod. Fabric API, Connector, and Mod Menu are not required.

- **F7 + H:** settings (hold F7 while pressing H).
- **Ctrl + H:** enable or disable.
- **F7 + I:** start or stop a cinematic.

Shortcuts are configurable; the default AFK threshold is 30 seconds. Existing settings remain in `config/ji-afk-cinematic.json`. Add local OGG music in `config/ji-afk-cinematic/music/`.

## Build from source

With a Java 21 JDK installed, set `JAVA_HOME` to that JDK and run from this repository:

```powershell
.\gradlew.bat build --console=plain
```

Linux/macOS: `./gradlew build --console=plain`. Build outputs are in `build/libs/`; dependencies download on the first build. Original mod JARs and decompilers are not needed to rebuild.

## Compatibility and validation

The original internal mod ID `ji_afk_cinematic` and resource/config namespaces are preserved to retain compatibility. The displayed name, distribution filename, repository, and maintainer credits use the new branding.

See [VALIDATION.md](VALIDATION.md) for verification of this edition. [PORT-NOTES.md](PORT-NOTES.md) records the earlier port's migration and historical testing, including its older filenames and NeoForge target; it does not establish runtime results for this edition.

## Credits and license

See [CREDITS.md](CREDITS.md) and [LICENSE](LICENSE). Achilleus maintains the NeoForge port; original authors retain credit for their work. This is an unofficial port. Original logos remain temporarily until replacement branding is supplied.
