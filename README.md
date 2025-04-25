# go-fish-mobile-app
An android app for a hobbyist fisherman who likes to take their kayak or boat out into a lake or the ocean to catch fish.

## App Backend
The app interacts with a backend available [here](https://github.com/Go-Fish-2025/go-fish-backend)

## Pre-Requisites



## Installation

**Application**: [Android](https://www.android.com/)<br>
**Language**: [Kotlin](https://kotlinlang.org/)<br>
**Build Tool**: [Gradle](https://gradle.org/)

Use the following command to clone the repo.
```agsl
git clone https://github.com/Go-Fish-2025/go-fish-mobile-app.git
```

Open the Project only in [Android Studio IDE](https://developer.android.com/studio) which comes with all other necessary tools for building Android apps.

## Generating Builds

You can either run the app in an [Android Emulator](https://developer.android.com/studio/run/emulator), or connect your [Android Device](https://developer.android.com/studio/debug/dev-options) (with USB Debugging turned on) to Android Studio and click on the Green Play Button on top.

Alternatively, you can run the following command to generate an apk for this app. 

```agsl
./gradlew clean :app:assembleRelease
```

You can do the same thing from Android Studio as well. Go to Build > Generate App Bundles or APKs > Generate APKs

Additionally, you can also build a new apk from Android Studio by following the steps mentioned [here](https://developer.android.com/studio/run).

## Features

| Screen                | Description                                                                                                                                                                                     | Screenshot |
|-----------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------|
| Login                 | Login Screen of the app.                                                                                                                                                                        | 
| Dashboard             | Feature overview of the app.                                                                                                                                                                    |            |
| Aqua Scan             | Click a picture of any fish and get it identified within seconds with detailed info.                                                                                                            |            |
| Weather               | Get weather forecast for any place.                                                                                                                                                             |            |
| Compass               | A geographic compass to help you navigate if you're lost.                                                                                                                                       |            |
| Fishing Logs          | Enter details and click add to set the reminder.                                                                                                                                                |            |
| Maps                  | Navigation along with the provision to save checkpoints and view them offline.                                                                                                                  |            |




