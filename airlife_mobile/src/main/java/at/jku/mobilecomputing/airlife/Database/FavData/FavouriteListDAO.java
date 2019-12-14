package at.jku.mobilecomputing.airlife.Database.FavData;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FavouriteListDAO {

    @Insert
    Long insert(FavouriteListDataSet u);

    @Query("SELECT * FROM `FavouriteListDataSet` ORDER BY `id` DESC")
    List<FavouriteListDataSet> getAllFavData();

    @Query("SELECT * FROM `FavouriteListDataSet` WHERE `id` =:id")
    FavouriteListDataSet getFavData(int id);

    @Query("SELECT count(*)as count FROM `FavouriteListDataSet`")
    int getFavDataCount();

    @Update
    void update(FavouriteListDataSet u);

    @Delete
    void delete(FavouriteListDataSet u);
}
