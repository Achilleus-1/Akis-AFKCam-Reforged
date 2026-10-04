# Validation

Target: Minecraft 1.21.1, NeoForge 21.1.252, Java 21.

The final JAR was loaded from an isolated client's mods folder using the
NeoForge development launcher. Its test world and configuration were confined
to port/run-smoke. The mod does not require runtime Fabric components.

Passed:
- Gradle build.
- All 12 mixins applied, with required injections.
- Cinematic activation and 15-shot category mixing.
- All 30 camera presets at five frame positions each (150 samples).
- Finite camera position, yaw, and pitch; detached camera mode.
- Cinematic HUD and passive chat.
- F7 + H keyboard shortcut, config-screen initialization and rendering.
- Persistent mode clearing Minecraft 1.21.1 movement and jump inputs.
- Cinematic music initialization (vanilla in the development smoke test).
- Local OGG pack generation, resource reload, and custom music event discovery
  in the packaged test.
- Toggle toast rendering.
- Camera and HUD restoration, cinematic teardown, and clean shutdown.
- Every packaged class targets Java 21 (class version 65).
- Original MIT license, assets, and translations retained.
- No Fabric loader/API references remain in packaged classes.

The packaged run printed JI_RUNTIME_TEST_PASS and BUILD SUCCESSFUL.
Full local log: tools/final-validation.log.

Third-party modpack, shader, Sodium, Embeddium, and Smooth F5 combinations
were not tested. The clean NeoForge target was tested.

JAR: ji-afk-cinematic-1.21.1-2.3.1-neoforge.1.jar
SHA-256: a8716583958f22ae29ff6e879e136a8167dd796bf5d1e25429a74f07805bf229
