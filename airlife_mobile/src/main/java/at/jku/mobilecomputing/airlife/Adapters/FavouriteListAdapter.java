package at.jku.mobilecomputing.airlife.Adapters;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import at.jku.mobilecomputing.airlife.Constants.Common;
import at.jku.mobilecomputing.airlife.Constants.Status;
import at.jku.mobilecomputing.airlife.CoreModules.ListFavActivity;
import at.jku.mobilecomputing.airlife.CoreModules.MainActivity;
import at.jku.mobilecomputing.airlife.Database.AirLifeDatabaseClient;
import at.jku.mobilecomputing.airlife.Database.FavData.FavouriteListDataSet;
import at.jku.mobilecomputing.airlife.DomainObjects.Data;
import at.jku.mobilecomputing.airlife.DomainObjects.Pollutant;
import at.jku.mobilecomputing.airlife.DomainObjects.WAQI;
import at.jku.mobilecomputing.airlife.NetworkUtils.AqiViewModel;
import at.jku.mobilecomputing.airlife.R;
import at.jku.mobilecomputing.airlife.Utilities.AsynkTaskCustom;
import at.jku.mobilecomputing.airlife.Utilities.onWriteCode;


public class FavouriteListAdapter extends RecyclerView.Adapter<FavouriteListAdapter.ViewHolder> {


    private List<FavouriteListDataSet> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    Context ctx;
    int sno = 0;
    RecyclerView recyclerView;

