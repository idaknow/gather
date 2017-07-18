package part4project.uoa.gather;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphRequestBatch;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Facebook"; // log Tag
    private static final List<String> KEYWORDS = Arrays.asList("Fitness","dance","run", "Vegetarian"); //TODO: Change to be more extensive depending on words we want to search for
    private static int facebookFitnessCount = 0; // The count of how many facebook user_action.fitness the user has done

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // FACEBOOK Integration: gets the facebook access token and applies to it get update the main activities summary
        AccessToken facebookAccessToken = SettingsActivity.accessToken;
        TextView facebook = (TextView) findViewById(R.id.social_media_app_summary);
        if (facebookAccessToken != null) {
            facebook.setText("Loading...");
            facebookFitnessCount = 0; // reset to default
            if (!facebookSummary()){
                facebook.setText("Unable to retrieve data as required permissions are disabled. Enable facebook permissions in Settings.");
            }
        } else {
            facebook.setText("Unable to retrieve data as you are not logged in. Enable facebook in Settings.");
        }
    }

    /**
     * This method is called to update the summary on Facebook if there exists an Access Token for Facebook
     */
    public boolean facebookSummary(){

        Set<String> grantedPermissions = getFBGrantedPermissions(); // gets all granted permissions
        Set<String> deniedPermissions = getFBDeniedPermissions();
        String requestedData = "";

        //TODO: error checking if certain permissions aren't granted (accesses certain values in array accordingly - currently assumes gets all
        if (!deniedPermissions.contains("user_posts") && !deniedPermissions.contains("user_events") && !deniedPermissions.contains("user_likes") && grantedPermissions.contains("user_posts") && grantedPermissions.contains("user_likes") && grantedPermissions.contains("user_events")){
            requestedData+="posts,likes,events";
            GraphRequest req = new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/me/",
                    null,
                    HttpMethod.GET, new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    if (response != null) {
                        Log.d(TAG, "Successful completion of asynch call");
                        TextView facebook = (TextView) findViewById(R.id.social_media_app_summary);
                        String outputString = transformFacebookPostsEventsLikes(response.getJSONObject()); // this uses user_likes, user_posts and user_events
                        Log.d(TAG, "output : " + response.getJSONObject().toString());
                        facebook.setText(outputString);
                        if (getFBGrantedPermissions().contains("user_actions.fitness")){
                            transformFacebookFitness(); // this uses user_actions.fitness
                        }
                    }
                }
            });
            Bundle parameters = new Bundle();
            parameters.putString("fields", requestedData); // adds requested permissions
            req.setParameters(parameters);
            req.executeAsync();
            return true;
        }
        return false;
    }

    public Set<String> getFBGrantedPermissions(){
        AccessToken facebookAccessToken = AccessToken.getCurrentAccessToken();
        Set<String> grantedPermissions = facebookAccessToken.getPermissions();
        return grantedPermissions;
    }

    public Set<String> getFBDeniedPermissions(){
        AccessToken facebookAccessToken = AccessToken.getCurrentAccessToken();
        Set<String> deniedPermissions = facebookAccessToken.getDeclinedPermissions();
        return deniedPermissions;
    }

    /**
     * This gets the user's data from fitness actions and counts them up
     * fitness actions include: bikes, walks & runs
     * Adds up the amount of data to a global variable
     */
    public void transformFacebookFitness(){
        AccessToken facebookAccessToken = AccessToken.getCurrentAccessToken();
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
                    outputString += "You have completed " + facebookFitnessCount + " facebook fitness action.";
                } else {
                    outputString += "You have completed " + facebookFitnessCount + " facebook fitness actions.";
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
        } catch (JSONException e){} //TODO: Error handling
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
            } catch (JSONException e){} //TODO add error response

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
        } catch (JSONException e){} //TODO add error response
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
}
