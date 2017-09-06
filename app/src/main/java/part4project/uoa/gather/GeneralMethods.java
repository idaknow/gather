package part4project.uoa.gather;

import android.util.Log;

import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.DataReadRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import static part4project.uoa.gather.MainActivity.mainPreferences;
import static part4project.uoa.gather.MainActivity.startOfWeek;
import static part4project.uoa.gather.MainActivity.today;

/**
 * Created by Ida on 1/08/2017.
 * This class contains helper methods for General Tasks
 */

class GeneralMethods {

    private static final SimpleDateFormat fitbitDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
    private static final DateFormat originalFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
    private static final DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    /**
     * This method builds a Data Read Request used by GoogleFit
     * @param isNutrition : Uses the appropriate datatypes depending on whether its nutrition or fitness
     * @return : the built data query
     */
    static DataReadRequest queryData(boolean isNutrition) {
        List<DataType> types = MainActivity.FITNESSDATATYPES;
        if (isNutrition){
            types = MainActivity.NUTRITIONDATATYPES;
        }

        DataReadRequest.Builder builder = new DataReadRequest.Builder();
        for (DataType dt : types) {
            builder.read(dt);
        }

        Log.d("Time", startOfWeek.toString());
        Log.d("Time", today.toString());
        return builder.setTimeRange(startOfWeek.getTime(), today.getTime(), TimeUnit.MILLISECONDS).build();
    }

    static Date generalGetDate(String time){
        Date parsed;
        try {
            parsed = fitbitDateFormat.parse(time);

        } catch(ParseException pe) {
            throw new IllegalArgumentException(pe);
        }
        return parsed;
    }

    private static String generalGetDateOnly(String formattedDate){
        String newDate;
        try {
            Date date = originalFormat.parse(formattedDate);
            newDate = targetFormat.format(date);

        } catch(ParseException pe) {
            throw new IllegalArgumentException(pe);
        }
        return newDate;
    }

    private static String plusOneDay(String original){
        String newDate = "";

        Calendar c = Calendar.getInstance();
        try {
            c.setTime(targetFormat.parse(original));
            c.add(Calendar.DATE, 1);
            newDate = targetFormat.format(c.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newDate;
    }

    static StringBuffer makeFitbitAPIRequest(String requestUrl) {
        StringBuffer response = new StringBuffer();
        URL url;
        try {
            url = new URL(requestUrl);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setReadTimeout(10000);//this is in milliseconds
            conn.setConnectTimeout(15000);//this is in milliseconds
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.addRequestProperty("Authorization", "Bearer " + mainPreferences.getString("access_token", null));

            int responseCode = conn.getResponseCode();

            //Check to make sure that the connection has been made successfully before trying to
            //read data.
            if (responseCode == 200) {

                //Read the input received
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                Log.d("general", "reader input: " + in.readLine());
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                    Log.d("fitbit", "general method: " + response);
                }
                in.close();
            } else if (responseCode == 401){
                response.append("expired");
            } else {
                response.append("error");
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("fitbit", "general method return: " + response);
        return response;
    }

    static ArrayList<String> getWeekDates(){

        ArrayList<String> daysToAdd = new ArrayList<>();

        //Get todays date as a string and in the format yyyy-mm-dd.
        String todaysDate = DateFormat.getDateInstance().format(today);
        String today = generalGetDateOnly(todaysDate);

        //Get the date of the start of the week as a string and in the correct format.
        String formattedDate = DateFormat.getDateInstance().format(startOfWeek);
        String currentDate = generalGetDateOnly(formattedDate);

        //Add the start of the week date to the array
        daysToAdd.add(currentDate);

        //Loop through and add each date to the array up until today's date
        while (!currentDate.equals(today)){
            currentDate = plusOneDay(currentDate);
            daysToAdd.add(currentDate);
        }

        return daysToAdd;
    }

}
