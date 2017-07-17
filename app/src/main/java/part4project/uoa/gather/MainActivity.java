package part4project.uoa.gather;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.ViewGroup;
import android.util.Log;
import android.view.ViewManager;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Facebook"; // log Tag
    private static final List<String> KEYWORDS = Arrays.asList("Fitness","dance","run", "Vegetarian");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // gets the facebook access token and applies to it get update the main activities summary
        AccessToken facebookAccessToken = AccessToken.getCurrentAccessToken();
        if (facebookAccessToken != null) {
            facebookSummary(facebookAccessToken);
        }
    }

    /**
     * This method is called to update the summary on Facebook if there exists an Access Token for Facebook
     * @param facebookAccessToken : the valid access token
     */
    public void facebookSummary(AccessToken facebookAccessToken){

        Set<String> grantedPermissions = facebookAccessToken.getPermissions(); // gets all granted permissions
        String requestedData = "";
        //TODO: error checking if certain permissions aren't granted (accesses certain values in array accordingly
        // TODO: currently assumes gets all
        if (grantedPermissions.contains("user_posts") && grantedPermissions.contains("user_likes") && grantedPermissions.contains("user_events")){
            requestedData+="posts,likes,events";

            GraphRequest req = new GraphRequest(
                    facebookAccessToken,
                    "/me/",
                    null,
                    HttpMethod.GET, new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    Log.d(TAG, "Successful completion of asynch call");
                    TextView facebook = (TextView) findViewById(R.id.social_media_app_summary);
                    String outputString = transformFacebookPosts(response.getJSONObject());
                    Log.d(TAG, "output : " + response.getJSONObject().toString());
                    facebook.setText(outputString);
                }
            });
            Bundle parameters = new Bundle();
            parameters.putString("fields", requestedData);
            req.setParameters(parameters);
            req.executeAsync();
        }
    }

    /**
     * This takes the input jsonObject which is the reponse from the GraphAPI request
     * It then transforms this data into a useful summary text string to return to the user
     * It currently just calculates the amount of times the user includes keywords, such as "fitness" in their fb actions such as posts
     * @param jsonObject : The response object from the Graph API successful request
     * @return String : the string to output on the summary page
     */
    public String transformFacebookPosts(JSONObject jsonObject){
        // array values
        int posts = 0;
        int likes = 1;
        int events = 2;

        int count = 0; // the number of times something fitness related is liked/ posted about
        Log.d(TAG, "JSON Object reponse in main activity: " + jsonObject.toString()); //TESTING

            try { // catch JSON exception

                // gets user's POST data
                JSONObject postsObject = (JSONObject) jsonObject.get(jsonObject.names().getString(posts));
                Log.d(TAG, "posts object = " + postsObject.toString());
                JSONArray postsArray = (JSONArray) postsObject.get(postsObject.names().getString(0));
                Log.d(TAG, "posts array = " + postsArray.toString());
                count+=loopThroughResponse(postsArray,"message"); // adds to count the number of times keywords are used in posts

                // gets  user's LIKES data
                JSONObject likesObject = (JSONObject) jsonObject.get(jsonObject.names().getString(likes));
                Log.d(TAG, "likes object = " + likesObject.toString());
                JSONArray likesArray = (JSONArray) likesObject.get(likesObject.names().getString(0));
                Log.d(TAG, "likes array length " + likesArray.length() + " with values = " + likesArray.toString());
                count+=loopThroughResponse(likesArray,"name"); // adds to count the number of times keywords are used in likes

                // gets  user's EVENTS data
                JSONObject eventsObject = (JSONObject) jsonObject.get(jsonObject.names().getString(events));
                Log.d(TAG, "events object = " + eventsObject.toString());
                JSONArray eventsArray = (JSONArray) eventsObject.get(eventsObject.names().getString(0));
                Log.d(TAG, "events array length " + eventsArray.length() + " with values = " + eventsArray.toString());
                count+=loopThroughResponse(eventsArray,"description"); // adds to count the number of times keywords are used in event descriptions

            } catch (JSONException e){} //TODO add error response

        // format string responses plurals accordingly to the output count
        if (count == 0){
            return "You have not posted or liked posts about fitness related things.";
        } else if (count == 1){
            return "You have posted or liked about " + count + " fitness related thing.";
        }
        return "You have posted or liked about " + count + " fitness related things.";
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
