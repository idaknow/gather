package part4project.uoa.gather;

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

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;

public class NutritionSummaryActivity extends AppCompatActivity {

    //Logging Data tags
    private static final String TAG = "Nutrition";

    private static List<String> social;
    private static List<String> general;

    public static final List<String> NUTRITIONKEYWORDS = Arrays.asList("Nutrition","Vegetables", "Vegetarian", "Tasty", "Food", "bean", "Coffee", "water");

    TwitterApiClient twitterApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition_summary);
        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayData((ToggleButton)v);
            }
        });
        displayData(toggle);
    }

    private void displayData(ToggleButton toggle){
        if (toggle.isChecked()){
            Log.d(TAG, "Toggle Button is checked");
            new SocialTask().execute();
        } else {
            Log.d(TAG, "Toggle Button is NOT checked");
            new GeneralTask().execute();
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
            facebookSummary();
            displayTweets();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private class GeneralTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) { // called on a seperate thread
            // TODO
            general = new LinkedList<>();
            return null;
        }
    }

    public void facebookSummary(){
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

                setListViewContent();
        } catch (JSONException e){
            Log.d(TAG,"Error: JSON Exception");
        } //TODO add error response
    }

    private void setListViewContent(){
        for (int i = 0; i < social.size(); i++){
            Log.d(TAG, i  + " : "+ social.get(i));
        }
        ListView view = (ListView) findViewById(R.id.listView);
        ArrayAdapter adapter = new ArrayAdapter<String>(this, R.layout.activity_listview, social);
        view.setAdapter(adapter);
    }

    public void loopThroughResponse(JSONArray array, String dataType, String timeName, String output){
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
        call.enqueue(getTwitterCallback());
    }

    /**
     * This method displays all the user's statuses
     */
    private void displayStatusTweets(){
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
                setListViewContent();
            }

            public void failure(TwitterException exception) {
                //TODO: Add an error
                Log.d(TAG, "Didn't get the results");
            }
        };
    }

}
