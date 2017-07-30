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


public class NutritionSummaryActivity extends AppCompatActivity {

    //Logging Data tags
    private static final String TAG = "Nutrition";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition_summary);
        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        toggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayData((ToggleButton) v);
            }
        });
        displayData(toggle);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        displayData(toggle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        displayData(toggle);
    }

    private void displayData(ToggleButton toggle){
        if (toggle.isChecked()){
            Log.d(TAG, "Toggle Button is checked");
            setListViewContent(true);
        } else {
            Log.d(TAG, "Toggle Button is NOT checked");
            setListViewContent(false);
        }
    }

    public void intentMainActivity(View view) {
        startActivity(new Intent(this,MainActivity.class));
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void setListViewContent(boolean isSocial){
        ListView view = (ListView) findViewById(R.id.listView);
        List<String> list;
        if (isSocial) {
            list = MainActivity.nutritionSocial;
        } else {
            list = MainActivity.nutritionGeneral;
        }
        for (int i = 0; i < list.size(); i++) {
            Log.d(TAG, i + " : " + list.get(i));
        }
        ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.activity_listview, list);
        view.setAdapter(adapter);
    }
}
