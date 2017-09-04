package part4project.uoa.gather;

import android.util.Log;

import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.DataReadRequest;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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

        Log.d("Time", MainActivity.startOfWeek.toString());
        Log.d("Time", MainActivity.today.toString());
        return builder.setTimeRange(MainActivity.startOfWeek.getTime(), MainActivity.today.getTime(), TimeUnit.MILLISECONDS).build();
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

    static String generalGetDateOnly(String formattedDate){
        String newDate;
        try {
            Date date = originalFormat.parse(formattedDate);
            newDate = targetFormat.format(date);

        } catch(ParseException pe) {
            throw new IllegalArgumentException(pe);
        }
        return newDate;
    }

    static String plusOneDay(String original){
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
}
