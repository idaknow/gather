package part4project.uoa.gather;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.Call;

import static part4project.uoa.gather.SocialMethods.doesStringContainKeyword;
import static part4project.uoa.gather.SocialMethods.getDate;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        WeekView.EventClickListener,
        MonthLoader.MonthChangeListener,
        WeekView.EventLongPressListener,
        WeekView.EmptyViewLongPressListener {

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

    boolean[] isFitness = new boolean[7];
    boolean[] isNutrition = new boolean[7];

    WeekView mWeekView; // Calendar
    ProgressDialog progress; // loading
    TwitterSession session; // Twitter Session

    // Week Date
    public static Date startOfWeek;
    public static Date endOfWeek;
    public static Date today;

    //Get SharedPreferences for Fitbit to store access token and to store first_time flag
    public static SharedPreferences mainPreferences = null;
    final String PREFS_NAME = "MainPreferencesFile";
    public List<ApplicationInfo> installedPackages;
    public static boolean twitterInstalled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        installedPackages = getPackageManager().getInstalledApplications(0);
        for (ApplicationInfo appInfo : installedPackages){
            String appName = (String)appInfo.loadLabel(getPackageManager());
            Log.d(TAG, "app name: " + appName);
            if (appName.equals("Twitter")){
                twitterInstalled = true;
            }
        }

        if (twitterInstalled){
            // TWITTER Initialised
            String CONSUMERKEY = getString(R.string.com_twitter_sdk_android_CONSUMER_KEY);
            String CONSUMERSECRET = getString(R.string.com_twitter_sdk_android_CONSUMER_SECRET);
            TwitterConfig config = new TwitterConfig.Builder(this)
                    .logger(new DefaultLogger(Log.DEBUG))
                    .twitterAuthConfig(new TwitterAuthConfig(CONSUMERKEY, CONSUMERSECRET))
                    .debug(true)
                    .build();
            Twitter.initialize(config); // this initialises Twitter. Must be done before a getInstance() call as done in the method below.
        }

        // Content Initialised
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayShowTitleEnabled(false); // removes the title, so only the image logo is displayed
        }
        Log.d(TAG, "Creating main page");

        setupDates();
        setupProgressDialog();

        // GOOGLEFIT builds the client and requests the appropriate permissions and subscribes to datatypes accordingly
        if (mGoogleApiClient == null){
            Log.d(TAG,"Google client is null");
            GoogleFit gf = new GoogleFit();
            gf.buildAndConnectClient(); // TODO: Check switch pref
            gf.subscribe();
        } else {
            mWeekView = (WeekView) findViewById(R.id.weekView);
            updateCalendarWithEvents();
        }

        //Get user information from Fitbit by starting the Async Task
        new FitbitSummaryTask().execute();

        // SOCIAL TASK
        new SocialTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // Setup Calendar
        setupCalendar();
        Log.d("STATUS", "Created");


    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO: used when the user resumes after accepting/ denying permissions
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
    private void setupDates(){
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("NZ"));
        today = new Date();
        cal.setTime(today); // sets todays date
        if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // gets monday for the week
            if (cal.get(Calendar.WEEK_OF_YEAR) == cal.getFirstDayOfWeek()){ //TODO test
                cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - 1);
                cal.set(Calendar.WEEK_OF_YEAR, cal.getWeeksInWeekYear());
            }
            cal.set(Calendar.WEEK_OF_YEAR,cal.get(Calendar.WEEK_OF_YEAR)-1);

        } else {
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // gets monday for the week
        }

        // Set time to be 12 am for start and end date
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        startOfWeek = cal.getTime();

        cal.add(Calendar.DAY_OF_WEEK, 6); // add 6 days, not 7 or it goes mon -> mon
        endOfWeek = cal.getTime();

        Log.d("Date", "Range Start: " + startOfWeek);
        Log.d("Date", "Range End: " + endOfWeek);
        Log.d("Date", "Today " + today);
    }

    /**
     * This class sets up the UI calendar to show a certain number of dates
     * and to move to the specific time
     */
    private void setupCalendar(){
        mWeekView = (WeekView) findViewById(R.id.weekView);
        mWeekView.setOnEventClickListener(this);
        mWeekView.setMonthChangeListener(this);
        mWeekView.setEventLongPressListener(this);
        setupDateTimeInterpreter();
        mWeekView.setHourHeight(80);
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("NZ"));
        cal2.setTime(today);
        if (cal2.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
            mWeekView.setNumberOfVisibleDays(7);
        } else {
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("NZ"));
            cal.setTime(startOfWeek);
            int hour = cal2.get(Calendar.HOUR_OF_DAY) - 5;
            if (hour > 0) {
                cal2.set(Calendar.HOUR_OF_DAY,hour);
            }
            mWeekView.goToDate(cal);
            mWeekView.goToHour(cal2.get(Calendar.HOUR_OF_DAY));
        }
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

    /**
     * This returns true/ false depending whether a date object is between the start and end date
     * @param parsed : The date to search for
     * @return T/F depending on whether is in between start & end date
     */
    private boolean isDateInWeek(Date parsed){
        return startOfWeek.before(parsed) && endOfWeek.after(parsed);
    }

    @Override
    public List<WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        return displayEvents();
    }

    /**
     * This loops through the lists of fitness and social data and adds them accordingly to the calendar
     * @return : The list of WeekViewEvents which are added to the calendar
     */
    private List<WeekViewEvent> displayEvents(){
        List<WeekViewEvent> events = new ArrayList<>(); // initialise empty events
        int colour = ContextCompat.getColor(getApplicationContext(), R.color.fitness); // sets colour as fitness
        for (int j = 0; j < 4; j++){ // loops through each of the arrays
            List<Data> list = null; // creates empty list to call later
            switch(j){
                case 0: // GENERAL & FITNESS
                    list = fitnessGeneral;
                    break;
                case 1: // SOCIAL & FITNESS
                    list = fitnessSocial;
                    break;
                case 2: // SOCIAL & NUTRITION
                    colour = ContextCompat.getColor(getApplicationContext(), R.color.nutrition);
                    list = nutritionSocial;
                    break;
                case 3: // GENERAL & NUTRITION
                    colour = ContextCompat.getColor(getApplicationContext(), R.color.nutrition);
                    list = nutritionGeneral;
                    break;
            }
            if (list!=null){ // only does this if it's not empty
                for (int i = 0; i < list.size(); i++){ // loops through list
                    Calendar startTime = Calendar.getInstance(TimeZone.getTimeZone("NZ"));
                    startTime.setTime(list.get(i).getCreatedAt());
                    Calendar endTime = (Calendar) startTime.clone();
                    endTime.add(Calendar.HOUR, 1); // each event lasts 1 hour
                    WeekViewEvent event = new WeekViewEvent(1, " ", startTime, endTime); // NOTE: Print empty string
                    event.setColor(colour); // sets the colour
                    events.add(event);
                }
            }
        }
        return events;
    }

    /**
     * Set up a date time interpreter which will show short date values when in week view and long
     * date values otherwise.
     */
    private void setupDateTimeInterpreter() {
        mWeekView.setDateTimeInterpreter(new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                String weekday = weekdayNameFormat.format(date.getTime());
                weekday = String.valueOf(weekday.charAt(0));
                return date.get(Calendar.DATE) + " " + weekday.toUpperCase();
            }

            @Override
            public String interpretTime(int hour) {
                if (hour == 12){ // PRINT 12PM not default 0PM
                    return hour +" PM";
                }
                return hour > 11 ? (hour - 12) + " PM" : (hour == 0 ? "12 AM" : hour + " AM");
            }
        });
    }

    @Override
    public void onEmptyViewLongPress(Calendar time) {

    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {

    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {

    }

    /**
     * This tasks executes getting data from facebook & twitter
     */
    private class SocialTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            progress.show();
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
//            progress.show();
        }

        protected Void doInBackground(Void... params) { // called on a seperate thread
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
//            progress.dismiss();
            updateCalendarWithEvents();
        }
    }

    /**
     * This re-initialises the weeks data into boolean functions and notifies the calendar of event changes
     * this is called to update the UI Calendar
     */
    public void updateCalendarWithEvents(){
        isFitness = DataCollection.getWeeksData(fitnessGeneral, fitnessSocial);
        isNutrition = DataCollection.getWeeksData(nutritionGeneral, nutritionSocial);
        mWeekView.notifyDatasetChanged();
    }

    /**
     * This class contains all the methods to create social summary of a week
     */
    private class Social {

        private boolean isNutrition = false;

        private void displaySocial(boolean isNutrition) {
            this.isNutrition = isNutrition;
            if (SocialMethods.getFBToken() != null) {
                facebookSummary();
                if (!isNutrition) {
                    transformFacebookFitness();
                }
            }

            if (twitterInstalled) {
                if (session == null) {
                    session = TwitterCore.getInstance().getSessionManager().getActiveSession();
                }

                if (session != null) {
                    twitterSummary();
                }
            }
        }

        private void facebookSummary(){
            if (SocialMethods.checkPermissionsFB()){ // gets the Denied and Granted permissions according to the access token
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

        /**
         * This method loops through the information in the response JSON Object
         * Calls loopThroughResponse method on each jsonArray: Posts, likes and events data
         * @param jsonObject : The JSON response object
         */
        private void transformFacebookPostsEventsLikes(JSONObject jsonObject){
            String[] array = {"posts", "likes", "events"}; // array of permissions
            for (int index = 0; index < array.length; index++){
                JSONArray jsonArray = SocialMethods.getArray(jsonObject, array[index]); // gets the data object
                if (jsonArray != null){
                    switch(index){
                        case 0:
                            loopThroughResponse(jsonArray, "message", "created_time", DataCollectionType.FPOST);
                            break;
                        case 1:
                            loopThroughResponse(jsonArray, "name", "created_time", DataCollectionType.FLIKE);
                            break;
                        case 2:
                            loopThroughResponse(jsonArray, "name", "start_time", DataCollectionType.FEVENT);
                            break;
                    }
                }
            }
        }

        /**
         * This method loops through the response JSON data array for each permission
         * @param array: This is the JSON response array
         * @param dataType: The string name of the data type provided
         * @param timeName: The time variable name - dependent on JSONarray provided
         * @param dct: Data Collection Type - which type to store if passes requirement
         */
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

        /**
         * Uses the twitterAPIClient to get statusees and favourites
         */
        private void twitterSummary(){
            TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
            if (twitterApiClient != null){
                SharedPreferences prefs = getSharedPreferences("MainPreferences", Context.MODE_PRIVATE); // shared preferences

                Log.d("Twitter","Fav " +prefs.getBoolean(SettingsActivity.TWITTERPREFERENCES.get(0), false));
                Log.d("Twitter","Status " +prefs.getBoolean(SettingsActivity.TWITTERPREFERENCES.get(1), false));

                if (prefs.getBoolean(SettingsActivity.TWITTERPREFERENCES.get(0), false)){ // checks if the user gave permission to favourites
                    displayFavouritedTweets(twitterApiClient);
                }
                if (prefs.getBoolean(SettingsActivity.TWITTERPREFERENCES.get(1), false)){ // checks if the user gave permission to statuses
                    displayStatusTweets(twitterApiClient);
                }
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
                    updateCalendarWithEvents();
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
                    updateCalendarWithEvents();
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
            Log.d(TAG, "fitbit async");
            retrieveFitbitData();
            return null;
        }
    }

    public void retrieveFitbitData() {
        try {

            Log.d(TAG, "fitbit retrieval");

            //set up the connection with the Authorisation header containing the user
            //access token
            /*
            Set up the HTTPS connection to pull data
            The authorisation header needs to be set to contain the user access_token
            If an access token doesn't exist because the user hasn't granted permissions yet, then
            display an notification of this?
            If the access token has expired, either open the browser for the user to reauthenticate,
            or uncheck the switch preference and state the permission needs to be given again.
             */
            String dataRequestUrl = "https://api.fitbit.com/1/user/-/activities/date/2017-01-20.json";
            URL url = new URL(dataRequestUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(10000);//this is in milliseconds
            conn.setConnectTimeout(15000);//this is in milliseconds
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            String access_token = mainPreferences.getString("access_token", null);
            if (access_token != null) {
                conn.addRequestProperty("Authorization", "Bearer " + access_token);

                //Send the request
                int responseCode = conn.getResponseCode();
                String responseType = conn.getContentType();
                Log.d(TAG, "\nResponse Type : " + responseType);
                Log.d(TAG, "Response Code : " + responseCode);

                //Check to make sure that the connection has been made successfully before trying to
                //read data.
                if (responseCode == 201) {

                    //Read the input received
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                } else if (responseCode == 401 ){ //401 is returned if the token has expired.
                    //Either take user to authentication page by opening browser?
                    Log.e(TAG, "access token for fitbit has expired..needs to be requested again");
                    SettingsActivity.browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(SettingsActivity.fitbitAuthLink));
                    startActivity(SettingsActivity.browserIntent);
                } else { //Any other errors with the connection
                    Log.e(TAG, "an error has occured accessing user information, fitbit");
                }
            } else { //If the user hasn't given authentication yet then display a message notifying them
                Log.e(TAG, "fitbit token is null");
            }

            //Read the JSON response and process the results...
//            JSONObject jsonResponse = JSONObject.parse(response.toString());
//            Log.d(TAG, "first response: " + response);
//            Log.d(TAG, response.getJSON);
//            JSONObject goalSteps = response.getJSONObject('goals');
//            Log.d(TAG, "steps " + goalSteps);


        } catch (Exception e) {
            Log.d("Fitbit", e.toString());
        }

    }

}
