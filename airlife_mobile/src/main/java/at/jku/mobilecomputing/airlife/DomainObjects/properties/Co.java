
package at.jku.mobilecomputing.airlife.DomainObjects.properties;

import androidx.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Co  {

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
