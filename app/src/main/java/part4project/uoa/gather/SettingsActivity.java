package part4project.uoa.gather;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
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

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;

import javax.net.ssl.HttpsURLConnection;

import static part4project.uoa.gather.R.id.social_media_all;

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

    static Uri data;
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
        data = getIntent().getData();
        Log.i("App uri", String.valueOf(data));

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
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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
                ;
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class FoodPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_food);
            setHasOptionsMenu(true);

            //bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
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
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class FitnessPreferenceFragment extends PreferenceFragment {

        private String fitbitToken = "";
        private Long expires;
        List<String> scopes;
        Intent browserIntent;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_fitness);
            setHasOptionsMenu(true);

            Preference pref = getPreferenceManager().findPreference("fitness_all");
            pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference){
                    SwitchPreference switchPreference = (SwitchPreference) preference;
                    if (switchPreference.isChecked()){

                    } else {
                        revokePreferences();
                    }
                    return true;
                }
            });

            //bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }

        public void getAuthToken(){
            try {
                String fitbitAuthLink = "https://www.fitbit.com/oauth2/authorize?response_type=token&client_id=228KQW&redirect_uri=gather%3A%2F%2Ffitbit&scope=activity%20heartrate%20nutrition%20sleep%20weight&expires_in=604800";

                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fitbitAuthLink));
                startActivity(browserIntent);

            } catch (Exception e) {
                Log.e("Fitbit", "An error occurred when dealing with the auth url: " + e);
            }
        }

        public void revokePreferences(){

        }

        public void parseAccessToken(){
            //Save Auth Token
            Uri returnUri = browserIntent.getData();
            fitbitToken = returnUri.getQueryParameter("access_token");
            expires = Long.parseLong(returnUri.getQueryParameter("expires_in")) + System.currentTimeMillis() / 1000;
            //scopes = parseScopes(uri.getQueryParameter("scope"));
            Log.d("Fitbit", "The token is: " + fitbitToken);
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
    private static LoginButton loginButton;
    private static CallbackManager callbackManager;
    private static AccessTokenTracker accessTokenTracker;
    public static AccessToken accessToken;
    private static ProfileTracker profileTracker;
    private static Profile profile;
    private static final String TAG = "Facebook"; // log Tag
    private static final List<String> PERMISSIONS = Arrays.asList("email","user_posts", "user_likes", "user_events", "user_actions.fitness", "public_profile", "user_friends");
    private static final List<String> PREFERENCES = Arrays.asList("user_posts", "user_likes", "user_events", "user_actions.fitness", "user_friends");
    public static List<String> grantedFBPermissions = new LinkedList<String>();
    public static List<String> deniedFBPermissions  = new LinkedList<String>();

    // GET CURRENT PERMISSIONS: AccessToken.getCurrentAccessToken().getPermissions();
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SocialMediaPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_social_media);
            setHasOptionsMenu(true);

            // added a callback manager for fb to use on login
            callbackManager = CallbackManager.Factory.create();

            // creates the fb login button that is used, but doesn't actually put it on the screen. This is invoked when the switch preferences are
            loginButton = new LoginButton(getActivity());
            // set permissions according to: email, status, posts, likes, events, fitness, profile and friends
            loginButton.setReadPermissions(PERMISSIONS);
            // code called on success or failure
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    // The code below enables/ disables switch preference according to whether they're
                    grantedFBPermissions = new LinkedList<String>(loginResult.getRecentlyGrantedPermissions());
                    deniedFBPermissions = new LinkedList<String>(loginResult.getRecentlyDeniedPermissions());
                    updatePermissionSwitchPreferences();
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
                                loginButton.setReadPermissions(PERMISSIONS);
                                loginButton.performClick();
                                Toast.makeText(getActivity(), "Changed permissions for Facebook",Toast.LENGTH_LONG).show();
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
                            deniedFBPermissions.add(preference.getKey().toString());
                            grantedFBPermissions.remove(preference.getKey().toString());
                            Log.d(TAG, "List Permissions are = " + grantedFBPermissions.toString());
                            Toast.makeText(getActivity(), "Changed permission " + switchPreference.getKey() + " for Facebook", Toast.LENGTH_LONG).show();
                        } else { // asks for the permission when it's enabled again
                            LoginManager.getInstance().logInWithReadPermissions(
                                    getActivity(),
                                    Arrays.asList(preference.getKey()));
                            Log.d(TAG, "List Permissions are = " + grantedFBPermissions.toString());
                            Toast.makeText(getActivity(), "Changed permission " + preference.getKey() + " for Facebook", Toast.LENGTH_LONG).show();
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
                    profile = currentProfile;
                }
            };

            // update permissions depending on permissions from accessToken
            if (accessToken == null) {
                accessToken = AccessToken.getCurrentAccessToken(); // If the access token is available already assign it
                profile = Profile.getCurrentProfile();
                if (accessToken != null){
                    grantedFBPermissions = new LinkedList<String>(accessToken.getPermissions());
                    deniedFBPermissions = new LinkedList<String>(accessToken.getDeclinedPermissions());
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
        // this implements changing the tokens accordingly, allowing for login & logout
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }


}
