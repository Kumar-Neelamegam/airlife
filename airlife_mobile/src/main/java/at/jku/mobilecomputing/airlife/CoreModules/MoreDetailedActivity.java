package at.jku.mobilecomputing.airlife.CoreModules;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import at.jku.mobilecomputing.airlife.Constants.Common;
import at.jku.mobilecomputing.airlife.Database.AqiData.AqiDataSet;
import at.jku.mobilecomputing.airlife.R;
import at.jku.mobilecomputing.airlife.Utilities.AsynkTaskCustom;
import at.jku.mobilecomputing.airlife.Utilities.SharedPrefUtils;
import at.jku.mobilecomputing.airlife.Utilities.onWriteCode;

/**
 * Muthukumar Neelamegam
 * Mobile Computing Project - JKU, Linz
 * WS2020
 * Adviser: Prof. Anna Karin Hummel
 */
public class MoreDetailedActivity extends AppCompatActivity {

    BarChart chart;
    private SharedPrefUtils sharedPrefUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moredetail);

        try {
            init();
            initChart();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initChart() {

        chart = findViewById(R.id.barchart);
        getDataSet();

    }

    private void init() {
        setUPTheme();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.moreinfoTitle));
    }

    private void getDataSet() {


        AsynkTaskCustom asynkTaskCustom = new AsynkTaskCustom(MoreDetailedActivity.this, "Please wait...Loading...");
        asynkTaskCustom.execute(new onWriteCode<List<AqiDataSet>>() {
            @Override
            public List<AqiDataSet> onExecuteCode() {
                List<AqiDataSet> predictionListObjects = new ArrayList<>();
                predictionListObjects = Common.getAllAQITopDataset(MoreDetailedActivity.this);
                return predictionListObjects;
            }

            @Override
            public List<AqiDataSet> onSuccess(List<AqiDataSet> result) {
                if (result.size() >= 7) {
                    ArrayList<BarDataSet> dataSets = null;
                    ArrayList<BarEntry> valueSet1 = null;
                    ArrayList<BarEntry> valueSet2 = null;
                    ArrayList<BarEntry> valueSet3 = null;
                    ArrayList<BarEntry> valueSet4 = null;
                    ArrayList<BarEntry> valueSet5 = null;
                    ArrayList<BarEntry> valueSet6 = null;
                    valueSet1 = new ArrayList<>();
                    for (int i = 0; i < 7; i++) {

                        BarEntry v1e1 = new BarEntry(result.get(i).getAirquality(), i); // quality
                        valueSet1.add(v1e1);
                    }

                    BarDataSet barDataSet1 = new BarDataSet(valueSet1, "Air Quality");
                    barDataSet1.setColors(ColorTemplate.JOYFUL_COLORS);

                    dataSets = new ArrayList<>();
                    dataSets.add(barDataSet1);

                    BarData data = new BarData(getXAxisValues(), dataSets);
                    chart.setData(data);
                    chart.setDescription("");
                    chart.animateXY(1000, 1000);
                    chart.setDoubleTapToZoomEnabled(true);
                    chart.invalidate();

                } else {
                    Toast.makeText(MoreDetailedActivity.this, "No sufficient data found!", Toast.LENGTH_SHORT).show();
                }

                return result;
            }
        });

    }

    private ArrayList<String> getXAxisValues() {
        ArrayList<String> xAxis = new ArrayList<>();
        xAxis.add("Monday");
        xAxis.add("Tuesday");
        xAxis.add("Wednesday");
        xAxis.add("Thursday");
        xAxis.add("Friday");
        xAxis.add("Saturday");
        xAxis.add("Sunday");
        return xAxis;
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



}
