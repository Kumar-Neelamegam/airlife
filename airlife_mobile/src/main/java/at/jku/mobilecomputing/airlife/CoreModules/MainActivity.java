package at.jku.mobilecomputing.airlife.CoreModules;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.ftoslab.openweatherretrieverz.CurrentWeatherInfo;
import com.ftoslab.openweatherretrieverz.OpenWeatherRetrieverZ;
import com.ftoslab.openweatherretrieverz.WeatherCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.yariksoffice.lingver.Lingver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import at.jku.mobilecomputing.airlife.Adapters.PollutantsAdapter;
import at.jku.mobilecomputing.airlife.Constants.Common;
import at.jku.mobilecomputing.airlife.Constants.Status;
import at.jku.mobilecomputing.airlife.CustomDialog.InfoDialog;
import at.jku.mobilecomputing.airlife.DomainObjects.Data;
import at.jku.mobilecomputing.airlife.DomainObjects.Pollutant;
import at.jku.mobilecomputing.airlife.DomainObjects.WAQI;
import at.jku.mobilecomputing.airlife.NetworkUtils.AqiViewModel;
import at.jku.mobilecomputing.airlife.R;
import at.jku.mobilecomputing.airlife.Utilities.CustomDialog;
import at.jku.mobilecomputing.airlife.Utilities.GPSUtils;
import at.jku.mobilecomputing.airlife.Utilities.SharedPrefUtils;
import at.jku.mobilecomputing.airlife.Widget.DataUpdateWidgetWorker;

import static at.jku.mobilecomputing.airlife.Constants.PollutionLevels.GOOD;
import static at.jku.mobilecomputing.airlife.Constants.PollutionLevels.HAZARDOUS;
import static at.jku.mobilecomputing.airlife.Constants.PollutionLevels.MODERATE;
import static at.jku.mobilecomputing.airlife.Constants.PollutionLevels.UNHEALTHY;
import static at.jku.mobilecomputing.airlife.Constants.PollutionLevels.UNHEALTHY_FOR_SENSITIVE;
import static at.jku.mobilecomputing.airlife.Constants.PollutionLevels.VERY_UNHEALTHY;
import static at.jku.mobilecomputing.airlife.Utilities.GPSUtils.GPS_REQUEST;


