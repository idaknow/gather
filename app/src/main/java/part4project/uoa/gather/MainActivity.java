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
    private static final List<String> KEYWORDS = Arrays.asList("Fitness","dance","run");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        if (accessToken != null) {
            GraphRequest req = new GraphRequest(
                    accessToken,
                    "/me/",
                    null,
                    HttpMethod.GET, new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    Log.d(TAG, "Successful completion of asynch call");
                    TextView facebook = (TextView) findViewById(R.id.social_media_app_summary);
                    String outputString = transformFacebookPosts(response.getJSONObject());
                    facebook.setText(outputString);
                }
            });
            Bundle parameters = new Bundle();
            parameters.putString("fields", "posts");
            req.setParameters(parameters);
            req.executeAsync();
        }
    }

    public String transformFacebookPosts(JSONObject jsonObject){
        Log.d(TAG, "JSON Object reponse in main activity: " + jsonObject.toString());
        int count = 0;
            try {
                JSONObject data = (JSONObject) jsonObject.get(jsonObject.names().getString(0));
                JSONArray values = (JSONArray) data.get(data.names().getString(0));


                for(int i = 0; i<values.length(); i++){
                    Log.d(TAG, "values = " + values.get(i) );//+ " value = " + values.getString(values.getString(i)));
                    JSONObject value = (JSONObject) values.get(i);
                    Object message = value.get("message");
                    Log.d(TAG, message.toString());
                    //TODO: search for key words in the message
                    for (String string : KEYWORDS){
                        if (message.toString().contains(string)){
                            count++;
                        }
                    }
                }
            } catch (JSONException e){} //TODO
        if (count == 0){
            return "You have not posted about fitness related things.";
        } else if (count == 1){
            return "You have posted about " + count + " fitness related thing.";
        }
        return "You have posted about " + count + " fitness related things.";
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