    // data is passed into the constructor
    public FavouriteListAdapter(Context context, List<FavouriteListDataSet> data, RecyclerView recyclerView) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        ctx = context;
        this.recyclerView = recyclerView;
    }


    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_item_favourite, parent, false);
        return new ViewHolder(view);
    }

    boolean rotate_flag=false;
    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        callService(mData.get(position).getLatitude(), mData.get(position).getLongitude(), holder, position);
        holder.textview_Sno.setText(String.valueOf(sno + 1));
        holder.textview_LocationName.setText(mData.get(position).getLocation());
        holder.textview_LocationInfo.setText(mData.get(position).getLocationInfo());
        holder.imageview_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean status = Common.deleteFavouriteItem(mData.get(position).getId(), ctx);
                Toast.makeText(ctx, status == true ? "Deleted successfully.." : "", Toast.LENGTH_SHORT).show();
                recyclerView.getAdapter().notifyItemChanged(position);
                sno = 0;
            }
        });
        holder.imageview_moreinfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(!rotate_flag)// false
                {
                    holder.imageview_moreinfo.animate().rotationBy(180).setDuration(0).start();
                    holder.moreLayout.setVisibility(View.VISIBLE);
                    rotate_flag=true;
                }else
                {
                    holder.imageview_moreinfo.animate().rotationBy(180).setDuration(0).start();
                    holder.moreLayout.setVisibility(View.GONE);
                    rotate_flag=false;
                }

            }
        });
        sno++;
    }

    private AqiViewModel aqiViewModel;
    Data data = new Data();
    private void callService(double lat, double longt, ViewHolder holder, int position) {

        aqiViewModel = ViewModelProviders.of((FragmentActivity) ctx).get(AqiViewModel.class);
        String geo = "geo:" + lat + ";" + longt;
        Log.e("Locations Info:", geo);

        aqiViewModel.getGPSApiResponse(geo).observe((LifecycleOwner) ctx, apiResponse -> {
            if (apiResponse != null) {

                Log.e("api", String.valueOf(apiResponse));

                data = apiResponse.getData();

                //quality scale
                holder.textview_QualityValue.setText(String.valueOf(data.getAqi()));
                setQualityScale(data.getAqi(), holder);

                //pressure, temp, humid, wind
                WAQI waqi = data.getWaqi();
                if (waqi.getTemperature() != null)
                    holder.textview_temp.setText(ctx.getResources().getString(R.string.temperature_unit_celsius, data.getWaqi().getTemperature().getV()));
                if (waqi.getPressure() != null)
                    holder.textview_pressure.setText(ctx.getResources().getString(R.string.pressure_unit, waqi.getPressure().getV()));
                if (waqi.getHumidity() != null)
                    holder.textview_humid.setText(ctx.getResources().getString(R.string.humidity_unit, waqi.getHumidity().getV()));
                if (waqi.getWind() != null)
                    holder.textview_wind.setText(ctx.getResources().getString(R.string.wind_unit, waqi.getWind().getV()));

                setMoreInfo(waqi, holder);

            }
        });
    }

    private void setMoreInfo(WAQI waqi, ViewHolder holder) {

        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("More Info:\n");
        if (waqi.getCo() != null){
            stringBuilder.append("Carbon Monoxide - AQI: "+waqi.getCo().getV().toString());
            stringBuilder.append(",\n");
        }
        if (waqi.getNo2() != null){
            stringBuilder.append("Nitrous Dioxide - AQI: "+waqi.getNo2().getV().toString());
            stringBuilder.append(",\n");
        }

        if (waqi.getO3() != null){
            stringBuilder.append("Ozone - AQI: "+ waqi.getO3().getV().toString());
            stringBuilder.append(",\n");
        }

        if (waqi.getPm2_5() != null){
            stringBuilder.append("PM 2.5 - AQI: "+ waqi.getPm2_5().getV().toString());
            stringBuilder.append(",\n");
        }

        if (waqi.getPm10() != null){
            stringBuilder.append("PM 10 - AQI: "+waqi.getPm10().getV().toString());
            stringBuilder.append(",\n");
        }

        if (waqi.getSo2() != null){
            stringBuilder.append("Sulfur Dioxide - AQI:"+ waqi.getSo2().getV().toString());
        }

        if (stringBuilder!=null) {
            holder.txtvw_moreinfo.setText(stringBuilder.toString());
        }
    }

    private void setQualityScale(int aqi, ViewHolder holder) {

        if (aqi >= 0 && aqi <= 50) {
            holder.imageview_QualityScale.setBackground(ctx.getResources().getDrawable(R.drawable.ic_smile_good));
        } else if (aqi >= 51 && aqi <= 100) {
            holder.imageview_QualityScale.setBackground(ctx.getResources().getDrawable(R.drawable.ic_smile_moderate));
        } else if (aqi >= 101 && aqi <= 150) {
            holder.imageview_QualityScale.setBackground(ctx.getResources().getDrawable(R.drawable.ic_smile_sensitive_unhealthy));
        } else if (aqi >= 151 && aqi <= 200) {
            holder.imageview_QualityScale.setBackground(ctx.getResources().getDrawable(R.drawable.ic_smile_unhealthy));
        } else if (aqi >= 201 && aqi <= 300) {
            holder.imageview_QualityScale.setBackground(ctx.getResources().getDrawable(R.drawable.ic_smile_very_unhealthy));
        } else if (aqi >= 301) {
            holder.imageview_QualityScale.setBackground(ctx.getResources().getDrawable(R.drawable.ic_smile_hazardous));
        } else {
            holder.imageview_QualityScale.setBackground(ctx.getResources().getDrawable(R.drawable.ic_smile_good));
        }


    }

    private int calculateDistance() {
        return 0;
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textview_Sno;
        TextView textview_LocationName;
        TextView textview_LocationInfo;
        TextView textview_QualityValue;

        TextView textview_temp;
        TextView textview_wind;
        TextView textview_pressure;
        TextView textview_humid;

        ImageView imageview_QualityScale;
        ImageView imageview_delete;

        LinearLayout moreLayout;
        ImageView imageview_moreinfo;
        TextView txtvw_moreinfo;

        ViewHolder(View itemView) {
            super(itemView);
            textview_Sno = itemView.findViewById(R.id.txtvw_sno);
            textview_LocationName = itemView.findViewById(R.id.txtvw_location);
            textview_LocationInfo = itemView.findViewById(R.id.txtvw_locationinfo);
            textview_QualityValue = itemView.findViewById(R.id.txtairquality);

            textview_temp = itemView.findViewById(R.id.txt_temp);
            textview_pressure = itemView.findViewById(R.id.txt_pressure);
            textview_wind = itemView.findViewById(R.id.txt_wind);
            textview_humid = itemView.findViewById(R.id.txt_humid);

            moreLayout=itemView.findViewById(R.id.layout_moreinfo);
            imageview_moreinfo=itemView.findViewById(R.id.imgvw_moreinfo);
            txtvw_moreinfo=itemView.findViewById(R.id.txt_moreinfo);

            imageview_QualityScale = itemView.findViewById(R.id.imgvw_airqualityscale);
            imageview_delete = itemView.findViewById(R.id.imgvw_delete);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    FavouriteListDataSet getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
