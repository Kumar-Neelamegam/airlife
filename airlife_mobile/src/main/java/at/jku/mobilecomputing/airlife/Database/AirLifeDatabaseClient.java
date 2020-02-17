package at.jku.mobilecomputing.airlife.Database;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.room.Room;

import at.jku.mobilecomputing.airlife.Database.AqiData.AqiDataSet;
import at.jku.mobilecomputing.airlife.Database.FavData.FavouriteListDAO;
import at.jku.mobilecomputing.airlife.Database.FavData.FavouriteListDataSet;

import static android.content.ContentValues.TAG;

public class AirLifeDatabaseClient {

    private Context mCtx;
    private static AirLifeDatabaseClient mInstance;

    //our app database object
    private static AirLifeDatabase appDatabase;

    public AirLifeDatabaseClient(Context mCtx) {
        this.mCtx = mCtx;
        //creating the app database with Room database builder
        //AirLifeDB is the name of the database
        appDatabase = Room.databaseBuilder(mCtx, AirLifeDatabase.class, "AirLifeDB").allowMainThreadQueries().build();
    }

    public static synchronized AirLifeDatabaseClient getInstance(Context mCtx) {
        if (mInstance == null) {
            mInstance = new AirLifeDatabaseClient(mCtx);
        }
        return mInstance;
    }

    public AirLifeDatabase getAppDatabase() {
        return appDatabase;
    }



}