package at.jku.mobilecomputing.airlife.Constants;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.widget.ImageView;

import at.jku.mobilecomputing.airlife.DomainObjects.Data;
import at.jku.mobilecomputing.airlife.R;

public class Common {


    public static String getscalefromquality(Integer aqi, Context ctx) {
        String scale="N/A";
        if (aqi >= 0 && aqi <= 50) {
            scale=ctx.getString(R.string.good);
        }
        else if (aqi >= 51 && aqi <= 100){
            scale=ctx.getString(R.string.moderate);
        } else if (aqi >= 101 && aqi <= 150){
            scale=ctx.getString(R.string.unhealthy_for_sensitive);
        } else if (aqi >= 151 && aqi <= 200){
            scale=ctx.getString(R.string.unhealthy);
        } else if (aqi >= 201 && aqi <= 300){
            scale=ctx.getString(R.string.very_unhealthy);
        } else if (aqi >= 301){
            scale=ctx.getString(R.string.hazardous);
        } else{
            scale=ctx.getString(R.string.good);
        }
        return scale;
    }



}
