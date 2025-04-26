# go-fish-mobile-app
An android app for a hobbyist fisherman who likes to take their kayak or boat out into a lake or the ocean to catch fish.

## App Backend
The app interacts with a backend available [here](https://github.com/Go-Fish-2025/go-fish-backend)

## Pre-Requisites

### Link your app to firebase

1. Create a new GoFish [Firebase](https://firebase.google.com/) Project. The same one you will create or have created for the [backend](https://github.com/Go-Fish-2025/go-fish-backend).
2. Go to Project Settings > General > Your Apps.
3. If you had already added this Android app while creating the Firebase project in Step 1, then it should be listed here. If not, you can add your app to the firebase project now by clicking Add App.
4. Once your app is listed. Perform the following steps.
   - Click on your app and add the `google-services.json` in the `/app` folder (exactly 1 level below the repository root folder). Your app can now use the Firebase resources.
   - Run the following command in your Android Studio terminal.
     ```agsl
     ./gradlew signingReport 
     ```
     Once it generates a result, copy the `SHA1` value and paste it in the SHA certificate fingerprints section under your app in the same page in Firebase after clicking Add Fingerprint.
   -  Copy the oauth-client-id from the `google-services.json` file mentioned earlier and paste the value inside the string resource [here](https://github.com/Go-Fish-2025/go-fish-mobile-app/blob/1b5199a8bb6967818b7bd11ca703a96728bf85f7/app/src/main/res/values/strings.xml#L98).

     This step is needed for the Google OAuth sign-in with Firebase Authentication.

### Link your app to the backend.
Once your backend is up and running (locally or deployed), make sure you enter its complete address with the port number (5001) in this app code so that they can communicate with each other. You need to just replace the url in [this](https://github.com/Go-Fish-2025/go-fish-mobile-app/blob/1b5199a8bb6967818b7bd11ca703a96728bf85f7/app/src/main/java/com/garlicbread/gofish/retrofit/RetrofitInstance.kt#L10) line 

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

| Screen        | Description                                                                                                                              | Screenshot                                                                                                                                                                                                                            |
| ------------- | ---------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Login         | Login Screen of the app.                                                                                                                 | <img src="https://github.com/user-attachments/assets/ef826dd5-8054-4969-8c0d-19806a5a7d46" width="240"/>                                                                                                                             |
| Dashboard     | Feature overview of the app.                                                                                                             | <img src="https://github.com/user-attachments/assets/5e8eb7b2-9ced-4820-bfef-991def8fb770" width="240"/>                                                                                                                             |
| Aqua Scan     | Click a picture of any fish and get it identified within seconds with detailed info.                                                     | <img src="https://github.com/user-attachments/assets/e8519f15-68b1-4be4-922f-3ca5b76df56a" width="240"/>                                                                                                                             |
| Weather       | Get weather forecast for any place.                                                                                                      | <img src="https://github.com/user-attachments/assets/d86440ce-f509-4143-a558-cfc005122b3e" width="240"/>                                                                                                                             |
| Compass       | A geographic compass to help you navigate if you’re lost.                                                                                | <img src="https://github.com/user-attachments/assets/660a30eb-552a-4ce4-bae4-e582a328b3c3" width="240"/>                                                                                                                             |
| Fishing Logs  | Log all your catches and also compare them to the currently held world record. Get the exact log you need using Smart Search           | <img src="https://github.com/user-attachments/assets/cc98fc2c-949c-49a6-b56e-783aadb947ed" width="240"/> <img src="https://github.com/user-attachments/assets/ddbed384-8f0c-4a22-90c6-44de605afd8f" width="240"/> <img src="https://github.com/user-attachments/assets/baf191de-f86e-483e-b337-28dc61608819" width="240"/> |
| Maps          | Navigation along with the provision to save checkpoints and view them offline.                                                           | <img src="https://github.com/user-attachments/assets/c9cc615e-bb2b-4313-be43-1d7557c3de2a" width="240"/> <img src="https://github.com/user-attachments/assets/eac73d03-690d-45ff-b193-8041d4e15b38" width="240"/>           |





