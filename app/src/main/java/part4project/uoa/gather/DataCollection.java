package part4project.uoa.gather;

import android.util.Log;

import java.util.LinkedList;
import java.util.List;

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
            Log.d("Weeks","Nutrition general" + list.size());
            for (int i = 0; i < list.size(); i++){ // loops through each index
                // Gets the difference in days (to get the index)
                int days = getDiffDate(MainActivity.endOfWeek.getTime(), list.get(i).getCreatedAt().getTime());
                Log.d("Weeks","days diff = " + days);
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
     * @return : how many days are in between those two dates
     */
    static int getDiffDate(long time1, long time2){
        long diff = time1 - time2;
        double days = Math.abs(Math.ceil( (double) diff / (86400000)));
        return (int) days;
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
                listToPrint.add(list.get(i).getType().toString() + list.get(i).getValue());
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
