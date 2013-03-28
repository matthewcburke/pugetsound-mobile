# Vi-Char Android App
This is one piece of the University of Puget Sound Fall 2012 Software Engineering course project.

## Overview

In the Fall of 2012 a Software Engineering course at the University of Puget Sound set out to develop a medium-sized software system as a group project involving all twenty-five students. Our intrepid professor, Joel Ross, wanted game in which a main character was controlled by a group of puppeteers whose actions were captured by a Microsoft Kinect. Furthermore, we needed to create a way for additional players to view and interact with the game using Android phones and augmented-reality technology. 

### More info 
- View the [course syllabus](http://cs.pugetsound.edu/~jross/courses/archive/f12/csci240/)
- View a static version of the [project website](http://cs.pugetsound.edu/~jross/courses/archive/f12/csci240/vichar).

## Outcomes

Overall, the project was a success.

The final product involved:

- A game engine built with Unity, to run the game,
- A module interpreting inputs from the Kinect and passing them the game engine, 
- An Android application to allow phone users to view the game play in augmented reality and participate in the game play by firing projectiles at the main character, and lastly,
- A web server to provide a communication link between the phones and the game engine and put a public face on the whole project. 

This repository contains the code of the resultant Android application. Ten students working in two teams created this application.

Unfortunately, without the game engine and web server running, there is no game to play. However, you can still download the Android application, install it on your phone, point it at one of our AR image targets, and see the game board and game objects in mind-blowing augmented reality. 

### Install the app
- Download the [Android application](http://cs.pugetsound.edu/~jross/courses/archive/f12/csci240/vichar/apk/edu.pugetsound.vichar.SplashScreen.apk) and install it on your Android device (that is, as long as you trust us student developers).
    - If you do download the application, you should also download an [image](https://raw.github.com/matthewcburke/pugetsound-mobile/develop/vichar/media/starCloud.jpg) to use as an augmented-reality target.
    - Once you get the application installed, launch it, start the game, and then point your devices camera at the above image target. Try to fill the screen and move in and out like you are scanning a QR code. You should see a static version of the game board and a line up of the game objects.

### A word on stability
The application was primarily tested on Dell Venue phones running Android 2.2, so that is where it runs most smoothly. Additionally, the application received a flurry of contributions, from diverse developers, in the days leading up to the final presentation. Needless to say this didn't help stability. So hopefully it doesn't crash on you ... if it does, it sometimes works on the second try, but honestly, I haven't checked how the current version performs without the web-server running.

## My Contributions
I have the repositories posted here so that others may see my work. As such, let me give an overview of what I contributed to this somewhat large project.

The Android application is a collaboration between the Mobile team and the augmented-reality (AR) team. I was a member of the AR team. The two teams initially had separate repositories. The AR team migrated their work into this, the mobile, repository about half way through the project. You can view a fork of the original AR repository [here](https://github.com/matthewcburke/augmented-reality).

I played a prominent role throughout the project(as evidenced by the [contributors graphs](https://github.com/matthewcburke/pugetsound-mobile/contributors)), but especially in the AR team([graph](https://github.com/matthewcburke/augmented-reality/contributors)). I carried the overall vision for the AR team and in many cases implemented key features of the application. I made significant contributions to the following files:

1. [GameParser.java](https://github.com/matthewcburke/pugetsound-mobile/blob/develop/vichar/src/edu/pugetsound/vichar/ar/GameParser.java)- This class parses a JSON object retrieved from the server that represents the current game state (locations of all of the objects) and inputs the data into a float array to be passed through the Java Native Interface to the C++ code.
2. [ARGameActivity.java](https://github.com/matthewcburke/pugetsound-mobile/blob/develop/vichar/src/edu/pugetsound/vichar/ar/ARGameActivity.java)- This is the game Activity in the Android application.
3. [ImageTargets.cpp](https://github.com/matthewcburke/pugetsound-mobile/blob/develop/vichar/jni/ImageTargets.cpp)- This is where the magic happens. Large portions of this file are from a [Vuforia](https://developer.vuforia.com/) sample application. We have made significant modifications to it so that it will draw multiple different objects where we want them.

In addition to contributing code, I spent a lot of time and energy fitting the whole system together. This included creating, modifying or converting assets like image targets and Blender models so that they would work with our application, fine tuning our interfaces with the other teams on the project, and frequently debugging and integrating other team members' contributions.
