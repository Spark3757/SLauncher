# SLauncher

## Introduction

SLauncher is a Minecraft launcher which supports Mod management, game customizing, auto installing(Forge, LiteLoader and
OptiFine), modpack creating, UI customizing and so on.

No plugin API is provided.

## Contribution

If you want to submit a pull request, there're some requirements:

* IDE: Intellij IDEA.
* Compiler: Java 1.8.
* Do NOT modify `gradle` files.

### Compilation

Simply execute following command:

```bash
./gradlew clean build
```

Make sure you have Java installed with Pack200 and JavaFX 8 at least. Liberica full JDK 8~11 is recommended.

## JVM Options (for debugging)

|Parameter|Description|
|---------|-----------|
|`-Dslauncher.bmclapi.override=<version>`|Override api root of BMCLAPI download provider, defaults to `https://bmclapi2.bangbang93.com`. e.g. `https://download.mcbbs.net`.|
|`-Dslauncher.font.override=<font family>`|Override font family.|
|`-Dslauncher.version.override=<version>`|Override the version number.|
|`-Dslauncher.update_source.override=<url>`|Override the update source.|
|`-Dslauncher.authlibinjector.location=<path>`|Use specified authlib-injector (instead of downloading one).|