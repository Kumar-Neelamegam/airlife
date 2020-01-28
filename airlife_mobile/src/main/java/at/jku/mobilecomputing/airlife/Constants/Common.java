package at.jku.mobilecomputing.airlife.Constants;

import android.app.Activity;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ftoslab.openweatherretrieverz.CurrentWeatherInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import at.jku.mobilecomputing.airlife.Database.AirLifeDatabaseClient;
import at.jku.mobilecomputing.airlife.Database.AqiData.AqiDAO;
import at.jku.mobilecomputing.airlife.Database.AqiData.AqiDataSet;
import at.jku.mobilecomputing.airlife.Database.FavData.FavouriteListDAO;
import at.jku.mobilecomputing.airlife.Database.FavData.FavouriteListDataSet;
import at.jku.mobilecomputing.airlife.DomainObjects.Attribution;
import at.jku.mobilecomputing.airlife.DomainObjects.Data;
import at.jku.mobilecomputing.airlife.R;
import at.jku.mobilecomputing.airlife.Widget.ALWidget;

/**
 * Muthukumar Neelamegam
 * Mobile Computing Project - JKU, Linz
 * WS2020
 * Adviser: Prof. Anna Karin Hummel
 */
public class Common {

    public static String openWeatherKey = "df21ec7ffa6b60adcee1e9f722b1e46d";
    public static double KelvinToCelcius = 273.15F;
    public static String defaultLanguage = "en";
    public static String germanLanguage = "de";

    public static String getscalefromquality(Integer aqi, Context ctx) {
        String scale="N/A";
        if (aqi >= 0 && aqi <= 50) {
            scale=ctx.getString(R.string.good);
        }
        else if (aqi >= 51 && aqi <= 100){
            scale=ctx.getString(R.string.moderate);
        } else if (aqi >= 101 && aqi <= 150){
            scale=ctx.getString(R.string.unhealthy_for_sensitive);
        } else if (aqi >= 151 && aqi <= 200){
            scale=ctx.getString(R.string.unhealthy);
        } else if (aqi >= 201 && aqi <= 300){
            scale=ctx.getString(R.string.very_unhealthy);
        } else if (aqi >= 301){
            scale=ctx.getString(R.string.hazardous);
        } else{
            scale=ctx.getString(R.string.good);
        }
        return scale;
    }

    public static boolean getNetworkStatus(Context ctx) {

        boolean status = false;
        ConnectivityManager conMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED
                || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {

            // notify user you are online
            status = true;

        } else if
        (conMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.DISCONNECTED
                        || conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.DISCONNECTED) {
            // notify user you are not online
            status = false;
        }
        return status;
    }

