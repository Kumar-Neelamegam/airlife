package at.jku.mobilecomputing.airlife.Adapters;

import android.content.ClipData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import at.jku.mobilecomputing.airlife.Constants.Common;
import at.jku.mobilecomputing.airlife.Database.FavData.FavouriteListDataSet;
import at.jku.mobilecomputing.airlife.R;


public class FavouriteListAdapter extends RecyclerView.Adapter<FavouriteListAdapter.ViewHolder> {


    private List<FavouriteListDataSet> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    Context ctx;
    static int sno = 0;

    // data is passed into the constructor
    public FavouriteListAdapter(Context context, List<FavouriteListDataSet> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        ctx = context;
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

        callService(mData.get(position), holder, position);

    }

    private void callService(FavouriteListDataSet mDatum, ViewHolder holder, int position) {

                    holder.textview_Sno.setText(String.valueOf(position+1));
                    holder.textview_LocationName.setText(mDatum.getLocation());
                    holder.textview_LocationInfo.setText(mDatum.getLocationInfo());


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

                    //quality scale
                    holder.textview_QualityValue.setText(String.valueOf(mDatum.getQualityScale()));
                    setQualityScale(mDatum.getQualityScale(), holder);

                    //pressure, temp, humid, wind
                    if (mDatum.getTemperature() != null)
                        holder.textview_temp.setText(ctx.getResources().getString(R.string.temperature_unit_celsius, mDatum.getTemperature()));
                    if (mDatum.getPressure() != null)
                        holder.textview_pressure.setText(ctx.getResources().getString(R.string.pressure_unit, mDatum.getPressure()));
                    if (mDatum.getHumidity() != null)
                        holder.textview_humid.setText(ctx.getResources().getString(R.string.humidity_unit, mDatum.getHumidity()));
                    if (mDatum.getWind() != null)
                        holder.textview_wind.setText(ctx.getResources().getString(R.string.wind_unit, mDatum.getWind()));

                    setMoreInfo(mDatum, holder);


    }

    private void setMoreInfo(FavouriteListDataSet favouriteListDataSet, ViewHolder holder) {

        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append("More Info:\n");
        if (favouriteListDataSet.getCo() != null){
            stringBuilder.append("Carbon Monoxide - AQI: "+favouriteListDataSet.getCo());
            stringBuilder.append(",\n");
        }
        if (favouriteListDataSet.getNo2() != null){
            stringBuilder.append("Nitrous Dioxide - AQI: "+favouriteListDataSet.getNo2());
            stringBuilder.append(",\n");
        }

        if (favouriteListDataSet.getO3() != null){
            stringBuilder.append("Ozone - AQI: "+ favouriteListDataSet.getO3());
            stringBuilder.append(",\n");
        }

        if (favouriteListDataSet.getPm25() != null){
            stringBuilder.append("PM 2.5 - AQI: "+ favouriteListDataSet.getPm25());
            stringBuilder.append(",\n");
        }

        if (favouriteListDataSet.getPm10() != null){
            stringBuilder.append("PM 10 - AQI: "+favouriteListDataSet.getPm10());
            stringBuilder.append(",\n");
        }

        if (favouriteListDataSet.getSo2() != null){
            stringBuilder.append("Sulfur Dioxide - AQI:"+ favouriteListDataSet.getSo2());
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

        LinearLayout moreLayout;
        ImageView imageview_moreinfo;
        TextView txtvw_moreinfo;

        public RelativeLayout viewBackground, viewForeground;

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

            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.view_foreground);

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


    public void removeItem(int position) {
        mData.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        sno=0;
        notifyItemRemoved(position);
    }

    public void restoreItem(FavouriteListDataSet item, int position) {
        mData.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }

}
