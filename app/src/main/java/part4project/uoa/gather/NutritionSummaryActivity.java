package part4project.uoa.gather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.FavoriteService;
import com.twitter.sdk.android.core.services.StatusesService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;

public class NutritionSummaryActivity extends AppCompatActivity {

    //Logging Data tags
    private static final String TAG = "Nutrition";
    ProgressDialog progress;

    private static List<String> social;
    private static List<String> general;

    public static final List<String> NUTRITIONKEYWORDS = Arrays.asList("Nutrition","Vegetables", "Vegetarian", "Tasty", "Food", "bean", "Coffee", "water");
    List<DataType> DATATYPES = Arrays.asList(DataType.AGGREGATE_CALORIES_EXPENDED, DataType.AGGREGATE_HYDRATION, DataType.AGGREGATE_NUTRITION_SUMMARY);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition_summary);
        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayData((ToggleButton) v);
            }
        });
        progress = new ProgressDialog(this);
        progress.setTitle("Loading");
        progress.setMessage("Wait while loading...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        displayData(toggle);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        displayData(toggle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        displayData(toggle);
    }

    private void displayData(ToggleButton toggle){
        if (toggle.isChecked()){
            Log.d(TAG, "Toggle Button is checked");
            progress.show();
            new SocialTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            Log.d(TAG, "Toggle Button is NOT checked");
            progress.show();
            new GeneralTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void intentMainActivity(View view) {
        startActivity(new Intent(this,MainActivity.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private class SocialTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) { // called on a seperate thread
            // TODO
            social = new LinkedList<>();
            Social socialClass = new Social();
            socialClass.displaySocial();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progress.dismiss();
        }
    }

    private class GeneralTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) { // called on a seperate thread
            // TODO
            general = new LinkedList<>();
            General generalClass = new General();
            generalClass.displayGeneral();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "post execute");
            setListViewContent(false);
            progress.dismiss();
        }
    }

    private void setListViewContent(boolean isSocial){
        ListView view = (ListView) findViewById(R.id.listView);
        List<String> list;
        if (isSocial) {
            list = social;
        } else {
            list = general;
        }
        for (int i = 0; i < list.size(); i++) {
            Log.d(TAG, i + " : " + list.get(i));
        }
        ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.activity_listview, list);
        view.setAdapter(adapter);
    }

    /**
     * This class contains all the methods to create social summary of a week
     */
    private class Social {

        private void displaySocial(){
            facebookSummary();
            twitterSummary();
        }

        private void facebookSummary(){
            if(MainActivity.checkPermissionsFB()){ // gets the Denied and Granted permissions according to the access token
                AccessToken facebookAccessToken = SettingsActivity.accessToken;
                if (facebookAccessToken == null){
                    facebookAccessToken = AccessToken.getCurrentAccessToken();
                }

                //Callback method sent with request
                GraphRequest.Callback callback = new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        if (response != null) {
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
            try {
                JSONObject postsObject = (JSONObject) jsonObject.get(jsonObject.names().getString(0));
                JSONArray postsArray = (JSONArray) postsObject.get(postsObject.names().getString(0));
                loopThroughResponse(postsArray, "message", "created_time","You posted: ");

                JSONObject likesObject = (JSONObject) jsonObject.get(jsonObject.names().getString(1));
                JSONArray likesArray = (JSONArray) likesObject.get(likesObject.names().getString(0));
                loopThroughResponse(likesArray, "name", "created_time", "You liked: ");

                JSONObject eventsObject = (JSONObject) jsonObject.get(jsonObject.names().getString(2));
                JSONArray eventsArray = (JSONArray) eventsObject.get(eventsObject.names().getString(0));
                loopThroughResponse(eventsArray, "name", "start_time", "You interacted with event: ");

                setListViewContent(true);
            } catch (JSONException e){
                Log.d(TAG,"Error: JSON Exception");
            } //TODO add error response
        }

        private void loopThroughResponse(JSONArray array, String dataType, String timeName, String output){
            try {
                for (int j = 0; j < array.length(); j++) { // loops through each element in the array
                    JSONObject obj = (JSONObject) array.get(j);
                    Object value = obj.get(dataType); //gets the parameter according to the data type
                    Object time = obj.get(timeName);

                    Date parsed;
                    try {
                        parsed = MainActivity.facebookDateFormat.parse(time.toString());
                        if (isDateInWeek(parsed)) {
                            Log.d(TAG,"True for string " + value.toString());
                            if (doesStringContainKeyword(value.toString())){
                                Log.d(TAG, "Added string: " + output + value);
                                social.add(output + value.toString());
                            }
                        }
                    }
                    catch(ParseException pe) {
                        throw new IllegalArgumentException(pe);
                    }
                }
            } catch (JSONException e){
                Log.d(TAG,"Error: JSON Exception");
            } //TODO add error response
        }

        private boolean doesStringContainKeyword(String value){
            for (String string : NUTRITIONKEYWORDS) { // loops through target keywords
                if (value.contains(string)) { // checks if the target string is contained within the current object string
                    return true;
                }
            }
            return false;
        }

        private boolean isDateInWeek(Date parsed){
            return MainActivity.startOfWeek.before(parsed) && MainActivity.endOfWeek.after(parsed);
        }

        private void twitterSummary(){
            TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
            displayFavouritedTweets(twitterApiClient);
            displayStatusTweets(twitterApiClient);
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
                        Date parsed;
                        try {
                            parsed = MainActivity.twitterDateFormat.parse(data.get(i).createdAt);
                            if (isDateInWeek(parsed)) {
                                Log.d(TAG,"True for string " + data.get(i).toString());
                                if (doesStringContainKeyword(data.get(i).text)){
                                    Log.d(TAG, "Added Tweet: " + data.get(i));
                                    social.add("You interacted with tweet: " + data.get(i).text);
                                }
                            }
                        }
                        catch(ParseException pe) {
                            throw new IllegalArgumentException(pe);
                        }
                    }
                    setListViewContent(true);
                }

                public void failure(TwitterException exception) {
                    //TODO: Add an error
                    Log.d(TAG, "Didn't get the results");
                }
            };
        }
    }

    private class General{

        private void displayGeneral(){
            displayLastWeeksData();
        }

        private void displayLastWeeksData(){

            // The read requests made to the list of datatypes
            DataReadRequest readRequest = queryData(MainActivity.startOfWeek.getTime(), MainActivity.endOfWeek.getTime(), DATATYPES);
            DataReadResult dataReadResult = Fitness.HistoryApi.readData(MainActivity.mGoogleApiClient, readRequest).await(1, TimeUnit.MINUTES);

            if (dataReadResult.getBuckets().size() > 0) { //Used for aggregated data
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

        private DataReadRequest queryData(long startTime, long endTime, List<DataType> types) {

            DataReadRequest.Builder builder = new DataReadRequest.Builder();
            for (DataType dt : types) {
                builder.read(dt);
            }
            return builder.setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS).build();
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
                for(Field field : dp.getDataType().getFields()) {
                    Log.d(TAG, "\tField: " + field.getName() +
                            " Value: " + dp.getValue(field));
                    if (field.getName().equals("calories")){
                        general.add(field.getName() + " expended are " + dp.getValue(field) + ".");
                    }
                }
            }
        }
    }
}
