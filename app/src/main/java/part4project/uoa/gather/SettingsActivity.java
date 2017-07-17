package part4project.uoa.gather;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageInstaller.Session;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.view.ViewGroup;
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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
    private static LoginButton loginButton;
    private static CallbackManager callbackManager;
    private static AccessTokenTracker accessTokenTracker;
    private static AccessToken accessToken;
    private static ProfileTracker profileTracker;
    private static Profile profile;
    private static final String TAG = "Facebook"; // log Tag
    private static final List<String> PERMISSIONS = Arrays.asList("email","user_posts", "user_likes", "user_events", "user_actions.fitness", "public_profile", "user_friends");


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
                    Set grantedPermissions = loginResult.getRecentlyGrantedPermissions();
                    Set deniedPermissions = loginResult.getRecentlyDeniedPermissions();
                    updatePermissionSwitchPreferences(grantedPermissions, deniedPermissions);
                }

                @Override
                public void onCancel() {
                    // TODO: App code
                }

                @Override
                public void onError(FacebookException exception) {
                    // TODO: App code
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

            for (String i : PERMISSIONS){
                Preference permission = getPreferenceManager().findPreference(i);
                permission.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        Log.d(TAG, "Preference clicker for preference " + preference.getKey());
                        SwitchPreference switchPreference = (SwitchPreference) preference; // gets the preference
                        if (!switchPreference.isChecked()){ // if it's changed to not checked, the permission must be revoked
                            GraphRequest req = new GraphRequest(
                                    accessToken,
                                    "/me/permissions/" + preference.getKey(),
                                    null,
                                    HttpMethod.DELETE, new GraphRequest.Callback() {
                                @Override
                                public void onCompleted(GraphResponse response) {
                                    Log.d(TAG,"Successful completion of asynch call");
                                }
                            });
                            req.executeAsync();
                        } else { // asks for the permission when it's enabled again
                            LoginManager.getInstance().logInWithReadPermissions(
                                    getActivity(),
                                    Arrays.asList(preference.getKey()));
                        }
                        Toast.makeText(getActivity(), "Changed permission " + preference.getKey() + " for Facebook", Toast.LENGTH_LONG).show();
                        return true;
                    }
                });
            }

            // tracks the token representing who is logged in
            accessTokenTracker = new AccessTokenTracker() {
                @Override
                protected void onCurrentAccessTokenChanged(
                        AccessToken oldAccessToken,
                        AccessToken currentAccessToken) {
                    Log.d(TAG, "new access token"); // TESTING
                    accessToken = currentAccessToken; // sets the access token variable to the current/ new one
                }
            };

            // tracks the profile logged in
            profileTracker = new ProfileTracker() {
                @Override
                protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                    profile = currentProfile;
                }
            };

            // If the access token is available already assign it
            accessToken = AccessToken.getCurrentAccessToken();
            profile = Profile.getCurrentProfile();

            // update permissions depending on permissions from accessToken
            if (accessToken != null) {
                updatePermissionSwitchPreferences(accessToken.getPermissions(), accessToken.getDeclinedPermissions());
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
        public void updatePermissionSwitchPreferences(Set grantedPermissions, Set deniedPermissions){
            for (Object i : grantedPermissions){ // loops through all the granted permissions
                Log.d(TAG, "granted permission " + i.toString()); //TESTING
                SwitchPreference granted_preference = (SwitchPreference) getPreferenceManager().findPreference(i.toString());
                if (granted_preference != null){
                    Log.d(TAG, "granted preference = "+granted_preference.toString()); //TESTING
                    granted_preference.setChecked(true); // enables the switch preference
                } else { // ERROR: permission granted doesn't exist as a switch preference
                    Log.d(TAG, "ERROR: Null granted permission " + i.toString());
                }
            }
            for (Object i : deniedPermissions){ // loops through all the denied permissions, disabling them accordingly
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

    // this implements changing the tokens accordingly, allowing for login & logout
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
