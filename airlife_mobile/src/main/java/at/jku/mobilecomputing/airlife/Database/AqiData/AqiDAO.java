package at.jku.mobilecomputing.airlife.Database.AqiData;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AqiDAO {

    @Insert
    Long insert(AqiDataSet u);

    @Query("SELECT * FROM `AqiDataSet` ORDER BY `id` DESC")
    List<AqiDataSet> getAllAqiData();

    @Query("SELECT * FROM `AqiDataSet` WHERE `id` =:id")
    AqiDataSet getAqiData(int id);

    @Query("SELECT count(*)as count FROM `AqiDataSet`")
    int getAqiDataCount();

    @Update
    void update(AqiDataSet u);

    @Delete
    void delete(AqiDataSet u);

}
