package part4project.uoa.gather;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.result.DataTypeResult;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            preference.setSummary(stringValue);
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || FitnessPreferenceFragment.class.getName().equals(fragmentName)
                || FoodPreferenceFragment.class.getName().equals(fragmentName)
                || SocialMediaPreferenceFragment.class.getName().equals(fragmentName)
                || SocialMedia2PreferenceFragment.class.getName().equals(fragmentName)
                ;
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            bindPreferenceSummaryToValue(findPreference("example_text"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    //GOOGLEFIT Initialised variables
    private static final String TAG2 = "GoogleFit"; // log Tag
    private static List<DataType> DATATYPES = Arrays.asList(DataType.AGGREGATE_HEART_RATE_SUMMARY, DataType.AGGREGATE_BASAL_METABOLIC_RATE_SUMMARY,DataType.AGGREGATE_DISTANCE_DELTA, DataType.AGGREGATE_SPEED_SUMMARY, DataType.AGGREGATE_BODY_FAT_PERCENTAGE_SUMMARY, DataType.AGGREGATE_CALORIES_EXPENDED, DataType.AGGREGATE_HYDRATION, DataType.AGGREGATE_NUTRITION_SUMMARY, DataType.AGGREGATE_ACTIVITY_SUMMARY, DataType.AGGREGATE_STEP_COUNT_DELTA,DataType.AGGREGATE_POWER_SUMMARY);
    private static List<String> PREFNAMES = Arrays.asList("HR", "BMR","distance", "speed", "fat", "calories", "hydration", "nutrition", "activity", "step", "power");
    private static Preference.OnPreferenceClickListener eachPreferenceListener;
    private static Preference.OnPreferenceClickListener googleFitParentListener;
//    public static List<String> grantedGFPermissions = new LinkedList<>(); //TODO
//    public static List<String> deniedGFPermissions  = new LinkedList<>(); //TODO

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class FoodPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_google_fit);
            setHasOptionsMenu(true);

            // Adds the Preference Listeners to the parent and children preferences accordingly
            if (googleFitParentListener == null) {
                createParentListener();
                Preference pref = getPreferenceManager().findPreference("google_fit_all");
                pref.setOnPreferenceClickListener(googleFitParentListener);
            }
            if (eachPreferenceListener == null) {
                createChildListener();
                for (int i = 0; i < PREFNAMES.size(); i++) {
                    Preference pref2 = getPreferenceManager().findPreference(PREFNAMES.get(i));
                    pref2.setOnPreferenceClickListener(eachPreferenceListener);
                }
            }
            MainActivity.mGoogleApiClient.connect();
        }

        /**
         * Creates the Parent Listener to be added as an on Click Listener
         * This connects or disconnects google fit to the google play services accordingly
         */
        private void createParentListener() {
            googleFitParentListener = new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SwitchPreference pref = (SwitchPreference) preference;
                    if (pref.isChecked()) { // Re-connects GoogleFit
                        connectGoogleFit();
                    } else {
                        disconnectGoogleFit(); // Disconnects GoogleFit
                    }
                    return true;
                }
            };
        }

        /**
         * Methods called by the parent listener
         * This disconnects the API Client to the Google Play Services
         */
        private void disconnectGoogleFit() {
            if (!MainActivity.mGoogleApiClient.isConnected()){
                Log.d(TAG2, "Google Client API Wasn't connected");
                MainActivity.mGoogleApiClient.connect();
            }

            PendingResult<Status> pendingResult = Fitness.ConfigApi.disableFit(MainActivity.mGoogleApiClient);
            pendingResult.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(@NonNull Status status) {
                    if (status.isSuccess()){
                        Toast.makeText(getActivity(), "Disabled permissions for Google Fit", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "Could not disable permissions for Google Fit", Toast.LENGTH_LONG).show();
                        //TODO: Change switch preference back
                    }
                }
            });
        }

        /**
         * Methods called by the parent listener
         * This connects the API Client to the Google Play Services
         */
        private void connectGoogleFit() {
            MainActivity.mGoogleApiClient.reconnect();
            Toast.makeText(getActivity(), "Enabled permissions for GoogleFit ", Toast.LENGTH_LONG).show();
        }

        /**
         * Creates the Child Listener that is used for each switch preference datatype that is a child
         * It subscribes or unsubscribes the recording API accordingly
         */
        private void createChildListener() {
            eachPreferenceListener = new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    int index = PREFNAMES.indexOf(preference.getKey());
                    DataType data = DATATYPES.get(index);

                    SwitchPreference pref = (SwitchPreference) preference;
                    //connectGoogleFit(); //TODO: Should be able to remove but keeping for now
                    if (!MainActivity.mGoogleApiClient.isConnected()){
                        MainActivity.mGoogleApiClient.connect();
                    }

                    if (pref.isChecked()) {
                        subscribeToDataType(data);
                    } else {
                        unsubscribeToDataType(data);
                    }
                    Toast.makeText(getActivity(), "Changed permissions for GoogleFit ", Toast.LENGTH_SHORT).show();
                    return true;
                }
            };
        }

        /**
         * Called by the child listener if the user decides to disable the preference
         * This method unsubscribes the Client API to the datatype with the RecordingAPI
         * @param data : The datatype that the user wants to unsubscribe from
         */
        private void unsubscribeToDataType(DataType data) {
            Fitness.RecordingApi.unsubscribe(MainActivity.mGoogleApiClient, data)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                Log.i(TAG2, "Successfully unsubscribed for data type");
                            } else {
                                // Subscription not removed
                                Log.i(TAG2, "Failed to unsubscribe for data type");
                            }
                        }
                    });
        }

        /**
         * Called by the child listener if the user decides to enable the preference
         * This method subscribes the Client API to the datatype with the RecordingAPI
         * @param data : This is the data type that wants the subscription
         */
        private void subscribeToDataType(DataType data) {
            Log.d(TAG2, "Subscribing " + data);
            Fitness.RecordingApi.unsubscribe(MainActivity.mGoogleApiClient, data)
                    .setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                if (status.getStatusCode() == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                    Log.d(TAG2, "Existing subscription for activity detected.");
                                } else {
                                    Log.d(TAG2, "Successfully subscribed!");
                                }
                            } else {
                                Log.d(TAG2, "There was a problem subscribing. " + status);
                            }
                        }
                    });
        }


        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }


    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class FitnessPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_fitness);
            setHasOptionsMenu(true);

            //bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Initialised variables to be used by Facebook - Social Media App
     */
    private static LoginButton facebookLogin;
    private static CallbackManager callbackManager;
    private static AccessTokenTracker accessTokenTracker;
    public static AccessToken accessToken;
    private static ProfileTracker profileTracker;
    private static final String TAG = "Facebook"; // log Tag
    private static final List<String> PERMISSIONS = Arrays.asList("email","user_posts", "user_likes", "user_events", "user_actions.fitness", "public_profile", "user_friends");
    private static final List<String> PREFERENCES = Arrays.asList("user_posts", "user_likes", "user_events", "user_actions.fitness", "user_friends");
    public static List<String> grantedFBPermissions = new LinkedList<>();
    public static List<String> deniedFBPermissions  = new LinkedList<>();

    // GET CURRENT PERMISSIONS: AccessToken.getCurrentAccessToken().getPermissions();
    public static class SocialMediaPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_social_media);
            setHasOptionsMenu(true);

            isFacebookOrTwitter = 0; // so onActivityResult does the correct code

            // added a callback manager for fb to use on login
            callbackManager = CallbackManager.Factory.create();

            // creates the fb login button that is used, but doesn't actually put it on the screen. This is invoked when the switch preferences are
            facebookLogin = new LoginButton(getActivity());
            // set permissions according to: email, status, posts, likes, events, fitness, profile and friends
            facebookLogin.setReadPermissions(PERMISSIONS);
            // code called on success or failure
            facebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    // The code below enables/ disables switch preference according to whether they're
                    grantedFBPermissions = new LinkedList<>(loginResult.getRecentlyGrantedPermissions());
                    deniedFBPermissions = new LinkedList<>(loginResult.getRecentlyDeniedPermissions());
                    updatePermissionSwitchPreferences();
                    Toast.makeText(getActivity(), "Changed permissions for Facebook",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancel() {
                    // TODO: Somehow get the SwitchPreference and change it back to what it was before
                    Log.d(TAG, "Login Button Cancel");
                }

                @Override
                public void onError(FacebookException exception) {
                    // TODO: Somehow get the SwitchPreference and change it back to what it was before
                    Log.d(TAG, "Login Button Error");
                }
            });

            // this code implements a fb login button click method for each time the parent switch preference is clicked
            Preference pref = getPreferenceManager().findPreference("social_media_all");
                pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                            @Override
                            public boolean onPreferenceClick(Preference preference) {
                                facebookLogin.setReadPermissions(PERMISSIONS);
                                facebookLogin.performClick();
                                return true;
                            }
                        });

            // this loops through all the permissions that have switch preferences in settings, adding click listeners to each one
            for (String i : PREFERENCES){
                Preference permission = getPreferenceManager().findPreference(i);
                permission.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Log.d(TAG, "Preference clicker for preference " + preference.getKey());
                        SwitchPreference switchPreference = (SwitchPreference) preference; // gets the preference

                        if (!switchPreference.isChecked()) { // if it's changed to not checked, the permission must be revoked
                            // Make callback function sent in graph request
                            GraphRequest.Callback callback = new GraphRequest.Callback() {
                                @Override
                                public void onCompleted(GraphResponse response) {
                                    Log.d(TAG, "Successful completion of asynch call ");
                                    if (response.getError()== null && response.getJSONObject() != null){ // checks not cancelled or an error
                                        accessToken = AccessToken.getCurrentAccessToken();
                                    } else { //TODO: Handle Errors & Test
                                        Log.d(TAG, "response request " + response.getRequest());
                                    }
                                }
                            };
                            GraphRequest req = new GraphRequest(
                                    accessToken,
                                    "/me/permissions/" + preference.getKey(),
                                    null,
                                    HttpMethod.DELETE,
                                    callback
                            );
                            req.executeAsync();
                            Log.d(TAG, "Access Token Permissions access_token is = " + accessToken.getPermissions());
                            Log.d(TAG, "Access Token Permissions System  is = " + AccessToken.getCurrentAccessToken().getPermissions());
                            // removes from denied & adds to granted - Could change these to be calls to AccessTokenTracker but idk how
                            deniedFBPermissions.add(preference.getKey());
                            grantedFBPermissions.remove(preference.getKey());
                            Log.d(TAG, "List Permissions are = " + grantedFBPermissions.toString());
                            Toast.makeText(getActivity(), "Changed permission " + switchPreference.getKey() + " for Facebook", Toast.LENGTH_SHORT).show();
                        } else { // asks for the permission when it's enabled again
                            LoginManager.getInstance().logInWithReadPermissions(
                                    getActivity(),
                                    Collections.singletonList(preference.getKey()));
                            Log.d(TAG, "List Permissions are = " + grantedFBPermissions.toString());
                            Toast.makeText(getActivity(), "Changed permission " + preference.getKey() + " for Facebook", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                });
            }

            // tracks the token and updates when it is on login/ logout
            accessTokenTracker = new AccessTokenTracker() {
                @Override
                protected void onCurrentAccessTokenChanged(
                        AccessToken oldAccessToken,
                        AccessToken currentAccessToken) {
                    AccessToken.setCurrentAccessToken(currentAccessToken);
                    accessToken = currentAccessToken; // sets the access token variable to the current/ new one
                    if (currentAccessToken == null){
                        grantedFBPermissions = null; // sets permissions to new permissions
                        deniedFBPermissions = null;
                    } else {
                        grantedFBPermissions = new LinkedList<>(accessToken.getPermissions()); // sets permissions to new permissions
                        deniedFBPermissions = new LinkedList<>(accessToken.getPermissions());
                    }
                }
            };

            // tracks the profile logged in
            profileTracker = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                    Profile.setCurrentProfile(currentProfile);
                }
            };

            // update permissions depending on permissions from accessToken
            if (accessToken == null) {
                accessToken = AccessToken.getCurrentAccessToken(); // If the access token is available already assign it
                if (accessToken != null){
                    grantedFBPermissions = new LinkedList<>(accessToken.getPermissions());
                    deniedFBPermissions = new LinkedList<>(accessToken.getDeclinedPermissions());
                    updatePermissionSwitchPreferences();
                }
            }
        }

        // removest the fb access token and profile token tracker when the activity is destroyed
        @Override
        public void onDestroy() {
            super.onDestroy();
            accessTokenTracker.stopTracking();
            profileTracker.stopTracking();
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        // updates switch preferences permissions according to granted/ denied permissions
        public void updatePermissionSwitchPreferences(){
            for (Object i : grantedFBPermissions){ // loops through all the granted permissions
                Log.d(TAG, "granted permission " + i.toString()); //TESTING
                SwitchPreference granted_preference = (SwitchPreference) getPreferenceManager().findPreference(i.toString());
                if (granted_preference != null){
                    Log.d(TAG, "granted preference = "+granted_preference.toString()); //TESTING
                    granted_preference.setChecked(true); // enables the switch preference
                } else { // ERROR: permission granted doesn't exist as a switch preference
                    Log.d(TAG, "ERROR: Null granted permission " + i.toString());
                }
            }
            for (Object i : deniedFBPermissions){ // loops through all the denied permissions, disabling them accordingly
                Log.d(TAG, "denied permission " + i.toString());
                SwitchPreference denied_preference = (SwitchPreference) getPreferenceManager().findPreference(i.toString());
                if (denied_preference != null){
                    Log.d(TAG, "denied_preference = "+denied_preference.toString());
                    denied_preference.setChecked(false);
                } else { // ERROR: permission denied doesn't exist as a switch preference
                    Log.d(TAG, "ERROR: Null denied permission " + i.toString());
                }
            }
        }
    }

    //TWITTER VARIABLES
    private static List<String> TWITTERPREFERENCES = Arrays.asList("favourites", "statuses"); // the names of the child switch preferences
    protected static TwitterLoginButton twitterLogin; // the component that isn't visible but is used to perform clicks
    private static TwitterSession session; // the twitter session variable
    private static final String TAG3 = "Twitter"; // for logging

    public static class SocialMedia2PreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_social_media_2);
            setHasOptionsMenu(true);

            isFacebookOrTwitter = 1; // used by onActivityResult

            createTwitterLoginButton(); // this creates the login button component to perform clicks on when the parent switch preference is changed
            createParentPreference(); // this calls the login/ logout methods initalised ^

        }

        /**
         * This creates the parent switch preference on click listener
         * This calls the appropriate login or logout methods accordingly
         */
        private void createParentPreference(){
            Preference pref = getPreferenceManager().findPreference("social_media_2_all");
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SwitchPreference switchpref = (SwitchPreference) preference;
                    if (switchpref.isChecked()){
                        if (session == null){
                            twitterLogin.performClick();
                        }
                    } else {
                        session = null;
                        TwitterCore.getInstance().getSessionManager().clearActiveSession();
                    }

                    return true;
                }
            });
        }

        /**
         * This adds the onclick listeners to each child switch preference
         * TODO: REFLECT THE ENABLED/DISABLED SWITCHES IN THE MAIN SUMMARY PAGE SOMEHOW
         */
        private void createChildPreferences(){
            // this loops through all the permissions that have switch preferences in settings, adding click listeners to each one
            for (String i : TWITTERPREFERENCES){
                Preference permission = getPreferenceManager().findPreference(i);
                permission.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Log.d(TAG, "Preference clicker for preference " + preference.getKey());
                        SwitchPreference switchPreference = (SwitchPreference) preference; // gets the preference

                        if (!switchPreference.isChecked()) { // if it's changed to not checked, the permission must be revoked
                            // Make callback function sent in graph request
                            Toast.makeText(getActivity(), "Changed permission " + switchPreference.getKey() + " for Twitter", Toast.LENGTH_SHORT).show();
                        } else { // asks for the permission when it's enabled again
                            Toast.makeText(getActivity(), "Changed permission " + switchPreference.getKey() + " for Twitter", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                });
            }
        }

        /**
         * This creates the twitter button, that calls login and authorisation
         */
        private void createTwitterLoginButton(){
            twitterLogin = new TwitterLoginButton(getActivity());
            twitterLogin.setCallback(new Callback<TwitterSession>() {
                @Override
                public void success(Result<TwitterSession> result) {
                    // The result provides a TwitterSession for making API calls
                    Log.d(TAG3, "Successfull callback from Twitter");
                    session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                    Toast.makeText(getActivity(), "Changed permissions for Twitter",Toast.LENGTH_LONG).show();
                }

                @Override
                public void failure(TwitterException exception) {
                    Toast.makeText(getActivity(), "Twitter Login Failed",Toast.LENGTH_SHORT).show();
                    Log.d(TAG3, "Failed callback from Twitter");
                    Log.d(TAG3, "Check you have the Twitter app actually downloaded!"); //TODO Scan for
                    // TODO: CHANGE SWITCH PREFERENCE BACK, this code doesn't work for some reason. Might need to remove listener and then add it again
//                    SwitchPreference switchPref = (SwitchPreference) getPreferenceManager().findPreference("social_media_2_all");
//                    switchPref.setChecked(false);
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    private static int isFacebookOrTwitter = -1; // changes the code depending on whether the fb fragment or twitter one is open

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result to the login button.
        if (isFacebookOrTwitter == 0){ // Facebook
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
        if (isFacebookOrTwitter == 1){ // Twitter
            twitterLogin.onActivityResult(requestCode, resultCode, data);
        }
    }
}
