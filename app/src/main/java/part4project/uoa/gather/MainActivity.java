package part4project.uoa.gather;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.util.Log;
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
import com.google.android.gms.fitness.request.DataReadRequest;
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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;

import static part4project.uoa.gather.SocialMethods.doesStringContainKeyword;
import static part4project.uoa.gather.SocialMethods.getDate;


@RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    //Logging Data TAGs
    private static final String TAG = "MainActivity";

    // Data lists
    public static List<Data> nutritionSocial = new LinkedList<>();
    public static List<Data> nutritionGeneral = new LinkedList<>();
    public static List<Data> fitnessSocial = new LinkedList<>();
    public static List<Data> fitnessGeneral  = new LinkedList<>();

    // GOOGLEFIT: Each of the permissions and datatypes categorised into different lists
    public static final List<DataType> NUTRITIONDATATYPES = Arrays.asList(DataType.AGGREGATE_CALORIES_EXPENDED, DataType.AGGREGATE_HYDRATION, DataType.AGGREGATE_NUTRITION_SUMMARY);
    public static final List<DataType> FITNESSDATATYPES = Arrays.asList(DataType.AGGREGATE_ACTIVITY_SUMMARY, DataType.AGGREGATE_STEP_COUNT_DELTA);
    public static GoogleApiClient mGoogleApiClient = null; // The API Client

    ProgressDialog progress;

    // TWITTER
    TwitterSession session;

    // Week Date
    public static Date startOfWeek;
    public static Date endOfWeek;
    public static Date today;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TWITTER Initialised
        String CONSUMERKEY = getString(R.string.com_twitter_sdk_android_CONSUMER_KEY);
        String CONSUMERSECRET = getString(R.string.com_twitter_sdk_android_CONSUMER_SECRET);
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(CONSUMERKEY, CONSUMERSECRET))
                .debug(true)
                .build();
        Twitter.initialize(config); // this initialises Twitter. Must be done before a getInstance() call as done in the method below.

        // Content Initialised
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupDates();
        setupProgressDialog();

        // GOOGLEFIT builds the client and requests the appropriate permissions and subscribes to datatypes accordingly
        if (mGoogleApiClient == null){
            Log.d(TAG,"Google client is null");
            GoogleFit gf = new GoogleFit();
            gf.buildAndConnectClient(); // TODO: Check switch pref
            gf.subscribe();
        } else {
            getWeeksData();
        }

        // SOCIAL
        AccessToken fbToken = SettingsActivity.accessToken;
        if (fbToken == null){ // If SettingsActivity hasn't been created yet, get the token
            fbToken = AccessToken.getCurrentAccessToken();
        }
        if (session == null){
            session = TwitterCore.getInstance().getSessionManager().getActiveSession();
            if (fbToken != null && session != null) {
                    new SocialTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    /**
     * Sets up the progress spinning dialog
     */
    private void setupProgressDialog(){
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Please wait while loading...");
        progress.setCancelable(false);
    }

    /**
     * Sets up the start and end dates
     */
    //TODO: Change start time to be a week, not a week from today
    private void setupDates(){
        Calendar cal = Calendar.getInstance();
        today = new Date();
        cal.setTime(today);
        cal.add(Calendar.WEEK_OF_YEAR, -1); // minus a week
        startOfWeek = cal.getTime();
        cal.add(Calendar.DAY_OF_WEEK, 7);
        endOfWeek = cal.getTime();

        Log.d("Date", "Range Start: " + startOfWeek);
        Log.d("Date", "Range End: " + endOfWeek);
        Log.d("Date", "Today " + today);
    }

    /**
     * Used to start the fitness summary page
     * @param view : The current view
     */
    public void intentFitness(View view) {
        startActivity(new Intent(this,FitnessSummaryActivity.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * This is used to begin the nutrition summary page (activity)
     * @param view : The current view
     */
    public void intentNutrition(View view) {
        startActivity(new Intent(this,NutritionSummaryActivity.class));
    }

    private boolean isDateInWeek(Date parsed){
        return startOfWeek.before(parsed) && endOfWeek.after(parsed);
    }

    /**
     * This tasks executes getting data from facebook & twitter
     */
    private class SocialTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.show();
        }

        protected Void doInBackground(Void... params) { // called on a seperate thread
            // clear both the social lists
            nutritionSocial = new LinkedList<>();
            fitnessSocial = new LinkedList<>();
            // Begins the social nutrition data collection
            Social socialNutritionClass = new Social();
            socialNutritionClass.displaySocial(true);
            // Begins the fitness social data collection
            Social socialFitnessClass = new Social();
            socialFitnessClass.displaySocial(false);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progress.dismiss();
        }
    }

    private class GeneralTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.show();
        }

        protected Void doInBackground(Void... params) { // called on a seperate thread
            // TODO
            nutritionGeneral = new LinkedList<>();
            fitnessGeneral = new LinkedList<>();
            General generalNutritionClass = new General();
            generalNutritionClass.displayGeneral(true);
            General generalFitnessClass = new General();
            generalFitnessClass.displayGeneral(false);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "post execute");
            progress.dismiss();
            getWeeksData();
        }
    }

    /**
     * This class contains all the methods to create social summary of a week
     */
    private class Social {

        private boolean isNutrition = false;

        private void displaySocial(boolean isNutrition){
            this.isNutrition = isNutrition;
            if (SocialMethods.getFBToken() != null) {
                facebookSummary();
            }
            twitterSummary();
            if (!isNutrition){
                transformFacebookFitness();
            }
        }

        private void facebookSummary(){
            if(SocialMethods.checkPermissionsFB()){ // gets the Denied and Granted permissions according to the access token
                AccessToken facebookAccessToken = SocialMethods.getFBToken();

                //Callback method sent with request
                GraphRequest.Callback callback = new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        if (response != null && response.getJSONObject()!= null) {
                            transformFacebookPostsEventsLikes(response.getJSONObject()); // this uses user_likes, user_posts and user_events
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
                parameters.putString("fields", "posts,likes,events"); // adds requested permissions
                req.setParameters(parameters);
                req.executeAsync();
            }
        }

        private void transformFacebookPostsEventsLikes(JSONObject jsonObject){
            JSONArray postsArray = SocialMethods.getArray(jsonObject, 0);
            if (postsArray != null) {
                loopThroughResponse(postsArray, "message", "created_time", DataCollectionType.FPOST);
            }
            JSONArray likesArray = SocialMethods.getArray(jsonObject, 1);
            if (likesArray != null) {
                loopThroughResponse(likesArray, "name", "created_time", DataCollectionType.FLIKE);
            }
            JSONArray eventsArray = SocialMethods.getArray(jsonObject, 2);
            if (likesArray != null) {
                loopThroughResponse(eventsArray, "name", "start_time", DataCollectionType.FEVENT);
            }
        }

        private void loopThroughResponse(JSONArray array, String dataType, String timeName, DataCollectionType dct){
            try {
                for (int j = 0; j < array.length(); j++) { // loops through each element in the array
                    JSONObject obj = (JSONObject) array.get(j);
                    Object value = obj.get(dataType); //gets the parameter according to the data type
                    Object time = obj.get(timeName);

                    Date parsed = getDate(time.toString(), true);
                    if (isDateInWeek(parsed)) {
                        Log.d(TAG,"True for string " + value.toString());
                        if (doesStringContainKeyword(value.toString(), isNutrition)){
                            Log.d(TAG, "Added string: " + dct.toString() + value);
                            Data data = new Data(parsed, dct, value.toString());
                            if (isNutrition){
                                nutritionSocial.add(data);
                            } else {
                                fitnessSocial.add(data);
                            }
                        }
                    }

                }
            } catch (JSONException e){
                Log.d(TAG,"Error: JSON Exception");
            } //TODO add error response
        }

        private void twitterSummary(){
            TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
            if (twitterApiClient != null){
                displayFavouritedTweets(twitterApiClient);
                displayStatusTweets(twitterApiClient);
            }
        }

        /**
         * This method displays all the users's favourited tweets
         */
        private void displayFavouritedTweets(TwitterApiClient twitterApiClient){
            FavoriteService service = twitterApiClient.getFavoriteService();
            Call<List<Tweet>> call = service.list(null,null,null,null,null,null);
            call.enqueue(getTwitterCallback());
        }

        /**
         * This method displays all the user's statuses
         */
        private void displayStatusTweets(TwitterApiClient twitterApiClient){
            StatusesService service = twitterApiClient.getStatusesService();
            Call<List<Tweet>> call = service.homeTimeline(null,null,null,null,null,null,null);
            call.enqueue(getTwitterCallback());
        }

        /**
         * This initialised the twitterCallback that is used to print the results from either a status or favourite request
         */
        private Callback<List<Tweet>> getTwitterCallback(){
            return new Callback<List<Tweet>>() {
                @Override
                public void success(Result<List<Tweet>> result) {
                    // loops through the data and prints each tweet to the debug console
                    List<Tweet> data = result.data;
                    for (int i = 0; i < data.size(); i++){
                        Date parsed = getDate(data.get(i).createdAt, false);
                        if (isDateInWeek(parsed)) {
                            Log.d(TAG,"True for string " + data.get(i).toString());
                            if (doesStringContainKeyword(data.get(i).text, isNutrition)){
                                Log.d(TAG, "Added Tweet: " + data.get(i));
                                Data tweetData = new Data(parsed, DataCollectionType.TWEET, data.get(i).text);
                                if (isNutrition){
                                    nutritionSocial.add(tweetData);
                                } else {
                                    fitnessSocial.add(tweetData);
                                }
                            }
                        }
                    }
                }

                public void failure(TwitterException exception) {
                    //TODO: Add an error
                    Log.d(TAG, "Didn't get the results");
                }
            };
        }

        /**
         * This gets the user's data from fitness actions and counts them up
         * fitness actions include: bikes, walks & runs
         * Adds up the amount of data to a global variable
         */
        private void transformFacebookFitness(){
            AccessToken facebookAccessToken = SocialMethods.getFBToken();

            // the callback used by each
            GraphRequest.Callback callback = new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    Log.d(TAG, "Successful completion of asynch call"); // TESTING
                    if (response != null && response.getJSONObject() != null){
                        Log.d(TAG, "output : " + response.getJSONObject().toString()); // TESTING
                        getFacebookFitnessActions(response.getJSONObject()); // uses the response data to count the amount of fitness actions
                    }
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
                }
            });
            batch.executeAsync();
        }

        /**
         * This is called on each callback to increment the facebook fitness count accordingly
         * @param jsonObject : the data response object
         */
        private void getFacebookFitnessActions(JSONObject jsonObject) {
            try {
                JSONArray array = SocialMethods.getFitnessArray(jsonObject);
                if (array != null && array.length() != 0) { // increment count if data object is not empty, depending on length of it
                    Log.d(TAG, "fitness object = " + array.toString());
                    for (int i = 0; i < array.length(); i++){
                        JSONObject obj = array.getJSONObject(i);
                        Log.d(TAG, "Fitness OBJ: " + obj.getString("end_time"));
                        String time = obj.getString("start_time");
                        Date parsed = getDate(time, true);

                        if (isDateInWeek(parsed)){
                            Data data = new Data(parsed, DataCollectionType.FFITNESS, obj.getString("type"));
                            fitnessSocial.add(data);
                        } else {
                            Log.d(TAG, "Fitness data "+ obj.getString("type") + " isn't within the week" );
                        }
                    }
                } else {
                    Log.d(TAG, "fitness object is null");
                }
            } catch (JSONException e) {
                Log.d(TAG, "Error: JSON Exception");
            } //TODO: Error handling
        }
    }

    private class General{

        boolean isNutrition = false;

        private void displayGeneral(boolean isNutrition){
            this.isNutrition = isNutrition;
            displayLastWeeksData();
        }

        private void displayLastWeeksData(){
            // The read requests made to the list of datatypes
            DataReadRequest readRequest = GeneralMethods.queryData(isNutrition);
            DataReadResult dataReadResult = Fitness.HistoryApi.readData(mGoogleApiClient, readRequest).await(1, TimeUnit.MINUTES);

            if (dataReadResult.getBuckets().size() > 0) { // Used for aggregated data
                Log.d(TAG, "Number of buckets: " + dataReadResult.getBuckets().size());
                for (Bucket bucket : dataReadResult.getBuckets()) {
                    List<DataSet> dataSets = bucket.getDataSets();
                    for (DataSet dataSet : dataSets) {
                        showDataSet(dataSet);
                    }
                }
            } else if (dataReadResult.getDataSets().size() > 0) { //Used for non-aggregated data
                Log.d(TAG, "Number of returned DataSets: " + dataReadResult.getDataSets().size());

                for (DataSet dataSet : dataReadResult.getDataSets()) {
                    showDataSet(dataSet);
                }
            }
        }

        private void showDataSet(DataSet dataSet) {
            Log.d(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
            DateFormat dateFormat = DateFormat.getDateInstance();
            DateFormat timeFormat = DateFormat.getTimeInstance();

            for (DataPoint dp : dataSet.getDataPoints()) {
                Log.d(TAG, "Data point:");
                Log.d(TAG, "\tType: " + dp.getDataType().getName());

                Log.d(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
                Log.d(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));

                Date parsed = new Date(dp.getStartTime(TimeUnit.MILLISECONDS));
                Log.d(TAG, "Parsed data : " + parsed);

                for(Field field : dp.getDataType().getFields()) {
                    Log.d(TAG, "\tField: " + field.getName() +
                            " Value: " + dp.getValue(field));
                    if (isDateInWeek(parsed)) {
                        if (field.getName().equals("calories")) {
                            Data data = new Data(parsed, DataCollectionType.GCALORIES, dp.getValue(field).toString());
                            nutritionGeneral.add(data);
                        }
                    }
                }
            }
        }
    }

    /**
     * Moved all GoogleFit instantiation into its own class
     * NOTE: has to stay in this activity because it uses mGoogleAPIClient
     * which doesn't work well connecting to in other classes
     */
    private class GoogleFit{
        /**
         * Builds to google client with the required scopes (permissions)
         */
        void buildAndConnectClient(){
            mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
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
                                    Log.d(TAG, "Connected!!!");
                                    subscribe(); // double check
                                    new GeneralTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }

                                @Override
                                public void onConnectionSuspended(int i) {
                                    // If your connection to the sensor gets lost at some point,
                                    // you'll be able to determine the reason and react to it here.
                                    if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                        Log.d(TAG, "Connection lost.  Cause: Network Lost.");
                                    } else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                        Log.d(TAG, "Connection lost.  Reason: Service Disconnected");
                                    }
                                }
                            }

                    )
                    .enableAutoManage(MainActivity.this, 0, new GoogleApiClient.OnConnectionFailedListener() {
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
        void subscribeToDataTypes(){
            List<DataType> newList = new ArrayList<>(NUTRITIONDATATYPES);
            newList.addAll(FITNESSDATATYPES);
            for (int i = 0; i < newList.size(); i++){
                // Subscription using RecordingAPI to the Google API Client
                Fitness.RecordingApi.subscribe(mGoogleApiClient, newList.get(i))
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {
                                if (status.isSuccess()) {
                                    if (status.getStatusCode() == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                        Log.d(TAG, "Existing subscription for activity detected.");
                                    } else {
                                        Log.d(TAG, "Successfully subscribed!");
                                    }
                                } else {
                                    Log.d(TAG, "There was a problem subscribing. " + status);
                                }
                            }
                        });
            }
        }

        /**
         * This class subscribes to datatypes only if no subscriptions currently exist
         */
        void subscribe(){
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
    }

    /**
     * This loops through each list
     */
    private void getWeeksData(){
        boolean[] isFitness = DataCollection.getWeeksData(fitnessGeneral, fitnessSocial);
        boolean[] isNutrition = DataCollection.getWeeksData(nutritionGeneral, nutritionSocial);

        String outputString = "Fitness: ";
        for (boolean truth : isFitness) {
            outputString += truth + " ";
        }
        outputString += "\n\nNutrition: ";
        for (boolean truth : isNutrition){
            outputString += truth + " ";
        }

        TextView textView = (TextView) findViewById(R.id.text);
        textView.setText(outputString);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "HistoryAPI onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "HistoryAPI onConnectionFailed");
    }

    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "HistoryAPI onConnected");

    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO: used when the user resumes after accepting/ denying permissions
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
}


