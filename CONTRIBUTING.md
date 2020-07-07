# Contributing to SkyblockAddons
Thank you for taking the time to contribute to SkyblockAddons!

## Biscuit's Bakery Discord
If you haven't already, please join our Discord server. We discuss everything about the project there. We have channels dedicated to contributors where you can talk to other contributors and ask questions.

Link: https://discord.gg/PqTAEek

## Reporting an Issue
There are two ways to report an issue with SkyblockAddons: creating an issue on Github Issues and sending a message to the staff in the Discord Server.
### Github Issues
* Before creating a new issue, please search the issues page to ensure your issue wasn't already reported.
* If your issue was already reported and you have additional information that wasn't included in the original report, please add it as a comment.
* When creating a new issue, please use the "Bug Report" template.
* Please answer the questions in the template as fully as you can. We'll be able to locate and resolve the issue faster the more information we get.

### The Discord Server
* Please write a new message in the skyblock-addons-chat channel or beta-tester-chat if you are a beta tester. A member of staff will respond to your message and record the issue in the issue tracker.
* Please include as much detail as you can about the issue, including screenshots if possible

## Suggestions
* Please leave suggestions on our suggestions page: https://skyblockaddons.featureupvote.com/
* Before leaving a suggestion, please searh to make sure your suggestion hasn't already been recorded.
* If your suggestion is already on the page, please upvote it by clicking the number of votes beside it and then the upvote button.

## Changes to the Code
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

Note: If your jar build is failing because the code is trying to access private methods or fields,
this may be because someone added some new access transformers. 
You may want to re-run the gradle tasks `setupDecompWorkspace` and `setupDevWorkspace` so 
the access transformers are applied to the source code!

## Translation
