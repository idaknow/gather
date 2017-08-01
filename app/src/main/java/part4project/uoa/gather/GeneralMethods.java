package part4project.uoa.gather;

import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.DataReadRequest;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ida on 1/08/2017.
 * This class contains helper methods for General Tasks
 */

class GeneralMethods {

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
        return builder.setTimeRange(MainActivity.startOfWeek.getTime(), MainActivity.endOfWeek.getTime(), TimeUnit.MILLISECONDS).build();
    }

}
