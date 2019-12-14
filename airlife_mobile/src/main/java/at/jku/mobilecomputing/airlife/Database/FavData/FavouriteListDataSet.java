package at.jku.mobilecomputing.airlife.Database.FavData;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

import at.jku.mobilecomputing.airlife.Database.TimestampConverter;

@Entity
public class FavouriteListDataSet {

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @PrimaryKey(autoGenerate = true)
    private int id;


    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getQualityValue() {
        return QualityValue;
    }

    public void setQualityValue(String qualityValue) {
        QualityValue = qualityValue;
    }

    public int getQualityScale() {
        return QualityScale;
    }

    public void setQualityScale(int qualityScale) {
        QualityScale = qualityScale;
    }


    String Location;
    String LocationInfo;
    String QualityValue;
    int QualityScale;
    boolean ActiveStatus;

    public boolean isActiveStatus() {
        return ActiveStatus;
    }

    public void setActiveStatus(boolean activeStatus) {
        ActiveStatus = activeStatus;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    double latitude;
    double longitude;

    public String getLocationInfo() {
        return LocationInfo;
    }

    public void setLocationInfo(String locationInfo) {
        LocationInfo = locationInfo;
    }

    @ColumnInfo(name = "created_at")
    @TypeConverters({TimestampConverter.class})
    private Date createdAt;


    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

}
