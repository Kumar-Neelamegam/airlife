package at.jku.mobilecomputing.airlife.CoreModules;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import at.jku.mobilecomputing.airlife.Adapters.PollutantsAdapter;
import at.jku.mobilecomputing.airlife.Constants.Common;
import at.jku.mobilecomputing.airlife.Constants.Status;
import at.jku.mobilecomputing.airlife.CustomDialog.InfoDialog;
import at.jku.mobilecomputing.airlife.DomainObjects.Attribution;
import at.jku.mobilecomputing.airlife.DomainObjects.Data;
import at.jku.mobilecomputing.airlife.DomainObjects.Pollutant;
import at.jku.mobilecomputing.airlife.DomainObjects.WAQI;
import at.jku.mobilecomputing.airlife.NetworkUtils.AqiViewModel;
import at.jku.mobilecomputing.airlife.NetworkUtils.RetrofitHelper;
import at.jku.mobilecomputing.airlife.R;
import at.jku.mobilecomputing.airlife.Utilities.GPSUtils;
import at.jku.mobilecomputing.airlife.Utilities.SharedPrefUtils;
import at.jku.mobilecomputing.airlife.Widget.ALWidget;
import at.jku.mobilecomputing.airlife.Widget.DataUpdateWidgetWorker;

import static at.jku.mobilecomputing.airlife.Constants.PollutionLevels.GOOD;
import static at.jku.mobilecomputing.airlife.Constants.PollutionLevels.HAZARDOUS;
import static at.jku.mobilecomputing.airlife.Constants.PollutionLevels.MODERATE;
import static at.jku.mobilecomputing.airlife.Constants.PollutionLevels.UNHEALTHY;
import static at.jku.mobilecomputing.airlife.Constants.PollutionLevels.UNHEALTHY_FOR_SENSITIVE;
import static at.jku.mobilecomputing.airlife.Constants.PollutionLevels.VERY_UNHEALTHY;
import static at.jku.mobilecomputing.airlife.Utilities.GPSUtils.GPS_REQUEST;

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

    double currentLatitude = 0;
    double currentLongitude = 0;

    String apiFullResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setUPTheme();
            setContentView(R.layout.activity_main);
            init();
            getData();
            checkGPSAndRequestLocation();
            scheduleWidgetUpdater();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void setUPTheme() {
        sharedPrefUtils = SharedPrefUtils.getInstance(this);
        if (sharedPrefUtils.getAppInstallTime() == 0)
            sharedPrefUtils.setAppInstallTime(System.currentTimeMillis());
        if (sharedPrefUtils.isDarkMode()) setTheme(R.style.AppTheme_Dark);
        else setTheme(R.style.AppTheme_Light);
    }

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

    private void checkGPSAndRequestLocation() {
      /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                checkLocationPermission();
            } else {*/
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            //Call for AQI data based on location is done in "locationCallback"
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            new GPSUtils(this).turnGPSOn();
        }
        //    }
        //}
    }

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

    private void init() {
        aqiTextView = findViewById(R.id.aqi_text_view);
        temperatureTextView = findViewById(R.id.temperature_text_view);
        locationTextView = findViewById(R.id.location_text_view);
        pressureTextView = findViewById(R.id.pressure_text_view);
        humidityTextView = findViewById(R.id.humidity_text_view);
        windTextView = findViewById(R.id.wind_text_view);
        attributionTextView = findViewById(R.id.attribution_text_view);
        circleBackground = findViewById(R.id.aqi_background);

        makeFavourite = findViewById(R.id.imgvw_favourite);
        listFavourite = findViewById(R.id.imgvw_favlist);
        predictMachineLearning = findViewById(R.id.imgvw_machinelarning);
        btnRefresh=findViewById(R.id.btnRefresh);

        setupRecyclerView();
        setupClickListeners();
    }

    private void setupClickListeners() {
        findViewById(R.id.scaleGood).setOnClickListener(this);
        findViewById(R.id.scaleModerate).setOnClickListener(this);
        findViewById(R.id.scaleUnhealthySensitive).setOnClickListener(this);
        findViewById(R.id.scaleUnhealthy).setOnClickListener(this);
        findViewById(R.id.scaleVeryUnhealthy).setOnClickListener(this);
        findViewById(R.id.scaleHazardous).setOnClickListener(this);
        findViewById(R.id.btnDarkMode).setOnClickListener(this);

        findViewById(R.id.imgvw_favourite).setOnClickListener(this);
        findViewById(R.id.imgvw_favlist).setOnClickListener(this);
        findViewById(R.id.imgvw_machinelarning).setOnClickListener(this);
        findViewById(R.id.btnRefresh).setOnClickListener(this);

    }

    private void setupRecyclerView() {
        pollutantsRecyclerView = findViewById(R.id.pollutants_recycler_view);
        pollutantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        pollutantsRecyclerView.setHasFixedSize(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(pollutantsRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        pollutantsRecyclerView.addItemDecoration(dividerItemDecoration);
    }

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
        RetrofitHelper.getInstance().showProgressDialog(this, s);
    }

    private void dismissDialog() {
        RetrofitHelper.getInstance().dismissProgressDialog();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.alert_title_location_access)
                        .setMessage(R.string.alert_content_location_access)
                        .setPositiveButton("Ok", (dialogInterface, i) -> {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_LOCATION);
                        })
                        .setNegativeButton("Cancel", (dialogInterface, i) -> {
                            //getAqiData();
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

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

    private void getAqiDataFromLatitudeLongitude(String latitude, String longitude) {
        try {
            String geo = "geo:" + latitude + ";" + longitude;
            Log.e("Geo information:", geo);
            aqiViewModel.getStatus().observe(MainActivity.this, status -> {
                if (status != null) {
                    if (status == Status.FETCHING) {
                        showDialog("Loading data from nearest station...");
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
                        setAQIScaleGroup();
                        WAQI waqi = data.getWaqi();
                        try {
                            if (waqi.getTemperature().getV() != null)
                                sharedPrefUtils.saveLatestTemp(getString(R.string.temperature_unit_celsius, data.getWaqi().getTemperature().getV()));
                            temperatureTextView.setText(getString(R.string.temperature_unit_celsius, data.getWaqi().getTemperature().getV()));
                            if (waqi.getPressure() != null)
                                pressureTextView.setText(getString(R.string.pressure_unit, waqi.getPressure().getV()));
                            if (waqi.getHumidity() != null)
                                humidityTextView.setText(getString(R.string.humidity_unit, waqi.getHumidity().getV()));
                            if (waqi.getWind() != null)
                                windTextView.setText(getString(R.string.wind_unit, waqi.getWind().getV()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        locationTextView.setText(data.getCity().getName());
                        //setupAttributions(data);
                        addPollutantsToList(data.getWaqi());
                        pollutantsAdapter.notifyDataSetChanged();
                        updateWidget();
                        Common.InserttoDB(MainActivity.this, data, latitude, longitude, apiFullResponse);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void getAqiData() {
        try {
            aqiViewModel.getStatus().observe(MainActivity.this, status -> {
                if (status != null) {
                    if (status == Status.FETCHING) {
                        showDialog("Loading Information..Please wait..");
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
                    setAQIScaleGroup();
                    WAQI waqi = data.getWaqi();
                    if (waqi.getTemperature() != null)
                        temperatureTextView.setText(getString(R.string.temperature_unit_celsius, data.getWaqi().getTemperature().getV()));
                    if (waqi.getPressure() != null)
                        pressureTextView.setText(getString(R.string.pressure_unit, waqi.getPressure().getV()));
                    if (waqi.getHumidity() != null)
                        humidityTextView.setText(getString(R.string.humidity_unit, waqi.getHumidity().getV()));
                    if (waqi.getWind() != null)
                        windTextView.setText(getString(R.string.wind_unit, waqi.getWind().getV()));
                    locationTextView.setText(data.getCity().getName());
                    // setupAttributions(data);
                    addPollutantsToList(data.getWaqi());
                    pollutantsAdapter.notifyDataSetChanged();
                    updateWidget();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void setAQIScaleGroup() {
        int aqi = data.getAqi();
        ImageView aqiScaleText;
        if (aqi >= 0 && aqi <= 50) {
            aqiScaleText = findViewById(R.id.scaleGood);
            circleBackground.setImageResource(R.drawable.circle_good);
        } else if (aqi >= 51 && aqi <= 100) {
            aqiScaleText = findViewById(R.id.scaleModerate);
            circleBackground.setImageResource(R.drawable.circle_moderate);
        } else if (aqi >= 101 && aqi <= 150) {
            aqiScaleText = findViewById(R.id.scaleUnhealthySensitive);
            circleBackground.setImageResource(R.drawable.circle_unhealthysg);
        } else if (aqi >= 151 && aqi <= 200) {
            aqiScaleText = findViewById(R.id.scaleUnhealthy);
            circleBackground.setImageResource(R.drawable.circle_unhealthy);
        } else if (aqi >= 201 && aqi <= 300) {
            aqiScaleText = findViewById(R.id.scaleVeryUnhealthy);
            circleBackground.setImageResource(R.drawable.circle_veryunhealthy);
        } else if (aqi >= 301) {
            aqiScaleText = findViewById(R.id.scaleHazardous);
            circleBackground.setImageResource(R.drawable.circle_harzardous);
        } else {
            aqiScaleText = findViewById(R.id.scaleGood);
            circleBackground.setBackgroundResource(R.drawable.circle_good);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            aqiScaleText.setForeground(getDrawable(R.drawable.selected_aqi_foreground));
        }
    }

    private void setupAttributions(Data data) {
        int index = 1;
        StringBuilder attributionText = new StringBuilder();
        for (Attribution attribution : data.getAttributions()) {
            attributionText.append(index++)
                    .append(". ")
                    .append(attribution.getName())
                    .append("\n")
                    .append(attribution.getUrl())
                    .append("\n\n");
        }
        attributionTextView.setText(attributionText);
    }

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

    private void updateWidget() {
        Intent intent = new Intent(this, ALWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        int[] ids = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), ALWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

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
                    Toast.makeText(this, "No list found..", Toast.LENGTH_SHORT).show();
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
                getData();
                Toast.makeText(this, "Please wait.. refreshing..", Toast.LENGTH_SHORT).show();
                break;

            default:
                break;
        }
    }


    public void sendNotification(View view) {
        String toSend = "sample testing";
        if(toSend.isEmpty())
            toSend = "You sent an empty notification";
        Notification notification = new NotificationCompat.Builder(getApplication())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("AndroidAuthority")
                .setContentText(toSend)
                .extend(new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true))
                .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplication());
        int notificationId = 1;
        notificationManager.notify(notificationId, notification);
    }



}
