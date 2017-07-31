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

    private static final SimpleDateFormat twitterDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
    private static final SimpleDateFormat facebookDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.ENGLISH);
    private static final List<String> FITNESSKEYWORDS = Arrays.asList("Fitness","dance","run", "active", "Rhythm");
    private static final List<String> NUTRITIONKEYWORDS = Arrays.asList("Nutrition","Vegetables", "Vegetarian", "Tasty", "Food", "bean", "Coffee", "water");


    static AccessToken getFBToken(){
        AccessToken facebookAccessToken = SettingsActivity.accessToken;
        if (facebookAccessToken == null){
            facebookAccessToken = AccessToken.getCurrentAccessToken();
        }
        return facebookAccessToken;
    }

    static JSONArray getArray(JSONObject jsonObject, int index){
        JSONObject object;
        JSONArray array;
        try {
            object = (JSONObject) jsonObject.get(jsonObject.names().getString(index));
            array = (JSONArray) object.get(object.names().getString(0));
        } catch (JSONException e){
            Log.d("SOCIAL", e.toString());
            return null;
        }
        return array;
    }

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
        return (grantedPermissions.contains("user_posts") && grantedPermissions.contains("user_likes") && grantedPermissions.contains("user_events"));
    }

    /**
     * This gets the access token and then returns the granted/ denied permissions according to it
     * @param wantGranted : true/ false depending on whether wants granted or denied permissions
     * @return List of permissions granted/ denied
     */
    static List<String> getFBPermissions(boolean wantGranted){
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

