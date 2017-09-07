package part4project.uoa.gather;

import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.DataReadRequest;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
    private static final DateFormat nutritionFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);


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
        return builder.setTimeRange(startOfWeek.getTime(), today.getTime(), TimeUnit.MILLISECONDS).build();
    }

    static Date generalGetDate(String time, boolean isNutrition){
        Date parsed;
        try {
            if (isNutrition){
                parsed = nutritionFormat.parse(time);
            } else{
                parsed = fitbitDateFormat.parse(time);
            }

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