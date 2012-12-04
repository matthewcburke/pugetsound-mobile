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
* Click `Browse File System...` to select a location. Navigate to where you installed the Android NDK and select the `ndk-build` program (`ndk-build.cmd` on windows). On a windows machine, eclipse will get angry if there is a space in this location string. Since mine was in `Program Files (x86)` I used the DOS short name. See here (http://en.wikipedia.org/wiki/Program_Files) to figure out what it is for your configuration (32 or 64 bit).
* Click `Browse Workspace...` or `Browse File System...` and select the root project folder `vichar` as the 'Working Directory'.
* In the Build Options set the builder to run `During a clean`, this way you can run Project > Clean everytime you pull
* In the "Refresh" tab, you might want to set the builder to automatically refresh all resources (just be sure to save before you pull and/or project > clean).
* Click `OK` to finish.

_Note:_ you only need to re-run `ndk-build` if you have made changes to the native resources in the `jni/` folder. Otherwise, I believe that you can modify java code and still run the program.

6. Back in Eclipse, Clean the project. 

At this point you should be able to run the application on a phone. 


