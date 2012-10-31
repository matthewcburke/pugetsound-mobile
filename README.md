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

1. You will need to install the [Android NDK](http://developer.android.com/tools/sdk/ndk/index.html). It is required to build the application. More on that later.

2. For now, you'll need to install the [Vuforia SDK](https://ar.qualcomm.at/qdevnet/). Another potential solution would be to include the relevant parts of the SDK in the git repo. This is how the augmented-reality repo is currently set up.

3. The project needs to be nested two directories inside the [Vuforia SDK](https://ar.qualcomm.at/qdevnet/). For example, the way that the repository is currently set up, the PATH could be: `.../vuforia-sdk-android-1-5-9/mobile/vichar/`. As noted above, another solution would be to include the relevant parts of the Vuforia SDK in the repository (for an example, see the augmented-reality repo).

4. Eclipse Settings: If you don't already have the project open in an Eclipse work space, open a new workspace, and import the project as existing android code. 

5. You need to add a new class path variable to the Java build path. Open the Eclipse Preferences pane. Go to Java->Build Path->Classpath Variables. Create a new classpath variable named 'QCAR_SDK_ROOT' and point its path at the root of the Vuforia SDK. This should be two directories up from the project directory.

6. Modify the Project->Properties, Select 'Java Build Path' from the left menu. Select the 'Libraries' pane. Click 'Add External JAR …' find QCAR.jar at `.../vuforia-sdk-android-1-5-9/build/java/QCAR/QCAR.jar`, and click open to add the library. Now click on the 'Order and Export' pane and ensure that QCAR.jar is there and checked. 

7. Go to the command line and navigate to the project directory (it should be something like `.../vuforia-sdk-android-1-5-9/mobile/vichar/`. Run `ndk-build` (note: you need to have added the android-ndk directory to you environment PATH variable). You can also set up eclipse to run ndk-build. 

8. Back in Eclipse, Clean the project. 

At this point you should be able to run the application on a phone. 


