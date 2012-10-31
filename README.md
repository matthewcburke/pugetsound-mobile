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

More detailed instructions, and/or better solutions to come. In the mean time:

1. Since integrating the AR activity into this app, the [Android NDK](http://developer.android.com/sdk/index.html) is required to build the application.

2. The AR activity will not run on the emulator, only on a phone with a camera. It has only been tested on the school's Dell Venue running Android 2.2.

3. The project needs to be nested two levels inside the [Vuforia SDK](https://ar.qualcomm.at/qdevnet/). For example, the way that the repository is currently set up, the PATH could be: vuforia-sdk-x-x-x/mobile/vichar/ . Another solution would be to include the relevant parts of the Vuforia SDK in the repository (for an example, see the augmented-reality repo).


