git-flow
========
This project uses [git-flow](https://github.com/nvie/gitflow/).

Here are the [installation instructions](https://github.com/nvie/gitflow/wiki/Installation).

Here are [usage instruction](http://jeffkreeftmeijer.com/2010/why-arent-you-using-git-flow/).

Here's an explanation of the [branching model](http://nvie.com/posts/a-successful-git-branching-model/) git-flow uses.

Also, it's best to to use `git pull --rebase` when pulling the develop branch so as to keep commit history clean.

Style Conventions
=================

All mobile Android code should follow the official Android style guide: http://source.android.com/source/code-style.html

Installation
===========

Note that the AR activity will not run on the emulator, only on a phone with a camera. It has only been tested on the school's Dell Venue running Android 2.2. Since integrating the AR activity into this app, some additional steps are required to get the app to build and run from Eclipse.

1. You will need to install the [Android NDK](http://developer.android.com/tools/sdk/ndk/index.html). It is required to build the application. More on building in step 5.

2. Eclipse Settings: If you don't already have the project open in an Eclipse work space, open a new workspace, and import the project as existing android code. 

3. You need to add a new class path variable to the Java build path. Open the Eclipse Preferences pane. Go to Java->Build Path->Classpath Variables. Create a new classpath variable named 'QCAR_SDK_ROOT' and point its path at the root of the repository (i.e. `mobile/`). The `build/` folder must be in this directory.

4. Modify the Project->Properties, Select 'Java Build Path' from the left menu. Select the 'Libraries' pane. Click 'Add External JAR …' find QCAR.jar in the repository at `mobile/build/java/QCAR/QCAR.jar`, and click open to add the library. Now click on the 'Order and Export' pane and ensure that QCAR.jar is there and checked. The order also seems to matter. I'm not sure of the details, but if you put the QCAR.jar above the project `src/` and `gen/` folders in this list, it seems to work. 

5. Go to the command line and navigate to the project directory (unless you renamed something it should be `.../mobile/vichar/`. Run `ndk-build` (note: you need to have added the android-ndk directory to you environment PATH variable). You can also set up eclipse to run ndk-build. To do this:

* open the project properties and
* select `Builders` from the left hand menu,
* click the `New...` button to create a new builder.
* in the next window, select `Program` and click `OK`.
* Name the builder 'ndk-build'.
* Click `Browse File System...` to select a location. Navigate to where you installed the Android NDK and select the `ndk-build` program.
* Click `Browse Workspace...` and select the `jni/` folder as the 'Working Directory'.
* Click `OK` to finish.

_Note:_ you only need to re-run `ndk-build` if you have made changes to the native resources in the `jni/` folder. Otherwise, I believe that you can modify java code and still run the program.

6. Back in Eclipse, Clean the project. 

At this point you should be able to run the application on a phone. 


