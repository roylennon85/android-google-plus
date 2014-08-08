android-google-plus
===================

GooglePlus integration for Android

Google+

Setup

1. Create your project and add Google Play Services. http://developer.android.com/google/play-services/setup.html
2. Enable Google+ API from https://code.google.com/apis/console.
3. Create an OAuth 2.0 client ID by adding your package name and SHA1 debug and production key.
4. Add permissions in AndroidManifest.xml: INTERNET, GET_ACCOUNTS, USE_CREDENTIALS.
5. Implement ConnectionCallbacks and OnConnectionFailedListener interfaces.
6. Initialize GoogleApiClient on onCreate() method and manage its lifecicle in onStart() and onStop() methods.
7. Implement onConnectionFailed() method to handle connection errors and connection pending intents.
8. Implement onActivityResult() method to handle the pending intent result.
9. Implement onConnectionSuspended() method to try to connect again.

Sign in

1. Add the sign in button in activity_main.xml.
2. Set onClickListener() to the button.
3. On the onConnectionFailed() method, try to connect.
4. Handle sign in button onClick() event. Try to connect.
5. Add sign out functionality.
6. Add revoke access functionality. See revokeAccess() method.
7. For more information visit https://developers.google.com/+/mobile/android/sign-in

Sharing to Google+

1. Add a share button in activity_main.xml.
2. Launch Google+ share dialog. See share() method.

Manage app activities

1. Create a target.
2. Create a moment.
3. Publish moment. See shareMoment() method.

Getting people and profile information

1. To get list of people in the user's circles, use loadVisiblePeople().
2. Implement ResultCallback<LoadPeopleResult> interface.
3. Handle the request result in onResult() method.
4. To get a list of people, use Plus.PeopleApi.load(mGoogleApiClient, userIdsList).setResultCallback(this).
5. To get user information, see getUserInfo() method.
