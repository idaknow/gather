package part4project.uoa.gather;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
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
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.fitness.result.ListSubscriptionsResult;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import javax.net.ssl.HttpsURLConnection;

import static part4project.uoa.gather.MainActivity.mainPreferences;

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
 *
 *  This class was developed guided by the apps documentation:
 *  - Google Fit: https://github.com/googlesamples/android-fit
 *  - Twitter: https://dev.twitter.com/twitterkit/android/overview
 *  - Facebook: https://developers.facebook.com/docs/android
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    static Uri data;

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        data = getIntent().getData();
        if (data != null) {
            String host = data.getHost();
            //Checks if returning from fitbit authentication. If so, get the access token.
            if (host.equals("fitbit")){
                String resultFragment = String.valueOf(data.getFragment());
                FitbitPreferenceFragment.setToken(resultFragment);
                browserResponseFragment = resultFragment;
                Toast.makeText(this, "Changed permissions for Fitbit ", Toast.LENGTH_LONG).show();
            }
        }

        mainPreferences.edit().putString("secret", "MjI4S1FXOjA0NDI4MDg0OGUzZGVmZTZiZGQyZGRmMzM3NDA2ODY3").apply();
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
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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
        if (MainActivity.twitterInstalled){
            loadHeadersFromResource(R.xml.pref_headers, target);
        } else {
            loadHeadersFromResource(R.xml.pref_no_twitter, target);
        }
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
            return PreferenceFragment.class.getName().equals(fragmentName)
                    || FitbitPreferenceFragment.class.getName().equals(fragmentName)
                    || GoogleFitPreferenceFragment.class.getName().equals(fragmentName)
                    || FacebookPreferenceFragment.class.getName().equals(fragmentName)
                    || TwitterPreferenceFragment.class.getName().equals(fragmentName)
                    || BlankTwitterPreferenceFragment.class.getName().equals(fragmentName)
                    ;
    }

    //GOOGLEFIT Initialised variables
    private static List<DataType> DATATYPES = Arrays.asList(DataType.AGGREGATE_CALORIES_EXPENDED, DataType.AGGREGATE_HYDRATION, DataType.AGGREGATE_NUTRITION_SUMMARY, DataType.AGGREGATE_ACTIVITY_SUMMARY, DataType.AGGREGATE_STEP_COUNT_DELTA);
    private static List<String> PREFNAMES = Arrays.asList("calories", "hydration", "nutrition", "activity", "step");
    private static Preference.OnPreferenceClickListener eachPreferenceListener;
    private static Preference.OnPreferenceClickListener googleFitParentListener;

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class GoogleFitPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_google_fit);
            setHasOptionsMenu(true);
            getActivity().setTheme(R.style.MySwitch);

            MainActivity.mGoogleApiClient.connect();
            fixChildPreferences(); // set child preferences according to the API Client's subscriptions

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
        }

        /**
         * This method gets all the subscriptions and changes the switch preferences accordingly to them
         */
        private void fixChildPreferences(){
                PendingResult<ListSubscriptionsResult> result = Fitness.RecordingApi.listSubscriptions(MainActivity.mGoogleApiClient);
                result.setResultCallback(new ResultCallback<ListSubscriptionsResult>() {
                    @Override
                    public void onResult(@NonNull ListSubscriptionsResult listSubscriptionsResult) {
                        List<Subscription> subscriptionList = listSubscriptionsResult.getSubscriptions();

                        for (int i = 0; i < DATATYPES.size(); i++) { // loop through all the datatypes
                            String prefname = PREFNAMES.get(i);
                            SwitchPreference pref = (SwitchPreference) getPreferenceManager().findPreference(prefname);

                            boolean subscriptionContains = false;
                            for (Subscription sc : subscriptionList){ // loops through all subscriptions
                                if (sc.getDataType().getName().equals(DATATYPES.get(i).getName())){
                                    subscriptionContains = true; // if subscribed to a datatype with a preference
                                }
                            }

                            if (subscriptionContains){
                                pref.setChecked(true);
                            } else {
                                pref.setChecked(false);
                            }
                        }
                    }
                });
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
         * This connects the API Client to the Google Play Services
         */
        private void connectGoogleFit() {
            MainActivity.mGoogleApiClient.reconnect();
            Toast.makeText(getActivity(), "Enabled permissions for GoogleFit ", Toast.LENGTH_LONG).show();
        }

        /**
         * Methods called by the parent listener
         * This disconnects the API Client to the Google Play Services
         */
        private void disconnectGoogleFit() {
            if (!MainActivity.mGoogleApiClient.isConnected()){
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
                    }
                }
            });
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
            Fitness.RecordingApi.unsubscribe(MainActivity.mGoogleApiClient, data);
        }

        /**
         * Called by the child listener if the user decides to enable the preference
         * This method subscribes the Client API to the datatype with the RecordingAPI
         * @param data : This is the data type that wants the subscription
         */
        private void subscribeToDataType(DataType data) {
            Fitness.RecordingApi.subscribe(MainActivity.mGoogleApiClient, data);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    //Fitbit variables
    private static List<String> PREFS = Arrays.asList("fitbit_activity", "fitbit_nutrition","fitbit_heartrate"); //Names of the preferences
    private static List<String> SCOPES = Arrays.asList("activity", "nutrition", "heartrate"); //Scopes that can be requested from Fitbit
    private static Preference.OnPreferenceClickListener fitbitPreferenceListener;
    private static Preference.OnPreferenceClickListener fitbitParentListener;
    public static Intent browserIntent; //Intent used to open the authentication page
    public static String fitbitAuthLink = "https://www.fitbit.com/oauth2/authorize?response_type=token&client_id=228KQW&redirect_uri=gather%3A%2F%2Ffitbit&scope=activity%20heartrate%20nutrition&expires_in=31536000";
    public static ArrayList<String> grantedfitbitPermissions = new ArrayList<>(); //Array containing the permissions the user has granted
    public static String browserResponseFragment; //String response from the browser authentication intent

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    public static class FitbitPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_fitbit);
            setHasOptionsMenu(true);
            getActivity().setTheme(R.style.MySwitch);
            PreferenceManager prefManager = getPreferenceManager();
            //If the fragment is being opened for the first time after the user has authenticated then
            //set the correct scopes and their appropriate switch preferences
            if (browserResponseFragment != null){
                setGrantedScopes(prefManager);
            }

            // Adds the Preference Listeners to the parent and children preferences accordingly
            if (fitbitParentListener == null) {
                createParentListener();
                Preference pref = getPreferenceManager().findPreference("fitbit_all");
                pref.setOnPreferenceClickListener(fitbitParentListener);
            }
            if (fitbitPreferenceListener == null) {
                createChildListener();
                for (int i = 0; i < PREFS.size(); i++) {
                    Preference pref2 = getPreferenceManager().findPreference(PREFS.get(i));
                    pref2.setOnPreferenceClickListener(fitbitPreferenceListener);
                }
            }
        }

        /**
         * Creates the Parent Listener to be added as an on Click Listener
         * This will authorise or unauthorise the app from accessing the users information.
         * It is the master switch, so when set to false, all permissions are set to false, when set
         * to true, it will attempt to get all scopes granted.
         */
        private void createParentListener() {
            fitbitParentListener = new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    SwitchPreference pref = (SwitchPreference) preference;
                    if (pref.isChecked()) {
                        requestToken();
                    } else {
                        grantedfitbitPermissions.clear();
                        for (int i = 0; i < PREFS.size(); i++) {
                            SwitchPreference pref2 = (SwitchPreference) getPreferenceManager().findPreference(PREFS.get(i));
                            pref2.setChecked(false);
                        }
                        Toast.makeText(getActivity(), "Permission disabled for access to Fitbit", Toast.LENGTH_LONG).show();
                        revokeToken();
                    }
                    return true;
                }
            };
        }

        /*
        The child listener listens for any of the fine-grained preference switches,
        and removes or adds the corresponding permission.
         */
        private void createChildListener() {
            fitbitPreferenceListener = new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    int index = PREFS.indexOf(preference.getKey());
                    String scope = SCOPES.get(index);

                    SwitchPreference pref = (SwitchPreference) preference;
                    if (pref.isChecked()) {
                        requestToken();
                    } else {
                        grantedfitbitPermissions.remove(scope);
                        Toast.makeText(getActivity(), scope + " permission disabled for Fitbit", Toast.LENGTH_LONG).show();
                    }
                    return true;
                }
            };
        }

        /*
        Method used to open a browser with the authentication url, so that the user can give permission
        to the app to access their information.
         */
        public void requestToken(){
            browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fitbitAuthLink));
            startActivity(browserIntent);
        }

        /*
        This method is passed the uri fragment which is returned to the app by fitbit, and extracts
        the access token from it. It then assigns this value to the fitbitToken string.
         */
        private static void setToken(String uriFragment){
            String temp = uriFragment.split("&")[0];
            mainPreferences.edit().putString("access_token", temp.substring(13)).apply();
        }

        /*
        This method clears the token so that the app can no longer access the Web API.
         */
        private static void revokeToken(){

            try {
                // Send post request to revoke the user access token to Fitbit.
                String revoke = "https://api.fitbit.com/oauth2/revoke";
                URL revokeUrl = new URL(revoke);
                HttpsURLConnection revokeCon = (HttpsURLConnection) revokeUrl.openConnection();
                revokeCon.setRequestMethod("POST");
                revokeCon.setRequestProperty  ("Authorization", "Basic " + mainPreferences.getString("secret", null));
                revokeCon.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(revokeCon.getOutputStream());
                wr.flush();
                wr.close();
                revokeCon.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Remove from MainActivity's SharedPreferences
            mainPreferences.edit().remove("access_token").apply();

        }

        /*
        Adds the granted scopes to an arraylist to keep track of and sets the preferences to true
        or false accordingly.
         */
        private static void setGrantedScopes(PreferenceManager prefManager) {
            try {
                String temp = browserResponseFragment.split("&")[2];
                String scopeFragment = temp.substring(6);
                String[] scopes = scopeFragment.split("\\+");
                grantedfitbitPermissions.clear();
                if (scopes.length != 0) {
                    for (String scope : scopes) {
                        grantedfitbitPermissions.add(scope);
                        String prefName = "fitbit_" + scope;
                        //Set the appropriate preferences to enabled
                        SwitchPreference switchPreference = (SwitchPreference) prefManager.findPreference(prefName);
                        switchPreference.setChecked(true);
                    }
                }
                browserResponseFragment = null;
            } catch (PatternSyntaxException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Initialised variables to be used by Facebook - SocialMethods Media App
     */
    private static LoginButton facebookLogin;
    private static CallbackManager callbackManager;
    private static AccessTokenTracker accessTokenTracker;
    public static AccessToken accessToken;
    private static ProfileTracker profileTracker;
    private static final List<String> PERMISSIONS = Arrays.asList("user_posts", "user_likes", "user_events", "user_actions.fitness", "public_profile");
    private static final List<String> PREFERENCES = Arrays.asList("user_posts", "user_likes", "user_events", "user_actions.fitness");
    public static List<String> grantedFBPermissions = new LinkedList<>();
    public static List<String> deniedFBPermissions  = new LinkedList<>();
    private static String changing_switch = null;

    public static class FacebookPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_facebook);
            setHasOptionsMenu(true);
            getActivity().setTheme(R.style.MySwitch);

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
                    if (changing_switch == null){
                        changing_switch = "social_media_all";
                    }
                    SwitchPreference overall_pref = (SwitchPreference) getPreferenceManager().findPreference(changing_switch);
                    overall_pref.setChecked(!overall_pref.isChecked());
                    changing_switch = null;
                }

                @Override
                public void onError(FacebookException exception) {
                    if (changing_switch == null){
                        changing_switch = "social_media_all";
                    }
                    SwitchPreference overall_pref = (SwitchPreference) getPreferenceManager().findPreference(changing_switch);
                    overall_pref.setChecked(!overall_pref.isChecked());
                    changing_switch = null;
                }
            });

            // this code implements a fb login button click method for each time the parent switch preference is clicked
            Preference pref = getPreferenceManager().findPreference("social_media_all");
                pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                            @Override
                            public boolean onPreferenceClick(Preference preference) {
                                changing_switch = preference.getKey();
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
                        changing_switch = preference.getKey();
                        SwitchPreference switchPreference = (SwitchPreference) preference; // gets the preference

                        if (!switchPreference.isChecked()) { // if it's changed to not checked, the permission must be revoked
                            // Make callback function sent in graph request
                            GraphRequest.Callback callback = new GraphRequest.Callback() {
                                @Override
                                public void onCompleted(GraphResponse response) {
                                    if (response.getError()== null && response.getJSONObject() != null){ // checks not cancelled or an error
                                        accessToken = AccessToken.getCurrentAccessToken();
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
                            // removes from denied & adds to granted - Could change these to be calls to AccessTokenTracker but idk how
                            deniedFBPermissions.add(preference.getKey());
                            grantedFBPermissions.remove(preference.getKey());
                            Toast.makeText(getActivity(), "Changed permission " + switchPreference.getKey() + " for Facebook", Toast.LENGTH_SHORT).show();
                        } else { // asks for the permission when it's enabled again
                            LoginManager.getInstance().logInWithReadPermissions(
                                    getActivity(),
                                    Collections.singletonList(preference.getKey()));
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

        // removes the fb access token and profile token tracker when the activity is destroyed
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
                getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        // updates switch preferences permissions according to granted/ denied permissions
        public void updatePermissionSwitchPreferences(){
            SwitchPreference overall_pref = (SwitchPreference) getPreferenceManager().findPreference("social_media_all");
            overall_pref.setChecked(true);
            for (Object i : grantedFBPermissions){ // loops through all the granted permissions
                SwitchPreference granted_preference = (SwitchPreference) getPreferenceManager().findPreference(i.toString());
                if (granted_preference != null){
                    granted_preference.setChecked(true); // enables the switch preference
                }
            }
            for (Object i : deniedFBPermissions){ // loops through all the denied permissions, disabling them accordingly
                SwitchPreference denied_preference = (SwitchPreference) getPreferenceManager().findPreference(i.toString());
                if (denied_preference != null){
                    denied_preference.setChecked(false);
                }
            }
        }
        // this implements changing the tokens accordingly, allowing for login & logout
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    //TWITTER VARIABLES
    public static List<String> TWITTERPREFERENCES = Arrays.asList("favourites", "statuses"); // the names of the child switch preferences
    protected static TwitterLoginButton twitterLogin; // the component that isn't visible but is used to perform clicks
    private static TwitterSession session; // the twitter session variable

    public static class TwitterPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            if (MainActivity.twitterInstalled) {
                super.onCreate(savedInstanceState);
                addPreferencesFromResource(R.xml.pref_twitter);
                setHasOptionsMenu(true);
                getActivity().setTheme(R.style.MySwitch);

                isFacebookOrTwitter = 1; // used by onActivityResult

                createTwitterLoginButton(); // this creates the login button component to perform clicks on when the parent switch preference is changed
                createParentPreference(); // this calls the login/ logout methods initalised ^
                createChildPreferences();
            }
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

                    // open shared preference editor to set the children switches to the same as the parent switch
                    for (String i : TWITTERPREFERENCES){ // loop through the child preferences
                        setVariable(i, switchpref.isChecked()); // set the variable in child preferences
                        SwitchPreference childSwitch = (SwitchPreference) getPreferenceManager().findPreference(i); // get the child switch pref
                        childSwitch.setChecked(switchpref.isChecked()); // sets the child preference to the same boolean as the parent
                    }

                    if (switchpref.isChecked()){ // login
                        if (session == null){ // if previously logged out - error checking
                            twitterLogin.performClick();
                        }
                    } else { // logout
                        session = null;
                        TwitterCore.getInstance().getSessionManager().clearActiveSession();
                    }

                    return true;
                }
            });
        }

        /**
         * This adds the onclick listeners to each child switch preference
         * Changes the variable boolean in shared preferences
         */
        private void createChildPreferences(){
            // this loops through all the permissions that have switch preferences in settings, adding click listeners to each one
            for (String i : TWITTERPREFERENCES){
                Preference permission = getPreferenceManager().findPreference(i);
                permission.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        SwitchPreference switchPreference = (SwitchPreference) preference; // gets the preference

                        setVariable(preference.getKey(),switchPreference.isChecked());

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
         * This class sets the shared preference variable specified as input to the specified boolean
         * @param preference : The variable namee
         * @param status : The new status to set the variable as
         */
        private void setVariable(String preference, boolean status){
            SharedPreferences prefs = getActivity().getSharedPreferences("MainPreferences", Context.MODE_PRIVATE); // get the shared preferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(preference, status); // set the boolean of the variable to the switch pref's value
            editor.apply();
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
                    session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                    Toast.makeText(getActivity(), "Changed permissions for Twitter",Toast.LENGTH_LONG).show();
                }

                @Override
                public void failure(TwitterException exception) {
                    Toast.makeText(getActivity(), "Twitter Login Failed",Toast.LENGTH_SHORT).show();
                    SwitchPreference switchPref = (SwitchPreference) getPreferenceManager().findPreference("social_media_2_all");
                    switchPref.setChecked(false);
                    for (String i : TWITTERPREFERENCES){ // loop through the child preferences
                        setVariable(i, false); // set the variable in child preferences
                        SwitchPreference childSwitch = (SwitchPreference) getPreferenceManager().findPreference(i); // get the child switch pref
                        childSwitch.setChecked(false); // sets the child preference to the same boolean as the parent
                    }

                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    public static class BlankTwitterPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_twitter);
            setHasOptionsMenu(true);
            getActivity().setTheme(R.style.MySwitch);
            Toast toast = Toast.makeText(getActivity(), "You must have Twitter installed on your device for Gather to collect information.", Toast.LENGTH_LONG);
            toast.show();
            createParentPreference();
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
                    Toast toast = Toast.makeText(getActivity(), "You must have Twitter installed on your device for Gather to collect information.", Toast.LENGTH_LONG);
                    toast.show();
                    switchpref.setEnabled(false);
                    return true;
                }
            });
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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