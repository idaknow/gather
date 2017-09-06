package part4project.uoa.gather;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Ida on 1/08/2017.
 * This is a helper class that provides methods for MainActivity and other activities to use
 * These are to do with collecting data and are not specific to a social or general type
 */

class DataCollection {

    /**
     * This loops through each list and outputs to main activity a string depending on enabled & disabled fitness or social for each day
     * @param list1 : The first list to loop through
     * @param list2 : The second list to loop through
     * @return : The boolean array where each day is represented by an array field
     */
    static boolean[] getWeeksData(List<Data> list1, List<Data> list2){
        boolean[] booleanArray = new boolean[7];

        booleanArray = loopThroughList(list1, booleanArray);
        booleanArray = loopThroughList(list2, booleanArray);

        return booleanArray;
    }

    /**
     * This loops through the list and sets the appropriate boolean array index to true
     * @param list : The list to loop through
     * @param array : The current array
     * @return : The new array
     */
    private static boolean[] loopThroughList(List<Data> list, boolean[] array){
        if (list != null) { // Checks isn't empty/ uninitialised
            for (int i = 0; i < list.size(); i++){ // loops through each index
                // Gets the difference in days (to get the index)
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("NZ"));
                cal.setTime(list.get(i).getCreatedAt());
                int days = getDiffDate(MainActivity.endOfWeek.getTime(), cal.getTime().getTime(), false);
                // Sets the array index as true
                array[days] = true;
            }
        }
        return array;
    }

    /**
     * returns the difference in number of days between two dates
     * @param time1 : milliseconds of time 1
     * @param time2 : milliseconds of time 2
     * @param isCeil : This is true/ false depending on whether the value must be rounded up or down
     *               it must be rounded down for queries on data objects, but rounded up for queries on calendar view dates compared to today
     * @return : how many days are in between those two dates
     */
    static int getDiffDate(long time1, long time2, boolean isCeil){
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("NZ"));
        cal1.setTimeInMillis(time1);
        int day1 = cal1.get(Calendar.DAY_OF_YEAR); // TODO: Check documentation of day of the year
        Calendar cal2 = Calendar.getInstance(TimeZone.getTimeZone("NZ"));
        cal2.setTimeInMillis(time2);
        int day2 = cal2.get(Calendar.DAY_OF_YEAR); // TODO: Check documentation of day of the year

        int days = Math.abs(day1 - day2);
        if (cal1.get(Calendar.YEAR) != cal2.get(Calendar.YEAR)){ // (Year after) Jan -> Dec (Year below)
            Calendar temp = Calendar.getInstance(TimeZone.getTimeZone("NZ"));
            temp.set(cal1.get(Calendar.YEAR),Calendar.DECEMBER, 31); // Set to last day of december to get number of days in the year
            // TODO: Might be 30 if day 1 = 0
            int daysTillEndOfYear = temp.get(Calendar.DAY_OF_YEAR) - day1; // number of days till the end of the year
            days = day2 + daysTillEndOfYear;
        }

        return days;
    }

    /**
     * This method returns a list that can be printed
     * @param isNutrition : T = nutrition, F = fitness
     * @param isSocial : T = Social, F = General
     * @return : String list of what to print on the screen
     */
    static List<String> getListToShow(boolean isNutrition, boolean isSocial){

        // Gets data list to use depending on whether it is nutrition, general, social and/or fitness
        List<Data> list = getListToUse(isNutrition, isSocial);

        // Loops through and gets the text
        List<String> listToPrint = new LinkedList<>();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("NZ"));
                cal.setTime(list.get(i).getCreatedAt());
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int month = cal.get(Calendar.MONTH);
                listToPrint.add(day + "/" + month + " | " + list.get(i).getType().toString() + list.get(i).getValue());
            }

            if (listToPrint.size() == 0){
                String dataType = "general";
                if (isSocial){
                    dataType = "social";
                }
                listToPrint.add("There is nothing to show, as you have not done any "+dataType+" activity this week.");
            }
        }

        return listToPrint;
    }

    /**
     * Gets the appropriate list to use depending on whether social/ general and nutrition/fitness
     * @param isNutrition : T = nutrition, F = fitness
     * @param isSocial : T = Social, F = General
     * @return : the list to use
     */
    private static List<Data> getListToUse(boolean isNutrition, boolean isSocial){
        if (isNutrition){
            if (isSocial){
                return MainActivity.nutritionSocial;
            } else {
                return MainActivity.nutritionGeneral;
            }
        } else {
            if (isSocial){
                return MainActivity.fitnessSocial;
            } else {
                return MainActivity.fitnessGeneral;
            }
        }
    }
}