    public static void callFavouriteDialog(Context ctx, double currentLatitidue, double currentLongitude, String currentLocation) {

        // Create custom dialog object
        final Dialog dialog = new Dialog(ctx);
        // Include dialog.xml file
        dialog.setContentView(R.layout.favourite_dialog_save);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();

        TextView txtLocation=dialog.findViewById(R.id.txt_currentlocation);
        EditText edtName=dialog.findViewById(R.id.edt_favname);

        txtLocation.setText(currentLocation);
        Button cancelButton = dialog.findViewById(R.id.btn_cancel);
        Button saveButton = dialog.findViewById(R.id.btn_save);
        // if decline button is clicked, close the custom dialog
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                dialog.dismiss();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(edtName.getText().toString().length()>0)
                {
                    InsertFavourite(ctx,currentLatitidue, currentLongitude, currentLocation, edtName.getText().toString());
                    Toast.makeText(ctx, "This location added to the favourite list..", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }else
                {
                    Toast.makeText(ctx, "Enter the favourite name (Example: Home / University)..", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }


    /**
     * Insert AQI data to the database
     * @param ctx
     * @param data
     * @param latitude
     * @param longitude
     * @param apiFullResponse
     */
    public static void InserttoDB(Context ctx, Data data, String latitude, String longitude, String apiFullResponse, CurrentWeatherInfo currentWeatherInfo) {
        try {
            AqiDataSet aqiDataSet=new AqiDataSet();
            aqiDataSet.setFullResponse(apiFullResponse);
            aqiDataSet.setAirquality(data.getAqi());
            aqiDataSet.setQualityscale(Common.getscalefromquality(data.getAqi(), ctx));
            aqiDataSet.setCurrentLatitude(latitude);
            aqiDataSet.setCurrentLongitude(longitude);
            aqiDataSet.setCity(data.getCity().getName());
            aqiDataSet.setAddress(data.getCity().getUrl());
            aqiDataSet.setDatetime(String.valueOf(System.currentTimeMillis()));
            //double temperature=data.getWaqi().getTemperature()!=null? data.getWaqi().getTemperature().getV(): 0;
            //double humidity=data.getWaqi().getHumidity()!=null? data.getWaqi().getHumidity().getV():0;
            //double pressure=data.getWaqi().getPressure()!=null? data.getWaqi().getPressure().getV():0;
            //double wind=data.getWaqi().getWind()!=null? data.getWaqi().getWind().getV():0;
            String temperature = currentWeatherInfo.getCurrentTemperature() != null ? currentWeatherInfo.getCurrentTemperature() : "0";
            String humidity = currentWeatherInfo.getHumidity() != null ? currentWeatherInfo.getHumidity() : "0";
            String pressure = currentWeatherInfo.getPressure() != null ? currentWeatherInfo.getPressure() : "0";
            String wind = currentWeatherInfo.getWindSpeed() != null ? currentWeatherInfo.getWindSpeed() : "0";
            aqiDataSet.setTemperature(ctx.getResources().getString(R.string.temperature_unit_celsius_2, Double.parseDouble(temperature) - Common.KelvinToCelcius));
            aqiDataSet.setHumidity(ctx.getResources().getString(R.string.humidity_unit_2, Double.parseDouble(humidity)));
            aqiDataSet.setPressure(ctx.getResources().getString(R.string.pressure_unit_2, Double.parseDouble(pressure)));
            aqiDataSet.setWind(ctx.getResources().getString(R.string.wind_unit_2, Double.parseDouble(wind)));
            insertAQIData(aqiDataSet,ctx);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all AQI data set
     * @param ctx
     * @return
     */
    public static List<AqiDataSet> getAllAQIDataSet(Context ctx)
    {
        AqiDAO dao = AirLifeDatabaseClient.getInstance(ctx).getAppDatabase().aqiDAO();
        List<AqiDataSet> allPojos = dao.getAllAqiData();
        return allPojos!=null? allPojos : new ArrayList<>();//return null or empty
    }

    /**
     * Insert favourite place
     * @param ctx
     * @param latitude
     * @param longitude
     * @param Location
     * @param favName
     */
    public static void InsertFavourite(Context ctx, double latitude, double longitude, String Location, String favName)
    {
        try{
            FavouriteListDataSet favouriteListDataSet=new FavouriteListDataSet();
            favouriteListDataSet.setLatitude(latitude);
            favouriteListDataSet.setLongitude(longitude);
            favouriteListDataSet.setLocation(favName);
            favouriteListDataSet.setLocationInfo(Location);
            favouriteListDataSet.setActiveStatus(true);
            insertFavData(favouriteListDataSet, ctx);
        }catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    /**
     * Get list of all favourite list
     * @param ctx
     * @return
     */
    public static List<FavouriteListDataSet> getAllFavouriteDataSet(Context ctx)
    {
        FavouriteListDAO dao = AirLifeDatabaseClient.getInstance(ctx).getAppDatabase().favouriteListDAO();
        List<FavouriteListDataSet> allPojos = dao.getAllFavData();
        return allPojos!=null? allPojos : new ArrayList<>();//return null or empty
    }

    public static int getFavouriteListCount(Context ctx)
    {
        FavouriteListDAO dao =   AirLifeDatabaseClient.getInstance(ctx).getAppDatabase().favouriteListDAO();
        int count = dao.getFavDataCount();
        Log.e("getFavouriteListCount:", String.valueOf(count));
        return count;
    }

    public static boolean deleteFavouriteItem(int id, Context ctx)
    {
        FavouriteListDataSet favouriteListDataSet=new FavouriteListDataSet();
        favouriteListDataSet.setId(id);
        FavouriteListDAO dao = AirLifeDatabaseClient.getInstance(ctx).getAppDatabase().favouriteListDAO();
        dao.delete(favouriteListDataSet);
        return true;
    }



    public static void insertAQIData(final AqiDataSet aqidata,Context ctx) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                AirLifeDatabaseClient airLifeDatabaseClient=new AirLifeDatabaseClient(ctx);
                airLifeDatabaseClient.getAppDatabase().aqiDAO().insert(aqidata);
                Log.e( "DB OPERATION: ","insertAQIData");
                return null;
            }
        }.execute();
    }

    public static void insertFavData(final FavouriteListDataSet favdata, Context ctx) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                AirLifeDatabaseClient airLifeDatabaseClient=new AirLifeDatabaseClient(ctx);
                airLifeDatabaseClient.getAppDatabase().favouriteListDAO().insert(favdata);
                Log.e( "DB OPERATION: ","insertFavData");
                return null;
            }
        }.execute();
    }


