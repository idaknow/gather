package part4project.uoa.gather;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ToggleButton;


import java.util.List;


public class NutritionSummaryActivity extends AppCompatActivity {

    //Logging Data tags
//    private static final String TAG = "Nutrition";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition_summary);
        // Set toggle on click listener
        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setListViewContent(((ToggleButton) v).isChecked());
            }
        });
        // Initially displays the information for the default value
        setListViewContent(toggle.isChecked());
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
        // Sets the UI as the created list of strings
        ArrayAdapter adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.activity_listview, list);
        ListView view = (ListView) findViewById(R.id.listView);
        view.setAdapter(adapter);
    }
}
