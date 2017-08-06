package part4project.uoa.gather;

import android.util.Log;

import com.facebook.AccessToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Ida on 1/08/2017.
 * This is a helper class for Social tasks
 */

class SocialMethods {

    // This provides the date format for both twitter and facebook that can transform a string into a Date object
    private static final SimpleDateFormat twitterDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
    private static final SimpleDateFormat facebookDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);

    // These are the keywords to search for in your social media accounts
    private static final List<String> FITNESSKEYWORDS = Arrays.asList("Fitness","dance","run", "active", "Rhythm");
    private static final List<String> NUTRITIONKEYWORDS = Arrays.asList("Nutrition","Vegetables", "Vegetarian", "Tasty", "Food", "bean", "Coffee", "water");

    /**
     * This returns the Facebook Access Token
     * @return : The current fb access token
     */
    static AccessToken getFBToken(){
        AccessToken facebookAccessToken = SettingsActivity.accessToken;
        if (facebookAccessToken == null){
            facebookAccessToken = AccessToken.getCurrentAccessToken();
        }
        return facebookAccessToken;
    }

    /**
     * This returns the JSON array from a response JSON Object
     * @param jsonObject : The response JSON Object
     * @param name : The index name of the array within the object
     * @return : The JSON Array
     */
    static JSONArray getArray(JSONObject jsonObject, String name){
        JSONObject object;
        JSONArray array;
        try {
            object = (JSONObject) jsonObject.get(name);
            array = (JSONArray) object.get(object.names().getString(0));
        } catch (JSONException e){
            Log.d("SOCIAL", e.toString());
            return null;
        }
        return array;
    }

    /**
     * This is used with Facebook_Actions Fitness to get the JSON Array from a JSON Object
     * This is similar to the getArray() method, but does one less step
     * @param jsonObject : This is the JSON Object
     * @return the JSON Array
     */
    static JSONArray getFitnessArray(JSONObject jsonObject){
        JSONArray array;
        try {
            array = (JSONArray) jsonObject.get(jsonObject.names().getString(0));
        } catch (JSONException e){
            Log.d("SOCIAL", e.toString());
            return null;
        }
        return array;
    }

    /**
     * This uses the Date formatter to turn a Facebook or Twitter time to a java Date object
     * @param time : This is the time in a string format from Twitter or Facebook
     * @param isFacebook : True if the object is a facebook time, False if the object is a Twitter time
     * @return : The date object parsed using the date format of twitter or facebook
     */
    static Date getDate(String time, boolean isFacebook){
        Date parsed;
        try {
            if (isFacebook){
                parsed = facebookDateFormat.parse(time);
            } else {
                parsed = twitterDateFormat.parse(time);
            }
        } catch(ParseException pe) {
            throw new IllegalArgumentException(pe);
        }

        return parsed;
    }

    /**
     * This returns true or false depending on whether a social action contains a keyword
     * @param value : The social activity that needs to be searched in
     * @param isNutrition : True if you need to search through for nutrition keywords, False if you need to search through fitness keywords
     * @return True or False depending on whether a keyword is found within the given social activity
     */
    static boolean doesStringContainKeyword(String value, boolean isNutrition){
        List<String> list = FITNESSKEYWORDS;
        if (isNutrition){
            list = NUTRITIONKEYWORDS;
        }

        for (String string : list) { // loops through target keywords
            if (value.contains(string)) { // checks if the target string is contained within the current object string
                return true;
            }
        }
        return false;
    }

    /**
    * Checks if user posts, user likes and user events permissions are granted
    * @return true or false whether all 3 permissions are granted
    */
    static boolean checkPermissionsFB(){
        List<String> grantedPermissions = getFBPermissions(true); // gets all granted permissions
        if (grantedPermissions != null){
            return grantedPermissions.contains("user_posts") || grantedPermissions.contains("user_likes") || grantedPermissions.contains("user_events");
        }
        return false;
    }

    /**
     * This gets the access token and then returns the granted/ denied permissions according to it
     * @param wantGranted : true/ false depending on whether wants granted or denied permissions
     * @return List of permissions granted/ denied
     */
    private static List<String> getFBPermissions(boolean wantGranted){
        List<String> list; // the returned list
        list = new LinkedList<>();
        if (SettingsActivity.accessToken == null){ // settingsActivity has been created
                AccessToken facebookAccessToken = AccessToken.getCurrentAccessToken();
                if (facebookAccessToken != null) {
                        list = new LinkedList<>(facebookAccessToken.getPermissions());
                        if (!wantGranted) {
                                list = new LinkedList<>(facebookAccessToken.getDeclinedPermissions());
                            }
                    }
            } else {
                list = SettingsActivity.grantedFBPermissions;
                if (!wantGranted) {
                        list = SettingsActivity.deniedFBPermissions;
                    }
            }
        return list;
    }
}

