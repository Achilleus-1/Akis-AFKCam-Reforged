# Validation of Aki AFK Cam Reforged

Date: October 4, 2026.
Target: **Minecraft 1.21.1, NeoForge 21.1.255, Java 21**.

## Completed checks

- Gradle build completed successfully against NeoForge 21.1.255.
- Packaged JAR ZIP integrity, TOML metadata, JSON resources, mod logo, mixin class references, author/credits, and preserved license notices verified.
- All packaged classes have Java 21 class-file version 65.
- All three renamed JARs loaded together into an isolated Minecraft client, opened a flat singleplayer test world, and exited normally following the AFK camera's smoke test.

- All 78 classes target Java 21.
- Packaged runtime test completed both alone and with the other two ports installed.
- Both runs printed **JI_RUNTIME_TEST_PASS** and **BUILD SUCCESSFUL**.
- Runtime probe checked cinematic activation, all 30 camera presets at five frame positions, camera state, cinematic HUD/chat, configuration rendering and shortcuts, persistent input locking, local OGG resource-pack reload and event discovery, toggle toast, camera/HUD restoration, and clean shutdown.
- Runtime mixin diagnostics found the required camera, HUD, keyboard, mouse, and Minecraft client hooks.

Not exercised: arbitrary modpacks, shaders, Sodium, Embeddium, or Smooth F5 combinations.

## Artifacts

Version: `2.3.1-neoforge.3`. SHA-256 hashes of the runtime and source JARs are in [SHA256SUMS.txt](SHA256SUMS.txt).

The historical PORT-NOTES and, where present, VALIDATION-ORIGINAL describe the earlier unbranded port and its older target/artifact hashes. This file describes the renamed edition.

## Branding update (2.3.1-neoforge.3)

The supplied black and white Achilleus logos replace the original mod icon. Both are copied unchanged and the white version is selected in mod metadata. Documentation uses theme-specific logos, the tagline “Product of Achilleus”, brand color `#ba0239`, a serif tagline, and the Linktree URL.

This edition was rebuilt against NeoForge 21.1.255. Packaged metadata, exact logo file hashes, preserved credits/licenses, ZIP integrity, source JARs, and checksums were checked. The runtime and gameplay checks above were performed on the preceding edition; they were not repeated for this branding update.
