package part4project.uoa.gather;

import android.Manifest.permission;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.ListSubscriptionsResult;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.FavoriteService;
import com.twitter.sdk.android.core.services.StatusesService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.Call;

@RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    //Logging Data tags
    private static final String TAG = "Facebook";
    private static final String TAG2 = "GoogleFit";
    private static final String TAG3 = "Fitbit";
    private static final String TAG4 = "Twitter";

    // FACEBOOK: Used to summarise
    private static final List<String> KEYWORDS = Arrays.asList("Fitness","dance","run", "Vegetarian"); //TODO: Change to be more extensive depending on words we want to search for
    private static int facebookFitnessCount = 0; // The count of how many facebook user_action.fitness the user has done

    // GOOGLEFIT: Each of the permissions and datatypes categorised into different lists
    List<DataType> NUTRITIONDATATYPES = Arrays.asList(DataType.AGGREGATE_BODY_FAT_PERCENTAGE_SUMMARY, DataType.AGGREGATE_CALORIES_EXPENDED, DataType.AGGREGATE_HYDRATION, DataType.AGGREGATE_NUTRITION_SUMMARY);
    List<DataType> ACTIVITYDATATYPES = Arrays.asList(DataType.AGGREGATE_ACTIVITY_SUMMARY, DataType.AGGREGATE_STEP_COUNT_DELTA,DataType.AGGREGATE_POWER_SUMMARY);
    List<DataType> PERMISSIONLOCATIONDATATYPES = Arrays.asList(DataType.AGGREGATE_DISTANCE_DELTA, DataType.AGGREGATE_SPEED_SUMMARY);
    List<DataType> PERMISSIONBODYSENSORDATATYPES = Arrays.asList(DataType.AGGREGATE_HEART_RATE_SUMMARY, DataType.AGGREGATE_BASAL_METABOLIC_RATE_SUMMARY);
    List<String> PERMISSIONS = Arrays.asList(permission.ACCESS_FINE_LOCATION, permission.BODY_SENSORS);

    // GOOGLEFIT: The API Client and the request code initialised
    public static GoogleApiClient mGoogleApiClient = null;
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    // GOOGLEFIT: Used by async tasks to update the summaries
    String outputFromWeeksTask;
    String outputFromDaysTask;

    // TWITTER
    TwitterSession session;
    TwitterApiClient twitterApiClient;
    private static Callback<List<Tweet>> twitterCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String CONSUMERKEY = getString(R.string.com_twitter_sdk_android_CONSUMER_KEY);
        String CONSUMERSECRET = getString(R.string.com_twitter_sdk_android_CONSUMER_SECRET);

        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(CONSUMERKEY, CONSUMERSECRET))
                .debug(true)
                .build();
        Twitter.initialize(config); // this initialises Twitter. Must be done before a getInstance() call as done in the method below.

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.d(TAG3, "Creating main page");

        // FACEBOOK Integration: gets the facebook access token and applies to it get update the main activities summary
        AccessToken fbToken = SettingsActivity.accessToken;
        TextView facebook = (TextView) findViewById(R.id.social_media_app_summary);
        if (fbToken == null){ // If SettingsActivity hasn't been created yet, get the token
            fbToken = AccessToken.getCurrentAccessToken();
        }
        if (fbToken != null) {
            facebook.setText(R.string.loading);
            facebookFitnessCount = 0; // reset to default
            if (!checkPermissionsFB()){
                facebook.setText(R.string.fb_disabled_permissions);
            } else {
                new FacebookSummaryTask().execute();
            }
        } else {
            facebook.setText(R.string.fb_logged_out);
        }

        // GOOGLEFIT Integration: builds the client and requests the appropriate permissions and subscribes to datatypes accordingly
        TextView gf = (TextView) findViewById(R.id.food_app_summary);
        gf.setText(R.string.loading);

        if (mGoogleApiClient == null){
            Log.d(TAG2,"Google client is null");
            buildAndConnectClient(); // TODO: Check switch pref
            checkAndRequestGoogleFitPermissions();
            subscribe();
        } else {
            new ViewDayGoogleFitTask().execute();
            new ViewWeekGoogleFitTask().execute();
        }

        TextView fitbitView = (TextView) findViewById(R.id.fitness_app_summary);
        fitbitView.setText(R.string.loading);
        new FitbitSummaryTask().execute();
