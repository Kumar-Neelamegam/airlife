package at.jku.mobilecomputing.airlife.CoreModules;

import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;

import com.ftoslab.openweatherretrieverz.DailyForecastCallback;
import com.ftoslab.openweatherretrieverz.DailyForecastInfo;
import com.ftoslab.openweatherretrieverz.OpenWeatherRetrieverZ;
import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.CubeGrid;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import at.jku.mobilecomputing.airlife.Constants.Common;
import at.jku.mobilecomputing.airlife.Database.AqiData.AqiDataSet;
import at.jku.mobilecomputing.airlife.R;
import at.jku.mobilecomputing.airlife.Utilities.AsynkTaskCustom;
import at.jku.mobilecomputing.airlife.Utilities.SharedPrefUtils;
import at.jku.mobilecomputing.airlife.Utilities.onWriteCode;
import at.jku.mobilecomputing.machinelearning.Prediction;
import at.jku.mobilecomputing.machinelearning.WeatherInfo;

public class PredictionActivity extends AppCompatActivity {

    private SharedPrefUtils sharedPrefUtils;
    double lat;
    double lng;
    List resultList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);

        Init();
        getAllDataSet();
        lat = getIntent().getExtras().getDouble("latitude");
        lng = getIntent().getExtras().getDouble("longitude");

    }

    private void callMachineLearning(List<AqiDataSet> aqiDataSets) {

        // show samples
        StringBuilder sb = new StringBuilder("Samples:\n");
        for (AqiDataSet aqiDataSet : aqiDataSets) {
            sb.append(aqiDataSet.getAirquality() + "\n");
        }

        createModelARFF(aqiDataSets);

    }

    AlertDialog alertDialog;

    /**
     * Create a training dataset from the local room database - step 1
     * ARFF (Attribute-Relation File Format)
     *
     * @param aqiDataSets
     */
    private void createModelARFF(List<AqiDataSet> aqiDataSets) {

        String newLine = "\n";
        String lineSeparator = ",";
        String lineQuotes = "\"";
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("% AirLife");
        stringBuilder.append(newLine);
        stringBuilder.append("@RELATION AirLife");
        stringBuilder.append(newLine);
        stringBuilder.append("@ATTRIBUTE Id NUMERIC");
        stringBuilder.append(newLine);
        stringBuilder.append("@ATTRIBUTE timestamp NUMERIC");
        stringBuilder.append(newLine);
        stringBuilder.append("@ATTRIBUTE airquality NUMERIC");
        stringBuilder.append(newLine);
        stringBuilder.append("@ATTRIBUTE currentLatitude NUMERIC");
        stringBuilder.append(newLine);
        stringBuilder.append("@ATTRIBUTE currentLongitude NUMERIC");
        stringBuilder.append(newLine);
        stringBuilder.append("@ATTRIBUTE temperature NUMERIC");
        stringBuilder.append(newLine);
        stringBuilder.append("@ATTRIBUTE humidity NUMERIC");
        stringBuilder.append(newLine);
        stringBuilder.append("@ATTRIBUTE pressure NUMERIC");
        stringBuilder.append(newLine);
        stringBuilder.append("@ATTRIBUTE wind NUMERIC");
        stringBuilder.append(newLine);
        stringBuilder.append("@ATTRIBUTE class {good,moderate,unhealthysensitive,unhealthy,veryunhealthy,hazardous}");
        stringBuilder.append(newLine);

        stringBuilder.append("@DATA");
        stringBuilder.append(newLine);
        for (AqiDataSet aqiDataSet : aqiDataSets) {
            stringBuilder.append(aqiDataSet.getId());
            stringBuilder.append(lineSeparator);
            stringBuilder.append(aqiDataSet.getDatetime());
            stringBuilder.append(lineSeparator);
            stringBuilder.append(aqiDataSet.getAirquality());
            stringBuilder.append(lineSeparator);
            stringBuilder.append(aqiDataSet.getCurrentLatitude());
            stringBuilder.append(lineSeparator);
            stringBuilder.append(aqiDataSet.getCurrentLongitude());
            stringBuilder.append(lineSeparator);

            stringBuilder.append(aqiDataSet.getTemperature().replace(",", "."));//fix for german locale
            stringBuilder.append(lineSeparator);

            stringBuilder.append(aqiDataSet.getHumidity().replace(",", "."));//fix for german locale
            stringBuilder.append(lineSeparator);

            stringBuilder.append(aqiDataSet.getPressure().replace(",", "."));//fix for german locale
            stringBuilder.append(lineSeparator);

            stringBuilder.append(aqiDataSet.getWind().replace(",", "."));//fix for german locale
            stringBuilder.append(lineSeparator);

            stringBuilder.append(getclassName(aqiDataSet.getAirquality()));
            stringBuilder.append(newLine);
        }

        // Log.e("createModelARFF: ", stringBuilder.toString());

        InputStream arffFile = new ByteArrayInputStream(stringBuilder.toString().getBytes());


        try {
            //load progress
            //calling this method to show our android custom alert dialog
            showCustomDialog("Prediction Inprogress", "Using machine learning to get the best results...");
            // Initialize OpenWeatherRetrieverZ by passing in  your openweathermap api key
            OpenWeatherRetrieverZ retriever = new OpenWeatherRetrieverZ(Common.openWeatherKey);


            retriever.updateDailyForecastInfo(lat, lng, new DailyForecastCallback() {
                @Override
                public void onReceiveDailyForecastInfoList(List<DailyForecastInfo> dailyForecastInfoList) {
                    // Your code here
                    // Toast.makeText(PredictionActivity.this, dailyForecastInfoList.toString(), Toast.LENGTH_SHORT).show();
                    ArrayList<WeatherInfo> weatherInfo = new ArrayList<WeatherInfo>();
                    WeatherInfo weatherInfo1 = new WeatherInfo();

                    for (DailyForecastInfo dailyForecastInfo : dailyForecastInfoList) {
                        String current_temp = getString(R.string.temperature_unit_celsius_2, Double.parseDouble(dailyForecastInfo.getDailyAverageTemperature()) - Common.KelvinToCelcius);
                        String current_pressure = getString(R.string.pressure_unit_2, Double.parseDouble(dailyForecastInfo.getAveragePressure()));
                        String current_humd = getString(R.string.humidity_unit_2, Double.parseDouble(dailyForecastInfo.getAverageHumidity()));
                        String current_wind = getString(R.string.wind_unit_2, Double.parseDouble(dailyForecastInfo.getAverageWindSpeed()));
                        weatherInfo1 = new WeatherInfo();
                        weatherInfo1.setTemperature(current_temp);//temp
                        weatherInfo1.setPressure(current_pressure);//pressure
                        weatherInfo1.setHumidity(current_humd);//humidity
                        weatherInfo1.setWind(current_wind);//windspeed
                        weatherInfo1.setTimestamp(dailyForecastInfo.getDateCalendar().getTimeInMillis());
                        //Getting entries
                        weatherInfo.add(weatherInfo1);
                    }
                    Prediction prediction = new Prediction();
                    resultList = prediction.loadTrainingSet(PredictionActivity.this, arffFile, lat, lng, weatherInfo);
                    prepareUiList(lat, lng, dailyForecastInfoList);
                }

                @Override
                public void onFailure(String error) {
                    // Your code here
                    Log.e("updateDailyForecastInfo-onFailure: ", error);
                }
            });


            //prediction.machineLearning(this);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void prepareUiList(double lat, double lng, List<DailyForecastInfo> dailyForecastInfoList) {

        // Parent layout
        LinearLayout parentLayout = findViewById(R.id.bindingview);

        // Layout inflater
        LayoutInflater layoutInflater = getLayoutInflater();
        View view;
        int sno = 0;
        String[] days = new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        for (int i = 0; i < dailyForecastInfoList.size(); i++) {
            // Add the text layout to the parent layout
            view = layoutInflater.inflate(R.layout.row_item_predict, parentLayout, false);
            // fill in any details dynamically here
            TextView txtvwSno = view.findViewById(R.id.txtvw_sno);
            TextView txtvwDay = view.findViewById(R.id.txtvw_day);
            TextView txtvwDateinfo = view.findViewById(R.id.txtvw_dateinfo);
            TextView txtvwLocationinfo = view.findViewById(R.id.txtvw_locationinfo);
            TextView predictTemp = view.findViewById(R.id.predict_temp);
            TextView predictPressure = view.findViewById(R.id.predict_pressure);
            TextView predictHumid = view.findViewById(R.id.predict_humid);
            TextView predictWind = view.findViewById(R.id.predict_wind);
            LinearLayout predaqibg = view.findViewById(R.id.predaqibg);
            TextView txtairquality = view.findViewById(R.id.txtairquality);
            ImageView imgvwAirqualityscale = view.findViewById(R.id.imgvw_airqualityscale);
            // Add the text view to the parent layout
            txtvwSno.setText(String.valueOf(sno + 1));
            txtvwDay.setText(days[i]);
            Calendar calendar = dailyForecastInfoList.get(i).getDateCalendar();
            txtvwDateinfo.setText(calendar.getTime().toGMTString());
            txtvwLocationinfo.setText("");
            predictTemp.setText(dailyForecastInfoList.get(i).getDailyAverageTemperature());
            predictPressure.setText(dailyForecastInfoList.get(i).getAveragePressure());
            predictHumid.setText(dailyForecastInfoList.get(i).getAverageHumidity());
            predictWind.setText(dailyForecastInfoList.get(i).getAverageWindSpeed());
            txtairquality.setText(resultList.get(i).toString());

            parentLayout.addView(view);
            sno++;
        }


        //cancel progress
        alertDialog.dismiss();
        Toast.makeText(this, R.string.predictsuccess, Toast.LENGTH_SHORT).show();

    }

    private void showCustomDialog(String titletxt, String messagetxt) {
        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        ViewGroup viewGroup = findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(this).inflate(R.layout.my_dialog, viewGroup, false);

        SpinKitView spinKitView = dialogView.findViewById(R.id.dgprogressBar);
        Sprite animate = new CubeGrid();
        spinKitView.setIndeterminateDrawable(animate);
        TextView title, message;
        ImageView logo;
        title = dialogView.findViewById(R.id.dgtitle);
        message = dialogView.findViewById(R.id.dgmessage);
        logo = dialogView.findViewById(R.id.logo);

        title.setText(titletxt);
        message.setText(messagetxt);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);


        //finally creating the alert dialog and displaying it
        alertDialog = builder.create();
        alertDialog.show();
    }


    private String getclassName(int aqi) {
        String returnValue = "good";
        if (aqi >= 0 && aqi <= 50) {
            returnValue = "good";
        } else if (aqi >= 51 && aqi <= 100) {
            returnValue = "moderate";
        } else if (aqi >= 101 && aqi <= 150) {
            returnValue = "unhealthysensitive";
        } else if (aqi >= 151 && aqi <= 200) {
            returnValue = "unhealthy";
        } else if (aqi >= 201 && aqi <= 300) {
            returnValue = "veryunhealthy";
        } else if (aqi >= 301) {
            returnValue = "hazardous";
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
        getSupportActionBar().setTitle(getResources().getString(R.string.predictionTitle));
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