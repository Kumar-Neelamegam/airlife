package at.jku.mobilecomputing.airlife.Database;


import net.redwarp.library.database.annotation.PrimaryKey;

public class AqiDataSet {

    @PrimaryKey
    long id;

    public String getCurrentLatitude() {
        return currentLatitude;
    }

    public void setCurrentLatitude(String currentLatitude) {
        this.currentLatitude = currentLatitude;
    }

    public String getCurrentLongitude() {
        return currentLongitude;
    }

    public void setCurrentLongitude(String currentLongitude) {
        this.currentLongitude = currentLongitude;
    }

    public int getAirquality() {
        return airquality;
    }

    public void setAirquality(int airquality) {
        this.airquality = airquality;
    }

    public String getQualityscale() {
        return qualityscale;
    }

    public void setQualityscale(String qualityscale) {
        this.qualityscale = qualityscale;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getWind() {
        return wind;
    }

    public void setWind(String wind) {
        this.wind = wind;
    }

    public String getFullResponse() {
        return fullResponse;
    }

    public void setFullResponse(String fullResponse) {
        this.fullResponse = fullResponse;
    }

    String currentLatitude;
    String currentLongitude;
    int airquality;
    String qualityscale;
    String city;
    String datetime;
    String address;
    String temperature;
    String humidity;
    String pressure;
    String wind;
    String fullResponse;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}
