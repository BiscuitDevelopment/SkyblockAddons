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

Please make sure to make your pull requests off the development branch if you're adding new features. If there's
an important bug fix, still make your PR on development, but put it in a separate commit so I can cherry-pick it
into master branch. Thank you!

1. Make sure to add this VM argument to your debug configuration or your IDE's equivalent, so that all the transformers
are applied properly in your dev environment!
```-Dfml.coreMods.load=codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsLoadingPlugin```
2. This project uses Lombok, which helps with things such as not having to manually create Getters and 
Setters. If you are using IntelliJ, please download the plugin called Lombok either in your IDE or by 
[visiting this link](https://plugins.jetbrains.com/plugin/6317-lombok) so that everything works correctly. 
If you are using Eclipse, [you can read this page here.](https://projectlombok.org/setup/eclipse)
3. After installing the plugin in IntelliJ, go to `File` → `Settings` → `Build` → 
`Execution, Deployment, Compiler` → `Annotation Processors`, and check ☑ 
`Enable annotation processing` on the top right. This will allow you to debug properly.
4. If you are using the Gradle builder (you can check in `File` → `Settings` → `Build, Execution, Deployment`
 → `Gradle` → `Build and run using`), set the Gradle task ```processResourcesDev``` to run every time before 
 launching the Minecraft client from your dev environment. This is required for the mod assets to load properly
 in the dev environment.

Note: If your jar build is failing because the code is trying to access private methods or fields,
this may be because someone added some new access transformers. 
You may want to re-run the gradle tasks `setupDecompWorkspace` and `setupDevWorkspace` so 
the access transformers are applied to the source code!
