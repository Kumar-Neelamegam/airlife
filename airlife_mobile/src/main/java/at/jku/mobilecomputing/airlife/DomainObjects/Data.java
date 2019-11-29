
package at.jku.mobilecomputing.airlife.DomainObjects;

import android.os.Debug;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import at.jku.mobilecomputing.airlife.Constants.Time;

public class Data {

    @SerializedName("aqi")
    @Expose
    private Integer aqi;
    @SerializedName("idx")
    @Expose
    private Integer idx;
    @SerializedName("attributions")
    @Expose
    private List<Attribution> attributions = null;
    @SerializedName("city")
    @Expose
    private City city;
    @SerializedName("dominentpol")
    @Expose
    private String dominentpol;
    @SerializedName("waqi")
    @Expose
    private WAQI waqi;
    @SerializedName("time")
    @Expose
    private Time time;
    @SerializedName("debug")
    @Expose
    private Debug debug;

    public Integer getAqi() {
        return aqi;
    }

    public void setAqi(Integer aqi) {
        this.aqi = aqi;
    }

    public Integer getIdx() {
        return idx;
    }

    public void setIdx(Integer idx) {
        this.idx = idx;
    }

    public List<Attribution> getAttributions() {
        return attributions;
    }

    public void setAttributions(List<Attribution> attributions) {
        this.attributions = attributions;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getDominentpol() {
        return dominentpol;
    }

    public void setDominentpol(String dominentpol) {
        this.dominentpol = dominentpol;
    }

    public WAQI getWaqi() {
        return waqi;
    }

    public void setWaqi(WAQI waqi) {
        this.waqi = waqi;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public Debug getDebug() {
        return debug;
    }

    public void setDebug(Debug debug) {
        this.debug = debug;
    }

}