//        if (SettingsActivity.fitbitToken != null) {
//            Log.d(TAG3, "Fitbit token on main page: " + SettingsActivity.fitbitToken);
//            String url = "https://api.fitbit.com/1/user/-/activities/date/2017-01-20.json";
//            retrieveFitbitData(url);
//        } else {
//            Log.d(TAG3, "Fitbit token is null");
//            fitbitView.setText(R.string.fitbit_not_authenticated);
//        }

        // TWITTER
        session = TwitterCore.getInstance().getSessionManager().getActiveSession();
        createTwitterCallback();
        if (session == null){
            Log.d(TAG3, "Twitter: Not logged in");
            // TODO: Reflect on the summary page in a message
        } else {
            displayTweets();
        }
        getSubscriptions();

    }

// ------------------------------------------------------------------------------
// TWITTER
// ------------------------------------------------------------------------------

    private void displayTweets(){
        twitterApiClient = TwitterCore.getInstance().getApiClient();
        displayFavouritedTweets();
        displayStatusTweets();
    }

    /**
     * This method displays all the users's favourited tweets
     */
    private void displayFavouritedTweets(){
        FavoriteService service = twitterApiClient.getFavoriteService();
        Call<List<Tweet>> call = service.list(null,null,null,null,null,null);
        call.enqueue(twitterCallback);
    }

    /**
     * This method displays all the user's statuses
     */
    private void displayStatusTweets(){
        StatusesService service = twitterApiClient.getStatusesService();
        Call<List<Tweet>> call = service.homeTimeline(null,null,null,null,null,null,null);
        call.enqueue(twitterCallback);
    }

    /**
     * This initialised the twitterCallback that is used to print the results from either a status or favourite request
     */
    private void createTwitterCallback(){
        twitterCallback = new Callback<List<Tweet>>() {
            @Override
            public void success(Result<List<Tweet>> result) {
                // TODO: Transform the data into a useful output to the user
                // TODO: Print the output to summary page
                Log.d(TAG4, "Succesfully got the results");

                // loops through the data and prints each tweat to the debug console
                List<Tweet> data = result.data;
                for (int i = 0; i < data.size(); i++){
                    String tweet = data.get(i).text;
                    Log.d(TAG4, tweet);
                }
            }

            public void failure(TwitterException exception) {
                //TODO: Add an error
                Log.d(TAG4, "Didn't get the results");
            }
        };
    }


