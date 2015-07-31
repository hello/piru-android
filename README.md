piru-android
============

A small tool for performing firmware updates over Bluetooth for Sleep Pills.

Prerequisites
=============

- [Java](http://support.apple.com/kb/DL1572) (on Yosemite).
- [Android Studio](http://developer.android.com/sdk/index.html).
- The [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) (for lambda support).
- The correct SDK and build tools. These will be automatically installed by Android Studio and the Square SDK manager gradle plugin.
- The key stores `Hello-Android-Internal.keystore` and `Hello-Android-Release.keystore`. Acquire these from another team member.

Building
========

Place the `.keystore` files into the same directory as your local copy of the `piru-android` repository.

If you're building the app on a platform other than OS X, you will need to define JAVA_HOME in order for the project to find your installation of the JDK 8.
