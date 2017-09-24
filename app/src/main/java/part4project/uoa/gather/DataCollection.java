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
                int month = cal.get(Calendar.MONTH) + 1;
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