// ------------------------------------------------------------------------------
// GOOGLE FIT
// ------------------------------------------------------------------------------

    /**
     * Builds to google client with the required scopes (permissions)
     */
    public void buildAndConnectClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.RECORDING_API)
                .addApi(Fitness.CONFIG_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                .addScope(new Scope(Scopes.FITNESS_NUTRITION_READ))
                .addScope(new Scope(Scopes.FITNESS_BODY_READ))
                .addScope(new Scope(Scopes.FITNESS_LOCATION_READ))
                .addConnectionCallbacks(
                        new GoogleApiClient.ConnectionCallbacks() {
                                @Override
                                public void onConnected(Bundle bundle) {
                                    Log.d(TAG2, "Connected!!!");
                                    new ViewDayGoogleFitTask().execute();
                                    new ViewWeekGoogleFitTask().execute();
                                }

                                @Override
                                public void onConnectionSuspended(int i) {
                                    // If your connection to the sensor gets lost at some point,
                                    // you'll be able to determine the reason and react to it here.
                                    if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                        Log.d(TAG2, "Connection lost.  Cause: Network Lost.");
                                    } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                        Log.d(TAG2, "Connection lost.  Reason: Service Disconnected");
                                    }
                                }
                            }

                )
                .enableAutoManage(this, 0, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult result) {
                        Log.i(TAG, "Google Play services connection failed. Cause: " +
                                result.toString());
                        Snackbar.make(
                                MainActivity.this.findViewById(R.id.main_activity_view),
                                "Exception while connecting to Google Play services: " +
                                        result.getErrorMessage(),
                                Snackbar.LENGTH_INDEFINITE).show();
                    }
                })
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * The initial subscription to all data types to record the output
     * TODO: Call this only on the very startup
     */
    private void subscribeToDataTypes(){
        List<DataType> newList = getListOfTypes();
        for (int i = 0; i < newList.size(); i++){
            Log.d(TAG2, "Subscribing " + newList.get(i).getName());
            // Subscription using RecordingAPI to the Google API Client
            Fitness.RecordingApi.subscribe(mGoogleApiClient, newList.get(i))
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
    }

    private void getSubscriptions(){
        PendingResult<ListSubscriptionsResult> result = Fitness.RecordingApi.listSubscriptions(mGoogleApiClient);
        result.setResultCallback(new ResultCallback<ListSubscriptionsResult>() {
            @Override
            public void onResult(@NonNull ListSubscriptionsResult listSubscriptionsResult) {
                for (Subscription sc : listSubscriptionsResult.getSubscriptions()) {
                    DataType dt = sc.getDataType();
                    Log.d(TAG2, "Active subscription for data type: " + dt.getName());
                }
            }
        });
    }

    private void subscribe(){
        PendingResult<ListSubscriptionsResult> result = Fitness.RecordingApi.listSubscriptions(mGoogleApiClient);
        result.setResultCallback(new ResultCallback<ListSubscriptionsResult>() {
            @Override
            public void onResult(@NonNull ListSubscriptionsResult listSubscriptionsResult) {
                // if there don't exist subscriptions, subscribe to all data types
                if (listSubscriptionsResult.getSubscriptions().size() <= 0){
                    subscribeToDataTypes();
                }
            }
        });
    }

    /**
     * This is an AsyncTask that is called to retrieve "Today's" data for each subscription
     * After this has executed, the summary of google fit is set with the calories extended as example
     */
    private class ViewDayGoogleFitTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) { // called on a seperate thread
            displayDataForToday();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (outputFromDaysTask != null) {
                TextView summary = (TextView) findViewById(R.id.food_app_summary);
                if (summary.getText().equals("Loading...")) {
                    summary.setText(outputFromDaysTask);
                } else {
                    summary.setText(summary.getText() + " " + outputFromDaysTask);
                }
            }
        }
    }

    /**
     * This retrieves the bulk info from GoogleFit and the subscriptions
     * Outputs this to the summary of google fit
     */
    private class ViewWeekGoogleFitTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            displayLastWeeksData();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (outputFromWeeksTask != null) {
                TextView summary = (TextView) findViewById(R.id.food_app_summary);
                if (summary.getText().equals("Loading...")) {
                    summary.setText(outputFromWeeksTask);
                } else {
                    summary.setText(summary.getText() + " " + outputFromWeeksTask);
                }
            }

        }
    }

    /**
     * This logs the bulk amount of data
     * TODO: Summarise it somehow
     */
    protected void displayLastWeeksData(){
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        java.text.DateFormat dateFormat = DateFormat.getDateInstance();
        Log.d(TAG2, "Range Start: " + dateFormat.format(startTime));
        Log.d(TAG2, "Range End: " + dateFormat.format(endTime));

        // The read requests made to the list of datatypes
        DataReadRequest readRequest = queryData(startTime, endTime, getListOfTypes());
        DataReadResult dataReadResult = Fitness.HistoryApi.readData(mGoogleApiClient, readRequest).await(1, TimeUnit.MINUTES);

        if (dataReadResult.getBuckets().size() > 0) { //Used for aggregated data
            Log.d(TAG2, "Number of buckets: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    showDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) { //Used for non-aggregated data
            outputFromWeeksTask = "Number of returned DataSets: " + dataReadResult.getDataSets().size();
            Log.d(TAG2, outputFromWeeksTask);

            for (DataSet dataSet : dataReadResult.getDataSets()) {
                showDataSet(dataSet);
            }
        }
    }

    /**
     * This method builds the read request using the list of datatypes (adds each one to it)
     * @param startTime : The start time to query data from
     * @param endTime : The end time to query data till
     * @param types : The list of datatypes that are used
     * @return : The build read request
     */
    private DataReadRequest queryData(long startTime, long endTime, List<DataType> types) {
        // TESTING EXTRAS
        types.add(DataType.TYPE_ACTIVITY_SAMPLES);
        types.add(DataType.TYPE_POWER_SAMPLE);

        DataReadRequest.Builder builder = new DataReadRequest.Builder();
        for (DataType dt : types) {
            builder.read(dt);
        }
        return builder.setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS).build();
    }

    /**
     * This is the method that displays the datasets
     * This is from the Android Get Started Exampler: BasicHistoryApi
     * @param dataSet : The Data set to be shown
     */
    private void showDataSet(DataSet dataSet) {
        Log.d(TAG2, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat timeFormat = DateFormat.getTimeInstance();

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.d(TAG2, "Data point:");
            Log.d(TAG2, "\tType: " + dp.getDataType().getName());

            Log.d(TAG2, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.d(TAG2, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                Log.d(TAG2, "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
                if (field.getName().equals("calories")){
                    outputFromDaysTask= field.getName() + " expended are " + dp.getValue(field) + ".";
                }
            }
        }
    }

    /**
     * This uses the permissions to create the datatypes to request
     * @return the datatypes permitted
     */
    private List<DataType> getListOfTypes(){
        List<DataType> newList = new ArrayList<>(NUTRITIONDATATYPES);
        newList.addAll(ACTIVITYDATATYPES);
        if (checkPermissions(PERMISSIONS.get(0))){ // if Permission AccessLocation is granted
            newList.addAll(PERMISSIONLOCATIONDATATYPES);
        }
        if (checkPermissions(PERMISSIONS.get(1))){ //
            newList.addAll(PERMISSIONBODYSENSORDATATYPES);
        }
        return newList;
    }

    /**
     * This printst "Todays" data from HISTORY Api
     */
    private void displayDataForToday() {
        List<DataType> newList = getListOfTypes();

        for (int i = 0; i < newList.size(); i++){
            DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mGoogleApiClient, newList.get(i) ).await(1, TimeUnit.MINUTES);
            showDataSet(result.getTotal());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG2, "HistoryAPI onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG2, "HistoryAPI onConnectionFailed");
    }

    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG2, "HistoryAPI onConnected");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO: used when the user resumes after accepting/ denying permissions
    }

    /**
     * This checks the current Permissions and requests them if denied or not asked yet
     */
    private void checkAndRequestGoogleFitPermissions(){
        for (int i = 0; i < PERMISSIONS.size(); i++){
            if (!checkPermissions(PERMISSIONS.get(i))){
                boolean shouldProvideAccessLocationRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, PERMISSIONS.get(i));
                requestPermissions(shouldProvideAccessLocationRationale, PERMISSIONS.get(i));
            }
        }
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions(String permission) {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                permission);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * This requests the permission to the user, this includes if the user has declined it
     * @param shouldProvideRationale : Whether
     * @param permission : The permissions to be requested to the user
     */
    private void requestPermissions(boolean shouldProvideRationale, final String permission) {

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG2, "Displaying permission rationale to provide additional context.");
            int rationale;
            if (permission.equals(PERMISSIONS.get(0))){
                rationale = R.string.permission_rationale_access_fine_location;
            } else {
                rationale = R.string.permission_rationale_body_sensors;
            }
            Snackbar.make(
                    findViewById(R.id.main_activity_view),
                    rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{permission},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG2, "Requesting permission " + permission);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

