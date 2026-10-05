# Aki AFK Cam Reforged

**[Product of Achilleus](https://linktr.ee/achilleus_)** — NeoForge port and maintenance by [Achilleus (Achilleus-1)](https://github.com/Achilleus-1).

Client-only AFK cinematic camera. Settings: F7 + H. Toggle: Ctrl + H. Start/stop: F7 + I.

## Installation

For **Minecraft 1.21.1**, **NeoForge 21.1.255**, and **Java 21**. Download `aki-afk-cam-reforged-1.21.1-2.3.1-neoforge.4.jar` from [Releases](https://github.com/Achilleus-1/Akis-AFKCam-Reforged/releases/latest) and place it in your `mods` folder. Remove older copies first.

Version `2.3.1-neoforge.4` fixes custom mod keybindings being reset at startup: local music-pack selection is saved only after NeoForge restores saved controls and resource loading finishes. Controls already reset by older versions need to be restored once after installing the fix.

## Development

Source code is in `src/main/java/`. Mod resources, logos, translations, and metadata are in `src/main/resources/`. Existing internal IDs and configuration paths are preserved.

Build with a Java 21 JDK: `./gradlew build` on Linux/macOS or `.\gradlew.bat build` on Windows. The installable JAR is written to `build/libs/`.

Launch the development client with `./gradlew runClient` or `.\gradlew.bat runClient`. Mod information and Minecraft/NeoForge versions are configured in `gradle.properties`. GitHub Actions checks the build on pushes and pull requests.

## Credits and license

Based on Ji AFK Cinematic by jiory_ (Jiory). Original copyright and license notices are retained in [LICENSE](LICENSE) and packaged resources. Port modifications are MIT licensed; Achilleus supplied the replacement branding.

## Development and reuse

See [DEVELOPMENT.md](DEVELOPMENT.md) for reproducible checks and known archival dependencies, [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidance, and [SECURITY.md](SECURITY.md) for private reports.
