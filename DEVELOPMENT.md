# Development

Install JDK 21. From the repository root run `./gradlew build` (Windows:
`gradlew.bat build`). The committed wrapper selects the project Gradle version.
Run `python tools/check_repository.py` with Python 3.12 for source/privacy checks.

The existing Minecraft/NeoForge workflow builds the mod; the repository workflow
checks source and metadata. Test behavior in the matching Minecraft and NeoForge
versions declared in `gradle.properties`. A successful build does not verify
in-game behavior. Preserve the existing mod license and upstream attribution.

## Keybinding persistence regression

Use the isolated `run-smoke` game directory, never a real modpack's options. Put
one valid `.ogg` file in `run-smoke/config/ji-afk-cinematic/music/`. In
`run-smoke/options.txt`, add or replace this line:

```text
key_key.ji_afk_cinematic.persistence_probe:key.keyboard.semicolon
```

Run `./gradlew runPackagedSmoke -PtestKeybindings` (Windows:
`.\gradlew.bat runPackagedSmoke -PtestKeybindings`). The probe registers a mod
binding whose default is H and verifies that its saved semicolon binding survives
startup and a local music resource reload, both in memory and in `options.txt`.
It also checks that the generated music pack selection is persisted. Success logs
`JI_KEYBINDING_PERSISTENCE_PASS` and `JI_RUNTIME_TEST_PASS`.

Test first with `resourcePacks:[]` in the isolated options file, then rerun without
editing the saved options to cover an already selected pack and a restart.
Restoring the old unconditional `options.save()` in music-pack setup should fail
with `custom mod keybinding was reset to key.keyboard.h`. Test registration and
assertions are enabled only by the smoke-test JVM properties.
