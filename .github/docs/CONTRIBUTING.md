Contributor's Guide
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
**Note:** You can use `gradle` instead of using gradle wrapper `./gradlew`.

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
2. (Optional) Run the **Minecraft Forge** client
    - Using an IDE
        - Run the debug configuration you created in "Getting Started."
    - Using the command line
    ```shell script
    ./gradlew runClient --args="--username <username> --password <password>"
    ```