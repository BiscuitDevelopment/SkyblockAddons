<h1 align="center">SkyblockAddons</h1>

<p align="center">
  <a href="https://github.com/BiscuitDevelopment/SkyblockAddons/releases" target="_blank">
    <img alt="downloads" src="https://img.shields.io/github/v/release/BiscuitDevelopment/SkyblockAddons?color=56bcd3" />
  </a>
  <a href="https://github.com/BiscuitDevelopment/SkyblockAddons/releases" target="_blank">
    <img alt="downloads" src="https://img.shields.io/github/downloads/BiscuitDevelopment/SkyblockAddons/total?color=56bcd3" />
  </a>
  <a href="https://github.com/BiscuitDevelopment/SkyblockAddons/blob/master/LICENSE" target="_blank">
    <img alt="license" src="https://img.shields.io/github/license/BiscuitDevelopment/SkyblockAddons?color=56bcd3" />
  </a>
  <a href="https://discord.gg/PqTAEek" target="_blank">
    <img alt="discord" src="https://img.shields.io/discord/450878205294018560?color=56bcd3&label=discord" />
  </a>
  <a href="https://twitter.com/bisccut" target="_blank">
    <img alt="twitter" src="https://img.shields.io/twitter/follow/bisccut?style=social" />
  </a>
  <a href="https://translate.biscuit.codes" target="_blank">
    <img alt="discord" src="https://badges.crowdin.net/skyblockaddons/localized.svg" />
  </a>
</p>

A Minecraft Forge mod with many features to make your Hypixel Skyblock experience better. Always looking for more features to add!

Special Credits
-----
InventiveTalent for allowing us to use her magma boss API in our project, please check out her magma boss timer website at https://hypixel.inventivetalent.org/skyblock-magma-timer/ .

TirelessTraveler for helping maintain the project in the past.

DidiSkywalker & CraftedFury (Nahydrin) for adding/PRing many incredible features!

Thanks to YourKit
------
![YourKit](https://www.yourkit.com/images/yklogo.png)

Big thank you to YourKit for supporting this project with their profiler to help us improve performance!


YourKit supports open source projects with innovative and intelligent tools 
for monitoring and profiling Java and .NET applications.
YourKit is the creator of [YourKit Java Profiler](https://www.yourkit.com/java/profiler/),
[YourKit .NET Profiler](https://www.yourkit.com/.net/profiler/),
and [YourKit YouMonitor](https://www.yourkit.com/youmonitor/).

Thanks to Crowdin & Our Translators
------
![Crowdin](https://crowdin.com/images/crowdin-logo.svg)

Big thanks to Crowdin for supporting this project with their 
localization management platform to help us serve the mod in many languages!
[Check out their website here](https://crowdin.com/)!

Also thanks for our countless translators who help us translate this mod!

Thanks to Open Source Software
------
Big thanks to all the open source software we use in this project! They are all listed [here](/.github/docs/OPEN_SOURCE_SOFTWARE.md).


For Contributors
------

Please make sure to make your pull requests off the `development` branch if you're adding new features. If there's
an important bug fix, still make your PR on development, but put it in a separate commit so I can cherry-pick it
into `master` branch. Thank you!
```shell script
# Not recommended
git checkout -b development --track origin/development
```

### Requirements
- **IDE** (One of them)
  - [IntelliJ IDEA](https://www.jetbrains.com/idea/) **(Recommended)**
  - [Eclipse](https://www.eclipse.org/)
- **(Plugin) Lombok** to help with things such as not having to manually create Getters and Setters.
  - [IntelliJ IDEA](https://plugins.jetbrains.com/plugin/6317-lombok)
    - **After installing the plugin**
      - Go to `Preferences`
      - Go to `Build, Execution, Deployment`
      - Go to `Compiler`
      - Go to `Annotation Processors`
      - Check ☑ `Enable annotation processing`.
  - [Eclipse](https://projectlombok.org/setup/eclipse)

### Getting started
**Note:** You can use `gradle` instead using gradle wrapper `./gradlew`.

1. Clone the repository
2. Setup the development environment
    ```shell script
    ./gradlew setupDecompWorkspace
    ```
3. Integrate the development environment with your IDE
    - IntelliJ IDEA
    ```shell script
    ./gradlew idea genIntellijRuns
    ```
    - Eclipse **(doesn't generate debug configuration)**
    ```shell script
    ./gradlew eclipse
    ```
    - (Eclipse) Change **Text File Encoding** from `Default` to `UTF-8`
      - Go to `Window` -> `Preferences` -> `General` -> `Workspace`
      - Change `Text File Encoding` from `Default` to `UTF-8`

4. Make sure to add the **VM** argument to your debug configuration
    ```text
    -Dfml.coreMods.load=codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsLoadingPlugin
    ```
5. Include your **Minecraft** username and password as arguments in the **debug configuration**.
   In order, to login into [Hypixel](https://hypixel.net) by your account.
    ```text
    --username "<username>" --password "<password>"
    ```
   > **Note:** Don't share your password with **anyone**.
   > **We aren't going to ask you about your password!**
6. **You are now ready to build the mod!**

### How to build
1. Build the mod
    ```shell script
    ./gradlew build
    ```
    > **Note**
    > 
    > If your jar **build** is **failing** because the code is trying to access private methods or fields,
    > this may be because someone added some new access transformers.
    >
    > You may want to re-run the gradle task
    > ```shell script
    > ./gradlew setupDecompWorkspace
    > ```
    > so the access transformers are applied to the source code!
    > 
2. (Optional) Run **Minecraft Forge** client
    ```shell script
    ./gradlew runClient
    ```
