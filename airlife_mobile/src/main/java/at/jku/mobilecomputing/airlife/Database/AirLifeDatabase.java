package at.jku.mobilecomputing.airlife.Database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import at.jku.mobilecomputing.airlife.Database.AqiData.AqiDAO;
import at.jku.mobilecomputing.airlife.Database.AqiData.AqiDataSet;
import at.jku.mobilecomputing.airlife.Database.FavData.FavouriteListDAO;
import at.jku.mobilecomputing.airlife.Database.FavData.FavouriteListDataSet;


@Database(entities = {AqiDataSet.class, FavouriteListDataSet.class}, version = 1, exportSchema = false)
public abstract class AirLifeDatabase extends RoomDatabase {

    public abstract AqiDAO aqiDAO();

    public abstract FavouriteListDAO favouriteListDAO();

}
