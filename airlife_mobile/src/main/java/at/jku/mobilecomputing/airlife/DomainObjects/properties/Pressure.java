package at.jku.mobilecomputing.airlife.DomainObjects.properties;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Pressure   {

    @SerializedName("v")
    @Expose
    private Double v;

    public Double getV() {
        return v;
    }

    public void setV(Double v) {
        this.v = v;
    }

}
