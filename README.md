<h1 align="center">SkyblockAddons</h1>

<p align="center">
  <a href="https://github.com/BiscuitDevelopment/SkyblockAddons/releases">
    <img alt="downloads" src="https://img.shields.io/github/downloads/BiscuitDevelopment/SkyblockAddons/total?color=56bcd3" target="_blank" />
  </a>
  <a href="https://github.com/BiscuitDevelopment/SkyblockAddons/blob/master/LICENSE">
    <img alt="license" src="https://img.shields.io/github/license/BiscuitDevelopment/SkyblockAddons?color=56bcd3" target="_blank" />
  </a>
    <a href="https://discord.gg/PqTAEek">
      <img alt="discord" src="https://img.shields.io/discord/450878205294018560?color=56bcd3&label=discord" target="_blank" />
    </a>
  <a href="https://twitter.com/bisccut">
    <img alt="twitter" src="https://img.shields.io/twitter/follow/bisccut?style=social" target="_blank" />
  </a>
</p>

A Minecraft Forge mod with many features to make your Hypixel skyblock experience better. Always looking for more features to add!

Join The Discord!
------
If you need support or have suggestions or whatnot, make sure to join [our discord!](https://discordapp.com/invite/PqTAEek)

Credits
-----
InventiveTalent for  allowing us to use her magma boss API in our project, please check out her magma boss timer website at https://hypixel.inventivetalent.org/skyblock-magma-timer/ .

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

Also thanks for our countless translators who help translate this mod!

For Contributors
------

Make sure to add this VM argument to your debug configuration or your IDE's equivalent, so all the transformers
are applied properly in your dev environment!
```-Dfml.coreMods.load=codes.biscuit.skyblockaddons.tweaker.SkyblockAddonsLoadingPlugin```

Note: If your jar build is failing because some code is trying to access private methods or fields, 
you may want to re-run the gradle tasks `setupDecompWorkspace` and `setupDevWorkspace` so 
the access transformer is applied to the source code!