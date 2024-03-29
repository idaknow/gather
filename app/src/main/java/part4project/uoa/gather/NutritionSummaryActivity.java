package part4project.uoa.gather;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.List;

import static part4project.uoa.gather.R.layout.activity_listview;

public class NutritionSummaryActivity extends AppCompatActivity {

    private static final String[] items = new String[]{"General","Social"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition_summary);

        Spinner dropdown = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        setListViewContent(false);
                        break;
                    case 1:
                        setListViewContent(true);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

    }



    /**
     * This is used when the back button is clicked
     * This begins the main activity and uses the appropriate animation
     * @param view : The provided view
     */
    public void intentMainActivity(View view) {
        startActivity(new Intent(this,MainActivity.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    /**
     * This method sets the UI to the appropriate list
     * @param isSocial : True if the list must be social, False if the list must be general
     */
    private void setListViewContent(boolean isSocial){
        // Gets the appropriate data list depending on whether social or general is enabled
        List<String> list = DataCollection.getListToShow(true,isSocial);
        ListView view = (ListView) findViewById(R.id.listView);
        ArrayAdapter adapter = new ArrayAdapter<>(getApplicationContext(), activity_listview, list); // Sets the UI as the created list of strings
        view.setAdapter(adapter);
    }
}
