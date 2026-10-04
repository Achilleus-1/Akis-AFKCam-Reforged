# Development

Install JDK 21. From the repository root run `./gradlew build` (Windows:
`gradlew.bat build`). The committed wrapper selects the project Gradle version.
Run `python tools/check_repository.py` with Python 3.12 for source/privacy checks.

The existing Minecraft/NeoForge workflow builds the mod; the repository workflow
checks source and metadata. Test behavior in the matching Minecraft and NeoForge
versions declared in `gradle.properties`. A successful build does not verify
in-game behavior. Preserve the existing mod license and upstream attribution.