    public static void setLanguage(Context ctx, String language) {
        try {
            //Change Application level locale
            LocaleHelper.setLocale(ctx, language);

            //It is required to recreate the activity to reflect the change in UI.
            ((Activity) ctx).recreate();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void setLocale_new(Context ctx, String lang) {

        try {

            Locale myLocale = new Locale(lang);
            Resources res = ctx.getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * Main activity - additional tasks
     */

    public static void setAQIScaleGroup(Data data, ImageView circleBackground, Context ctx) {
        int aqi = data.getAqi();
        ImageView aqiScaleText;
        if (aqi >= 0 && aqi <= 50) {
            aqiScaleText = ((Activity) ctx).findViewById(R.id.scaleGood);
            circleBackground.setImageResource(R.drawable.circle_good);
        } else if (aqi >= 51 && aqi <= 100) {
            aqiScaleText = ((Activity) ctx).findViewById(R.id.scaleModerate);
            circleBackground.setImageResource(R.drawable.circle_moderate);
        } else if (aqi >= 101 && aqi <= 150) {
            aqiScaleText = ((Activity) ctx).findViewById(R.id.scaleUnhealthySensitive);
            circleBackground.setImageResource(R.drawable.circle_unhealthysg);
        } else if (aqi >= 151 && aqi <= 200) {
            aqiScaleText = ((Activity) ctx).findViewById(R.id.scaleUnhealthy);
            circleBackground.setImageResource(R.drawable.circle_unhealthy);
        } else if (aqi >= 201 && aqi <= 300) {
            aqiScaleText = ((Activity) ctx).findViewById(R.id.scaleVeryUnhealthy);
            circleBackground.setImageResource(R.drawable.circle_veryunhealthy);
        } else if (aqi >= 301) {
            aqiScaleText = ((Activity) ctx).findViewById(R.id.scaleHazardous);
            circleBackground.setImageResource(R.drawable.circle_harzardous);
        } else {
            aqiScaleText = ((Activity) ctx).findViewById(R.id.scaleGood);
            circleBackground.setBackgroundResource(R.drawable.circle_good);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            aqiScaleText.setForeground(ctx.getDrawable(R.drawable.selected_aqi_foreground));
        }
    }

    public static void setupAttributions(Data data, TextView attributionTextView) {
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

    public static void updateWidget(Context ctx) {
        Intent intent = new Intent(ctx, ALWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        int[] ids = AppWidgetManager.getInstance(ctx.getApplicationContext()).getAppWidgetIds(new ComponentName(ctx.getApplicationContext(), ALWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        ctx.sendBroadcast(intent);
    }


    public static String getCompleteAddressString(Context ctx, double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(ctx, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("My Current loction address", strReturnedAddress.toString());
            } else {
                Log.w("My Current loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("My Current loction address", "Canont get Address!");
        }
        return strAdd;
    }


}