// ------------------------------------------------------------------------------
// FACEBOOK
// ------------------------------------------------------------------------------

    /**
     * This is the async task called when the activity is created.
     * It updates the summary for facebook on a different thread
     */
    private class FacebookSummaryTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            facebookSummary();
            return null;
        }
    }

    /**
     * This method is called to update the summary on Facebook if there exists an Access Token for Facebook
     */
    public void facebookSummary(){
        String requestedData = ""; // incase we want to seperate the method depending on permissions

        if(checkPermissionsFB()){ // gets the Denied and Granted permissions according to the access token
        //TODO: error checking if certain permissions aren't granted (accesses certain values in array accordingly - currently assumes gets all

            requestedData+="posts,likes,events";
            AccessToken facebookAccessToken = SettingsActivity.accessToken;
            if (facebookAccessToken == null){
                facebookAccessToken = AccessToken.getCurrentAccessToken();
            }

            //Callback method sent with request
            GraphRequest.Callback callback = new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    if (response != null) {
                        TextView facebook = (TextView) findViewById(R.id.social_media_app_summary);
                        String outputString = transformFacebookPostsEventsLikes(response.getJSONObject()); // this uses user_likes, user_posts and user_events
                        Log.d(TAG, "output : " + response.getJSONObject().toString());
                        facebook.setText(outputString);
                        if (getFBPermissions(true).contains("user_actions.fitness")){
                            transformFacebookFitness(); // this uses user_actions.fitness
                        }
                    } //TODO: Error checking
                }
            };
            GraphRequest req = new GraphRequest(
                    facebookAccessToken,
                    "/me/",
                    null,
                    HttpMethod.GET,
                    callback
            );
            Bundle parameters = new Bundle();
            parameters.putString("fields", requestedData); // adds requested permissions
            req.setParameters(parameters);
            req.executeAsync();
        } // No permissions granted
    }

    /**
     * Checks if user posts, user likes and user events permissions are granted
     * @return true or false whether all 3 permissions are granted
     */
    private boolean checkPermissionsFB(){
        List<String> grantedPermissions = getFBPermissions(true); // gets all granted permissions
        return (grantedPermissions.contains("user_posts") && grantedPermissions.contains("user_likes") && grantedPermissions.contains("user_events"));
    }

    /**
     *
     * This gets the access token and then returns the granted/ denied permissions according to it
     * @param wantGranted : true/ false depending on whether wants granted or denied permissions
     * @return List of permissions granted/ denied
     */
    public List<String> getFBPermissions(boolean wantGranted){
        List<String> list; // the returned list
        if (SettingsActivity.accessToken == null){ // settingsActivity has been created
            AccessToken facebookAccessToken = AccessToken.getCurrentAccessToken();
            list = new LinkedList<>(facebookAccessToken.getPermissions());
            if (!wantGranted) {
                list = new LinkedList<>(facebookAccessToken.getDeclinedPermissions());
            }
        } else {
            list = SettingsActivity.grantedFBPermissions;
            if (!wantGranted) {
                list = SettingsActivity.deniedFBPermissions;
            }
        }
        return list;
    }

    /**
     * This gets the user's data from fitness actions and counts them up
     * fitness actions include: bikes, walks & runs
     * Adds up the amount of data to a global variable
     */
    public void transformFacebookFitness(){
        AccessToken facebookAccessToken = SettingsActivity.accessToken;
        if (facebookAccessToken == null){
            facebookAccessToken = AccessToken.getCurrentAccessToken();
        }
        // the callback used by each
        GraphRequest.Callback callback = new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                Log.d(TAG, "Successful completion of asynch call"); // TESTING
                Log.d(TAG, "output : " + response.getJSONObject().toString()); // TESTING
                fitnessDataCount(response.getJSONObject()); // uses the response data to count the amount of fitness actions
            }
        };
        // creates a batch request querying fitness.bikes, fitness.walk and fitness.runs
        GraphRequestBatch batch = new GraphRequestBatch(
                new GraphRequest(
                        facebookAccessToken,
                        "/me/fitness.bikes",
                        null,
                        HttpMethod.GET
                        , callback
                ),
                new GraphRequest(
                        facebookAccessToken,
                        "/me/fitness.walk",
                        null,
                        HttpMethod.GET
                        , callback
                ),
                new GraphRequest(
                        facebookAccessToken,
                        "/me/fitness.runs",
                        null,
                        HttpMethod.GET
                        , callback
                )
        );
        // adds a callback which uses the incremented count to output to the text on the front screen to the user
        batch.addCallback(new GraphRequestBatch.Callback() {
            @Override
            public void onBatchCompleted(GraphRequestBatch graphRequests) {
                // Application code for when the batch finishes
                Log.d(TAG,"Graph Batch Executed"); // TESTING
                TextView facebook = (TextView) findViewById(R.id.social_media_app_summary);
                String outputString = (String) facebook.getText(); // gets the current text
                if (facebookFitnessCount == 1){ // updates the current text of the facebook summary, takes account of plural of the sentence
                    outputString += " You have completed " + facebookFitnessCount + " facebook fitness action.";
                } else {
                    outputString += " You have completed " + facebookFitnessCount + " facebook fitness actions.";
                }
                facebook.setText(outputString); // sets the output text
            }
        });
        batch.executeAsync();
    }

    /**
     * This is called on each callback to increment the facebook fitness count accordingly
     * @param jsonObject : the data response object
     */
    public void fitnessDataCount(JSONObject jsonObject){
        try {
            JSONArray array = (JSONArray) jsonObject.get(jsonObject.names().getString(0));
            if (array != null){ // increment count if data object is not empty, depending on length of it
                Log.d(TAG, "fitness object = " + array.toString());
                facebookFitnessCount+= array.length();
            } else {
                Log.d(TAG, "fitness object is null");
            }
        } catch (JSONException e){
            Log.d(TAG,"Error: JSON Exception");
        } //TODO: Error handling
    }

    /**
     * This takes the input jsonObject which is the reponse from the GraphAPI request
     * It then transforms this data into a useful summary text string to return to the user
     * It currently just calculates the amount of times the user includes keywords, such as "fitness" in their fb actions such as posts
     * @param jsonObject : The response object from the Graph API successful request
     * @return String : the string to output on the summary page
     */
    public String transformFacebookPostsEventsLikes(JSONObject jsonObject){
        // array values
        int posts = 0;
        int likes = 1;
        int events = 2;

        int countPostsEvents = 0; // the number of times something fitness related is liked/ posted about
        int countEvents = 0;
        Log.d(TAG, "JSON Object reponse in main activity: " + jsonObject.toString()); //TESTING

            try { // catch JSON exception
                Log.d(TAG, "length = " +jsonObject.length());
                if(jsonObject.length() > 1) {
                    // gets user's POST data
                    JSONObject postsObject = (JSONObject) jsonObject.get(jsonObject.names().getString(posts));
                    Log.d(TAG, "posts object = " + postsObject.toString());
                    JSONArray postsArray = (JSONArray) postsObject.get(postsObject.names().getString(0));
                    Log.d(TAG, "posts array = " + postsArray.toString());
                    countPostsEvents += loopThroughResponse(postsArray, "message"); // adds to count the number of times keywords are used in posts
                }
                if (jsonObject.length() > 2) {
                    // gets  user's LIKES data
                    JSONObject likesObject = (JSONObject) jsonObject.get(jsonObject.names().getString(likes));
                    Log.d(TAG, "likes object = " + likesObject.toString());
                    JSONArray likesArray = (JSONArray) likesObject.get(likesObject.names().getString(0));
                    Log.d(TAG, "likes array length " + likesArray.length() + " with values = " + likesArray.toString());
                    countPostsEvents += loopThroughResponse(likesArray, "name"); // adds to count the number of times keywords are used in likes
                }
                if (jsonObject.length() > 3) {
                    // gets  user's EVENTS data
                    JSONObject eventsObject = (JSONObject) jsonObject.get(jsonObject.names().getString(events));
                    Log.d(TAG, "events object = " + eventsObject.toString());
                    JSONArray eventsArray = (JSONArray) eventsObject.get(eventsObject.names().getString(0));
                    Log.d(TAG, "events array length " + eventsArray.length() + " with values = " + eventsArray.toString());
                    countEvents = loopThroughResponse(eventsArray, "description"); // adds to count the number of times keywords are used in event descriptions
                }
            } catch (JSONException e){
                Log.d(TAG,"Error: JSON Exception");
            } //TODO add error response

        // format string responses plurals accordingly to the output count
        String outputString;
        if (countPostsEvents == 0){ //POSTS AND LIKES
            outputString = "You have not posted or liked posts about fitness related things ";
        } else if (countPostsEvents == 1){
            outputString = "You have posted or liked about " + countPostsEvents + " fitness related thing ";
        } else {
            outputString = "You have posted or liked about " + countPostsEvents + " fitness related things ";
        }
        if (countEvents == 0){ //EVENTS
            outputString += "and attended no events.";
        } else if (countEvents == 1){
            outputString += "and attended " + countEvents + " health related events.";
        } else {
            outputString += "and attended " + countEvents + " health related events.";
        }
        return outputString;
    }

    /**
     * This loops through the given array and looks for key words in it, such as "Fitness"
     * @param array : of the user's fb data
     * @param dataType : the type of data it is, such as "message" or "name"
     * @return an integer value representing the number of matches
     */
    public int loopThroughResponse(JSONArray array, String dataType){
        int count = 0; // counter of number of times a keyword is matches
        try {
            for (int j = 0; j < array.length(); j++) { // loops through each element in the array
                Log.d(TAG, "values = " + array.get(j)); //TESTING
                JSONObject obj = (JSONObject) array.get(j);
                Object value = obj.get(dataType); //gets the parameter according to the data type
                Log.d(TAG, value.toString()); // TESTING

                for (String string : KEYWORDS) { // loops through target keywords
                    if (value.toString().contains(string)) { // checks if the target string is contained within the current object string
                        count++; // increases the count if there's a match
                    }
                }
            }
        } catch (JSONException e){
            Log.d(TAG,"Error: JSON Exception");
        } //TODO add error response
        return count;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return openSettings();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This function opens the settings menu
     * sets the current intent to the settings page
     */
    public boolean openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        return true;
    }

