package part4project.uoa.gather;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ida on 31/08/2017.
 * Modified from : https://stackoverflow.com/questions/38415680/how-to-parse-csv-file-into-an-array-in-android-studio
 */
class CSVFile {

    private InputStream inputStream;

    CSVFile(InputStream inputStream){
        this.inputStream = inputStream;
    }

    List<String> read(){
        ArrayList<String> resultList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            String csvLine;
            while ((csvLine = reader.readLine()) != null) {
                resultList.add(csvLine);
            }
        }
        catch (IOException ex) {
            throw new RuntimeException("Error in reading CSV file: "+ex);
        }
        finally {
            try {
                inputStream.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultList;
    }
}
