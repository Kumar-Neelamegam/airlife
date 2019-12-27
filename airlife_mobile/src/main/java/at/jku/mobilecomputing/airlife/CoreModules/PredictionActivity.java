package at.jku.mobilecomputing.airlife.CoreModules;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import java.util.ArrayList;
import java.util.List;

import at.jku.mobilecomputing.airlife.Constants.Common;
import at.jku.mobilecomputing.airlife.Database.AqiData.AqiDataSet;
import at.jku.mobilecomputing.airlife.Database.FavData.FavouriteListDataSet;
import at.jku.mobilecomputing.airlife.R;
import at.jku.mobilecomputing.airlife.Utilities.AsynkTaskCustom;
import at.jku.mobilecomputing.airlife.Utilities.SharedPrefUtils;
import at.jku.mobilecomputing.airlife.Utilities.onWriteCode;

public class PredictionActivity extends AppCompatActivity {

    private SharedPrefUtils sharedPrefUtils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);

        Init();
        getAllDataSet();

    }

    private void getAllDataSet() {
        AsynkTaskCustom asynkTaskCustom = new AsynkTaskCustom(PredictionActivity.this, "Please wait...Loading...");
        asynkTaskCustom.execute(new onWriteCode<List<AqiDataSet>>() {
            @Override
            public List<AqiDataSet> onExecuteCode() {
                List<AqiDataSet> favouriteListObjects = new ArrayList<>();
                favouriteListObjects = Common.getAllAQIDataSet(PredictionActivity.this);
                return favouriteListObjects;
            }

            @Override
            public List<AqiDataSet> onSuccess(List<AqiDataSet> result) {
                //Toast.makeText(PredictionActivity.this, result.toString(), Toast.LENGTH_SHORT).show();

                return result;
            }
        });
    }

    private void Init()
    {
        setUPTheme();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Air Life - Prediction");
    }

    public void setUPTheme() {
        sharedPrefUtils = SharedPrefUtils.getInstance(this);
        if (sharedPrefUtils.isDarkMode()){
            setTheme(R.style.AppTheme_Dark);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorthemeDarkPrimary)));
        }
        else {
            setTheme(R.style.AppTheme_Light);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorthemeLightPrimary)));
        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

