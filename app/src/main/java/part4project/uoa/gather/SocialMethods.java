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
 */

class SocialMethods {

    public static final SimpleDateFormat twitterDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
    public static final SimpleDateFormat facebookDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
    public static final List<String> FITNESSKEYWORDS = Arrays.asList("Fitness","dance","run", "active", "Rhythm");
    public static final List<String> NUTRITIONKEYWORDS = Arrays.asList("Nutrition","Vegetables", "Vegetarian", "Tasty", "Food", "bean", "Coffee", "water");


    public static AccessToken getFBToken(){
        AccessToken facebookAccessToken = SettingsActivity.accessToken;
        if (facebookAccessToken == null){
            facebookAccessToken = AccessToken.getCurrentAccessToken();
        }
        return facebookAccessToken;
    }

    public static JSONArray getArray(JSONObject jsonObject, int index){
        JSONObject object;
        JSONArray array = new JSONArray();
        try {
            object = (JSONObject) jsonObject.get(jsonObject.names().getString(index));
            array = (JSONArray) object.get(object.names().getString(0));
        } catch (JSONException e){
            Log.d("SOCIAL", e.toString());
            return null;
        }
        return array;
    }

    public static JSONArray getFitnessArray(JSONObject jsonObject){
        JSONArray array = new JSONArray();
        try {
            array = (JSONArray) jsonObject.get(jsonObject.names().getString(0));
        } catch (JSONException e){
            Log.d("SOCIAL", e.toString());
            return null;
        }
        return array;
    }

    public static JSONObject getJsonObject(JSONArray array, int index){
        JSONObject obj;
        try {
            obj = array.getJSONObject(index);
        } catch (JSONException e) {
            return null;
        }
        return obj;
    }

    public static Date getDate(String time, boolean isFacebook){
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

    public static boolean doesStringContainKeyword(String value, boolean isNutrition){
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
    public static boolean checkPermissionsFB(){
        List<String> grantedPermissions = getFBPermissions(true); // gets all granted permissions
        return (grantedPermissions.contains("user_posts") && grantedPermissions.contains("user_likes") && grantedPermissions.contains("user_events"));
    }

    /**
     * This gets the access token and then returns the granted/ denied permissions according to it
     * @param wantGranted : true/ false depending on whether wants granted or denied permissions
     * @return List of permissions granted/ denied
     */
    public static List<String> getFBPermissions(boolean wantGranted){
        List<String> list = new LinkedList<>(); // the returned list
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

