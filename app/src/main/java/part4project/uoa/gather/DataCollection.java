package part4project.uoa.gather;

import android.util.Log;

import java.util.List;

/**
 * Created by Ida on 1/08/2017.
 */

class DataCollection {

    /**
     * This loops through each list
     */
    static String getWeeksData(List<Data> nutritionGeneral, List<Data> nutritionSocial, List<Data> fitnessGeneral, List<Data> fitnessSocial ){
        boolean[] isNutrition = new boolean[7];
        boolean[] isFitness = new boolean[7];

        isNutrition = loopThroughList(nutritionGeneral, isNutrition);
        isNutrition = loopThroughList(nutritionSocial, isNutrition);
        isFitness = loopThroughList(fitnessGeneral, isFitness);
        isFitness = loopThroughList(fitnessSocial, isFitness);

        String outputString = "Fitness: ";
        for (boolean truth : isFitness) {
            outputString += truth + " ";
        }
        outputString += "\n\nNutrition: ";
        for (boolean truth : isNutrition){
            outputString += truth + " ";
        }

        return outputString;
    }

    private static boolean[] loopThroughList(List<Data> list, boolean[] array){
        if (list != null) {
            Log.d("Weeks","Nutrition general" + list.size());
            for (int i = 0; i < list.size(); i++){
                long diff = MainActivity.endOfWeek.getTime() - list.get(i).getCreatedAt().getTime();
                long days = Math.abs(diff / (86400000));
                Log.d("Weeks","days diff = " + days);
                array[(int)days] = true;
            }
        }
        return array;
    }
}
