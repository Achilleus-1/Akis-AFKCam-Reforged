# Aki's AFK Cam Reforged

**[Product of Achilleus](https://linktr.ee/achilleus_)** — Created and maintained by [Achilleus (Achilleus-1)](https://github.com/Achilleus-1).

Client-only AFK cinematic camera. Assign **Open Camera Settings**, **Enable / Disable AFK Camera**, and **Start / Stop Cinematic** in **Options → Controls → Key Binds → Aki's AFK Cam Reforged**. All three start unbound. Settings are also available through the mod's configuration button in the Mods screen.

## Installation

For **Minecraft 1.21.1**, **NeoForge 21.1.255**, and **Java 21**. Download `akis-afk-cam-reforged-1.21.1-2.3.1-neoforge.7.jar` from [Releases](https://github.com/Achilleus-1/Akis-AFKCam-Reforged/releases/latest) and place it in your `mods` folder. Remove older copies first.

The camera uses standard NeoForge keybindings and removes the custom key-sequence/rebinding system, global options saves, and persistent-mode movement input clearing. Legacy shortcuts are ignored; assign the new actions in Controls. Music-pack selection is applied in memory for each session without forcing an options save. Controls already reset by older versions need to be restored once.

## Development

Source code is in `src/main/java/`. Mod resources, logos, translations, and metadata are in `src/main/resources/`. Existing internal IDs and configuration paths are preserved.

Build with a Java 21 JDK: `./gradlew build` on Linux/macOS or `.\gradlew.bat build` on Windows. The installable JAR is written to `build/libs/`.

Launch the development client with `./gradlew runClient` or `.\gradlew.bat runClient`. Mod information and Minecraft/NeoForge versions are configured in `gradle.properties`. GitHub Actions checks the build on pushes and pull requests.

## Attribution

Ported from Ji AFK Cinematic by jiory_ (Jiory).

Licensed under **MIT**; copyright and license notices are included in [LICENSE](LICENSE) and packaged resources.

## Development and reuse

See [DEVELOPMENT.md](DEVELOPMENT.md) for reproducible checks and known archival dependencies, [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidance, and [SECURITY.md](SECURITY.md) for private reports.