/**
 * Muthukumar Neelamegam
 * Mobile Computing Project - JKU, Linz
 * WS2020
 * Adviser: Prof. Anna Karin Hummel
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //Views
    private TextView aqiTextView, temperatureTextView, locationTextView, pressureTextView, humidityTextView, windTextView, attributionTextView;
    private RecyclerView pollutantsRecyclerView;


    //Data
    private AqiViewModel aqiViewModel;
    private Data data = new Data();
    private PollutantsAdapter pollutantsAdapter;
    private List<Pollutant> pollutantsList = new ArrayList<>();
    private SharedPrefUtils sharedPrefUtils;

    //Location
    private FusedLocationProviderClient fusedLocationClient;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private LocationManager locationManager;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location latestLocation;
    AppCompatImageView circleBackground;
    AppCompatImageView makeFavourite;
    AppCompatImageView listFavourite;
    AppCompatImageView predictMachineLearning;
    AppCompatImageView btnRefresh;
    AppCompatImageView btnLanguage;
    AppCompatImageView btnStat;

    double currentLatitude = 0;
    double currentLongitude = 0;

    String apiFullResponse;
    //**********************************************************************************************
    CustomDialog customDialog;

    //**********************************************************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setUPTheme();
            setContentView(R.layout.activity_main);
            init();
            if (Common.getNetworkStatus(this)) {
                getData();
                checkGPSAndRequestLocation();
                scheduleWidgetUpdater();
            } else {
                showInternetDialog(getResources().getString(R.string.msg_nonetworkconnection));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //**********************************************************************************************
    public void setUPTheme() {
        sharedPrefUtils = SharedPrefUtils.getInstance(this);
        if (sharedPrefUtils.getAppInstallTime() == 0)
            sharedPrefUtils.setAppInstallTime(System.currentTimeMillis());
        if (sharedPrefUtils.isDarkMode()) setTheme(R.style.AppTheme_Dark);
        else setTheme(R.style.AppTheme_Light);
    }

    //**********************************************************************************************
    private void getData() {
        aqiViewModel = ViewModelProviders.of(this).get(AqiViewModel.class);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //locationRequest.setInterval(50 * 1000);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        latestLocation = location;
                        getAqiDataFromLatitudeLongitude(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
                        currentLatitude = location.getLatitude();
                        currentLongitude = location.getLongitude();
                        Log.e("onLocationResult: ", currentLatitude + "=" + currentLongitude);
                    }
                }
            }
        };
    }

    //**********************************************************************************************
    private void checkGPSAndRequestLocation() {

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Call for AQI data based on location is done in "locationCallback"
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            new GPSUtils(this).turnGPSOn();
        }

    }

    //**********************************************************************************************

    //**********************************************************************************************
    private void scheduleWidgetUpdater() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();

        PeriodicWorkRequest periodicWorkRequest =
                new PeriodicWorkRequest.Builder(DataUpdateWidgetWorker.class, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

        WorkManager.getInstance().enqueue(periodicWorkRequest);
    }

    //**********************************************************************************************

    /**
     * Initiate widgets
     */
    private void init() {
        aqiTextView = findViewById(R.id.aqi_text_view);
        temperatureTextView = findViewById(R.id.temperature_text_view);
        locationTextView = findViewById(R.id.location_text_view);
        pressureTextView = findViewById(R.id.pressure_text_view);
        humidityTextView = findViewById(R.id.humidity_text_view);
        windTextView = findViewById(R.id.wind_text_view);
        attributionTextView = findViewById(R.id.attribution_text_view);
        circleBackground = findViewById(R.id.aqi_background);
        btnLanguage = findViewById(R.id.btnLanguage);
        btnStat = findViewById(R.id.btnstats);
        makeFavourite = findViewById(R.id.imgvw_favourite);
        listFavourite = findViewById(R.id.imgvw_favlist);
        predictMachineLearning = findViewById(R.id.imgvw_machinelarning);
        btnRefresh = findViewById(R.id.btnRefresh);

        setupRecyclerView();
        setupClickListeners();
    }

    /**
     * Setup widgets control listeners
     */
    private void setupClickListeners() {
        findViewById(R.id.scaleGood).setOnClickListener(this);
        findViewById(R.id.scaleModerate).setOnClickListener(this);
        findViewById(R.id.scaleUnhealthySensitive).setOnClickListener(this);
        findViewById(R.id.scaleUnhealthy).setOnClickListener(this);
        findViewById(R.id.scaleVeryUnhealthy).setOnClickListener(this);
        findViewById(R.id.scaleHazardous).setOnClickListener(this);
        findViewById(R.id.btnDarkMode).setOnClickListener(this);
        findViewById(R.id.btnstats).setOnClickListener(this);

        findViewById(R.id.imgvw_favourite).setOnClickListener(this);
        findViewById(R.id.imgvw_favlist).setOnClickListener(this);
        findViewById(R.id.imgvw_machinelarning).setOnClickListener(this);
        findViewById(R.id.btnRefresh).setOnClickListener(this);
        btnLanguage.setOnClickListener(this);

    }

    //**********************************************************************************************
    private void setupRecyclerView() {
        pollutantsRecyclerView = findViewById(R.id.pollutants_recycler_view);
        pollutantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pollutantsRecyclerView.setHasFixedSize(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(pollutantsRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        pollutantsRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    //**********************************************************************************************
    private void addPollutantsToList(WAQI waqi) {
        try {
            pollutantsList.clear();
            if (waqi.getCo() != null)
                pollutantsList.add(new Pollutant("Carbon Monoxide - AQI", waqi.getCo().getV()));
            if (waqi.getNo2() != null)
                pollutantsList.add(new Pollutant("Nitrous Dioxide - AQI", waqi.getNo2().getV()));
            if (waqi.getO3() != null)
                pollutantsList.add(new Pollutant("Ozone - AQI", waqi.getO3().getV()));
            if (waqi.getPm2_5() != null)
                pollutantsList.add(new Pollutant("PM 2.5 - AQI", waqi.getPm2_5().getV()));
            if (waqi.getPm10() != null)
                pollutantsList.add(new Pollutant("PM 10 - AQI", waqi.getPm10().getV()));
            if (waqi.getSo2() != null)
                pollutantsList.add(new Pollutant("Sulfur Dioxide - AQI", waqi.getSo2().getV()));
            pollutantsAdapter = new PollutantsAdapter(pollutantsList);
            pollutantsRecyclerView.setAdapter(pollutantsAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDialog(String s) {
        //RetrofitHelper.getInstance().showProgressDialog(this, s);
        customDialog = new CustomDialog(this)
                .setImage(R.drawable.ic_dataimport)
                .setTitle("Information")
                .setNegativeButtonVisible(View.GONE)
                .setDescription(s)
                .setPositiveButtonVisible(View.GONE);
    }

    //**********************************************************************************************
    private void showInternetDialog(String s) {
        //RetrofitHelper.getInstance().showProgressDialog(this, s);
        CustomDialog customDialog = new CustomDialog(this);
        customDialog.setImage(R.drawable.ic_no_connection)
                .setTitle("Information")
                .setNegativeButtonVisible(View.VISIBLE)
                .setNegativeButtonTitle("Ok")
                .setDescription(s)
                .setprogressBarVisible(View.GONE)
                .setPositiveButtonVisible(View.GONE)
                .setOnNegativeListener(new CustomDialog.negativeOnClick() {
                    @Override
                    public void onNegativePerformed() {
                        customDialog.dismiss();
                    }
                });
    }

    //**********************************************************************************************
    private void dismissDialog() {
        // RetrofitHelper.getInstance().dismissProgressDialog();
        if (customDialog != null)
            customDialog.dismiss();

    }

    //**********************************************************************************************
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        checkGPSAndRequestLocation();
                    }

                } else {
                    getAqiData();
                }
            }
        }
    }
    //**********************************************************************************************
    /**
     * Get air quality data by passing latitude and longitude
     *
     * @param latitude
     * @param longitude
     */
    private void getAqiDataFromLatitudeLongitude(String latitude, String longitude) {
        try {
            String geo = "geo:" + latitude + ";" + longitude;
            Log.e("Geo information:", geo);
            aqiViewModel.getStatus().observe(MainActivity.this, status -> {
                if (status != null) {
                    if (status == Status.FETCHING) {
                        showDialog(getResources().getString(R.string.loadingmain));
                    } else dismissDialog();
                }
            });
            aqiViewModel.getGPSApiResponse(geo).observe(MainActivity.this, apiResponse -> {
                if (apiResponse != null) {
                    try {
                        apiFullResponse = String.valueOf(apiResponse);
                        Log.e("api", String.valueOf(apiResponse));
                        data = apiResponse.getData();
                        aqiTextView.setText(String.valueOf(data.getAqi()));
                        //TODO: Find better implementation
                        sharedPrefUtils.saveLatestAQI(String.valueOf(data.getAqi()));
                        Common.setAQIScaleGroup(data, circleBackground, this);
                        setWeatherInfo(latitude, longitude);
                        locationTextView.setText(data.getCity().getName());
                        //setupAttributions(data);
                        addPollutantsToList(data.getWaqi());
                        pollutantsAdapter.notifyDataSetChanged();
                        Common.updateWidget(this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //**********************************************************************************************
    private void setWeatherInfo(String lat, String lng) {

        // Initialize OpenWeatherRetrieverZ by passing in  your openweathermap api key
        OpenWeatherRetrieverZ retriever = new OpenWeatherRetrieverZ(Common.openWeatherKey);
            /*
            You can retrieve weather information with either OpenWeatherMap cityID or geolocation(Latitude, Logitude)
            */
        retriever.updateCurrentWeatherInfo(Double.parseDouble(lat), Double.parseDouble(lng), new WeatherCallback() {
            @Override
            public void onReceiveWeatherInfo(CurrentWeatherInfo currentWeatherInfo) {
                // Your code here
                //Toast.makeText(MainActivity.this, currentWeatherInfo.toString(), Toast.LENGTH_SHORT).show();
                if (currentWeatherInfo != null) {
                    //WAQI waqi = data.getWaqi();
                    try {
                        if (currentWeatherInfo.getCurrentTemperature() != null)
                            sharedPrefUtils.saveLatestTemp(getString(R.string.temperature_unit_celsius, Double.parseDouble(currentWeatherInfo.getCurrentTemperature()) - Common.KelvinToCelcius));
                        temperatureTextView.setText(getString(R.string.temperature_unit_celsius, Double.parseDouble(currentWeatherInfo.getCurrentTemperature()) - Common.KelvinToCelcius));
                        if (currentWeatherInfo.getPressure() != null)
                            pressureTextView.setText(getString(R.string.pressure_unit, Double.parseDouble(currentWeatherInfo.getPressure())));
                        if (currentWeatherInfo.getHumidity() != null)
                            humidityTextView.setText(getString(R.string.humidity_unit, Double.parseDouble(currentWeatherInfo.getHumidity())));
                        if (currentWeatherInfo.getWindSpeed() != null)
                            windTextView.setText(getString(R.string.wind_unit, Double.parseDouble(currentWeatherInfo.getWindSpeed())));

                        Common.InserttoDB(MainActivity.this, data, lat, lng, apiFullResponse, currentWeatherInfo);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

            @Override
            public void onFailure(String error) {
                // Your code here
                Log.e("WeatherInfo-onFailure: ", error);
            }
        });

    }

    //**********************************************************************************************
    /**
     * Get air quality data by using network ip
     */
    private void getAqiData() {
        try {
            aqiViewModel.getStatus().observe(MainActivity.this, status -> {
                if (status != null) {
                    if (status == Status.FETCHING) {
                        showDialog(getResources().getString(R.string.loading));
                    } else dismissDialog();
                }
            });
            aqiViewModel.getApiResponse().observe(MainActivity.this, apiResponse -> {
                if (apiResponse != null) {
                    Log.d("api", String.valueOf(apiResponse));
                    data = apiResponse.getData();
                    aqiTextView.setText(String.valueOf(data.getAqi()));
                    //TODO: Find better implementation
                    sharedPrefUtils.saveLatestAQI(String.valueOf(data.getAqi()));
                    Common.setAQIScaleGroup(data, circleBackground, this);
                    setWeatherInfo();
                    locationTextView.setText(data.getCity().getName());
                    // setupAttributions(data);
                    addPollutantsToList(data.getWaqi());
                    pollutantsAdapter.notifyDataSetChanged();
                    Common.updateWidget(this);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //**********************************************************************************************
    private void setWeatherInfo() {

        // Initialize OpenWeatherRetrieverZ by passing in  your openweathermap api key
        OpenWeatherRetrieverZ retriever = new OpenWeatherRetrieverZ(Common.openWeatherKey);
            /*
            You can retrieve weather information with either OpenWeatherMap cityID or geolocation(Latitude, Logitude)
            */
        retriever.updateCurrentWeatherInfo(currentLatitude, currentLongitude, new WeatherCallback() {
            @Override
            public void onReceiveWeatherInfo(CurrentWeatherInfo currentWeatherInfo) {
                // Your code here
                //Toast.makeText(MainActivity.this, currentWeatherInfo.toString(), Toast.LENGTH_SHORT).show();

                //WAQI waqi = data.getWaqi();
                try {
                    if (currentWeatherInfo.getCurrentTemperature() != null)
                        sharedPrefUtils.saveLatestTemp(getString(R.string.temperature_unit_celsius, Double.parseDouble(currentWeatherInfo.getCurrentTemperature()) - Common.KelvinToCelcius));
                    temperatureTextView.setText(getString(R.string.temperature_unit_celsius, Double.parseDouble(currentWeatherInfo.getCurrentTemperature()) - Common.KelvinToCelcius));
                    if (currentWeatherInfo.getPressure() != null)
                        pressureTextView.setText(getString(R.string.pressure_unit, Double.parseDouble(currentWeatherInfo.getPressure())));
                    if (currentWeatherInfo.getHumidity() != null)
                        humidityTextView.setText(getString(R.string.humidity_unit, Double.parseDouble(currentWeatherInfo.getHumidity())));
                    if (currentWeatherInfo.getWindSpeed() != null)
                        windTextView.setText(getString(R.string.wind_unit, Double.parseDouble(currentWeatherInfo.getWindSpeed())));

                    Common.InserttoDB(MainActivity.this, data, String.valueOf(currentLatitude), String.valueOf(currentLongitude), apiFullResponse, currentWeatherInfo);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(String error) {
                // Your code here
                Log.e("WeatherInfo-onFailure: ", error);
            }
        });


    }


    //**********************************************************************************************
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPS_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                checkGPSAndRequestLocation();
            } else {
                getAqiData();
            }
        }
    }

    //**********************************************************************************************
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scaleGood:
                new InfoDialog(this, GOOD).show();
                break;
            case R.id.scaleModerate:
                new InfoDialog(this, MODERATE).show();
                break;
            case R.id.scaleUnhealthySensitive:
                new InfoDialog(this, UNHEALTHY_FOR_SENSITIVE).show();
                break;
            case R.id.scaleUnhealthy:
                new InfoDialog(this, UNHEALTHY).show();
                break;
            case R.id.scaleVeryUnhealthy:
                new InfoDialog(this, VERY_UNHEALTHY).show();
                break;
            case R.id.scaleHazardous:
                new InfoDialog(this, HAZARDOUS).show();
                break;
            case R.id.btnDarkMode:
                sharedPrefUtils.isDarkMode(!sharedPrefUtils.isDarkMode());
                recreate();
                break;
            case R.id.imgvw_favourite:

                Common.callFavouriteDialog(MainActivity.this, currentLatitude, currentLongitude, locationTextView.getText().toString());

                break;
            case R.id.imgvw_favlist:
                int count = Common.getFavouriteListCount(MainActivity.this);
                if (count > 0) {
                    startActivity(new Intent(this, ListFavActivity.class));
                } else {
                    Toast.makeText(this, getResources().getString(R.string.nolist), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btnstats:
                count = Common.getFavouriteListCount(MainActivity.this);
                if (count > 0) {
                    startActivity(new Intent(this, MoreDetailedActivity.class));
                } else {
                    Toast.makeText(this, getResources().getString(R.string.nolist), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.imgvw_machinelarning:
                Intent mIntent = new Intent(this, PredictionActivity.class);
                Bundle extras = new Bundle();
                extras.putDouble("latitude", currentLatitude);
                extras.putDouble("longitude", currentLongitude);
                mIntent.putExtras(extras);
                startActivity(mIntent);
                break;

            case R.id.btnRefresh:
                recreate();
                Toast.makeText(this, getResources().getString(R.string.pleasewait), Toast.LENGTH_SHORT).show();
                break;

            case R.id.btnLanguage:
                if (Lingver.getInstance().getLanguage().equals(Common.defaultLanguage)) {//==en
                    Lingver.getInstance().setLocale(this, Common.germanLanguage);
                    Toast.makeText(this, getResources().getString(R.string.languageupdate), Toast.LENGTH_SHORT).show();
                    sharedPrefUtils.saveLatestLanguage(Common.germanLanguage);
                } else {
                    Lingver.getInstance().setLocale(this, Common.defaultLanguage);
                    sharedPrefUtils.saveLatestLanguage(Common.defaultLanguage);
                    Toast.makeText(this, getResources().getString(R.string.languageupdate), Toast.LENGTH_SHORT).show();
                }
                recreate();

                break;

            default:
                break;
        }
    }

    //**********************************************************************************************
    @Override
    protected void onStop() {
        super.onStop();
        System.gc();
    }

    //**********************************************************************************************
    @Override
    public void onBackPressed() {

        CustomDialog customDialog=new CustomDialog(this);
        customDialog.setImage(R.drawable.ic_close_black_24dp)
                .setTitle(getResources().getString(R.string.informationtitle))
                .setNegativeButtonVisible(View.VISIBLE)
                .setNegativeButtonTitle(getResources().getString(R.string.no))
                .setPositiveButtonVisible(View.VISIBLE)
                .setPossitiveButtonTitle(getString(R.string.yes))
                .setDescription(getResources().getString(R.string.are_you_sure_want_to_exit))
                .setprogressBarVisible(View.GONE)
                .setOnPossitiveListener(new CustomDialog.possitiveOnClick() {
                    @Override
                    public void onPossitivePerformed() {
                        finishAffinity();
                        System.gc();
                    }
                })
                .setOnNegativeListener(new CustomDialog.negativeOnClick() {
                    @Override
                    public void onNegativePerformed() {
                        customDialog.dismiss();
                    }
                });
    }

    //**********************************************************************************************
}//END
