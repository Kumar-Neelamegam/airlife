package at.jku.mobilecomputing.airlife.Constants;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import at.jku.mobilecomputing.airlife.Database.AqiData.AqiDAO;
import at.jku.mobilecomputing.airlife.Database.AqiData.AqiDataSet;
import at.jku.mobilecomputing.airlife.Database.AirLifeDatabaseClient;
import at.jku.mobilecomputing.airlife.Database.FavData.FavouriteListDAO;
import at.jku.mobilecomputing.airlife.Database.FavData.FavouriteListDataSet;
import at.jku.mobilecomputing.airlife.DomainObjects.Data;
import at.jku.mobilecomputing.airlife.R;

public class Common {
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
    public  static void InserttoDB(Context ctx, Data data, String latitude, String longitude, String apiFullResponse) {
        try {
            AqiDataSet aqiDataSet=new AqiDataSet();
            aqiDataSet.setFullResponse(apiFullResponse);
            aqiDataSet.setAirquality(data.getAqi());
            aqiDataSet.setQualityscale(Common.getscalefromquality(data.getAqi(), ctx));
            aqiDataSet.setCurrentLatitude(latitude);
            aqiDataSet.setCurrentLongitude(longitude);
            aqiDataSet.setCity(data.getCity().getName());
            aqiDataSet.setAddress(data.getCity().getUrl());
            aqiDataSet.setDatetime(String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())));
            double temperature=data.getWaqi().getTemperature()!=null? data.getWaqi().getTemperature().getV(): 0;
            double humidity=data.getWaqi().getHumidity()!=null? data.getWaqi().getHumidity().getV():0;
            double pressure=data.getWaqi().getPressure()!=null? data.getWaqi().getPressure().getV():0;
            double wind=data.getWaqi().getWind()!=null? data.getWaqi().getWind().getV():0;
            aqiDataSet.setTemperature(ctx.getString(R.string.temperature_unit_celsius,temperature));
            aqiDataSet.setHumidity(ctx.getString(R.string.humidity_unit,humidity));
            aqiDataSet.setPressure(ctx.getString(R.string.pressure_unit, pressure));
            aqiDataSet.setWind(ctx.getString(R.string.wind_unit, wind));
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


}
