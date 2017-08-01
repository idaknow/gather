package part4project.uoa.gather;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ToggleButton;

import java.util.List;

public class FitnessSummaryActivity extends AppCompatActivity {

    //TESTING: Log data
    private static final String TAG = "Fitness";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitness_summary);
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
    }

    /**
     * This method sets the UI to the appropriate list
     * @param isSocial : True if the list must be social, False if the list must be general
     */
    private void setListViewContent(boolean isSocial){
        List<String> list = DataCollection.getListToShow(false,isSocial);
        ListView view = (ListView) findViewById(R.id.listView);
        ArrayAdapter adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.activity_listview, list);
        view.setAdapter(adapter);
    }

}