//FITBIT

    private class FitbitSummaryTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            Log.d(TAG3, "fitbit async");
            retrieveFitbitData();
            return null;
        }
    }

    public void retrieveFitbitData() {
        try {
            Log.d(TAG3, "fitbit retrieval");

            String dataRequestUrl = "https://api.fitbit.com/1/user/-/activities/date/2017-01-20.json";
            URL url = new URL(dataRequestUrl);

            //set up the connection with the Authorisation header containing the previously obtained
            //access token
            //TO DO: currently hardcoded token
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(10000);//this is in milliseconds
            conn.setConnectTimeout(15000);//this is in milliseconds
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.addRequestProperty("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI1QkRXRFEiLCJhdWQiOiIyMjhLUVciLCJpc3MiOiJGaXRiaXQiLCJ0eXAiOiJhY2Nlc3NfdG9rZW4iLCJzY29wZXMiOiJyYWN0IHJociBybnV0IiwiZXhwIjoxNTAxNjc1MDYwLCJpYXQiOjE1MDEzMDExNjV9.dVwRx9kOFT8VcR9NupTnuveBIMRR-2uLgQ23OcOUVSo");            //starts the query

            //Send the request
            int responseCode = conn.getResponseCode();
            String responseType = conn.getContentType();
            Log.d(TAG3, "\nResponse Type : " + responseType);
            Log.d(TAG3, "Response Code : " + responseCode);

            //Read the input received
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONArray fitbitJson = new JSONArray(response);
            String goals = fitbitJson.getString(1);
            //print result
            Log.d(TAG3, "Goals: " + goals);

        } catch (Exception e) {

        }

    }

}

