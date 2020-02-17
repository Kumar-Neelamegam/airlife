package at.jku.mobilecomputing.airlife.Database.FavData;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.Date;

import at.jku.mobilecomputing.airlife.Database.TimestampConverter;

@Entity
public class FavouriteListDataSet  {

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

@Ignore
    private Double co;
   @Ignore private Double no2;
   @Ignore private Double o3;
   @Ignore private Double pressure;
   @Ignore private Double pm10;
   @Ignore private Double pm25;
   @Ignore private Double so2;
   @Ignore private Double temperature;
   @Ignore private Double humidity;
   @Ignore private Double wind;

    public Double getCo() {
        return co;
    }

    public void setCo(double co) {
        this.co = co;
    }

    public Double getNo2() {
        return no2;
    }

    public void setNo2(Double no2) {
        this.no2 = no2;
    }

    public Double getO3() {
        return o3;
    }

    public void setO3(Double o3) {
        this.o3 = o3;
    }

    public Double getPressure() {
        return pressure;
    }

    public void setPressure(Double pressure) {
        this.pressure = pressure;
    }

    public Double getPm10() {
        return pm10;
    }

    public void setPm10(Double pm10) {
        this.pm10 = pm10;
    }

    public Double getPm25() {
        return pm25;
    }

    public void setPm25(Double pm25) {
        this.pm25 = pm25;
    }

    public Double getSo2() {
        return so2;
    }

    public void setSo2(Double so2) {
        this.so2 = so2;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Double getWind() {
        return wind;
    }

    public void setWind(Double wind) {
        this.wind = wind;
    }
}
