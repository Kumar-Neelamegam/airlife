package at.jku.mobilecomputing.airlife.CoreModules;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import at.jku.mobilecomputing.airlife.Constants.Common;
import at.jku.mobilecomputing.airlife.Database.AqiData.AqiDataSet;
import at.jku.mobilecomputing.airlife.R;
import at.jku.mobilecomputing.airlife.Utilities.AsynkTaskCustom;
import at.jku.mobilecomputing.airlife.Utilities.SharedPrefUtils;
import at.jku.mobilecomputing.airlife.Utilities.onWriteCode;
import at.jku.mobilecomputing.machinelearning.Prediction;

public class PredictionActivity extends AppCompatActivity {

    private SharedPrefUtils sharedPrefUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);

        Init();
        getAllDataSet();


    }

    private void callMachineLearning(List<AqiDataSet> aqiDataSets) {


        // show samples
        StringBuilder sb = new StringBuilder("Samples:\n");
        for (AqiDataSet aqiDataSet : aqiDataSets) {
            sb.append(aqiDataSet.getAirquality() + "\n");
        }


        createModelARFF(aqiDataSets);



    }

    private void createModelARFF(List<AqiDataSet> aqiDataSets) {

        String newLine="\n";
        String lineSeparator=",";
        String lineQuotes="\"";
        StringBuilder stringBuilder=new StringBuilder();

        stringBuilder.append("% AirLife");stringBuilder.append(newLine);
        stringBuilder.append("@RELATION AirLife");stringBuilder.append(newLine);
        stringBuilder.append("@ATTRIBUTE Id NUMERIC");stringBuilder.append(newLine);
        stringBuilder.append("@ATTRIBUTE timestamp NUMERIC");stringBuilder.append(newLine);
        stringBuilder.append("@ATTRIBUTE airquality NUMERIC");stringBuilder.append(newLine);
        stringBuilder.append("@ATTRIBUTE currentLatitude REAL");stringBuilder.append(newLine);
        stringBuilder.append("@ATTRIBUTE currentLongitude REAL");stringBuilder.append(newLine);
        stringBuilder.append("@ATTRIBUTE temperature NUMERIC");stringBuilder.append(newLine);
        stringBuilder.append("@ATTRIBUTE humidity NUMERIC");stringBuilder.append(newLine);
        stringBuilder.append("@ATTRIBUTE pressure NUMERIC");stringBuilder.append(newLine);
        stringBuilder.append("@ATTRIBUTE wind NUMERIC");stringBuilder.append(newLine);
        stringBuilder.append("@ATTRIBUTE class {good,moderate,unhealthysensitive,unhealthy,veryunhealthy,hazardous}");stringBuilder.append(newLine);

        stringBuilder.append("@DATA");stringBuilder.append(newLine);
        for (AqiDataSet aqiDataSet : aqiDataSets) {
            stringBuilder.append(aqiDataSet.getId()); stringBuilder.append(lineSeparator);
            String timeStamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
            stringBuilder.append(timeStamp); stringBuilder.append(lineSeparator);
            stringBuilder.append(aqiDataSet.getAirquality()); stringBuilder.append(lineSeparator);
            stringBuilder.append(aqiDataSet.getCurrentLatitude()); stringBuilder.append(lineSeparator);
            stringBuilder.append(aqiDataSet.getCurrentLongitude()); stringBuilder.append(lineSeparator);

            stringBuilder.append(aqiDataSet.getTemperature().split(" ")[0]);
            stringBuilder.append(lineSeparator);

            stringBuilder.append(aqiDataSet.getHumidity().replace("%",""));
            stringBuilder.append(lineSeparator);

            stringBuilder.append(aqiDataSet.getPressure().split(" ")[0]);
            stringBuilder.append(lineSeparator);

            stringBuilder.append(aqiDataSet.getWind().split(" ")[0]);
            stringBuilder.append(lineSeparator);

            stringBuilder.append(getclassName(aqiDataSet.getAirquality()));
            stringBuilder.append(newLine);
        }

        Log.e( "createModelARFF: ", stringBuilder.toString());

        InputStream arffFile=new ByteArrayInputStream(stringBuilder.toString().getBytes());


        try {
            Prediction prediction=new Prediction();
            prediction.loadTrainingSet(this, arffFile);
            //prediction.machineLearning(this);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private String getclassName(int aqi) {
        String returnValue="good";
        if (aqi >= 0 && aqi <= 50) {
           returnValue="good";
        } else if (aqi >= 51 && aqi <= 100) {
            returnValue="moderate";
        } else if (aqi >= 101 && aqi <= 150) {
            returnValue="unhealthysensitive";
        } else if (aqi >= 151 && aqi <= 200) {
            returnValue="unhealthy";
        } else if (aqi >= 201 && aqi <= 300) {
            returnValue="veryunhealthy";
        } else if (aqi >= 301) {
            returnValue="hazardous";
        }
        return returnValue;
    }


    private void getAllDataSet() {
        AsynkTaskCustom asynkTaskCustom = new AsynkTaskCustom(PredictionActivity.this, "Please wait...Loading...");
        asynkTaskCustom.execute(new onWriteCode<List<AqiDataSet>>() {
            @Override
            public List<AqiDataSet> onExecuteCode() {
                List<AqiDataSet> predictionListObjects = new ArrayList<>();
                predictionListObjects = Common.getAllAQIDataSet(PredictionActivity.this);
                return predictionListObjects;
            }

            @Override
            public List<AqiDataSet> onSuccess(List<AqiDataSet> result) {
                //Toast.makeText(PredictionActivity.this, result.toString(), Toast.LENGTH_SHORT).show();
                callMachineLearning(result);
                return result;
            }
        });
    }

    private void Init() {
        setUPTheme();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Air Life - Prediction");
    }

    public void setUPTheme() {
        sharedPrefUtils = SharedPrefUtils.getInstance(this);
        if (sharedPrefUtils.isDarkMode()) {
            setTheme(R.style.AppTheme_Dark);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorthemeDarkPrimary)));
        } else {
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

